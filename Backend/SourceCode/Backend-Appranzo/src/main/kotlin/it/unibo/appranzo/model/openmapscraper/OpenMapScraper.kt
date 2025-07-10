package it.unibo.appranzo.model.openmapscraper

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.get
import it.unibo.appranzo.model.openmapscraper.comunication.OpenMapPlace
import kotlinx.serialization.json.Json
import io.ktor.util.toUpperCasePreservingASCIIRules
import it.unibo.appranzo.commons.geography.Coordinates
import it.unibo.appranzo.communication.dtos.places.AddressDto
import it.unibo.appranzo.communication.dtos.places.ReverseGeocodingResponse
import it.unibo.appranzo.data.repositories.PlaceRepository
import it.unibo.appranzo.model.openmapscraper.comunication.OverpassResponse
import kotlinx.coroutines.delay
import org.jetbrains.exposed.dao.id.EntityID


class OpenMapScraper(
    private val httpClient: HttpClient, val pr: PlaceRepository
) {
    companion object {
        private const val OPEN_MAP_API_URL = "https://overpass-api.de/api/interpreter"
    }

    suspend fun searchRoad(coordinates: Coordinates): AddressDto{
        val url = "https://nominatim.openstreetmap.org/reverse"
        val query = "$url?lat=${coordinates.latitude}&lon=${coordinates.longitude}&format=json"

        return try {
            val response = httpClient.get(query).body<ReverseGeocodingResponse>()
            response.address
        }
        catch (e: Error){
            AddressDto("Via Sconosciuta", null)
        }

    }

    suspend fun searchPlaces(city: String): List<OpenMapPlace> {
        val query = """
                    [out:json][timeout:25];
                    area["name"="$city"]["boundary"="administrative"]["admin_level"="8"]->.searchArea;
                    (
                      node["amenity"~"restaurant|cafe|bar|pub"]["name"](area.searchArea);
                      way["amenity"~"restaurant|cafe|bar|pub"]["name"](area.searchArea);
                      relation["amenity"~"restaurant|cafe|bar|pub"]["name"](area.searchArea);
                    );
                    out center;
                """.trimIndent()

        val response = httpClient.post(OPEN_MAP_API_URL) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("data=$query")
        }
        val toOverpassResponse = Json.decodeFromString<OverpassResponse>(response.bodyAsText())
        return toOverpassResponse.elements.mapNotNull { it }
    }



    suspend fun insertOpenMapPlaces(city: String,region:String?) {

        val places =searchPlaces(city)
        print(places)
            for (place in places) {
                var description = ""
                val c: EntityID<Int>?
                if (place.amenity == "restaurant") {
                    if (place.cuisine == "pizza") {
                        c = pr.categoriesRepository.addCategory(place.cuisine!!.toUpperCasePreservingASCIIRules())
                        description = "pizzeria"
                    } else {
                        c = pr.categoriesRepository.addCategory(place.amenity!!.toUpperCasePreservingASCIIRules())
                        description = "restaurant"
                    }
                } else if (place.amenity == "cafe" || place.amenity == "bar" || place.amenity == "pub") {
                    c = pr.categoriesRepository.addCategory(place.amenity!!.toUpperCasePreservingASCIIRules())
                    description = place.amenity!!
                } else {
                    c = pr.categoriesRepository.addCategory("UNDEFINED")
                    description = "Unknown"
                }
                delay(500)
                if (place.name != null && place.lat != null && place.lon != null && c != null) {
                    var street ="Via Sconosciuta"
                    var houseNumber ="S/N"
                    if (place.street==null){
                        val foundStreet = searchRoad(Coordinates(place.lat,place.lon))
                        street=foundStreet.road?:"Via Sconosciuta"
                        houseNumber = foundStreet.houseNumber?.toString()?:"S/N"
                    }
                    pr.saveNewPlace(
                        name = place.name!!,
                        description = place.cuisine?: "",
                        latitude = place.lat,
                        longitude = place.lon,
                        categoryId = c,
                        photoUrl = null,
                        city = city,
                        address = "${place.street?: street}  ${place.houseNumber?: houseNumber}",
                        region= region
                    )
                }
            }
    }
}