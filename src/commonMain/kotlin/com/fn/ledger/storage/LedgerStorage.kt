package com.fn.ledger.storage

import com.fn.ledger.domain.Ledger

interface LedgerStorage {
    fun load(): Ledger?
    fun save(ledger: Ledger)
}
