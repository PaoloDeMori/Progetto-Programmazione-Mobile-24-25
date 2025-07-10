package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.CategoriesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
class CategoryEntity(id: EntityID<Int>):IntEntity(id){
    companion object: IntEntityClass<CategoryEntity>(CategoriesTable)

    var name by CategoriesTable.name

}