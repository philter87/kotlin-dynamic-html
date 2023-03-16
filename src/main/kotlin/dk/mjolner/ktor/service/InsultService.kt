package dk.mjolner.ktor.service

val vowels = listOf("a","e","i","o","u","y")

class InsultService(private val repository: InsultRepository) {
    val words1 = listOf("aggressive","arrogant","boastful","bossy","boring","careless","clingy","cruel","cowardly","deceitful","dishonest","fussy","greedy","grumpy","harsh","impatient","impulsive","jealous","moody","narrowminded","overcritical","rude","selfish","untrustworthy","unhappy");
    val words2 = listOf("stupid","jerk","retard", "lunatic", "humanist", "asshole", "bugger", "dickhead", "prick", "penisbreath", "shit-ass", "donkey-face", "motherfucker", "turd")

    fun createInsult(): String {
        return "You are ${getFirstWord()} ${getSecondWord()}";
    }

    fun createInsultAndPersist(): String {
        val sentence = "You are ${getFirstWord()} ${getSecondWord()}";
        repository.addInsult(sentence);
        return sentence;
    }

    private fun getSecondWord(): String{
        return words2.random();
    }

    private fun getFirstWord(): String{
        val firstWord = words1.random();

        val aOrAn = getPrefix(firstWord);
        return "$aOrAn $firstWord";
    }

    private fun getPrefix(word: String): String {

        // ternary operator. Does not exist
        // return isFirstLetterVowel(word) ? "an" : "a";
        return if (isFirstLetterVowel(word)) "an" else "a";
    }

    private fun isFirstLetterVowel(word: String): Boolean {
        val predicate: (String) -> Boolean = {word.startsWith(it)};
        return vowels.any(predicate);
    }
}