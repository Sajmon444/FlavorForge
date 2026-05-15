package pl.edu.anstar.flavorforge.ui.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.edu.anstar.flavorforge.data.model.SignUpResponse
import pl.edu.anstar.flavorforge.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signInResult = MutableLiveData<Result<SignUpResponse>>()
    val signInResult: LiveData<Result<SignUpResponse>> = _signInResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signIn(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signIn(email, password)
            _signInResult.value = result
            _isLoading.value = false
        }
    }
}
