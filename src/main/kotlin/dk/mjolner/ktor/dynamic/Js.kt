package dk.mjolner.ktor.dynamic

object Js {

    fun chain(vararg calls: String): String{
        if (calls.isEmpty()){
            return "";
        }
        if (calls.size == 1){
            return calls[0];
        }
        return "${calls[0]}.then(() => ${calls[1]})";
    }

    fun call(triggerName: String, vararg inputId: String): String {
        if(inputId.isEmpty()){
            return "$triggerName()";
        }
        return "$triggerName(document.getElementById('${inputId[0]}').value)"
    }
}

