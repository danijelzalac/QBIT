package com.qbit.chat.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * QBIT Secure PIN Manager
 *
 * Handles PIN storage, hashing, and verification using PBKDF2-HMAC-SHA256
 * with per-device random salt. PINs are never stored in plaintext.
 *
 * Storage: EncryptedSharedPreferences (AES-256-GCM)
 * Hash: PBKDF2WithHmacSHA256, 200,000 iterations, 256-bit output
 */
object PinManager {

    private const val PREFS_NAME = "qbit_secure_prefs"
    private const val KEY_REAL_PIN_HASH = "pin_real_hash"
    private const val KEY_REAL_PIN_SALT = "pin_real_salt"
    private const val KEY_DECOY_PIN_HASH = "pin_decoy_hash"
    private const val KEY_DECOY_PIN_SALT = "pin_decoy_salt"
    private const val KEY_PANIC_PIN_HASH = "pin_panic_hash" // QBIT: Panic PIN
    private const val KEY_PANIC_PIN_SALT = "pin_panic_salt"
    private const val KEY_PIN_CONFIGURED = "pin_configured"

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 200_000
    private const val HASH_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 32

    private fun getStorage(context: Context): PinStorage {
        return AndroidPinStorage(context)
    }

    /**
     * Check if the user has already configured their PINs.
     * If false, the app should show the PIN setup screen.
     */
    fun isPinConfigured(context: Context): Boolean = isPinConfigured(getStorage(context))

    fun isPinConfigured(storage: PinStorage): Boolean {
        return storage.getBoolean(KEY_PIN_CONFIGURED, false)
    }

    /**
     * Store the real (unlock) PIN.
     */
    fun setRealPin(context: Context, pin: String) = setRealPin(getStorage(context), pin)

    fun setRealPin(storage: PinStorage, pin: String) {
        val (hash, salt) = PinCrypto.hashPin(pin)
        storage.putString(KEY_REAL_PIN_HASH, hash)
        storage.putString(KEY_REAL_PIN_SALT, salt)
        markConfigured(storage)
    }

    /**
     * Store the decoy PIN (optional).
     */
    fun setDecoyPin(context: Context, pin: String) = setDecoyPin(getStorage(context), pin)

    fun setDecoyPin(storage: PinStorage, pin: String) {
        val (hash, salt) = PinCrypto.hashPin(pin)
        storage.putString(KEY_DECOY_PIN_HASH, hash)
        storage.putString(KEY_DECOY_PIN_SALT, salt)
    }

    /**
     * Store the panic (wipe) PIN (optional).
     */
    fun setPanicPin(context: Context, pin: String) = setPanicPin(getStorage(context), pin)

    fun setPanicPin(storage: PinStorage, pin: String) {
        val (hash, salt) = PinCrypto.hashPin(pin)
        storage.putString(KEY_PANIC_PIN_HASH, hash)
        storage.putString(KEY_PANIC_PIN_SALT, salt)
    }

    /**
     * Verify the entered PIN.
     * Returns: REAL, DECOY, WIPE, or WRONG.
     */
    fun verifyEnteredPin(context: Context, pin: String): PinResult = verifyEnteredPin(getStorage(context), pin)

    fun verifyEnteredPin(storage: PinStorage, pin: String): PinResult {
        // Check real PIN
        val realHash = storage.getString(KEY_REAL_PIN_HASH, null)
        val realSalt = storage.getString(KEY_REAL_PIN_SALT, null)
        if (realHash != null && realSalt != null && PinCrypto.verifyPin(pin, realHash, realSalt)) {
            return PinResult.REAL
        }

        // Check decoy PIN
        val decoyHash = storage.getString(KEY_DECOY_PIN_HASH, null)
        val decoySalt = storage.getString(KEY_DECOY_PIN_SALT, null)
        if (decoyHash != null && decoySalt != null && PinCrypto.verifyPin(pin, decoyHash, decoySalt)) {
            return PinResult.DECOY
        }

        // Check panic PIN
        val panicHash = storage.getString(KEY_PANIC_PIN_HASH, null)
        val panicSalt = storage.getString(KEY_PANIC_PIN_SALT, null)
        if (panicHash != null && panicSalt != null && PinCrypto.verifyPin(pin, panicHash, panicSalt)) {
            return PinResult.WIPE
        }

        return PinResult.WRONG
    }

    /**
     * Check if decoy PIN has been configured.
     */
    fun isDecoyConfigured(context: Context): Boolean = isDecoyConfigured(getStorage(context))

    fun isDecoyConfigured(storage: PinStorage): Boolean {
        return storage.getString(KEY_DECOY_PIN_HASH, null) != null
    }

    /**
     * Check if panic PIN has been configured.
     */
    fun isPanicConfigured(context: Context): Boolean = isPanicConfigured(getStorage(context))

    fun isPanicConfigured(storage: PinStorage): Boolean {
        return storage.getString(KEY_PANIC_PIN_HASH, null) != null
    }

    /**
     * Silent Wipe: Deletes all chat data, keys, and settings (except PINs).
     * Kills the process to ensure memory is cleared.
     */
    fun wipe(context: Context) {
        try {
            // 1. Delete Databases
            context.databaseList().forEach { dbName ->
                context.deleteDatabase(dbName)
            }

            // 2. Clear Shared Preferences (except secure prefs)
            val prefsDir = java.io.File(context.filesDir.parent, "shared_prefs")
            if (prefsDir.exists()) {
                prefsDir.listFiles()?.forEach { file ->
                    if (file.name != "$PREFS_NAME.xml") {
                        file.delete()
                    }
                }
            }

            // 3. Clear Files
            context.filesDir.deleteRecursively()
            context.cacheDir.deleteRecursively()

            // 4. Kill Process
            android.os.Process.killProcess(android.os.Process.myPid())
            kotlin.system.exitProcess(0)
        } catch (e: Exception) {
            // Best effort - ensures crash implies data loss/unstable state
            e.printStackTrace()
            kotlin.system.exitProcess(1)
        }
    }

    /**
     * Clear all PIN data (for factory reset / full wipe).
     */
    fun clearAll(context: Context) {
        getStorage(context).clear()
    }

    private fun markConfigured(storage: PinStorage) {
        storage.putBoolean(KEY_PIN_CONFIGURED, true)
    }

    private class AndroidPinStorage(val context: Context) : PinStorage {
        private val prefs: SharedPreferences by lazy {
             val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
             EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }

        override fun getString(key: String, defValue: String?): String? = prefs.getString(key, defValue)
        override fun putString(key: String, value: String) = prefs.edit().putString(key, value).apply()
        override fun getBoolean(key: String, defValue: Boolean): Boolean = prefs.getBoolean(key, defValue)
        override fun putBoolean(key: String, value: Boolean) = prefs.edit().putBoolean(key, value).apply()
        override fun clear() = prefs.edit().clear().apply()
        override fun getAll(): Map<String, *> = prefs.all
    }

}

enum class PinResult {
    REAL,
    DECOY,
    WIPE,
    WRONG
}
