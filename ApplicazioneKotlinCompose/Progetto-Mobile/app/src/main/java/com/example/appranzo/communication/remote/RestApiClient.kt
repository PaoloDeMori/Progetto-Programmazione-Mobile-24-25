package com.example.appranzo.communication.remote

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import com.example.appranzo.communication.remote.friendship.FriendshipRequestDto
import io.ktor.http.ContentType
import com.example.appranzo.communication.remote.loginDtos.AuthResults
import com.example.appranzo.communication.remote.loginDtos.CategoryDto
import com.example.appranzo.communication.remote.loginDtos.FavoriteRequest
import com.example.appranzo.communication.remote.loginDtos.LoginErrorReason
import com.example.appranzo.communication.remote.loginDtos.LoginRequestsDtos
import com.example.appranzo.communication.remote.loginDtos.PlaceDto
import com.example.appranzo.communication.remote.loginDtos.PositionDto
import com.example.appranzo.communication.remote.loginDtos.RegistrationErrorReason
import com.example.appranzo.communication.remote.loginDtos.RegistrationRequestsDtos
import com.example.appranzo.communication.remote.loginDtos.RequestId
import com.example.appranzo.communication.remote.loginDtos.ResearchDto
import com.example.appranzo.communication.remote.loginDtos.ReviewDto
import com.example.appranzo.communication.remote.loginDtos.ReviewRequestDto
import com.example.appranzo.communication.remote.loginDtos.UserDto
import com.example.appranzo.data.models.Category
import com.example.appranzo.data.models.Place
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import io.ktor.http.*

import kotlinx.serialization.encodeToString



class RestApiClient(private val httpClient: HttpClient){
    companion object{
        const val REST_API_ADDRESS = "http://10.0.2.2:8080"//localhost va bene se si utilizza un dispositivo fisico collegato con cavo, altrimenti per emulatore 10.0.2.2

    fun isOnline(ctx:Context):Boolean{
        val connectivityManager = ctx
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager
            .getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

        fun openWirelessSettings(ctx:Context) {
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(intent)
            }
        }

    }

    private var accessToken :String = ""
    private var refreshToken :String = ""

    suspend fun login(username:String,password:String):AuthResults{
        try {
            val url = "$REST_API_ADDRESS/login"
            val loginDto = LoginRequestsDtos(username, password)
            val result = httpClient.post(url) {
                    contentType(ContentType.Application.Json)
                setBody(loginDto)
            }
            return when (result.status) {
                HttpStatusCode.BadRequest -> Json.decodeFromString<AuthResults.ErrorLoginResponseDto>(
                    result.bodyAsText()
                )

                HttpStatusCode.Accepted -> Json.decodeFromString<AuthResults.TokenDtos>(result.bodyAsText())
                else -> AuthResults.ErrorLoginResponseDto(
                    LoginErrorReason.INTERNAL_ERROR,
                    "Error Receiving Datas"
                )
            }
        }
        catch (e:Throwable){
            println(e.message)
            return AuthResults.ErrorLoginResponseDto(
                LoginErrorReason.INTERNAL_ERROR,
                "Unreachable Server"
            )
        }
    }

    suspend fun canILog():Boolean {
        try {
            val url = "$REST_API_ADDRESS/login/trylogin"
            val result = httpClient.get(url) {
                bearerAuth(accessToken)
            }
            return result.status==HttpStatusCode.Accepted
        }
        catch (e:Exception){
            return false
        }
    }

    suspend fun getCurrentUser(): UserDto? {
        val url = "$REST_API_ADDRESS/users/me"
        return try {
            val response = httpClient.get(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<UserDto>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserById(id:Int): UserDto? {
        val url = "$REST_API_ADDRESS/users/$id"
        return try {
            val response = httpClient.get(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
            }
            if (response.status == HttpStatusCode.OK) {
                response.body<UserDto>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMyReviews(): List<ReviewDto> {
        val url = "$REST_API_ADDRESS/reviews/me"
        return try {
            httpClient.get(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
            }.body()
        } catch (_: Exception) {
            emptyList()
        }
    }


    suspend fun placeById(id:Int): Place? {
        return try {
            val url = "$REST_API_ADDRESS/places/byId"
            val result = httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(RequestId(id))
            }
            if (result.status==HttpStatusCode.OK){
                Json.decodeFromString<PlaceDto>(result.bodyAsText()).toDto()
            } else null
        } catch (e:Exception){
            null
        }
    }

    suspend fun getCategories(): List<Category> {
        val url = "$REST_API_ADDRESS/categories"
        return try {
            val dtoList: List<CategoryDto> = httpClient.get(url) {
                bearerAuth(accessToken)
            }.body()


            dtoList
                .sortedBy { it.id }
                .map { Category(id=it.id, name = it.name) }
        } catch (e: Exception) {
            emptyList()
        }
    }


    suspend fun getPlacesByCategory(categoryId: Int): List<Place> {
        val url = "$REST_API_ADDRESS/places/category/$categoryId"
        return try {
            httpClient.get(url) {
                bearerAuth(accessToken)
            }.body<List<PlaceDto>>()
                .map { it.toDto() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun searchPlaces(query: String): List<Place> {
        val url = "$REST_API_ADDRESS/places/search"
        val bodyDto = ResearchDto(
            latitude        = null,
            longitude       = null,
            researchInput   = query
        )
        return try {
            httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(bodyDto)
            }
                .body<List<PlaceDto>>()
                .map { it.toDto() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun addReview(request: ReviewRequestDto): Boolean {
        val response = httpClient.post("$REST_API_ADDRESS/reviews/add") {
            bearerAuth(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.status == HttpStatusCode.Created
    }

    suspend fun addReviewWithPhotos(
        request: ReviewRequestDto,
        photos: List<Pair<String, ByteArray>>
    ): Boolean {
        val url = "$REST_API_ADDRESS/reviews/add/photos"
        return try {
            val response = httpClient.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("review", Json.encodeToString(request))
                    photos.forEachIndexed { idx, (fieldName, bytes) ->
                    append(
                            key = "photos",
                            value = bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, ContentType.Image.Any.toString())
                                append(HttpHeaders.ContentDisposition, "filename=\"img$idx.jpg\"")
                            }
                        )
                    }
                }
            ) {
                bearerAuth(accessToken)
            }
            response.status == HttpStatusCode.Created
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getReviews(placeId: Int): List<ReviewDto> {
        val url = "$REST_API_ADDRESS/reviews"
        return try {
            httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(RequestId(placeId))
            }.body()
        } catch (_: Exception) {
            emptyList()
        }
    }


    suspend fun toggleFavourite(placeId: Int): Boolean {
        val url = "$REST_API_ADDRESS/favorites/toggle"
        return try {
            val response: HttpResponse = httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(FavoriteRequest(placeId))
            }
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }


    suspend fun getFavorites(): List<Place> {
        val url = "$REST_API_ADDRESS/favorites"
        return try {
            httpClient.post(url) {
                bearerAuth(accessToken)
            }.body<List<PlaceDto>>()
                .map { it.toDto() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getTopRated(): List<Place> {
        val url = "$REST_API_ADDRESS/places/topRated"
        return try {
            httpClient.get(url) {
                bearerAuth(accessToken)
            }.body<List<PlaceDto>>()
                .map { it.toDto() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getAllFriends():List<UserDto>{
        try {
            val url = "$REST_API_ADDRESS/friends"
            val result = httpClient.get(url) {
                bearerAuth(accessToken)
            }
            return if (result.status==HttpStatusCode.OK){
                Json.decodeFromString<List<UserDto>>(result.bodyAsText())
            } else emptyList()
        }
        catch (e:Exception){
            return emptyList()
        }
    }

    suspend fun removeAFriend(userDto: UserDto):Boolean{
        return try {
            val url = "$REST_API_ADDRESS/friends/removeAFriend/${userDto.id}"
            val result = httpClient.delete(url) {
                bearerAuth(accessToken)
            }
            result.status==HttpStatusCode.OK
        } catch (e:Exception){
            false
        }
    }

    suspend fun sendFriendshipRequest(userDto: UserDto):Boolean{
        return try {
            val url = "$REST_API_ADDRESS/friends/sendRequest"
            val result = httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(userDto)
            }
            result.status==HttpStatusCode.OK
        } catch (e:Exception){
            false
        }
    }

    suspend fun getNearRestaurants(latitude: Double, longitude: Double): List<Place> {
        val url = "$REST_API_ADDRESS/places/nearPlaces"
        val bodyDto = PositionDto(
            latitude = latitude,
            longitude = longitude
        )
        return try {
            httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(bodyDto)
            }
                .body<List<PlaceDto>>()
                .map { it.toDto() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun rejectFriendshipRequest(friendshipRequestDto: FriendshipRequestDto):Boolean{
        return try {
            val url = "$REST_API_ADDRESS/friends/rejectRequest"
            val result = httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(friendshipRequestDto)
            }
            result.status==HttpStatusCode.OK
        } catch (e:Exception){
            false
        }
    }

    suspend fun acceptFriendshipRequest(friendshipRequestDto: FriendshipRequestDto):Boolean{
        return try {
            val url = "$REST_API_ADDRESS/friends/acceptRequest"
            val result = httpClient.post(url) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(friendshipRequestDto)
            }
            result.status==HttpStatusCode.OK
        } catch (e:Exception){
            false
        }
    }



    suspend fun getPendingRequests():List<FriendshipRequestDto>{
        return try {
            val url = "$REST_API_ADDRESS/friends/pendingRequests"
            val result = httpClient.get(url) {
                bearerAuth(accessToken)
            }
            if (result.status==HttpStatusCode.OK){
                Json.decodeFromString<List<FriendshipRequestDto>>(result.bodyAsText())
            } else emptyList()
        } catch (e:Exception){
            emptyList()
        }
    }



    suspend fun refresh():AuthResults {
        try {
            val url = "$REST_API_ADDRESS/token/expiredAccess"
            val result = httpClient.post(url) {
                bearerAuth(refreshToken)
            }
            if (result.status==HttpStatusCode.OK){
                val tokens = Json.decodeFromString<AuthResults.TokenDtos>(result.bodyAsText())
                updateTokens(tokens.accessToken,tokens.refreshToken)
                return tokens
            }
            else{
                return AuthResults.ErrorLoginResponseDto(LoginErrorReason.CREDENTIALS_INVALID,"Error while using refresh token")
            }
        }
        catch (e:Exception){
            return AuthResults.ErrorLoginResponseDto(LoginErrorReason.INTERNAL_ERROR,"Error while using refresh token")
        }
    }

    suspend fun register(username: String,password: String,email: String):AuthResults {
        try {
            val url = "$REST_API_ADDRESS/register"
            val registerDto = RegistrationRequestsDtos(username, password, email, null)
            val result = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(registerDto)
            }
            return when (result.status) {
                HttpStatusCode.BadRequest -> Json.decodeFromString<AuthResults.ErrorRegistrationResponseDto>(
                    result.bodyAsText()
                )

                HttpStatusCode.Accepted -> Json.decodeFromString<AuthResults.TokenDtos>(result.bodyAsText())

                else -> AuthResults.ErrorRegistrationResponseDto(RegistrationErrorReason.INTERNAL_ERROR,
                    "Error Receiving Datas")
            }
        }
        catch (e:Throwable){
            return  AuthResults.ErrorRegistrationResponseDto(RegistrationErrorReason.INTERNAL_ERROR,
                "Unreachable server")
        }
    }

    fun logout(){
        accessToken=""
        refreshToken=""
    }

    fun updateTokens(newAccessToken: String, newRefreshToken: String) {
        this.accessToken = newAccessToken
        this.refreshToken = newRefreshToken
    }

}