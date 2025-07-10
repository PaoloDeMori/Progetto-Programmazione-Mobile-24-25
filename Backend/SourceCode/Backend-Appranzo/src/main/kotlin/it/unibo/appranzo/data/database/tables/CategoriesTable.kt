package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object CategoriesTable : IntIdTable("CATEGORIES") {
    val name = varchar("name", 100).uniqueIndex()
}
