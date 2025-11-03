package com.fn.ledger.domain

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val accountId: String,
    val categoryId: String?,
    val amount: Money,
    val date: LocalDate,
    val notes: String? = null
) {
    init {
        require(id.isNotBlank()) { "Transaction id cannot be blank" }
        require(accountId.isNotBlank()) { "Transaction must reference an account" }
    }
}
