package it.unibo.appranzo

import io.ktor.server.application.Application
import it.unibo.appranzo.model.openmapscraper.OpenMapScraper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import kotlin.getValue

@OptIn(DelicateCoroutinesApi::class)
fun Application.scraperService(city: String = "Cesena", region: String = "Emilia-Romagna"){
    GlobalScope.launch {
        try {
            val city = city
            println("\nAvvio Scraping \n")
            val oms: OpenMapScraper by inject()
            oms.insertOpenMapPlaces(city, region)
            println("\nScraping Completato con successo della città $city")
        }
        catch (e: Exception){
            println("\nScraping Terminato\n")
            }
        }
    }