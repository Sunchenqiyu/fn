package com.fn.ledger.storage

import com.fn.ledger.domain.Ledger
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class FileLedgerStorage(private val path: Path) : LedgerStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override fun load(): Ledger? {
        if (!Files.exists(path)) {
            return null
        }
        val text = Files.readString(path)
        if (text.isBlank()) {
            return null
        }
        return json.decodeFromString<Ledger>(text)
    }

    override fun save(ledger: Ledger) {
        val parent = path.toAbsolutePath().parent
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent)
        }
        Files.writeString(path, json.encodeToString(ledger))
    }
}
