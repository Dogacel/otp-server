package io.github.dogacel

import com.fasterxml.jackson.databind.JsonNode
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.common.SessionProtocol.HTTP
import com.linecorp.armeria.server.HttpStatusException
import com.linecorp.armeria.server.Server
import com.linecorp.armeria.server.annotation.ConsumesJson
import com.linecorp.armeria.server.annotation.Post
import com.linecorp.armeria.server.annotation.ProducesJson
import com.linecorp.armeria.server.annotation.decorator.CorsDecorator
import com.linecorp.armeria.server.file.FileService
import org.apache.commons.codec.binary.Base32
import java.security.MessageDigest
import java.util.Base64

fun main() {
    runServer()
}

fun runServer() {
    val server =
        Server
            .builder()
            .port(8080, HTTP)
            .tlsSelfSigned()
            .serviceUnder("/", FileService.of(ClassLoader.getSystemClassLoader(), "/web"))
            .annotatedService("/api/v1/generate-otp", OTPService())
            .build()

    server.start().join()
}

@ProducesJson
@ConsumesJson
class OTPService {
    @Post
    @CorsDecorator(
        origins = ["*"],
        allowedRequestMethods = [HttpMethod.POST, HttpMethod.OPTIONS],
        allowedRequestHeaders = ["Content-Type"],
    )
    fun generateOTP(body: JsonNode): HttpResponse {
        val secretKey = body.get("secretKey").asText()
        val format = body.get("format").asText()

        val secretKeyByteArray: ByteArray =
            when (format) {
                "UTF-8" -> secretKey.toByteArray()
                "Hex" -> secretKey.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                "Base32" -> Base32().decode(secretKey)
                "Base64" -> Base64.getDecoder().decode(secretKey)
                else -> throw HttpStatusException.of(
                    HttpStatus.BAD_REQUEST,
                    IllegalArgumentException("Invalid secret key format: $format"),
                )
            }

        val algorithm = body.get("algorithm").asText()
        val messageDigest = MessageDigest.getInstance(algorithm)

        val digits = body.get("digits").asInt()
        val otpType = body.get("otpType").asText()

        when (otpType) {
            "HOTP" -> {
                val counter = body.get("counter").asLong()
                val otp = HOTPGenerator(secretKeyByteArray, digits, messageDigest).generate(counter)
                return HttpResponse.of(otp.toString())
            }

            "TOTP" -> {
                val timeStep = body.get("timeStep").asInt()
                if (timeStep <= 0) {
                    throw HttpStatusException.of(
                        HttpStatus.BAD_REQUEST,
                        IllegalArgumentException("Invalid timeStep: $timeStep"),
                    )
                }
                val otp = TOTPGenerator(secretKeyByteArray, digits, timeStep, 0, messageDigest).generate()
                return HttpResponse.of(otp.toString())
            }

            else -> throw HttpStatusException.of(
                HttpStatus.BAD_REQUEST,
                IllegalArgumentException("Invalid OTP type: $otpType"),
            )
        }
    }
}
