package com.izivia.ocpp.json

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import java.io.InputStream
import java.util.Locale

/**
 * Loads and caches the JSON schemas of a single OCPP version.
 *
 * Schemas are resolved as classpath resources by action name under [schemaFolder]. Every version
 * module ships schemas under the same action names, and the classpath returns the first match when
 * several versions are present, so each module must keep its schemas in a folder of its own.
 */
class OcppJsonValidator(
    private val specVersion: SpecVersion.VersionFlag,
    private val schemaFolder: String
) {
    init {
        require(schemaFolder.isNotBlank()) { "schemaFolder must name the resource folder holding the schemas" }
    }

    private val jsonSchemas = mutableMapOf<String, JsonSchema>()

    // Since json-schema-validator 1.5.x validation messages are localized using the
    // default JVM Locale. Force English so the OCPP error details stay deterministic
    // regardless of the server's locale.
    private val config: SchemaValidatorsConfig =
        SchemaValidatorsConfig.builder().locale(Locale.ENGLISH).build()

    private fun getJsonSchema(action: String): JsonSchema {
        val file = "$schemaFolder/$action.json"
        val factory: JsonSchemaFactory = JsonSchemaFactory.getInstance(specVersion)
        val input: InputStream = checkNotNull(Thread.currentThread().contextClassLoader.getResourceAsStream(file)) {
            "Schema $file not found on the classpath"
        }
        return factory.getSchema(input, config)
    }

    /**
     * Serialize the object with jackson and verify that the format is conformed to the
     * json schema
     */
    fun isValidObject(action: String, payload: JsonNode): List<ValidationMessage> =
        // Info :  loading JsonSchema is not thread safe. Can affect performance during the first instanciation
        (jsonSchemas[action] ?: getJsonSchema(action).also { jsonSchemas[action] = it })
            .validate(payload)
            .toList()
}
