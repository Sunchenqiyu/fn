package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType
) {
    init {
        require(id.isNotBlank()) { "Category id cannot be blank" }
        require(name.isNotBlank()) { "Category name cannot be blank" }
    }
}
