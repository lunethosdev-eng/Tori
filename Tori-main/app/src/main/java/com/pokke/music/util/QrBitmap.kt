package com.pokke.music.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface

/**
 * Pairing visual without ZXing (avoids missing dependency on CI).
 * Draws a high-contrast card with PIN + short code; phone can still use deep link / manual PIN.
 * When you add `implementation("com.google.zxing:core:3.5.3")` in build.gradle.kts
 * you can replace this with a real QR matrix later.
 */
object QrBitmap {
    fun encode(payload: String, sizePx: Int = 512): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(AndroidColor.WHITE)

        val pin = Regex("""pin=([^&]+)""").find(payload)?.groupValues?.getOrNull(1).orEmpty()
        val code = Regex("""code=([^&]+)""").find(payload)?.groupValues?.getOrNull(1).orEmpty()
        val shortCode = if (code.length > 8) code.take(8) else code

        val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.02f
        }
        c.drawRoundRect(
            RectF(sizePx * 0.06f, sizePx * 0.06f, sizePx * 0.94f, sizePx * 0.94f),
            sizePx * 0.04f, sizePx * 0.04f, border
        )

        val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.07f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        c.drawText("POKKE TV", sizePx / 2f, sizePx * 0.22f, title)

        val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.DKGRAY
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.055f
        }
        c.drawText("PIN", sizePx / 2f, sizePx * 0.38f, label)

        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.16f
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        val pinText = if (pin.isNotBlank()) pin.chunked(1).joinToString(" ") else "----"
        c.drawText(pinText, sizePx / 2f, sizePx * 0.55f, pinPaint)

        c.drawText("código", sizePx / 2f, sizePx * 0.68f, label)
        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.BLACK
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.06f
            typeface = Typeface.MONOSPACE
        }
        c.drawText(shortCode.ifBlank { "--------" }, sizePx / 2f, sizePx * 0.78f, codePaint)

        val hint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.GRAY
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.04f
        }
        c.drawText("pokke://pair", sizePx / 2f, sizePx * 0.88f, hint)

        return bmp
    }

    fun pairPayload(code: String, pin: String): String =
        "pokke://pair?code=${code.trim()}&pin=${pin.trim()}"
}
