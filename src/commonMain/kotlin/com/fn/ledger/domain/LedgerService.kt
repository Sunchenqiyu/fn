package com.fn.ledger.domain

import kotlinx.serialization.Serializable

class LedgerService {
    fun addAccount(ledger: Ledger, account: Account): Ledger {
        require(account.id !in ledger.accounts) { "Account with id ${account.id} already exists" }
        return ledger.copy(accounts = ledger.accounts + (account.id to account))
    }

    fun addCategory(ledger: Ledger, category: Category): Ledger {
        require(category.id !in ledger.categories) { "Category with id ${category.id} already exists" }
        return ledger.copy(categories = ledger.categories + (category.id to category))
    }

    fun addBudget(ledger: Ledger, budget: Budget): Ledger {
        require(budget.id !in ledger.budgets) { "Budget with id ${budget.id} already exists" }
        requireNotNull(ledger.categories[budget.categoryId]) { "Unknown category ${budget.categoryId}" }
        return ledger.copy(budgets = ledger.budgets + (budget.id to budget))
    }

    fun recordTransaction(ledger: Ledger, transaction: Transaction): Ledger {
        val account = requireNotNull(ledger.accounts[transaction.accountId]) { "Unknown account ${transaction.accountId}" }
        if (transaction.categoryId != null) {
            requireNotNull(ledger.categories[transaction.categoryId]) { "Unknown category ${transaction.categoryId}" }
        }
        require(account.currency == transaction.amount.currency) {
            "Currency mismatch between account ${account.currency} and transaction ${transaction.amount.currency}"
        }
        require(transaction.id !in ledger.transactions.map { it.id }) { "Transaction with id ${transaction.id} already exists" }
        return ledger.copy(transactions = ledger.transactions + transaction)
    }

    fun ledgerTotalsByAccountType(ledger: Ledger): Map<AccountType, Map<String, Money>> {
        val grouped = mutableMapOf<AccountType, MutableMap<String, Money>>()
        ledger.accounts.values.forEach { account ->
            val total = ledger.accountBalance(account.id)
            val perCurrency = grouped.getOrPut(account.type) { mutableMapOf() }
            perCurrency[account.currency] = perCurrency[account.currency]?.plus(total) ?: total
        }
        return grouped
    }

    fun transactionsForMonth(
        ledger: Ledger,
        year: Int,
        month: Int,
        accountId: String? = null,
        categoryId: String? = null
    ): List<Transaction> = ledger.transactions.filter { transaction ->
        val date = transaction.date
        val matchesMonth = date.year == year && date.monthNumber == month
        val matchesAccount = accountId?.let { transaction.accountId == it } ?: true
        val matchesCategory = categoryId?.let { transaction.categoryId == it } ?: true
        matchesMonth && matchesAccount && matchesCategory
    }

    fun spendingByCategory(
        ledger: Ledger,
        year: Int,
        month: Int
    ): Map<String, Map<String, Money>> {
        val result = mutableMapOf<String, MutableMap<String, Money>>()
        transactionsForMonth(ledger, year, month).forEach { transaction ->
            val categoryId = transaction.categoryId ?: return@forEach
            val perCurrency = result.getOrPut(categoryId) { mutableMapOf() }
            val amount = perCurrency[transaction.amount.currency]
            val value = Money(transaction.amount.currency, transaction.amount.minorUnits)
            perCurrency[transaction.amount.currency] = amount?.plus(value) ?: value
        }
        return result
    }

    fun monthlyBudgetStatuses(ledger: Ledger, year: Int, month: Int): List<BudgetStatus> {
        return ledger.budgets.values.map { budget ->
            val transactions = transactionsForMonth(ledger, year, month, categoryId = budget.categoryId)
            val spentMinorUnits = transactions
                .filter { it.amount.currency == budget.currency }
                .sumOf { it.amount.minorUnits.absoluteValue }
            val spent = Money(budget.currency, spentMinorUnits)
            val remaining = budget.monthlyLimit - spent
            BudgetStatus(budget, spent, remaining)
        }
    }
}

private val Long.absoluteValue: Long
    get() = if (this < 0) -this else this

@Serializable
data class BudgetStatus(
    val budget: Budget,
    val spent: Money,
    val remaining: Money
)
