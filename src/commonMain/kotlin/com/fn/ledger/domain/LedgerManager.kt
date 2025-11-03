package com.fn.ledger.domain

import com.fn.ledger.storage.LedgerStorage

class LedgerManager(
    private val storage: LedgerStorage,
    private val service: LedgerService = LedgerService()
) {
    fun current(): Ledger = storage.load() ?: Ledger.EMPTY

    fun update(transform: (Ledger) -> Ledger): Ledger {
        val updated = transform(current())
        storage.save(updated)
        return updated
    }

    fun service(): LedgerService = service
}
