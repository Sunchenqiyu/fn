package com.fn.ledger.domain

import kotlinx.serialization.Serializable

@Serializable
data class Ledger(
    val accounts: Map<String, Account> = emptyMap(),
    val categories: Map<String, Category> = emptyMap(),
    val transactions: List<Transaction> = emptyList(),
    val budgets: Map<String, Budget> = emptyMap()
) {
    fun accountBalance(accountId: String): Money {
        val account = requireNotNull(accounts[accountId]) { "Unknown account: $accountId" }
        val total = transactions
            .asSequence()
            .filter { it.accountId == accountId }
            .fold(0L) { acc, transaction -> acc + transaction.amount.minorUnits }
        return Money(account.currency, total)
    }

    fun transactionsForAccount(accountId: String): List<Transaction> =
        transactions.filter { it.accountId == accountId }

    companion object {
        val EMPTY = Ledger()
    }
}
