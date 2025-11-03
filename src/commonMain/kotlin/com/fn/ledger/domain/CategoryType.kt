package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
enum class CategoryType {
    INCOME,
    EXPENSE,
    TRANSFER
}
