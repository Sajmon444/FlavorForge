package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class SignUpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("data") val data: SignUpUserData
)

data class SignUpUserData(
    @SerializedName("name") val name: String
)
