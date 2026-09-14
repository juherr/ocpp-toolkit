package com.izivia.ocpp.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.networknt.schema.SpecVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isNotNull
import strikt.assertions.message

class OcppJsonValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `rejects a blank schema folder`(schemaFolder: String) {
        // A blank folder would resolve schemas from the classpath root, where the versions collide.
        expectThrows<IllegalArgumentException> { OcppJsonValidator(SpecVersion.VersionFlag.V4, schemaFolder) }
            .message.isNotNull().contains("schemaFolder")
    }

    @Test
    fun `accepts a schema folder`() {
        OcppJsonValidator(SpecVersion.VersionFlag.V4, "ocpp16")
    }

    @Test
    fun `names the missing schema when it is not on the classpath`() {
        val validator = OcppJsonValidator(SpecVersion.VersionFlag.V4, "nowhere")

        expectThrows<IllegalStateException> { validator.isValidObject("Authorize", JsonNodeFactory.instance.objectNode()) }
            .message.isNotNull().contains("nowhere/Authorize.json")
    }
}
