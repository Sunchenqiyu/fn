package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val name: String,
    val type: AccountType,
    val currency: String
) {
    init {
        require(id.isNotBlank()) { "Account id cannot be blank" }
        require(name.isNotBlank()) { "Account name cannot be blank" }
    }
}
