package com.example.papertrail.localization

import android.content.Context
import android.content.res.Configuration
import java.util.*

object LocaleHelper {
    fun wrap(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
