package com.fn.ledger.cli

import com.fn.ledger.domain.Account
import com.fn.ledger.domain.AccountType
import com.fn.ledger.storage.FileLedgerStorage
import com.fn.ledger.domain.Budget
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import com.fn.ledger.domain.LedgerManager
import com.fn.ledger.domain.Money
import com.fn.ledger.domain.Category
import com.fn.ledger.domain.CategoryType
import com.fn.ledger.domain.LedgerService
import com.fn.ledger.domain.Transaction
import com.fn.ledger.report.BalanceReportGenerator
import com.fn.ledger.storage.LedgerStorage
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID
import kotlinx.datetime.LocalDate

fun main(args: Array<String>) {
    LedgerCli().run(args)
}

class LedgerCli(
    private val storageFactory: (Path) -> LedgerStorage = { path -> FileLedgerStorage(path) },
    private val reportGenerator: BalanceReportGenerator = BalanceReportGenerator()
) {
    fun run(args: Array<String>) {
        if (args.isEmpty()) {
            printHelp()
            return
        }
        val parsed = ParsedArguments.parse(args.toList())
        val dataFile = parsed.flags["data"] ?: "ledger.json"
        val storage = storageFactory(Paths.get(dataFile))
        val manager = LedgerManager(storage, LedgerService())
        when (parsed.command) {
            "help" -> printHelp()
            "add-account" -> addAccount(manager, parsed)
            "add-category" -> addCategory(manager, parsed)
            "add-transaction" -> addTransaction(manager, parsed)
            "list-accounts" -> listAccounts(manager)
            "list-transactions" -> listTransactions(manager, parsed)
            "report" -> report(manager, parsed)
            "set-budget" -> setBudget(manager, parsed)
            else -> {
                println("Unknown command: ${parsed.command}")
                printHelp()
            }
        }
    }

    private fun addAccount(manager: LedgerManager, parsed: ParsedArguments) {
        val id = parsed.flags["id"] ?: UUID.randomUUID().toString()
        val name = parsed.flags.require("name")
        val type = parsed.flags.require("type").let { AccountType.valueOf(it.uppercase()) }
        val currency = parsed.flags.require("currency").uppercase()
        manager.update { ledger ->
            manager.service().addAccount(ledger, Account(id, name, type, currency))
        }
        println("Account $name ($id) created")
    }

    private fun addCategory(manager: LedgerManager, parsed: ParsedArguments) {
        val id = parsed.flags["id"] ?: UUID.randomUUID().toString()
        val name = parsed.flags.require("name")
        val type = parsed.flags.require("type").let { CategoryType.valueOf(it.uppercase()) }
        manager.update { ledger ->
            manager.service().addCategory(ledger, Category(id, name, type))
        }
        println("Category $name ($id) created")
    }

    private fun setBudget(manager: LedgerManager, parsed: ParsedArguments) {
        val id = parsed.flags["id"] ?: UUID.randomUUID().toString()
        val categoryId = parsed.flags.require("category")
        val currency = parsed.flags.require("currency").uppercase()
        val limit = parsed.flags.require("limit")
        val amount = Money.parse(currency, limit)
        manager.update { ledger ->
            manager.service().addBudget(ledger, Budget(id, categoryId, currency, amount))
        }
        println("Budget $id saved for category $categoryId")
    }

    private fun addTransaction(manager: LedgerManager, parsed: ParsedArguments) {
        val accountId = parsed.flags.require("account")
        val amount = parsed.flags.require("amount")
        val categoryId = parsed.flags["category"]
        val date = parsed.flags["date"]?.let { LocalDate.parse(it) } ?: today()
        val notes = parsed.flags["notes"]
        manager.update { ledger ->
            val account = ledger.accounts[accountId]
                ?: error("Account $accountId does not exist")
            val money = Money.parse(account.currency, amount)
            val transaction = Transaction(
                id = parsed.flags["id"] ?: UUID.randomUUID().toString(),
                accountId = accountId,
                categoryId = categoryId,
                amount = money,
                date = date,
                notes = notes
            )
            manager.service().recordTransaction(ledger, transaction)
        }
        println("Transaction recorded for $accountId")
    }

    private fun listAccounts(manager: LedgerManager) {
        val ledger = manager.current()
        if (ledger.accounts.isEmpty()) {
            println("No accounts registered")
            return
        }
        println("Accounts:")
        ledger.accounts.values.sortedBy { it.name }.forEach { account ->
            val balance = ledger.accountBalance(account.id).format()
            println("- ${account.name} [${account.type}] ${account.currency} (balance $balance)")
        }
    }

    private fun listTransactions(manager: LedgerManager, parsed: ParsedArguments) {
        val ledger = manager.current()
        val accountId = parsed.flags["account"]
        val year = parsed.flags["year"]?.toInt()
        val month = parsed.flags["month"]?.toInt()
        val transactions = when {
            year != null && month != null -> manager.service().transactionsForMonth(ledger, year, month, accountId)
            accountId != null -> ledger.transactionsForAccount(accountId)
            else -> ledger.transactions
        }
        if (transactions.isEmpty()) {
            println("No transactions found")
            return
        }
        println("Transactions:")
        transactions.sortedBy { it.date }.forEach { transaction ->
            val account = ledger.accounts[transaction.accountId]
            val category = transaction.categoryId?.let { ledger.categories[it]?.name }
            val noteSuffix = transaction.notes?.let { " - $it" } ?: ""
            println(
                "- ${transaction.date} ${transaction.amount.format()} " +
                    "${account?.name ?: transaction.accountId}" +
                    (category?.let { " [$it]" } ?: "") + noteSuffix
            )
        }
    }

    private fun report(manager: LedgerManager, parsed: ParsedArguments) {
        val ledger = manager.current()
        val year = parsed.flags["year"]?.toInt()
        val month = parsed.flags["month"]?.toInt()
        if (year != null && month != null) {
            val report = reportGenerator.monthly(ledger, year, month)
            println("Account balances:")
            report.totals.forEach { item ->
                println("- ${item.accountType} ${item.currency}: ${item.amount.format()}")
            }
            if (report.spendingByCategory.isNotEmpty()) {
                println("Spending by category:")
                report.spendingByCategory.forEach { spending ->
                    val categoryName = ledger.categories[spending.categoryId]?.name ?: spending.categoryId
                    println("- $categoryName (${spending.currency}): ${spending.amount.format()}")
                }
            }
            if (report.budgetStatuses.isNotEmpty()) {
                println("Budget status:")
                report.budgetStatuses.forEach { status ->
                    val categoryName = ledger.categories[status.budget.categoryId]?.name ?: status.budget.categoryId
                    println(
                        "- ${status.budget.id} for $categoryName: spent ${status.spent.format()} of ${status.budget.monthlyLimit.format()} (remaining ${status.remaining.format()})"
                    )
                }
            }
        } else {
            val report = reportGenerator.overall(ledger)
            println("Account balances:")
            report.totals.forEach { item ->
                println("- ${item.accountType} ${item.currency}: ${item.amount.format()}")
            }
        }
    }

    private fun printHelp() {
        println(
            """
            Kotlin Ledger CLI

            Usage: ledger <command> [options]

            Commands:
              help                        Show this message
              add-account                 Create a new account (requires --name, --type, --currency)
              add-category                Create a new category (requires --name, --type)
              add-transaction             Record a transaction (requires --account, --amount)
              list-accounts               List accounts and balances
              list-transactions           List transactions (optional --account, --year, --month)
              report                      Print a balance report (optional --year and --month)
              set-budget                  Set a monthly budget for a category (--category, --limit, --currency)

            Global options:
              --data <file>               Path to the ledger JSON file (default ledger.json)
            """.trimIndent()
        )
    }
}

private data class ParsedArguments(
    val command: String,
    val flags: Map<String, String>,
    val params: List<String>
) {
    companion object {
        fun parse(args: List<String>): ParsedArguments {
            require(args.isNotEmpty()) { "No command provided" }
            val command = args.first()
            val flags = mutableMapOf<String, String>()
            val params = mutableListOf<String>()
            var index = 1
            while (index < args.size) {
                val token = args[index]
                if (token.startsWith("--")) {
                    val key = token.removePrefix("--")
                    val value = args.getOrNull(index + 1)
                    if (value == null || value.startsWith("--")) {
                        throw IllegalArgumentException("Option --$key requires a value")
                    }
                    flags[key] = value
                    index += 2
                } else {
                    params += token
                    index += 1
                }
            }
            return ParsedArguments(command, flags, params)
        }
    }
}

private fun today(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun Map<String, String>.require(name: String): String =
    this[name] ?: error("Missing required option --$name")
