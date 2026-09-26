package com.mirrortouch.app

import android.content.SharedPreferences

object Prefs {
    const val NAME = "mirror_prefs"
    private const val KEY_OFFSET_X = "offset_x"
    private const val KEY_ENABLED = "enabled"

    // Default guess only — must be calibrated per device/orientation.
    fun getOffsetX(prefs: SharedPreferences): Float =
        prefs.getFloat(KEY_OFFSET_X, 540f)

    fun setOffsetX(prefs: SharedPreferences, value: Float) {
        prefs.edit().putFloat(KEY_OFFSET_X, value).apply()
    }

    fun isMirrorEnabled(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(KEY_ENABLED, true)

    fun setMirrorEnabled(prefs: SharedPreferences, value: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, value).apply()
    }
}
