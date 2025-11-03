package com.fn.ledger

import com.fn.ledger.domain.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyTest {
    @Test
    fun `parse handles decimals`() {
        val money = Money.parse("USD", "123.45")
        assertEquals("USD", money.currency)
        assertEquals(12345L, money.minorUnits)
        assertEquals("USD 123.45", money.format())
    }

    @Test
    fun `parse rejects invalid input`() {
        assertFailsWith<IllegalArgumentException> {
            Money.parse("USD", "abc")
        }
    }
}
