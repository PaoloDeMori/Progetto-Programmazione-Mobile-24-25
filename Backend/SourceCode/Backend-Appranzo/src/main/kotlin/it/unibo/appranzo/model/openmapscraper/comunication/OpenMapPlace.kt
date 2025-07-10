package it.unibo.appranzo.model.openmapscraper.comunication

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class OverpassResponse(
    val elements: List<OpenMapPlace?>
)
@OptIn(ExperimentalSerializationApi::class)
@Serializable @JsonIgnoreUnknownKeys
data class OpenMapPlace(
    val type: String,
    val lon: Double? = null,
    val lat: Double? = null,
    val tags: Map<String, String> = emptyMap()
) {
    val name: String? get() = tags["name"]
    val amenity: String? get() = tags["amenity"]
    val city: String? get() = tags["addr:city"]
    val street: String? get() = tags["addr:street"]
    val houseNumber: String? get() = tags["addr:housenumber"]
    val cuisine: String? get() = tags["cuisine"]
    val image: String? get() = tags["image"]
}
