package pl.edu.anstar.flavorforge.data.model

import com.google.gson.annotations.SerializedName

data class SignUpResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("token_type") val token_type: String?,
    @SerializedName("user") val user: UserInfo?
)

data class UserInfo(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("user_metadata") val userMetadata: UserMetadata?
)

data class UserMetadata(
    @SerializedName("name") val name: String?
)

data class SupabaseError(
    @SerializedName("code") val code: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("error_code") val errorCode: String?,
    @SerializedName("msg") val msg: String?,
    @SerializedName("message") val messageField: String?,
    @SerializedName("error_description") val errorDescription: String?
) {
    val message: String
        get() = msg ?: messageField ?: errorDescription ?: error ?: "Unknown error"
}
