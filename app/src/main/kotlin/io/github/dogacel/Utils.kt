package io.github.dogacel

import java.nio.ByteBuffer
import kotlin.experimental.xor

object Utils {
    infix fun ByteArray.xor(other: ByteArray): ByteArray {
        val result = ByteArray(size.coerceAtLeast(other.size))

        for (i in result.indices) {
            if (this.size > i && other.size > i) {
                result[i] = this[i] xor other[i]
            }

            if (this.size > i && other.size <= i) {
                result[i] = this[i]
            }

            if (this.size <= i && other.size > i) {
                result[i] = other[i]
            }
        }

        return result
    }

    fun Long.toByteArray(): ByteArray =
        ByteArray(8) { index ->
            (this shr (8 * (7 - index))).toByte()
        }

    fun ByteArray.toInt(): Int = ByteBuffer.wrap(this).int

    infix fun Long.pow(exp: Int): Long {
        var result = 1L
        repeat(exp) {
            result *= this
        }
        return result
    }
}
