package com.fn.ledger.domain

import kotlin.math.absoluteValue
import kotlinx.serialization.Serializable

/**
 * Represents an amount of money stored in the smallest currency unit (for example cents).
 */
@Serializable
data class Money(val currency: String, val minorUnits: Long) : Comparable<Money> {
    init {
        require(currency.isNotBlank()) { "Currency must not be blank" }
    }

    operator fun plus(other: Money): Money {
        requireCompatible(other)
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    operator fun minus(other: Money): Money {
        requireCompatible(other)
        return copy(minorUnits = minorUnits - other.minorUnits)
    }

    operator fun unaryMinus(): Money = copy(minorUnits = -minorUnits)

    fun absolute(): Money = copy(minorUnits = minorUnits.absoluteValue)

    fun format(): String {
        val sign = if (minorUnits < 0) "-" else ""
        val abs = minorUnits.absoluteValue
        val units = abs / SCALE
        val decimals = (abs % SCALE).toString().padStart(2, '0')
        return "$sign$currency $units.$decimals"
    }

    override fun compareTo(other: Money): Int {
        requireCompatible(other)
        return minorUnits.compareTo(other.minorUnits)
    }

    private fun requireCompatible(other: Money) {
        require(currency == other.currency) {
            "Currency mismatch: $currency vs ${other.currency}"
        }
    }

    companion object {
        private const val SCALE = 100L

        fun zero(currency: String): Money = Money(currency, 0)

        /**
         * Parses a human readable representation such as "123.45" into a [Money] value.
         */
        fun parse(currency: String, amount: String): Money {
            val trimmed = amount.trim()
            require(trimmed.isNotEmpty()) { "Amount cannot be blank" }
            val negative = trimmed.startsWith("-")
            val digits = if (negative) trimmed.substring(1) else trimmed
            val parts = digits.split('.')
            require(parts.size <= 2) { "Invalid amount format: $amount" }
            val wholePart = parts.getOrNull(0)?.ifEmpty { "0" } ?: "0"
            val fractionalPart = parts.getOrNull(1)?.padEnd(2, '0')?.take(2) ?: "00"
            require(wholePart.all { it.isDigit() }) { "Invalid digits in amount: $amount" }
            require(fractionalPart.all { it.isDigit() }) { "Invalid digits in amount: $amount" }
            val wholeUnits = wholePart.toLong()
            val fractionUnits = fractionalPart.toLong()
            val combined = wholeUnits * SCALE + fractionUnits
            val signed = if (negative) -combined else combined
            return Money(currency, signed)
        }
    }
}
