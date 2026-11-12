package com.pokke.music.data.pairing

import android.content.Context
import java.util.UUID
import kotlin.random.Random

data class PairingSession(
    val code: String,
    val pin: String,
    val createdAt: Long = System.currentTimeMillis()
)

object DevicePairing {
    private const val PREFS = "pokke_pairing"
    private const val KEY_CODE = "active_code"
    private const val KEY_PIN = "active_pin"
    private const val KEY_LINKED = "linked_devices" // simple CSV for demo

    fun createSession(context: Context): PairingSession {
        val code = UUID.randomUUID().toString().replace("-", "").take(12)
        val pin = Random.nextInt(1000, 9999).toString()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_CODE, code)
            .putString(KEY_PIN, pin)
            .apply()
        return PairingSession(code, pin)
    }

    fun current(context: Context): PairingSession? {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val code = p.getString(KEY_CODE, null) ?: return null
        val pin = p.getString(KEY_PIN, null) ?: return null
        return PairingSession(code, pin)
    }

    /** Phone confirms pair with code+pin → marks device linked locally. */
    fun confirmPair(context: Context, code: String, pin: String, deviceName: String): Boolean {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ok = p.getString(KEY_CODE, null) == code && p.getString(KEY_PIN, null) == pin
        if (ok) {
            val linked = p.getString(KEY_LINKED, "").orEmpty()
            val next = (linked.split("|").filter { it.isNotBlank() } + deviceName).distinct().joinToString("|")
            p.edit().putString(KEY_LINKED, next).apply()
        }
        return ok
    }

    fun linkedDevices(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LINKED, "").orEmpty()
        return raw.split("|").filter { it.isNotBlank() }
    }

    fun unlink(context: Context, deviceName: String) {
        val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = linkedDevices(context).filterNot { it == deviceName }.joinToString("|")
        p.edit().putString(KEY_LINKED, next).apply()
    }
}
