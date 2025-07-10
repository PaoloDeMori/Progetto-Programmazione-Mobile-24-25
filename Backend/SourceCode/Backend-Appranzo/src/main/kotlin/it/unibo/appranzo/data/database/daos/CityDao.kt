package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.data.database.tables.CitiesTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CityEntity(id: EntityID<Int>): IntEntity(id){
    companion object: IntEntityClass<CityEntity>(CitiesTable)


    var name by CitiesTable.name
    var region by CitiesTable.region
}