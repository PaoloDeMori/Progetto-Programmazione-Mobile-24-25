package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object CitiesTable: IntIdTable("CITIES") {
    val name = varchar("name",100).uniqueIndex()
    val region = varchar("region",100)
}