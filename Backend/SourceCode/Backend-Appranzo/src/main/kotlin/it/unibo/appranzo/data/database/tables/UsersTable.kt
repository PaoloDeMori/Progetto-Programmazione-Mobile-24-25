package it.unibo.appranzo.data.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object UsersTable : IntIdTable("USERS") {
    val username = varchar("username", 100).uniqueIndex()
    val email = varchar("email", 150).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val hashedRefreshToken = varchar("hashed_refresh_token", 255)
    val points = integer("points").default(0)
    val photoUrl = varchar("photo_url", 255).nullable()
}
