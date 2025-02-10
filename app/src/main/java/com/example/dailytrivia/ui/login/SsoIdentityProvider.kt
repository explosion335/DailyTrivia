package com.example.dailytrivia.ui.login
// https://github.com/element-hq/element-android/
import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class SsoIdentityProvider(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String?,
    @Json(name = "icon") val iconUrl: String?,
    @Json(name = "brand") val brand: String?
) : Parcelable, Comparable<SsoIdentityProvider> {

    companion object {
        const val BRAND_GOOGLE = "google"
        const val BRAND_GITHUB = "github"
        const val BRAND_FACEBOOK = "facebook"
        const val BRAND_GITLAB = "gitlab"
    }

    override fun compareTo(other: SsoIdentityProvider): Int {
        return other.toPriority().compareTo(toPriority())
    }

    private fun toPriority(): Int {
        return when (brand) {
            BRAND_GOOGLE -> 3
            BRAND_FACEBOOK -> 2
            BRAND_GITHUB,
            BRAND_GITLAB -> 1

            else -> 0
        }
    }
}
