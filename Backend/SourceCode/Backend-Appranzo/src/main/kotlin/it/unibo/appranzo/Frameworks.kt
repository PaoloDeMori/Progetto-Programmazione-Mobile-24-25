package it.unibo.appranzo

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import it.unibo.appranzo.data.repositories.CategoriesRepository
import it.unibo.appranzo.data.repositories.CitiesRepository
import it.unibo.appranzo.data.repositories.ClientPhotoRepository
import it.unibo.appranzo.data.repositories.FavoritesRepository
import it.unibo.appranzo.data.repositories.FriendsRepository
import it.unibo.appranzo.data.repositories.PlaceRepository
import it.unibo.appranzo.data.repositories.ReviewsRepository
import it.unibo.appranzo.data.repositories.UserRepository
import it.unibo.appranzo.model.friends.FriendsService
import it.unibo.appranzo.model.openmapscraper.OpenMapScraper
import it.unibo.appranzo.model.places.ClientPhotosService
import it.unibo.appranzo.model.places.PlacesService
import it.unibo.appranzo.model.places.ReviewsService
import it.unibo.appranzo.model.security.AuthenticationService
import it.unibo.appranzo.model.security.JwtHelper
import it.unibo.appranzo.model.security.JwtData
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks(jwtData: JwtData) {
    install(Koin) {
        slf4jLogger()
        modules(module {
                single {
                        HttpClient {
                            install(ContentNegotiation) {
                                json(Json {
                                    ignoreUnknownKeys = true
                                })
                            }
                        }
                }
                single { CategoriesRepository() }
                single { CitiesRepository() }
                single { UserRepository() }
                single { PlaceRepository(get(),get(),get()) }
                single { FavoritesRepository(get()) }
                single { PlacesService(get(),get(), get()) }
                single { ReviewsRepository() }
                single { ClientPhotoRepository() }
            single<JwtData> { jwtData }
            single<String>(named("photoStoragePath")) {
                get<JwtData>().photoStoragePath
            }
            single {
                ReviewsService(
                    reviewsRepository     = get(),
                    clientPhotoRepository = get(),
                    photoStoragePath      = get(named("photoStoragePath")),
                    userRepository = get(),
                    placeRepository = get()
                )
            }
            single { ClientPhotosService(get()) }
            single{ FriendsRepository()}
            single{ FriendsService(get())}


                single { JwtHelper(get()) }
                single { AuthenticationService(get(),get()) }
                single { OpenMapScraper(get(),get()) }
        })
    }
}

