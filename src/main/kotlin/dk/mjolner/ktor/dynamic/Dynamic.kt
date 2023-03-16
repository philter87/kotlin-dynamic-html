package dk.mjolner.ktor.dynamic

import kotlinx.html.*
import kotlinx.html.attributes.StringAttribute

@HtmlTagMarker
fun <T> Tag.dynamic(trigger: String, value: T, block: DYNAMIC<T>.() -> Unit = {}): Unit =
    DYNAMIC(trigger, value, false, consumer, block).visit(block)

@HtmlTagMarker
fun Tag.dynamic(trigger: String, block: DYNAMIC<String>.() -> Unit = {}): Unit =
    DYNAMIC(trigger, "", false, consumer, block).visit(block)

@Suppress("unused")
open class DYNAMIC<T>(var trigger: String, var value: T, var isTriggered: Boolean = false, override val consumer: TagConsumer<*>, var block: DYNAMIC<T>.() -> Unit) :
    HTMLTag("div", consumer, attributesMapOf("id", trigger, "serverTrigger", "true"), null, false, false),
    HtmlBlockTag {
}

class Call() {

}

class Javascript() {
    var calls: MutableList<Call> = mutableListOf();

    fun call(script: Call.() -> Unit): Call {
        val call = Call()
        calls.add(call);
        return call;
    }
}

fun js(script: Javascript.() -> Unit = {}): String {
    val javascript = Javascript()
    return javascript.toString();
}


var jsScript = js { call{""} }

var CommonAttributeGroupFacade.klass : String
    get()  =  StringAttribute().get(this, "class")
    set(newValue) { StringAttribute().set(this, "class", newValue)}