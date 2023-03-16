package dk.mjolner.ktor.dynamic

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*
import kotlinx.html.consumers.PredicateResult
import kotlinx.html.consumers.filter
import kotlinx.html.dom.append
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.serialize
import kotlinx.html.stream.createHTML
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

var triggerMap: MutableMap<String, DYNAMIC<Any>> = mutableMapOf();

suspend fun ApplicationCall.respondDynamic(block: HTML.() -> Unit) {
    val endpointUrl = request.uri;
    val triggerBodyEncodedString = request.queryParameters.get("triggerBody")
    if(triggerBodyEncodedString != null){
        val json = String(Base64.getDecoder().decode(triggerBodyEncodedString));
        val html = patch(json);
        respondText(html, ContentType.Text.Html)
        return;
    }

    var document = createHTML()
        .filter() { t ->
            if (t.attributes["serverTrigger"] != null) {
                val dynamic: DYNAMIC<Any> = t as DYNAMIC<Any>;
                triggerMap[dynamic.trigger] = dynamic
            }
            PredicateResult.PASS;
        }
        .html(block = block);

    document = addScriptBeforeEndOfBody(document, endpointUrl, triggerMap)


    respondText(document, ContentType.Text.Html)
}

fun addScriptBeforeEndOfBody(document: String, endpointUrl: String, triggerMap: MutableMap<String, DYNAMIC<Any>>): String {
    val script = createScript(endpointUrl, triggerMap);
    return document.replace("</body>", "$script</body>");
}

fun Routing.dynamicRoutePatch(){
    patch("*") {
        patch(call.receiveText());
    }
}

private fun patch(jsonString: String): String {
    val triggerRequest = Json.decodeFromString<TriggerRequest>(jsonString);
    val s = triggerMap[triggerRequest.trigger];
    if (s != null) {

        if(!triggerRequest.isValueEmpty){
            s.value = triggerRequest.value;
        }


        val html = createHTML(false).div {
            // We indicate with isTriggered=true that the block is triggered from the frontend
            dynamic(s.trigger, s.value, true, s.block)
        }

        return html.substring(5, html.length - 6);
    }
    return "";
}

@HtmlTagMarker
private fun <T> Tag.dynamic(trigger: String, v: T, isTriggered: Boolean, block: DYNAMIC<T>.() -> Unit = {}): Unit =
    DYNAMIC(trigger, v, isTriggered, consumer, block).visit(block)

private fun createScript(r: String, dynamicTags: Map<String, DYNAMIC<*>>): String {
    val scriptBuilder = StringBuilder("<script>");
    scriptBuilder.append(triggerNameGenericScript(r))

    dynamicTags.values.forEach {
        scriptBuilder.append(triggerNameFunctionScript(it));
    }

    scriptBuilder.append("</script>")
//    scriptBuilder.append("</body>")
    return scriptBuilder.toString()
}

fun triggerNameGenericScript(route: String): String{
    return """
    async function trigger(triggerName, value) {
        var json = JSON.stringify({
            value: value || "",
            isValueEmpty: !value,
            trigger: triggerName
        });
        console.log(json)
        var encodedString = window.btoa(json);
        var url = "$route?" + new URLSearchParams({triggerBody: encodedString})
        let response = await fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "text/plain",
                "Accept": "text/html"
            }
        });
        
        var htmlRaw = await response.text();
        
        var element = document.getElementById(triggerName);
        element.outerHTML = htmlRaw                
    }
    async function triggerInOrder(triggerNames) {
        for(var triggerName of triggerNames) { 
            console.log("Calling", triggerName)
            await trigger(triggerName) 
        }
    }
    """.trimIndent();
}

fun triggerNameFunctionScript(DYNAMIC: DYNAMIC<*>): String {
    return """
        async function ${DYNAMIC.trigger}(value){
                await trigger("${DYNAMIC.trigger}", value);
            }
    """.trimIndent();
}


fun Routing.dynamicRoute(r: String, block: HTML.() -> Unit) {
    get(r){
        val document = createHTMLDocument()
            .filter() { t ->
                if (t.attributes["serverTrigger"] != null) {
                    val DYNAMIC: DYNAMIC<Any> = t as DYNAMIC<Any>;
                    triggerMap[DYNAMIC.trigger] = DYNAMIC
                }
                PredicateResult.PASS;
            }
            .html(block = block);

        document.childNodes.item(0).append {
            createScript(r, triggerMap)
        }

        val html = document.serialize(true);
        call.respondText(html, ContentType.Text.Html)
    }
    patch(r) {
        val html = patch(call.receiveText());
        call.respondText(html, ContentType.Text.Html)
    }
}
