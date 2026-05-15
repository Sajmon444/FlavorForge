package pl.edu.anstar.flavorforge.domain.repository

import pl.edu.anstar.flavorforge.data.model.SignUpResponse

interface AuthRepository {
    suspend fun signUp(email: String, password: String, name: String): Result<SignUpResponse>
    suspend fun signIn(email: String, password: String): Result<SignUpResponse>
}
