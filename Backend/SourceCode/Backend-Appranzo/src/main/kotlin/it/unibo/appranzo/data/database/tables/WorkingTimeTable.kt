package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.time

object WorkingTimeTable : IntIdTable("WORKING_TIME") {
    val dayId = reference("day_id", WorkingDayTable, onDelete = ReferenceOption.CASCADE)
    val openingTime = time("opening_time")
    val closingTime = time("closing_time")
}
