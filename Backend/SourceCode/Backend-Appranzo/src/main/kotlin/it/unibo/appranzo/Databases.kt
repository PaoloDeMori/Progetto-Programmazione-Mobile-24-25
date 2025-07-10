package it.unibo.appranzo

import io.ktor.server.application.*
import it.unibo.appranzo.commons.DatabaseError
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(databaseName:String,username:String,password:String) {
        val database = Database.connect(
            url = "jdbc:mysql://localhost:3306/$databaseName",
            driver = "com.mysql.cj.jdbc.Driver",
            user = username,
            password = password
        )
    transaction(database) {
        try {
            println("Verifico la connessione al DB...")
            exec("SELECT 1")
        }
        catch (e: Exception){
            throw DatabaseError(e.message?:"Error")
        }
    }
}
