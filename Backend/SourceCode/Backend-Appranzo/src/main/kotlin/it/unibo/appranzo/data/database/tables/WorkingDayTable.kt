package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object WorkingDayTable : IntIdTable("WORKING_DAY") {
    val name = varchar("name", 20)
    val placeId = reference("place_id", PlacesTable, onDelete = ReferenceOption.CASCADE)
}
