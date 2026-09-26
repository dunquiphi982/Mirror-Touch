package com.mirrortouch.app

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var offsetInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 96, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "MirrorTouch"
            textSize = 22f
        })

        root.addView(TextView(this).apply {
            text = "Cách dùng:\n" +
                "1. Bật Accessibility Service cho app này.\n" +
                "2. Cầm máy ngang, chia đôi màn hình, mở game ở cả 2 bên.\n" +
                "3. Bấm 'Dùng nửa chiều rộng' rồi 'Áp dụng offset' (chỉnh tay nếu lệch).\n" +
                "4. Bật nút nổi để bật/tắt mirroring nhanh trong lúc chơi.\n" +
                "5. Chạm ở nửa bên TRÁI để kiểm tra — nửa bên PHẢI sẽ tự chạm theo."
            setPadding(0, 24, 0, 32)
        })

        root.addView(Button(this).apply {
            text = "Mở cài đặt Accessibility"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        val halfWidth = resources.displayMetrics.widthPixels / 2f

        offsetInput = EditText(this).apply {
            hint = "Offset X (px)"
            setText(Prefs.getOffsetX(prefs).toString())
        }
        root.addView(offsetInput)

        root.addView(Button(this).apply {
            text = "Dùng nửa chiều rộng ($halfWidth px)"
            setOnClickListener {
                offsetInput.setText(halfWidth.toString())
            }
        })

        root.addView(Button(this).apply {
            text = "Áp dụng offset"
            setOnClickListener {
                val value = offsetInput.text.toString().toFloatOrNull()
                if (value != null) {
                    Prefs.setOffsetX(prefs, value)
                    Toast.makeText(this@MainActivity, "Đã lưu offset = $value", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Giá trị không hợp lệ", Toast.LENGTH_SHORT).show()
                }
            }
        })

        val toggleBtn = Button(this).apply {
            text = if (Prefs.isMirrorEnabled(prefs)) "Tắt mirroring" else "Bật mirroring"
            setOnClickListener {
                val newState = !Prefs.isMirrorEnabled(prefs)
                Prefs.setMirrorEnabled(prefs, newState)
                text = if (newState) "Tắt mirroring" else "Bật mirroring"
            }
        }
        root.addView(toggleBtn)

        root.addView(Button(this).apply {
            text = "Bật nút nổi bật/tắt nhanh"
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !Settings.canDrawOverlays(this@MainActivity)
                ) {
                    startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:$packageName")
                        )
                    )
                } else {
                    startService(Intent(this@MainActivity, OverlayControlService::class.java))
                }
            }
        })

        setContentView(root)
    }
}
