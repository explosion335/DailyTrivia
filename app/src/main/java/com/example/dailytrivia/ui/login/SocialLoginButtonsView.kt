package com.example.dailytrivia.ui.login

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.google.android.material.button.MaterialButton
import com.example.dailytrivia.R
import java.util.Locale

class SocialLoginButtonsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    private var listener: InteractionListener? = null

    private var ssoIdentityProviders: List<SsoIdentityProvider>? = null
        set(value) {
            field = value
            updateButtons()
        }

    init {
        orientation = VERTICAL

    }

    private fun updateButtons() {
        removeAllViews()
//
//        // If no providers are set, add a default SSO button
//        if (ssoIdentityProviders.isNullOrEmpty()) {
//            addDefaultButton()
//            return
//        }

        // Dynamically add buttons for each provider
        ssoIdentityProviders?.forEach { provider ->
            val button = createButtonForProvider(provider)
            addView(button)
        }
    }

//    private fun addDefaultButton() {
//        val button = MaterialButton(context).apply {
//            text = "Single Sign-On"
//            setOnClickListener { listener?.onProviderSelected(null) }
//
//        }
//        addView(button)
//    }

    private fun createButtonForProvider(provider: SsoIdentityProvider): MaterialButton {
        return MaterialButton(context).apply {
            text =
                String.format(
                    Locale.getDefault(),
                    resources.getString(R.string.continue_with) + "${provider.name}"
                )
            setOnClickListener { listener?.onProviderSelected(provider) }

            // Hardcoded styles based on provider branding
            when (provider.brand) {
                SsoIdentityProvider.BRAND_GOOGLE -> setStyle(R.attr.social_login_button_google_style)
                SsoIdentityProvider.BRAND_FACEBOOK -> setStyle(R.attr.social_login_button_facebook_style)
                SsoIdentityProvider.BRAND_GITHUB -> setStyle(R.attr.social_login_button_github_style)
                SsoIdentityProvider.BRAND_GITLAB -> setStyle(R.attr.social_login_button_gitlab_style)
            }
        }
    }

    private fun MaterialButton.setStyle(style: Int) {
        setTextAppearance(style)
        textAlignment = TEXT_ALIGNMENT_CENTER
    }

    fun interface InteractionListener {
        fun onProviderSelected(provider: SsoIdentityProvider?)
    }
}
