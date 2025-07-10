package it.unibo.appranzo.data.repositories

import it.unibo.appranzo.data.database.daos.UserEntity
import it.unibo.appranzo.data.database.tables.UsersTable
import it.unibo.appranzo.model.security.Encrypter
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository() {
    fun findUserByUsername(username: String): UserEntity? {
        return transaction {
            UserEntity.find { UsersTable.username eq username }.singleOrNull()
        }
    }

    fun findUserById(id: EntityID<Int>): UserEntity? {
        return transaction {
            UserEntity.find { UsersTable.id eq id }.singleOrNull()
        }
    }

    fun findUserByEmail(email: String): UserEntity? {
        return transaction {
            UserEntity.find { UsersTable.email eq email }.singleOrNull()
        }
    }

    fun saveNewUser(userName: String,password: String, email: String,hashedRefreshToken:String, photoUrlIn: String? ): UserEntity? {
        return transaction {
           UserEntity.new {
                this.userName = userName
                this.password = password
               this.hashedRefreshToken = hashedRefreshToken
                this.email = email
                photoUrlIn?.let{ this.photoUrl = it }
            }
        }
    }

    fun checkRefreshToken(username: String,hashedRefreshToken: String):Boolean{
        val user = findUserByUsername(username)
        val result:Boolean = user?.let{ Encrypter.compareStrings(hashedRefreshToken,it.hashedRefreshToken)} ?: false
        return result
    }

    fun updateRefreshToken(hashedToken:String,user: UserEntity){
            return transaction {
                user.hashedRefreshToken = hashedToken
            }
    }
    fun addPoints(userId: EntityID<Int>, pointsToAdd: Int): Boolean = transaction {
        val user = UserEntity.findById(userId)
        if (user != null) {
            user.points = user.points + pointsToAdd
            true
        } else {
            false
        }
    }
}