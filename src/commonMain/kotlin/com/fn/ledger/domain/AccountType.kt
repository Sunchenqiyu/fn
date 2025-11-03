package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
enum class AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    INCOME,
    EXPENSE
}
