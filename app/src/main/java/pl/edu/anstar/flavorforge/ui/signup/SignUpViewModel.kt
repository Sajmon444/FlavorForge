package pl.edu.anstar.flavorforge.ui.signup

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
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _signUpResult = MutableLiveData<Result<SignUpResponse>>()
    val signUpResult: LiveData<Result<SignUpResponse>> = _signUpResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signUp(email: String, password: String, name: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = authRepository.signUp(email, password, name)
            _signUpResult.value = result
            _isLoading.value = false
        }
    }
}
