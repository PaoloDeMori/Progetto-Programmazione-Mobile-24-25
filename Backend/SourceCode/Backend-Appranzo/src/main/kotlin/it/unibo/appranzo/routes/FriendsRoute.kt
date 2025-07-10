package it.unibo.appranzo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import it.unibo.appranzo.communication.dtos.UserDto
import it.unibo.appranzo.communication.dtos.friendship.FriendshipRequestDto
import it.unibo.appranzo.model.friends.FriendsService
import it.unibo.appranzo.model.security.AuthenticationService
import kotlin.text.removePrefix

fun Route.friendsRoute(friendsService: FriendsService,auth: AuthenticationService){
    authenticate("auth-jwt"){
        suspend fun getUsernameFromCall(call: ApplicationCall): String? {
            val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
            val userName = token?.let { auth.getUsernameFromToken(it) }
            return userName
        }
        get("/friends"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
            }
            else{
                val friends = friendsService.allMyFriends(userName)
                call.respond(HttpStatusCode.OK,friends)
            }
        }
        post("/friends/sendRequest"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val requestName = call.receive<UserDto>().username
            val success = friendsService.sendFriendshipRequest(userName,requestName)
            if(success==null){
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            else{
                call.respond(HttpStatusCode.OK,success)
                return@post
            }
        }
        post("/friends/acceptRequest"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val requestId = call.receive<FriendshipRequestDto>().id
            val success=friendsService.acceptFriendshipRequest(userName,requestId)
            if(success){
                call.respond(HttpStatusCode.OK)
                return@post
            }
            else {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
        post("/friends/rejectRequest"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val requestId = call.receive<FriendshipRequestDto>().id
            val success=friendsService.rejectFriendshipRequest(userName,requestId)
            if(success){
                call.respond(HttpStatusCode.OK)
                return@post
            }
            else {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
        }
        get("/friends/pendingRequests"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val requests=friendsService.getPendingRequestsForUser(userName)
            call.respond(HttpStatusCode.OK,requests)
            return@get
        }
        delete("/friends/removeAFriend/{friendId}"){
            val userName= getUsernameFromCall(call)
            if (userName==null){
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val friendToRemove = call.parameters["friendId"]
                ?: return@delete call.respond(HttpStatusCode.BadRequest)
            try {
                val friendToRemoveId = friendToRemove.toInt()
                val success=friendsService.removeAFriend(userName,friendToRemoveId)
                if(success){
                    call.respond(HttpStatusCode.OK)
                    return@delete
                }
                else {
                    call.respond(HttpStatusCode.BadRequest)
                    return@delete
                }
            }
            catch (e:NumberFormatException){
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

        }

    }

}