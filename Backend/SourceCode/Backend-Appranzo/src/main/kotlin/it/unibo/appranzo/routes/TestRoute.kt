package it.unibo.appranzo.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.testRoute(){
    get("/"){
         call.respond(HttpStatusCode.Accepted, "Ciao")
    }
}