package io.github.dogacel

import io.github.dogacel.Utils.pow
import io.github.dogacel.Utils.toByteArray
import io.github.dogacel.Utils.toInt
import io.github.dogacel.Utils.xor
import java.security.MessageDigest
import kotlin.experimental.and

// Based on https://www.rfc-editor.org/rfc/rfc4226
class HOTPGenerator(
    val key: ByteArray,
    val digits: Int = 6,
    val hashFunction: MessageDigest = MessageDigest.getInstance("SHA1"),
) {
    fun generate(counter: Long): Long {
        val HS = hmac(key, counter.toByteArray())

        val Sbits = DT(HS)
        val Snum = Sbits.toInt()

        return (Snum % (10L pow digits))
    }

    // Based on https://www.rfc-editor.org/rfc/rfc2104
    private fun hmac(
        K: ByteArray,
        text: ByteArray,
    ): ByteArray {
        val H: (ByteArray) -> ByteArray = hashFunction::digest
        val B = 64

        val ipad = ByteArray(B) { 0x36 }
        val opad = ByteArray(B) { 0x5C }

        // append 0x00 to the end of the array to reach size B or truncate the array by applying the hash function.
        val Kpad = if (K.size <= B) K.copyOf(B) else H(K)

        return H(Kpad xor opad + H(Kpad xor ipad + text))
    }

    private fun DT(x: ByteArray): ByteArray {
        val OffsetBits = x.last() and 0xF

        val result =
            ByteArray(4) { index ->
                x[OffsetBits + index]
            }

        // Take last 4 bits from the result.
        result[0] = result[0] and 0x7F

        return result
    }
}
