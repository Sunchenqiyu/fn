package com.fn.ledger.storage

import com.fn.ledger.domain.Ledger

class InMemoryLedgerStorage(initial: Ledger? = null) : LedgerStorage {
    private var snapshot: Ledger? = initial

    override fun load(): Ledger? = snapshot

    override fun save(ledger: Ledger) {
        snapshot = ledger
    }
}
