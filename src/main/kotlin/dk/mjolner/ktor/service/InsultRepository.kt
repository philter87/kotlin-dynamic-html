package dk.mjolner.ktor.service
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class InsultRepository {
    init {
        Database.connect("jdbc:sqlite:sample.db")
        transaction {
            SchemaUtils.create(Insults);
        }

        println("Database created")
    }

    fun addInsult(insult: String){
        if(insult.isEmpty()){
            return;
        }
        transaction {
            Insults.insert {
                it[this.insult] = insult
            }
        }
    }

    fun getInsults(): List<String> {
        var insults = listOf<String>();
        transaction {
            insults = Insults.selectAll().map { it[Insults.insult] }
        }
        return insults
    }

    fun deleteInsult(): Int {
        var count = 0;
        transaction {
            count = Insults.deleteAll();
        }
        return count;
    }

    fun search(value: String): List<String>  {
        var insults = listOf<String>();
        println("Search $value")
        transaction {
            insults = Insults.select { Insults.insult like "%$value%" }.map { it[Insults.insult] }
        }
        return insults;
    }
}

object Insults : Table() {
    var id = integer("id").autoIncrement()
    val insult = varchar("insult", 50);

    override val primaryKey = PrimaryKey(id, name = "PK_Insults_ID")
}