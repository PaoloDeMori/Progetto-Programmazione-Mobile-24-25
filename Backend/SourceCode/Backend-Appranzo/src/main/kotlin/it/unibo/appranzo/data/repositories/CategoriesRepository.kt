package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.data.database.daos.CategoryEntity
import it.unibo.appranzo.data.database.tables.CategoriesTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CategoriesRepository {

    fun findCategoryIdByName(name: String): EntityID<Int>?{
        return transaction{
            CategoryEntity.find { CategoriesTable.name eq name}.singleOrNull()?.id
        }
    }

    fun addCategory(name: String): EntityID<Int>?{
        return transaction {
            val category = CategoryEntity.find { CategoriesTable.name eq name }.singleOrNull()
            category?.id ?: CategoryEntity.new {
                this.name = name
            }.id
        }
    }

    fun getAllCategories(): List<CategoryEntity> = transaction {
        CategoryEntity.all().toList()
    }
}