package io.github.dogacel

import java.security.MessageDigest

// Based on https://www.rfc-editor.org/rfc/rfc6238
class TOTPGenerator(
    val key: ByteArray,
    val digits: Int = 6,
    val timestep: Int = 30,
    val t0: Long = 0,
    val hashFunction: MessageDigest = MessageDigest.getInstance("SHA1"),
) {
    val HOTPGenerator = HOTPGenerator(key, digits, hashFunction)

    fun generate(): Long {
        val X = timestep

        val time = ((System.currentTimeMillis() / 1000) - t0) / X

        return HOTPGenerator.generate(time)
    }
}
