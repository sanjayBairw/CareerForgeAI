package com.pynexia.resumebuilder.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Extension to apply window insets (status bar and navigation bar) as view padding
 * to prevent content from drawing underneath system bars or notches on Android 15+.
 */
fun View.applySystemBarsPadding(applyTop: Boolean = true, applyBottom: Boolean = false) {
    val initialPaddingTop = paddingTop
    val initialPaddingBottom = paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.updatePadding(
            top = if (applyTop) initialPaddingTop + systemBars.top else v.paddingTop,
            bottom = if (applyBottom) initialPaddingBottom + systemBars.bottom else v.paddingBottom
        )
        insets
    }

    if (isAttachedToWindow) {
        ViewCompat.requestApplyInsets(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }
            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}
