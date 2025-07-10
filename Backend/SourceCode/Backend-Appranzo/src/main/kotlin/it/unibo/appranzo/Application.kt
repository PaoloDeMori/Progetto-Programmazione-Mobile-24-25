package it.unibo.appranzo

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import it.unibo.appranzo.commons.DatabaseError
import it.unibo.appranzo.commons.security.RegistrationErrorReason
import it.unibo.appranzo.data.repositories.UserRepository
import it.unibo.appranzo.model.security.AuthenticationService
import it.unibo.appranzo.model.security.JwtData
import it.unibo.appranzo.model.security.JwtHelper
import it.unibo.appranzo.model.security.RegistrationResult
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import org.koin.ktor.ext.getKoin
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.getValue

fun main(args: Array<String>) {
    val parser = ArgParser("Appranzo_Backend")
    val jwtDataFromFile = try {JwtHelper.loadJwtConfig(".jwt.env.json")} catch (_:Exception){null}

    val port by parser.option(
        ArgType.Int,
                shortName = "p",
        fullName = "port",
        description = "Porta del server"
    ).default(8080)

    val host by parser.option(
        ArgType.String,
        shortName = "ho",
        fullName = "host",
        description = "Indirizzo del server"
    ).default("0.0.0.0")

    val scraping by parser.option(
        ArgType.Boolean,
        shortName = "scr",
        fullName = "scraping",
        description = "Eseguire lo scraping di una determinatà città"
    ).default(false)

    val city by parser.option(
        ArgType.String,
        shortName = "ci",
        fullName = "city",
        description = "Città di cui effettuare lo scraping"
    ).default("Cesena")

    val region by parser.option(
        ArgType.String,
        shortName = "re",
        fullName = "region",
        description = "Regione della di cui effettuare lo scraping"
    ).default("Emilia-Romagna")

    val admin by parser.option(
        ArgType.Boolean,
        shortName = "ad",
        fullName = "admin",
        description = "Crea un utente user:admin,pwd:admin di default se non esiste"
    ).default(false)

    val nameDatabase by parser.option(
        ArgType.String,
        fullName = "db-name",
        description = "Nome Del Database MySql"
    ).default("ApPranzo")

    val userDatabase by parser.option(
        ArgType.String,
        fullName = "db-user",
        description = "Username dell'utente per accedere al database"
    ).default("root")

    val passwordDatabase by parser.option(
        ArgType.String,
        fullName = "db-password",
        description = "Password dell'utente per accedere al database"
    ).default("")

    val photoPath by parser.option(
        ArgType.String,
        shortName = "pp",
        fullName = "photo-path",
        description = "Cartella in cui salvare le foto"
    ).default(jwtDataFromFile?.photoStoragePath?:"Uploads")

    parser.parse(args)

    try {
        val photoDir = File(photoPath)
        if (!photoDir.exists()) {
            println("Cartella non presente provo a crearla $photoPath")
            if (photoDir.mkdirs()) {
                println("Cartella creata con successo.")
            } else {
                println("Errore nella creazione della cartella, interrompo esecuzione")
                return
            }
        }
    } catch (e: SecurityException) {
        println("Errore nella creazione della cartella, interrompo esecuzione")
        return
    }

    val finalJwtData = jwtDataFromFile?.copy(
        photoStoragePath = photoPath
    ) ?: JwtData(
        secretAccess = "secret-access",
        secretRefresh = "secret-refresh",
        audience = "AppsUsers",
        expirationAccessTime = 900000,
        expirationRefreshTime = 60480000,
        photoStoragePath = photoPath
    )
    try {
    embeddedServer(Netty, port = port, host = host){
            module(finalJwtData, scraping, city, region, nameDatabase, userDatabase, passwordDatabase)
            if (admin) {
                println("Tentativo di creazione utente Admin")
                val authService: AuthenticationService = this.getKoin().get<AuthenticationService>()
                val result = authService.register(
                    username = "Admin",
                    password = "Admin",
                    email = "Admin@example.com",
                    photoUrl = null
                )

                when (result) {
                    is RegistrationResult.Success -> {
                        println("Admin creato con successo.")
                    }

                    is RegistrationResult.RegistrationError -> {
                        if (result.errorReason == RegistrationErrorReason.USERNAME_TAKEN || result.errorReason == RegistrationErrorReason.EMAIL_TAKEN) {
                            println("Admin esiste già.")
                        } else {
                            println("Errore durante la creazione dell' Admin")
                        }
                    }
                }

            }
    }.start(wait = true)
    }
    catch (e: DatabaseError){
        println("Un errore è stato riscontrato tentando di accedere al database, impossibile avviare applicativo ${e.customMessage}")
        return
    }
    catch (e: Exception){
        println("Un errore fatale è stato riscontrato tentando di accedere al database, impossibile avviare applicativo")
        return
    }

}

fun Application.module(jwtData: JwtData,scrapering:Boolean,city:String,region:String,databaseName:String,
                       usernameDatabase:String,passwordDatabase:String) {
    configureFrameworks(jwtData)
    val actualUserRepository: UserRepository by inject()
    configureSerialization()
    try{
    configureDatabases(databaseName,usernameDatabase,passwordDatabase)
    }
    catch (e: Exception){
        throw DatabaseError(e.message?:"Error")
    }
    configureSecurity( actualUserRepository)
    configureRouting()
    if(scrapering) {
        scraperService(city,region)
    }
}
