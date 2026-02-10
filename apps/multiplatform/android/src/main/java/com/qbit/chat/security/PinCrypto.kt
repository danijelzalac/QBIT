package com.qbit.chat.security

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * QBIT Crypto Utilities for PIN management.
 * Pure Kotlin/Java implementation suitable for local unit testing.
 */
object PinCrypto {

    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val PBKDF2_ITERATIONS = 200_000
    private const val HASH_LENGTH_BITS = 256
    private const val SALT_LENGTH_BYTES = 32

    /**
     * Hash a PIN using PBKDF2 with a random salt.
     * Returns Pair(hash as hex string, salt as hex string)
     */
    fun hashPin(pin: String): Pair<String, String> {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        val hash = deriveKey(pin, salt)
        return Pair(hash.toHexString(), salt.toHexString())
    }

    /**
     * Verify a PIN against a stored hash + salt.
     */
    fun verifyPin(pin: String, storedHash: String, storedSalt: String): Boolean {
        val salt = storedSalt.hexToByteArray()
        val derivedHash = deriveKey(pin, salt).toHexString()
        // Constant-time comparison to prevent timing attacks
        return constantTimeEquals(derivedHash, storedHash)
    }

    /**
     * Derive key from PIN + salt using PBKDF2.
     */
    fun deriveKey(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Constant-time string comparison to prevent timing-based side-channel attacks.
     */
    fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    // --- Utility ---

    private fun ByteArray.toHexString(): String =
        joinToString("") { "%02x".format(it) }

    private fun String.hexToByteArray(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
