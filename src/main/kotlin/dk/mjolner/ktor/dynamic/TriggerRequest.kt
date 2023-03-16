package dk.mjolner.ktor.dynamic

import kotlinx.serialization.Serializable

@Serializable
data class TriggerRequest (
    var value: String,
    var isValueEmpty: Boolean,
    var trigger: String,
)