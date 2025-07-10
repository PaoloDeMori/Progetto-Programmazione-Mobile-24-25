package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.loginDtos.UserDto
import com.example.appranzo.data.models.Badge
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeRoadUiState(
    val points: Int = 0,
    val badges: List<Badge> = emptyList(),
    val lastUnlockedIndex: Int = 0,
    val username: String = "",
    val hasNewBadges: Boolean=false
)

class BadgeRoadViewModel(private val profileDetailViewModel: ProfileDetailViewModel) : ViewModel() {

    private val _uiState = MutableStateFlow(BadgeRoadUiState())
    val uiState: StateFlow<BadgeRoadUiState> = _uiState.asStateFlow()
    private var currentUserLastUnlockedIndex: Int? = null

    fun loadBadgeData(userDto: UserDto) {
        viewModelScope.launch {
            val badges = Badge.roadmapBadge
            val currentUser = profileDetailViewModel.user.first()

            val previousUnlockedIndex = _uiState.value.lastUnlockedIndex
            val newLastUnlockedIndex = badges.indexOfLast { userDto.points >= it.threshold }
            var newBadgeWasUnlocked = false

            if (currentUser != null && userDto.id == currentUser.id) {
                if (currentUserLastUnlockedIndex == null) {
                    currentUserLastUnlockedIndex = newLastUnlockedIndex
                } else {
                    if (newLastUnlockedIndex > currentUserLastUnlockedIndex!!) {
                        newBadgeWasUnlocked = true
                    }
                    currentUserLastUnlockedIndex = newLastUnlockedIndex
                }
            }

            _uiState.update {
                it.copy(
                    points = userDto.points,
                    badges = badges,
                    lastUnlockedIndex = newLastUnlockedIndex,
                    username = userDto.username,
                    hasNewBadges = (uiState.value.hasNewBadges || newBadgeWasUnlocked) && (userDto.id == currentUser?.id)
                )
            }
        }
    }

    fun onBadgesScreenViewed() {
        _uiState.update { it.copy(hasNewBadges = false) }
    }
}