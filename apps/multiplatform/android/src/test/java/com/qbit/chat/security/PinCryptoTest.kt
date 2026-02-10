package com.qbit.chat.security

import org.junit.Assert.*
import org.junit.Test

class PinCryptoTest {

    @Test
    fun testHashPin_ReturnsConsistentResults() {
        val pin = "1234"
        val (hash1, salt1) = PinCrypto.hashPin(pin)
        
        // Verify we got something back
        assertNotNull(hash1)
        assertNotNull(salt1)
        assertTrue(hash1.isNotEmpty())
        assertTrue(salt1.isNotEmpty())

        // Verify salt length (32 bytes = 64 hex chars)
        assertEquals(64, salt1.length)
        
        // Use the salt to verify
        assertTrue(PinCrypto.verifyPin(pin, hash1, salt1))
    }

    @Test
    fun testVerifyPin_RejectsWrongPin() {
        val pin = "1234"
        val wrongPin = "0000"
        val (hash, salt) = PinCrypto.hashPin(pin)

        // Correct PIN works
        assertTrue(PinCrypto.verifyPin(pin, hash, salt))

        // Wrong PIN fails
        assertFalse(PinCrypto.verifyPin(wrongPin, hash, salt))
    }

    @Test
    fun testHashPin_GeneratesDifferentSalts() {
        val pin = "1234"
        val (hash1, salt1) = PinCrypto.hashPin(pin)
        val (hash2, salt2) = PinCrypto.hashPin(pin)

        // Salts should be random and different
        assertNotEquals(salt1, salt2)
        // Hashes should thus be different
        assertNotEquals(hash1, hash2)

        // But both should be verifiable with their respective salts
        assertTrue(PinCrypto.verifyPin(pin, hash1, salt1))
        assertTrue(PinCrypto.verifyPin(pin, hash2, salt2))
    }

    @Test
    fun testConstantTimeEquals() {
        assertTrue(PinCrypto.constantTimeEquals("abc", "abc"))
        assertFalse(PinCrypto.constantTimeEquals("abc", "abd"))
        assertFalse(PinCrypto.constantTimeEquals("abc", "ab"))
        assertFalse(PinCrypto.constantTimeEquals("abc", ""))
    }
}
