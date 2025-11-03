package com.fn.ledger.report

import com.fn.ledger.domain.AccountType
import com.fn.ledger.domain.BudgetStatus
import com.fn.ledger.domain.Ledger
import com.fn.ledger.domain.LedgerService
import com.fn.ledger.domain.Money
import kotlinx.serialization.Serializable

class BalanceReportGenerator(private val service: LedgerService = LedgerService()) {
    fun overall(ledger: Ledger): BalanceReport {
        val totals = service.ledgerTotalsByAccountType(ledger)
        val items = totals.flatMap { (type, perCurrency) ->
            perCurrency.map { (currency, amount) ->
                AccountTypeBalance(type, currency, amount)
            }
        }
        return BalanceReport(items.sortedWith(compareBy({ it.accountType.name }, { it.currency })))
    }

    fun monthly(
        ledger: Ledger,
        year: Int,
        month: Int
    ): MonthlyBalanceReport {
        val overall = overall(ledger)
        val spending = service.spendingByCategory(ledger, year, month).flatMap { (categoryId, perCurrency) ->
            perCurrency.map { (currency, amount) ->
                CategorySpending(categoryId, currency, amount)
            }
        }
        val budgets = service.monthlyBudgetStatuses(ledger, year, month)
        return MonthlyBalanceReport(overall.totals, spending, budgets)
    }
}

@Serializable
data class BalanceReport(val totals: List<AccountTypeBalance>)

@Serializable
data class MonthlyBalanceReport(
    val totals: List<AccountTypeBalance>,
    val spendingByCategory: List<CategorySpending>,
    val budgetStatuses: List<BudgetStatus>
)

@Serializable
data class AccountTypeBalance(
    val accountType: AccountType,
    val currency: String,
    val amount: Money
)

@Serializable
data class CategorySpending(
    val categoryId: String,
    val currency: String,
    val amount: Money
)
