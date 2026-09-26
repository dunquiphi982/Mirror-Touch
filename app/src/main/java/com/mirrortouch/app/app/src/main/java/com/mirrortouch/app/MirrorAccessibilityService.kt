package com.mirrortouch.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.SharedPreferences
import android.graphics.Path
import android.graphics.PointF
import android.os.Build
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi

class MirrorAccessibilityService : AccessibilityService() {

    private lateinit var prefs: SharedPreferences

    private val pathPoints = mutableListOf<PointF>()
    private var startTime = 0L
    private var tracking = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val info = serviceInfo
            info.motionEventSources = InputDevice.SOURCE_TOUCHSCREEN
            serviceInfo = info
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onMotionEvent(event: MotionEvent) {
        super.onMotionEvent(event)

        if (!::prefs.isInitialized) return
        if (!Prefs.isMirrorEnabled(prefs)) return
        if (event.source and InputDevice.SOURCE_TOUCHSCREEN != InputDevice.SOURCE_TOUCHSCREEN) return
        if (event.pointerCount > 1) return

        val offsetX = Prefs.getOffsetX(prefs)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                if (event.x < offsetX) {
                    tracking = true
                    startTime = event.eventTime
                    pathPoints.clear()
                    pathPoints.add(PointF(event.x + offsetX, event.y))
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (tracking) {
                    pathPoints.add(PointF(event.x + offsetX, event.y))
                }
            }

            MotionEvent.ACTION_UP -> {
                if (tracking) {
                    val duration = (event.eventTime - startTime).coerceAtLeast(10)
                    dispatchMirroredGesture(pathPoints.toList(), duration)
                    tracking = false
                }
            }

            MotionEvent.ACTION_CANCEL -> {
                tracking = false
            }
        }
    }

    private fun dispatchMirroredGesture(points: List<PointF>, durationMs: Long) {
        if (points.isEmpty()) return

        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (p in points.drop(1)) {
            path.lineTo(p.x, p.y)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0, durationMs)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()
        dispatchGesture(gesture, null, null)
    }
}
