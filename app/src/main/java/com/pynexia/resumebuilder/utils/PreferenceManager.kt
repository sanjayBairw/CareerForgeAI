package com.pynexia.resumebuilder.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("careerforge_preferences", Context.MODE_PRIVATE)

    var isDarkModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var aiApiKey: String
        get() = prefs.getString(KEY_AI_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_AI_API_KEY, value).apply()

    var aiEndpointUrl: String
        get() = prefs.getString(KEY_AI_ENDPOINT, "https://api.openai.com/v1/") ?: "https://api.openai.com/v1/"
        set(value) = prefs.edit().putString(KEY_AI_ENDPOINT, value).apply()

    var aiModelName: String
        get() = prefs.getString(KEY_AI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"
        set(value) = prefs.edit().putString(KEY_AI_MODEL, value).apply()

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Alex Johnson") ?: "Alex Johnson"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var userHeadline: String
        get() = prefs.getString(KEY_USER_HEADLINE, "Lead Mobile Architect") ?: "Lead Mobile Architect"
        set(value) = prefs.edit().putString(KEY_USER_HEADLINE, value).apply()

    var userEmail: String
        get() = prefs.getString(KEY_USER_EMAIL, "alex.johnson@pynexia.io") ?: "alex.johnson@pynexia.io"
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var userPhone: String
        get() = prefs.getString(KEY_USER_PHONE, "+1 (555) 019-2834") ?: "+1 (555) 019-2834"
        set(value) = prefs.edit().putString(KEY_USER_PHONE, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, true)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    companion object {
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_AI_API_KEY = "key_ai_api_key"
        private const val KEY_AI_ENDPOINT = "key_ai_endpoint"
        private const val KEY_AI_MODEL = "key_ai_model"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_HEADLINE = "key_user_headline"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_PHONE = "key_user_phone"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }
}
