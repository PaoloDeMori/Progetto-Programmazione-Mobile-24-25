package com.example.appranzo.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appranzo.communication.remote.RestApiClient
import com.example.appranzo.communication.remote.loginDtos.AuthResults
import com.example.appranzo.communication.remote.loginDtos.LoginErrorReason
import com.example.appranzo.communication.remote.loginDtos.RegistrationErrorReason
import com.example.appranzo.data.repository.TokensRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


data class AuthUiState(
    val name: String = "",
    val surname: String = "",
    val username: String = "",

    val email: String = "",
    val password: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val restApiClient: RestApiClient,private val tokensRepository: TokensRepository) : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onSurnameChange(value: String) = _state.update { it.copy(surname = value) }
    fun onUsernameChange(value: String) = _state.update { it.copy(username = value) }


    fun onEmailChange(value: String) {
        _state.update { it.copy(email = value, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, error = null) }
    }


    fun login(onSuccess: () -> Unit, onFailureDisplay: (message: String) -> Unit, ctx: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentValues = _state.value
            val username = currentValues.username
            val password = currentValues.password
            _state.update { it.copy(isLoading = true) }
            try {
                if (RestApiClient.isOnline(ctx)) {
                    when (val result = restApiClient.login(username, password)) {
                        is AuthResults.TokenDtos -> {
                            restApiClient.updateTokens(result.accessToken,result.refreshToken)
                            addTokens(result.accessToken,result.refreshToken)
                            onSuccess()
                        }

                        is AuthResults.ErrorLoginResponseDto -> {
                            val resultStatus = result.errorSignal
                            val optionalError = result.optionalMessage
                            when (resultStatus) {
                                LoginErrorReason.CREDENTIALS_INVALID -> {
                                    optionalError?.let { onFailureDisplay(it) }
                                    _state.update { it.copy(error = "Credenziali non valide") }
                                }

                                LoginErrorReason.DATABASE_ERROR -> {
                                    optionalError?.let { onFailureDisplay(it) }
                                    _state.update { it.copy(error = "Errore del Server") }
                                }

                                LoginErrorReason.INTERNAL_ERROR -> {
                                    optionalError?.let { onFailureDisplay(it) }
                                    _state.update { it.copy(error = "Errore Interno") }
                                }
                            }
                            _state.update { it.copy(isLoading = false) }
                        }

                        else -> {
                            onFailureDisplay("unexpected result")
                            _state.update { it.copy(error = "Errore inaspettato") }
                        }
                    }

                } else {
                    RestApiClient.openWirelessSettings(ctx)
                }
            } catch (e: Error) {
                onFailureDisplay(e.message ?: "Error")
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    fun register(onSuccess: () -> Unit, onFailureDisplay: (message: String) -> Unit, ctx: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val currentValues = _state.value
            val username = currentValues.username
            val password = currentValues.password
            val email = currentValues.email
            try {
                if (username.isBlank()) {
                    throw Error("Per favore scegli uno username valido")
                }
                if (email.isBlank()) {
                    throw Error("Per favore scegli un indirizzo email valido")
                }
                if (!email.contains(".")||!email.contains("@")){
                    throw Error("Per favore scegli un indirizzo email valido")
                }
                when {
                    password.isEmpty() -> {
                        throw Error("Per favore scegli una password valida")
                    }

                    password.length < 8 -> {
                        throw Error("La tua password deve contenere almeno 8 caratteri")
                    }

                    password.none { it.isUpperCase() } -> {
                        throw Error("La tua password deve contenere almeno una lettera maiuscola")
                    }

                    password.none { it.isLowerCase() } -> {
                        throw Error("La tua password deve contenere almeno una lettera minuscola")
                    }

                    password.none { it.isDigit() } -> {
                        throw Error("La tua password deve contenere almeno un numero")
                    }

                    password.all { it.isLetterOrDigit() } -> {
                        throw Error("La tua password deve contenere almeno un carattere speciale")
                    }
                }
                if (RestApiClient.isOnline(ctx)) {
                    when (val result = restApiClient.register(username, password,email)) {
                        is AuthResults.TokenDtos -> {
                            restApiClient.updateTokens(result.accessToken,result.refreshToken)
                            addTokens(result.accessToken,result.refreshToken)
                            onSuccess()
                        }

                        is AuthResults.ErrorRegistrationResponseDto -> {
                            when (result.errorSignal) {
                                RegistrationErrorReason.EMAIL_TAKEN -> _state.update {
                                    it.copy(error ="Email address is already taken")}
                                RegistrationErrorReason.PASSWORD_INVALID -> _state.update {
                                    it.copy(error ="The password provided is invalid")}
                                RegistrationErrorReason.USERNAME_TAKEN -> _state.update {
                                    it.copy(error ="Username is already taken")}
                                RegistrationErrorReason.USERNAME_INVALID -> _state.update {
                                    it.copy(error ="The username provided is invalid")}
                                else ->_state.update {
                                    it.copy(error ="Internal error occured")}
                            }
                            _state.update { it.copy(error = "Unexpected Error") }

                        }

                        else -> {
                            _state.update { it.copy(error = "Unexpected Error") }
                        }
                    }


                } else {
                    RestApiClient.openWirelessSettings(ctx)
                }
            } catch (e: Error) {
                _state.update {
                    it.copy(error = e.message?:"Unexpected Error")
                }
            }
            _state.update { it.copy(isLoading = false) }
        }
    }

    private fun addTokens(accessToken :String, refreshToken:String){
        viewModelScope.launch {
            tokensRepository.changeTokens(accessToken, refreshToken)
        }
    }

    suspend fun automaticLogin():Boolean{
        if(tokensRepository.hasTokens()){
            restApiClient.updateTokens(tokensRepository.accessToken.first(),tokensRepository.refreshToken.first())
            if(restApiClient.canILog()){
                return true
            }
            else{
                try {
                    when(val refreshResults = restApiClient.refresh()){
                        is AuthResults.TokenDtos -> {
                            tokensRepository.changeTokens(refreshResults.accessToken,refreshResults.refreshToken)
                            return true
                        }
                        else ->return false
                    }
                }
                catch (e:Exception){
                    return false
                }

            }
        }
        return false
    }
}