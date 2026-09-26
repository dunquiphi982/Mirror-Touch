package com.mirrortouch.app

import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import kotlin.math.abs

class OverlayControlService : Service() {

    private var windowManager: WindowManager? = null
    private var overlayButton: Button? = null
    private lateinit var prefs: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            170,
            170,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        val enabled = Prefs.isMirrorEnabled(prefs)
        val btn = Button(this).apply {
            text = if (enabled) "ON" else "OFF"
            setBackgroundColor(if (enabled) Color.parseColor("#4CAF50") else Color.parseColor("#F44336"))
            alpha = 0.85f
        }

        var initialX = 0
        var initialY = 0
        var lastRawX = 0f
        var lastRawY = 0f
        var downTime = 0L
        var moved = false

        btn.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    lastRawX = event.rawX
                    lastRawY = event.rawY
                    downTime = System.currentTimeMillis()
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - lastRawX).toInt()
                    val dy = (event.rawY - lastRawY).toInt()
                    if (abs(dx) > 8 || abs(dy) > 8) moved = true
                    params.x = initialX + dx
                    params.y = initialY + dy
                    windowManager?.updateViewLayout(view, params)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!moved && System.currentTimeMillis() - downTime < 300) {
                        val newState = !Prefs.isMirrorEnabled(prefs)
                        Prefs.setMirrorEnabled(prefs, newState)
                        (view as Button).text = if (newState) "ON" else "OFF"
                        view.setBackgroundColor(
                            if (newState) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
                        )
                    }
                    true
                }
                else -> false
            }
        }

        overlayButton = btn
        windowManager?.addView(btn, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayButton?.let { windowManager?.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
