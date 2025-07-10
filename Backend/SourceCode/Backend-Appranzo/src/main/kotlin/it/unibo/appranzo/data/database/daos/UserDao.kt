package it.unibo.appranzo.data.database.daos

import it.unibo.appranzo.communication.dtos.UserDto
import it.unibo.appranzo.data.database.tables.UsersTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)

    val userId by UsersTable.id
    var userName by UsersTable.username
    var password by UsersTable.passwordHash
    var email by UsersTable.email
    var hashedRefreshToken by UsersTable.hashedRefreshToken
    var points by UsersTable.points
    var photoUrl by UsersTable.photoUrl

    fun toDto(): UserDto {
        return UserDto(
                id       = this.id.value,
                username = this.userName,
                email    = this.email,
                points   = this.points,
                photoUrl = this.photoUrl
        )
    }
}