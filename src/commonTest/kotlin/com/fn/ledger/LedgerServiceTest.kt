package com.fn.ledger

import com.fn.ledger.domain.Account
import com.fn.ledger.domain.AccountType
import com.fn.ledger.domain.Category
import com.fn.ledger.domain.CategoryType
import com.fn.ledger.domain.Ledger
import com.fn.ledger.domain.LedgerService
import com.fn.ledger.domain.Money
import com.fn.ledger.domain.Transaction
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LedgerServiceTest {
    private val service = LedgerService()

    @Test
    fun `records transactions and computes balances`() {
        val ledger = Ledger.EMPTY
        val cashAccount = Account("cash", "Cash", AccountType.ASSET, "USD")
        val groceryCategory = Category("groceries", "Groceries", CategoryType.EXPENSE)
        val afterAccount = service.addAccount(ledger, cashAccount)
        val afterCategory = service.addCategory(afterAccount, groceryCategory)
        val transaction = Transaction(
            id = "t1",
            accountId = "cash",
            categoryId = "groceries",
            amount = Money.parse("USD", "-54.20"),
            date = LocalDate(2024, 10, 1)
        )
        val updated = service.recordTransaction(afterCategory, transaction)
        val balance = updated.accountBalance("cash")
        assertEquals(-5420L, balance.minorUnits)
    }

    @Test
    fun `aggregates balances by account type`() {
        val ledger = Ledger(
            accounts = mapOf(
                "cash" to Account("cash", "Cash", AccountType.ASSET, "USD"),
                "loan" to Account("loan", "Loan", AccountType.LIABILITY, "USD")
            ),
            transactions = listOf(
                Transaction("t1", "cash", null, Money.parse("USD", "100.00"), LocalDate(2024, 1, 1)),
                Transaction("t2", "loan", null, Money.parse("USD", "-40.00"), LocalDate(2024, 1, 2))
            )
        )
        val totals = service.ledgerTotalsByAccountType(ledger)
        assertEquals(1, totals[AccountType.ASSET]?.size)
        assertEquals(10000L, totals[AccountType.ASSET]?.get("USD")?.minorUnits)
        assertEquals(-4000L, totals[AccountType.LIABILITY]?.get("USD")?.minorUnits)
    }
}
