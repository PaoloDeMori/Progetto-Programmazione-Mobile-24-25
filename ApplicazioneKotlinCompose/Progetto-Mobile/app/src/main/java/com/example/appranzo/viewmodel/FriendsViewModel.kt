package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.friendship.FriendshipRequestDto
import com.example.appranzo.communication.remote.loginDtos.UserDto
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UiState {
    IDLE, LOADING, SUCCESS, ERROR
}

class FriendsViewModel(private val api: RestApiClient) : ViewModel() {

    private val _friends = MutableStateFlow<List<UserDto>>(emptyList())
    val friends = _friends.asStateFlow()

    private val _pendingRequests = MutableStateFlow<Map<FriendshipRequestDto,String>>(emptyMap())
    val pendingRequests = _pendingRequests.asStateFlow()

    private val _uiState = MutableStateFlow(UiState.IDLE)
    val uiState = _uiState.asStateFlow()

    private val _usernameInput = MutableStateFlow("")
    val usernameInput = _usernameInput.asStateFlow()

    init {
        fetchAllData()
    }

    fun onUsernameChange(newUsername: String) {
        _usernameInput.value = newUsername
    }

     fun fetchAllData() {
        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            try {
                val friendsJob = async { fetchFriends() }
                val requestsJob = async { fetchPendingRequests() }
                friendsJob.await()
                requestsJob.await()
                _uiState.value = UiState.SUCCESS
            } catch (e: Exception) {
                _uiState.value = UiState.ERROR
                println("Errore durante il fetch dei dati: ${e.message}")
            }
        }
    }

    private suspend fun fetchFriends() {
        _friends.update { api.getAllFriends() }
    }

    private suspend fun fetchPendingRequests() {
        val pendingRequests=api.getPendingRequests()
        val map:MutableMap<FriendshipRequestDto,String> = LinkedHashMap()
        for(item in pendingRequests){
            val username = api.getUserById(item.senderId)
            map[item] = username?.username?:"Sconosciuto"
        }
        _pendingRequests.update { map }
    }

    fun sendFriendRequest() {
        if (_usernameInput.value.isBlank()) return

        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            val userToSendRequest = UserDto(
                id = 1, username = _usernameInput.value, photoUrl = null,
                email = "",
                points = 0
            )
            val success = api.sendFriendshipRequest(userToSendRequest)

            if (success) {
                _usernameInput.value = ""
                fetchAllData()
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }

    fun acceptRequest(request: FriendshipRequestDto) {
        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            val success = api.acceptFriendshipRequest(request)
            if (success) {
                fetchAllData()
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }

    fun rejectRequest(request: FriendshipRequestDto) {
        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            val success = api.rejectFriendshipRequest(request)
            if (success) {
                fetchPendingRequests()
                _uiState.value = UiState.SUCCESS
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }

    fun deleteFriend(friend:UserDto) {
        viewModelScope.launch {
            _uiState.value = UiState.LOADING
            val success = api.removeAFriend(friend)
            if (success) {
                fetchFriends()
                _uiState.value = UiState.SUCCESS
            } else {
                _uiState.value = UiState.ERROR
            }
        }
    }
}