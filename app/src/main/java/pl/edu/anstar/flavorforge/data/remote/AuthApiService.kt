package pl.edu.anstar.flavorforge.data.remote

import pl.edu.anstar.flavorforge.data.model.SignUpRequest
import pl.edu.anstar.flavorforge.data.model.SignUpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("/auth/v1/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<SignUpResponse>
}
