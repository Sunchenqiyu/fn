package com.fn.ledger.storage

import com.fn.ledger.domain.Account
import com.fn.ledger.domain.AccountType
import com.fn.ledger.domain.Ledger
import com.fn.ledger.domain.Transaction
import com.fn.ledger.domain.Money
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div

class FileLedgerStorageTest {
    @Test
    fun `persists ledger to disk`() {
        val directory = createTempDirectory()
        val file = directory / "ledger.json"
        val storage = FileLedgerStorage(file)
        val ledger = Ledger(
            accounts = mapOf("cash" to Account("cash", "Cash", AccountType.ASSET, "USD")),
            transactions = listOf(
                Transaction("t1", "cash", null, Money.parse("USD", "12.34"), LocalDate(2024, 1, 1))
            )
        )
        storage.save(ledger)
        val loaded = storage.load()
        assertNotNull(loaded)
        assertEquals(1, loaded.transactions.size)
        assertEquals(ledger.transactions.first().amount.minorUnits, loaded.transactions.first().amount.minorUnits)
    }
}
