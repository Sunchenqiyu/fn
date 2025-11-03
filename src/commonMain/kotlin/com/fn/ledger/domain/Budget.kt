package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
data class Budget(
    val id: String,
    val categoryId: String,
    val currency: String,
    val monthlyLimit: Money
) {
    init {
        require(id.isNotBlank()) { "Budget id cannot be blank" }
        require(categoryId.isNotBlank()) { "Budget must target a category" }
        require(currency == monthlyLimit.currency) { "Budget currency must match limit currency" }
    }
}
