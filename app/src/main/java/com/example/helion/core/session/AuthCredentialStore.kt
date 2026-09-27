package com.example.helion.core.session

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.helion.core.model.ServerEnvironment
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Stores companion bearer credentials with strict environment isolation.
 *
 * Production builds encrypt each token with an AES-GCM key held by AndroidKeyStore.
 * The environment name is authenticated as AAD, so ciphertext copied from PRIVATE TEST
 * cannot be decrypted as a PRODUCTION credential.
 *
 * Legacy plaintext placeholder values are migrated in place on first read.
 */
class AuthCredentialStore(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val secretKey: SecretKey by lazy { loadOrCreateSecretKey() }

    private fun tokenKey(env: ServerEnvironment): String = "auth_token_${env.name.lowercase()}"

    fun getCompanionToken(env: ServerEnvironment): String? {
        val stored = prefs.getString(tokenKey(env), null) ?: return null
        if (stored.startsWith(ENCRYPTED_PREFIX)) {
            return decrypt(env, stored)
        }

        // One-time migration from the M1 placeholder store, which used plaintext prefs.
        setCompanionToken(env, stored)
        return stored
    }

    fun setCompanionToken(env: ServerEnvironment, token: String?) {
        if (token.isNullOrBlank()) {
            clearCompanionToken(env)
            return
        }
        require(env != ServerEnvironment.DEMO) {
            "DEMO/OFFLINE must not persist authoritative companion credentials."
        }
        val encrypted = encrypt(env, token)
        prefs.edit().putString(tokenKey(env), encrypted).apply()
    }

    fun clearCompanionToken(env: ServerEnvironment) {
        prefs.edit().remove(tokenKey(env)).apply()
    }

    // Compatibility wrappers for earlier repository/tests.
    fun getAuthToken(env: ServerEnvironment): String? = getCompanionToken(env)

    fun setAuthToken(env: ServerEnvironment, token: String?) = setCompanionToken(env, token)

    fun clearAuthToken(env: ServerEnvironment) = clearCompanionToken(env)

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun encrypt(env: ServerEnvironment, token: String): String {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        cipher.updateAAD(aad(env))
        val ciphertext = cipher.doFinal(token.toByteArray(StandardCharsets.UTF_8))
        val payload = ByteArray(cipher.iv.size + ciphertext.size)
        cipher.iv.copyInto(payload, destinationOffset = 0)
        ciphertext.copyInto(payload, destinationOffset = cipher.iv.size)
        return ENCRYPTED_PREFIX + Base64.encodeToString(payload, Base64.NO_WRAP)
    }

    private fun decrypt(env: ServerEnvironment, stored: String): String {
        val payload = Base64.decode(stored.removePrefix(ENCRYPTED_PREFIX), Base64.NO_WRAP)
        require(payload.size > GCM_IV_BYTES) { "Stored companion credential is malformed." }
        val iv = payload.copyOfRange(0, GCM_IV_BYTES)
        val ciphertext = payload.copyOfRange(GCM_IV_BYTES, payload.size)

        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
        cipher.updateAAD(aad(env))
        return String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8)
    }

    private fun aad(env: ServerEnvironment): ByteArray =
        "HELION-COMMANDER:${env.name}:COMPANION-TOKEN:v1".toByteArray(StandardCharsets.UTF_8)

    private fun loadOrCreateSecretKey(): SecretKey {
        // Robolectric does not expose the AndroidKeyStore provider. Keep tests on real AES-GCM
        // without introducing a production fallback or storing a key on disk.
        if (Build.FINGERPRINT == "robolectric") {
            val bytes = "helion-robolectric-credential-key".toByteArray(StandardCharsets.UTF_8)
            val key = ByteArray(32)
            bytes.copyInto(key, endIndex = minOf(bytes.size, key.size))
            return SecretKeySpec(key, "AES")
        }

        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val PREFS_NAME = "helion_auth_credentials"
        private const val KEY_ALIAS = "helion_commander_companion_credentials_v1"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTED_PREFIX = "v1:"
        private const val GCM_IV_BYTES = 12
        private const val GCM_TAG_BITS = 128
    }
}
