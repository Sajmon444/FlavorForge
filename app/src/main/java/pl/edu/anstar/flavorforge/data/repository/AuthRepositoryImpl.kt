package pl.edu.anstar.flavorforge.data.repository

import android.util.Log
import com.google.gson.Gson
import pl.edu.anstar.flavorforge.data.model.*
import pl.edu.anstar.flavorforge.data.remote.AuthApiService
import pl.edu.anstar.flavorforge.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApiService: AuthApiService,
    private val gson: Gson
) : AuthRepository {

    override suspend fun signUp(email: String, password: String, name: String): Result<SignUpResponse> {
        return try {
            val request = SignUpRequest(email, password, SignUpUserData(name))
            Log.d("AuthRepo", "Attempting signUp for: $email")
            
            val response = authApiService.signUp(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AuthRepo", "SignUp successful. Body: $body")
                if (body != null) {
                    Result.success(body)
                } else {
                    Log.e("AuthRepo", "SignUp successful but body is null")
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepo", "SignUp failed. Code: ${response.code()}, Error Body: $errorBody")
                
                val errorResponse = try {
                    gson.fromJson(errorBody, SupabaseError::class.java)
                } catch (e: Exception) {
                    Log.e("AuthRepo", "Could not parse error JSON", e)
                    null
                }
                
                val errorMessage = errorResponse?.message ?: "Registration failed (Status: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Exception during signUp", e)
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<SignUpResponse> {
        return try {
            val request = SignInRequest(email, password)
            Log.d("AuthRepo", "Attempting signIn for: $email")

            val response = authApiService.signIn(request = request)

            if (response.isSuccessful) {
                val body = response.body()
                Log.d("AuthRepo", "SignIn successful. Body: $body")
                if (body != null) {
                    Result.success(body)
                } else {
                    Log.e("AuthRepo", "SignIn successful but body is null")
                    Result.failure(Exception("Response body is null"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("AuthRepo", "SignIn failed. Code: ${response.code()}, Error Body: $errorBody")

                val errorResponse = try {
                    gson.fromJson(errorBody, SupabaseError::class.java)
                } catch (e: Exception) {
                    Log.e("AuthRepo", "Could not parse error JSON", e)
                    null
                }

                val errorMessage = errorResponse?.message ?: "Login failed (Status: ${response.code()})"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e("AuthRepo", "Exception during signIn", e)
            Result.failure(e)
        }
    }
}
