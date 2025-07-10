package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object PlacesTable : IntIdTable("PLACES") {
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val cityId = reference("city_id", CitiesTable)
    val address = varchar("address", 255).nullable()
    val latitude = decimal("latitude", 9, 6)
    val longitude = decimal("longitude", 9, 6)
    val photoUrl = varchar("photo_url", 255).nullable()
    val categoryId = reference("category_id", CategoriesTable)
}
