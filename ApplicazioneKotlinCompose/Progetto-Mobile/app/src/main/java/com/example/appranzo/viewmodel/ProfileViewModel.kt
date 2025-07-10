package com.example.appranzo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.data.repository.TokensRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileViewModel(private val restApiClient: RestApiClient, private val tokenRepo:TokensRepository):ViewModel(){



    fun logOut(onSuccessfullLogout:()->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            restApiClient.logout()
            tokenRepo.clearTokens()
            onSuccessfullLogout()
        }
    }

}