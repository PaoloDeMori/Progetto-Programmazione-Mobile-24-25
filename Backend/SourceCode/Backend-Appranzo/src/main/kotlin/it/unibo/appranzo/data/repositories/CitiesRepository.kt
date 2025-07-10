package it.unibo.appranzo.data.repositories

import io.ktor.util.toUpperCasePreservingASCIIRules
import it.unibo.appranzo.data.database.daos.CityEntity
import it.unibo.appranzo.data.database.tables.CitiesTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class CitiesRepository {
    fun findCityIdByName(name: String): EntityID<Int>?{
        return transaction{
            CityEntity.find{ CitiesTable.name eq name}.singleOrNull()?.id
        }
    }

    fun allCities(): List<CityEntity>{
        return transaction{
            CityEntity.all().toList()
        }
    }

    fun findNameById(id: EntityID<Int>): String?{
        return transaction{
            CityEntity.find{ CitiesTable.id eq id}.singleOrNull()?.name
        }
    }

    fun addCity(name: String,region: String?): EntityID<Int>{
        return transaction {
            val city = CityEntity.find { CitiesTable.name eq name }.singleOrNull()
            val safeRegion = region ?: "UNDEFINED"
            city?.id ?: CityEntity.new {
                this.name = name
                this.region = safeRegion.toUpperCasePreservingASCIIRules()
            }.id
        }
    }
}