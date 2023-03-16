package dk.mjolner.ktor.plugins

import dk.mjolner.ktor.dynamic.Js
import dk.mjolner.ktor.dynamic.dynamic
import dk.mjolner.ktor.dynamic.klass
import dk.mjolner.ktor.dynamic.respondDynamic
import dk.mjolner.ktor.service.InsultRepository
import dk.mjolner.ktor.service.InsultService
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import kotlinx.html.*
import org.koin.ktor.ext.inject

import java.time.LocalDateTime

fun Application.configureRouting() {
    val wordService:InsultService by inject();
    val db: InsultRepository by inject();

    routing {

        get("/") {
            call.respondText(wordService.createInsult())
        }

        get("/html") {
            call.respondHtml {
                header()
                body {
                    navBar()
                    h1 { +"Example: Kotlin DSL" }
                    h5 { +LocalDateTime.now().toString() }

                    linkButton("/html1", "Go to the next Example")
                }
            }
        }

        get("/html1") {
            call.respondDynamic {
                header()
                body {
                    navBar()
                    h3 { +"Example: Dynamic 1"}
                    h5 { +"Server Render Time: ${LocalDateTime.now()}"  }

                    dynamic("refreshTime"){
                        h5 { +"Dynamic Server Render Time: ${LocalDateTime.now()}"  }
                    }

                    a { onClick = "refreshTime()"; klass = "waves-effect waves-light btn"; +"Rerender time on server" }

                    linkButton("/html2", "Go to the next Example")
                }
            }
        }

        get("/html2"){
            call.respondDynamic {
                header()
                body {
                    navBar()
                    h3 { +"Example: Dynamic 2"}

                    myButton("Refresh", "refreshInsults()")

                    dynamic("refreshInsults") {
                        val insults = db.getInsults();
                        div { klass="row"
                            div { klass="collection col s2"
                                insults.map {
                                    a { classes= setOf("collection-item"); +it }
                                }
                                if(insults.isEmpty()){
                                    p {
                                        +"Nothing is added to list"
                                    }
                                }
                            }
                        }
                    }


                    div {klass = "row"
                        div {klass="col s1"
//                            myButton("Add insult", "addInsult()")
                            myButton("Add insult", Js.chain("addInsult()", "refreshInsults()"))

                            dynamic("addInsult"){
                                if(isTriggered){
                                    val insult = wordService.createInsult();
                                    db.addInsult(insult);
                                    p {+"'$insult' was added"}
                                }
                            }
                        }
                        div { klass="col s1"
//                            myButton("Remove all", "deleteAll()")
                            myButton("Remove all", Js.chain("deleteAll()", "refreshInsults()"))


                            dynamic("deleteAll"){
                                if(isTriggered){
                                    val deleteCount = db.deleteInsult();
                                    p {
                                        +"$deleteCount insults where removed"
                                    }
                                }
                            }
                        }
                    }

                    linkButton("/html3", "Go to the next Example")
                }
            }
        }

        get("/html3"){

            call.respondDynamic {
                header()
                body {
                    navBar()
                    h3 { +"Example: Dynamic 3"}
                    h5 { +"Server Render Time: ${LocalDateTime.now()}"  }

                    insultList(db)

                    div { klass="row"
                        div { klass="card col s4"
                            h5 { +"Add item" }
                            div { klass="row"
                                div{
                                    klass = "col s8 input-field full-width"
                                    input { placeholder="Insert Insult"; id="inputInsultId"; type=InputType.text; classes= setOf("validate") }
                                    label { +"Insult" }
                                }
                                div { klass="col s4"
//                                    myButton("Add Item", "addInsult(document.getElementById('inputInsultId').value)")
//                                    myButton("Add Item", Js.call("addInsult", "inputInsultId"))
                                    myButton("Add Item", Js.chain(Js.call("addInsult", "inputInsultId"), "refreshInsults()"))
                                }
                            }
                            dynamic("addInsult"){
                                if (isTriggered) {
                                    db.addInsult(this@dynamic.value)
                                    p { +"'${this@dynamic.value}' was added " }
                                }
                            }
                        }
                    }

                    linkButton("/html4", "Go to the next Example")
                }
            }
        }

        get("/html4"){
            call.respondDynamic {
                header()
                body {
                    navBar()
                    h3 { +"Example: Dynamic 4"}
                    h5 { +"Server Render Time: ${LocalDateTime.now()}"  }


                    div { klass="row"
                        div { klass="card col s4"
                            h5 { +"Add item" }
                            div { klass="row"
                                div{
                                    klass = "col s8 input-field full-width"
                                    input { placeholder="Insert Insult"; id="inputInsultId"; type=InputType.text; classes= setOf("validate"); onInput="searchInsults(this.value)" }
                                    label { +"Insult" }
                                }
                            }
                            div { klass="row"
                                dynamic("searchInsults"){
                                    val queryString = this@dynamic.value;
                                    if(queryString.length < 3) {
                                        p { + "You need at least three characters"}
                                        return@dynamic;
                                    }
                                    val sentences = db.search(queryString);
                                    div { klass = "collection"
                                        sentences.map {
                                            a { klass="collection-item"; +it }
                                        }
                                        if (sentences.isEmpty()) {
                                            p { +"No insults where found with $queryString" }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun BODY.insultList(db: InsultRepository) {
    myButton("Refresh", "refreshInsults()")

    dynamic("refreshInsults") {
        val sentences = db.getInsults();
        div {
            klass = "row"
            div {
                klass = "collection col s2"
                sentences.map {
                    a { classes = setOf("collection-item"); +it }
                }
                if (sentences.isEmpty()) {
                    p {
                        +"Nothing is added to list"
                    }
                }
            }
        }
    }
}


private fun BODY.linkButton(hrefString: String, text: String) {
    a { style="position: absolute; bottom:20px; left: 50%"; href = hrefString; classes = setOf("waves-effect waves-light btn"); +text }
}

private fun DIV.myButton(buttonText: String, onClickFn: String) {
    a { classes = setOf("waves-effect waves-light btn"); onClick = onClickFn; +buttonText }
}

private fun BODY.myButton(buttonText: String, onClickFn: String) {
    a { klass = "waves-effect waves-light btn"; onClick = onClickFn; +buttonText }
}

private fun BODY.navBar() {
    nav {
        div {
            classes = setOf("nav-wrapper")
            a { href = "/html"; classes = setOf("brand-logo"); +"Kotlin" }
        }
    }
}

private fun HTML.header() {
    head {
        link {
            rel = "stylesheet"; href =
            "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css"
        }
        script { src = "https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js" }
    }
}