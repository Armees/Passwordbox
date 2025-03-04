package com.example.passwordbox

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeyManager(private val keyAlias: String) {

    private val ANDROID_KEY_STORE = "AndroidKeyStore"
    private val AES_MODE = "AES/GCM/NoPadding"
    private val IV_SIZE = 12 // Рекомендуемый размер IV для GCM — 96 бит (12 байт)
    private val TAG = "KeyManager"

    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return (keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: generateSecretKey()
    }

    fun encrypt(data: String): String {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv // Получаем сгенерированный IV
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        // Сохраняем IV в начале зашифрованного сообщения
        val ivAndEncrypted = iv + encryptedBytes
        val encodedString = Base64.encodeToString(ivAndEncrypted, Base64.NO_WRAP)
        // Выводим зашифрованную строку в лог удалить нужен для проверки шифрования
        Log.d(TAG, "Encrypted data: $encodedString")
        return encodedString
    }

    fun decrypt(encrypted: String): String {
        val ivAndEncrypted = Base64.decode(encrypted, Base64.NO_WRAP)
        // Извлекаем IV из начала зашифрованного сообщения
        val iv = ivAndEncrypted.copyOfRange(0, IV_SIZE)
        val encryptedBytes = ivAndEncrypted.copyOfRange(IV_SIZE, ivAndEncrypted.size)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        val decodedBytes = cipher.doFinal(encryptedBytes)
        return String(decodedBytes, Charsets.UTF_8)
    }
}