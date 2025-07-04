package org.fossify.commons.models.contacts

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    var value: String,
    var type: Int,
    var label: String,
    var country: String = "",
    var region: String = "",
    var city: String = "",
    var postcode: String = "",
    var pobox: String = "",
    var street: String = "",
    var neighborhood: String = "",
)
