package com.example.accountancyapp

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

enum class TipCategory(val title: String, val icon: ImageVector) {
    ALL("All Tips", Icons.Default.AllInclusive),
    TAILORED("For Your Report", Icons.Default.AutoFixHigh),
    TAX("HMRC & Corp Tax", Icons.Default.ReceiptLong),
    BOOKKEEPING("Bookkeeping", Icons.Default.MenuBook),
    EXCEL("Excel Hacks", Icons.Default.GridOn),
    VAT("VAT & Compliance", Icons.Default.FactCheck),
    PRO("UK Practice & Firm", Icons.Default.TrendingUp)
}

data class AccountancyTip(
    val id: Int,
    val title: String,
    val summary: String,
    val deepDive: String,
    val category: TipCategory,
    val impact: String,
    val triggerCondition: (BalanceSheetReport) -> Boolean = { false }
)

data class CheatSheetEntry(
    val id: String,
    val transaction: String,
    val debit: String,
    val credit: String,
    val explanation: String,
    val isSuggestedForReport: Boolean = false,
    val suggestedContext: String = ""
)

data class ActionTask(
    val id: String,
    val title: String,
    val urgency: String,
    val urgencyColor: Color,
    val rationale: String,
    val procedure: String,
    val statutoryReference: String,
    var isDone: Boolean = false
)

data class BalanceSheetReport(
    val cashAtBank: Double = 0.0,
    val tradeDebtors: Double = 0.0,
    val stockInventory: Double = 0.0,
    val currentAssets: Double = 0.0,
    val totalAssets: Double = 0.0,
    val creditorsDueWithinOneYear: Double = 0.0,
    val creditorsDueAfterOneYear: Double = 0.0,
    val totalCreditors: Double = 0.0,
    val shareholderFunds: Double = 0.0,
    val turnover: Double = 0.0,
    val netProfitBeforeTax: Double = 0.0,
    val rawFileName: String = "UK FRS Sample"
)

data class FinancialHealthMetric(
    val name: String,
    val valueStr: String,
    val status: String,
    val statusColor: Color,
    val explanation: String
)

data class AdvisoryRecommendation(
    val id: String,
    val title: String,
    val severity: String,
    val color: Color,
    val advice: String,
    val actionItem: String
)

object AccountancyRepository {
    val tips = listOf(
        AccountancyTip(
            id = 1,
            title = "Annual Investment Allowance (AIA) & Full Expensing",
            summary = "Write off 100% of qualifying plant & machinery up to £1,000,000 upfront.",
            deepDive = "Under UK tax law, the Annual Investment Allowance allows companies to deduct 100% of the cost of qualifying plant and machinery up to the £1m cap. Companies can also utilize permanent Full Expensing (100% first-year allowance for main rate expenditure) to slash taxable profits immediately.",
            category = TipCategory.TAX,
            impact = "Immediate 25% Corporation Tax Shield",
            triggerCondition = { report -> report.netProfitBeforeTax > 50000.0 }
        ),
        AccountancyTip(
            id = 2,
            title = "UK Corporation Tax Marginal Relief Taper (26.5% Effective Rate)",
            summary = "Profits between £50,000 and £250,000 face a 26.5% marginal tax rate on every extra pound.",
            deepDive = "Companies with taxable profits below £50,000 pay the 19% small profits rate, while profits over £250,000 pay the full 25% main rate. In the £50k-£250k bracket, marginal relief applies using the statutory fraction (3/200). Tax planning (pension contributions, capital allowances, bonuses) is doubly effective here because each deducted pound saves 26.5p in tax.",
            category = TipCategory.TAX,
            impact = "Saves 26.5p per £1 of Allowable Expense",
            triggerCondition = { report -> report.netProfitBeforeTax in 50000.0..250000.0 }
        ),
        AccountancyTip(
            id = 3,
            title = "VAT Bad Debt Relief (6-Month Rule)",
            summary = "Reclaim output VAT on unpaid customer invoices older than 6 months.",
            deepDive = "Under Section 36 of VATA 1994, if a UK trade debtor hasn't paid you after 6 months from the date of the invoice (and has been written off in your day-to-day accounts), you can reclaim the VAT already accounted for on Box 4 of your next Making Tax Digital (MTD) VAT return.",
            category = TipCategory.VAT,
            impact = "Recovers 20% Cash on Defaulted Invoices",
            triggerCondition = { report -> report.tradeDebtors > 30000.0 }
        ),
        AccountancyTip(
            id = 4,
            title = "Construction Industry Scheme (CIS) Domestic Reverse Charge",
            summary = "Prevent fatal cash-flow errors on standard VAT building services.",
            deepDive = "For standard and reduced-rate construction services between VAT-registered sub-contractors and contractors, the customer accounts for the VAT directly to HMRC rather than paying it to the supplier. Never invoice standard VAT to a VAT/CIS contractor unless they provide written confirmation of End-User status.",
            category = TipCategory.VAT,
            impact = "Eliminates Severe HMRC Penalty Assessments"
        ),
        AccountancyTip(
            id = 5,
            title = "Dynamic Excel SUMIFS with EOMONTH for UK Quarters",
            summary = "Automate UK VAT quarter-end reconciliation without brittle pivot tables.",
            deepDive = "Combine SUMIFS with =EOMONTH(Date, 0) to aggregate journal entries into rolling quarterly VAT windows (March/June/Sept/Dec or Jan/April/July/Oct) dynamically. Formula: =SUMIFS(GrossAmount, InvoiceDate, \">=\"&StartDate, InvoiceDate, \"<=\"&EndDate).",
            category = TipCategory.EXCEL,
            impact = "Saves 4+ Hours on Quarterly VAT Returns"
        ),
        AccountancyTip(
            id = 6,
            title = "Companies House Audit Exemption Thresholds",
            summary = "Verify your small company eligibility under Section 477 of the Companies Act 2006.",
            deepDive = "A private limited company in the UK is exempt from statutory audit if it satisfies at least 2 of 3 conditions: Annual Turnover ≤ £15m (for periods beginning on/after 6 April 2025; £10.2m previously), Balance Sheet Total ≤ £7.5m (formerly £5.1m), and average employees ≤ 50.",
            category = TipCategory.PRO,
            impact = "Saves £5,000 - £15,000 in External Audit Fees",
            triggerCondition = { report -> report.turnover in 1.0..15000000.0 && report.totalAssets in 1.0..7500000.0 }
        ),
        AccountancyTip(
            id = 7,
            title = "Segregated HMRC Tax Reservoir Account",
            summary = "Isolate PAYE/NIC and collected Output VAT weekly.",
            deepDive = "Never retain collected VAT or PAYE/National Insurance payroll deductions in your operational current account. Sweep 20% of net trade receipts and monthly payroll deductions to an interest-bearing business savings account every Friday so HMRC direct debits never risk default surcharges.",
            category = TipCategory.TAX,
            impact = "Zero HMRC Surcharge Notice Defaults",
            triggerCondition = { report -> report.creditorsDueWithinOneYear > report.cashAtBank }
        ),
        AccountancyTip(
            id = 8,
            title = "Stock Write-Down & Net Realisable Value (NRV)",
            summary = "Write down slow-moving stock to reduce Corporation Tax and clean balance sheet.",
            deepDive = "Under FRS 102 Section 13, inventories must be measured at the lower of cost and estimated selling price less costs to complete and sell (NRV). Writing down aged or obsolete stock provides an immediate deduction against trading profit, reducing Corporation Tax while improving working capital accuracy.",
            category = TipCategory.BOOKKEEPING,
            impact = "Lowers Taxable Trading Profit Immediately",
            triggerCondition = { report -> report.stockInventory > (report.currentAssets * 0.4) }
        )
    )

    fun getStandardCheatSheet(): List<CheatSheetEntry> = listOf(
        CheatSheetEntry(
            id = "cs_depreciation",
            transaction = "Depreciation on Plant & Machinery",
            debit = "Depreciation Charge (Profit & Loss)",
            credit = "Accumulated Depreciation (Balance Sheet)",
            explanation = "Reduces carrying value under FRS 102/105. Disallowed for Corp Tax (replaced by Capital Allowances)."
        ),
        CheatSheetEntry(
            id = "cs_vat_payment",
            transaction = "VAT Quarter Settlement (HMRC Payment)",
            debit = "HMRC VAT Control Account (Creditors)",
            credit = "Bank Current Account (Cash)",
            explanation = "Clears net liability of Box 5 (Output VAT minus Input VAT) paid via MTD direct debit."
        ),
        CheatSheetEntry(
            id = "cs_advance",
            transaction = "Customer Advance / Deposit with VAT",
            debit = "Bank Current Account (Cash)",
            credit = "Deferred Income (Creditors) & Output VAT",
            explanation = "Creates a tax point immediately for VAT even though turnover is not yet recognized in P&L."
        ),
        CheatSheetEntry(
            id = "cs_dividend",
            transaction = "Director's Loan Account (DLA) Dividend",
            debit = "Retained Earnings / Equity (Dividends Paid)",
            credit = "Director's Loan Account (Creditors)",
            explanation = "Credits the director after formal board approval, provided sufficient distributable reserves exist."
        ),
        CheatSheetEntry(
            id = "cs_paye",
            transaction = "PAYE & Employee NIC Monthly Sweep",
            debit = "Gross Wages & Employer's NIC Expense",
            credit = "Net Wages Payable & PAYE/NIC Liability",
            explanation = "Recognizes operational staffing cost and sets up liabilities payable to HMRC by the 22nd."
        )
    )
}

object UKReportAnalysisEngine {

    /**
     * Calculates UK Corporation Tax using standard HMRC rate bands:
     * - Profit <= £50k: 19% (Small profits rate)
     * - Profit >= £250k: 25% (Main rate)
     * - £50k - £250k: 25% minus Marginal Relief using fraction 3/200: (3/200) * (£250,000 - Profit)
     */
    fun calculateCorporationTax(profit: Double): Double {
        if (profit <= 0.0) return 0.0
        return when {
            profit <= 50000.0 -> profit * 0.19
            profit >= 250000.0 -> profit * 0.25
            else -> {
                val fullTax = profit * 0.25
                val marginalRelief = (3.0 / 200.0) * (250000.0 - profit)
                (fullTax - marginalRelief).coerceAtLeast(0.0)
            }
        }
    }

    /**
     * Helper to split CSV lines while respecting quotation marks (e.g. "26,000.00").
     */
    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '\"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                char == '\t' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.clear()
                }
                else -> sb.append(char)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    /**
     * Cleans string currency amounts: strips '£', '$', commas, whitespace, and handles brackets for negatives: (1,500) -> -1500
     */
    private fun parseAmount(raw: String): Double? {
        var str = raw.trim().replace("£", "").replace("$", "").replace("€", "").trim()
        if (str.isEmpty() || str == "-" || str == "--") return null
        
        var isNegative = false
        if (str.startsWith("(") && str.endsWith(")")) {
            isNegative = true
            str = str.substring(1, str.length - 1).trim()
        } else if (str.startsWith("-")) {
            isNegative = true
            str = str.substring(1).trim()
        }
        str = str.replace(",", "").trim()
        val num = str.toDoubleOrNull() ?: return null
        return if (isNegative) -abs(num) else num
    }

    /**
     * Upgraded parsing engine that handles:
     * 1. Multi-column Trial Balance exports (Code, Description, Debit, Credit) from Xero, Sage, QuickBooks
     * 2. Key: Value financial statements and summaries
     * 3. Prevents code numbers like 200, 260, 4000 from being mistaken for £200 or £26
     */
    fun parseReportText(rawText: String, sourceName: String = "Manual Import"): BalanceSheetReport {
        var cash = 0.0
        var debtors = 0.0
        var stock = 0.0
        var curAssets = 0.0
        var totalAssets = 0.0
        var curCreditors = 0.0
        var ltCreditors = 0.0
        var totalCreditors = 0.0
        var equity = 0.0
        var turnover = 0.0
        var netProfit = 0.0
        var totalExpensesFromTb = 0.0

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotEmpty() }

        for (line in lines) {
            // First check if line is standard key-value with ':' or '='
            if (line.contains(":") || line.contains("=")) {
                val parts = line.split(Regex("[:=]"), 2)
                if (parts.size == 2) {
                    val key = parts[0].trim().lowercase()
                    val value = parseAmount(parts[1]) ?: 0.0

                    when {
                        key.contains("cash") || key.contains("bank") -> cash = value
                        key.contains("debtor") || key.contains("receivable") || key.contains("trade debtor") -> debtors = value
                        key.contains("stock") || key.contains("inventor") -> stock = value
                        key.contains("current asset") -> curAssets = value
                        key.contains("total asset") -> totalAssets = value
                        (key.contains("creditor") && key.contains("within")) || key.contains("current liabilit") -> curCreditors = value
                        (key.contains("creditor") && key.contains("after")) || key.contains("long-term") || key.contains("long term") -> ltCreditors = value
                        key.contains("total creditor") || key.contains("total liabilit") -> totalCreditors = value
                        key.contains("shareholder") || key.contains("equity") || key.contains("capital") -> equity = value
                        key.contains("turnover") || key.contains("revenue") || key.contains("sales") || key.contains("billings") -> turnover = value
                        key.contains("profit before tax") || key.contains("net profit") || key.contains("net income") || key.contains("pbt") -> netProfit = value
                    }
                    continue
                }
            }

            // Otherwise, process as tabular/CSV line (Trial Balance / Ledger row)
            val tokens = parseCsvLine(line)
            if (tokens.size >= 2) {
                // Find all valid numeric amounts in this row
                val amountsWithIndices = tokens.mapIndexedNotNull { index, token ->
                    parseAmount(token)?.let { index to it }
                }

                // If no numeric amounts found, skip header rows
                if (amountsWithIndices.isEmpty()) continue

                // Construct row description by taking the non-numeric parts
                val description = tokens
                    .filterIndexed { idx, _ -> amountsWithIndices.none { it.first == idx } }
                    .joinToString(" ")
                    .lowercase()

                // In Trial Balance:
                // Credit = Income / Liabilities / Equity (Usually column 3 or last column)
                // Debit = Assets / Expenses
                // Pick the most substantial non-zero balance on this line
                val highestAmount = amountsWithIndices.maxByOrNull { abs(it.second) }?.second ?: 0.0
                val absVal = abs(highestAmount)

                // Match UK Account Codes (Xero: 200, 260; Sage: 4000-4999) or account descriptions
                val isTurnoverRow = description.contains("turnover") ||
                        description.contains("sales") ||
                        description.contains("revenue") ||
                        description.contains("fee income") ||
                        description.contains("billings") ||
                        line.startsWith("200 ") || line.startsWith("260 ") ||
                        Regex("^4[0-9]{3}\\b").containsMatchIn(line)

                val isPbtRow = description.contains("profit before tax") ||
                        description.contains("net profit") ||
                        description.contains("operating profit") ||
                        description.contains("pbt")

                val isExpenseRow = description.contains("expense") ||
                        description.contains("wages") ||
                        description.contains("salaries") ||
                        description.contains("cost of sales") ||
                        description.contains("rent") ||
                        description.contains("overheads") ||
                        description.contains("depreciation")

                when {
                    isTurnoverRow -> {
                        // Avoid treating header lines or small account numbers as amount
                        if (absVal > 50.0) {
                            turnover += absVal
                        }
                    }
                    isPbtRow -> {
                        netProfit = highestAmount
                    }
                    description.contains("cash") || description.contains("bank") || description.contains("current account") -> {
                        cash += absVal
                    }
                    description.contains("debtor") || description.contains("receivable") || description.contains("trade debtor") -> {
                        debtors += absVal
                    }
                    description.contains("stock") || description.contains("inventory") || description.contains("raw materials") -> {
                        stock += absVal
                    }
                    description.contains("current asset") -> {
                        curAssets = absVal
                    }
                    description.contains("total asset") -> {
                        totalAssets = absVal
                    }
                    (description.contains("creditor") && description.contains("within")) || description.contains("trade creditor") || description.contains("accounts payable") -> {
                        curCreditors += absVal
                    }
                    (description.contains("creditor") && description.contains("after")) || description.contains("bank loan") || description.contains("long-term") -> {
                        ltCreditors += absVal
                    }
                    description.contains("shareholder") || description.contains("retained earning") || description.contains("share capital") -> {
                        equity += absVal
                    }
                    isExpenseRow -> {
                        totalExpensesFromTb += absVal
                    }
                }
            }
        }

        // Auto-reconciliation logic for Trial Balance & UK Balance Sheet formats
        if (curAssets == 0.0 && (cash > 0 || debtors > 0 || stock > 0)) {
            curAssets = cash + debtors + stock
        }
        if (totalAssets == 0.0 && curAssets > 0) {
            totalAssets = curAssets
        }
        if (totalCreditors == 0.0 && (curCreditors > 0 || ltCreditors > 0)) {
            totalCreditors = curCreditors + ltCreditors
        }
        // If Trial Balance had turnover and expenses but no explicit PBT row, compute it!
        if (netProfit == 0.0 && turnover > 0.0 && totalExpensesFromTb > 0.0) {
            netProfit = turnover - totalExpensesFromTb
        } else if (netProfit == 0.0 && turnover > 0.0 && totalCreditors > 0.0 && totalAssets > totalCreditors) {
            // Heuristic fallback for simple TB where retained profit is in equity
            if (equity > 0.0 && equity < turnover) {
                netProfit = equity * 0.4
            }
        }

        return BalanceSheetReport(
            cashAtBank = cash,
            tradeDebtors = debtors,
            stockInventory = stock,
            currentAssets = curAssets,
            totalAssets = totalAssets,
            creditorsDueWithinOneYear = curCreditors,
            creditorsDueAfterOneYear = ltCreditors,
            totalCreditors = totalCreditors,
            shareholderFunds = equity,
            turnover = turnover,
            netProfitBeforeTax = netProfit,
            rawFileName = sourceName
        )
    }

    fun computeMetrics(report: BalanceSheetReport): List<FinancialHealthMetric> {
        val list = mutableListOf<FinancialHealthMetric>()

        // 1. Current Working Ratio
        if (report.creditorsDueWithinOneYear > 0) {
            val cr = report.currentAssets / report.creditorsDueWithinOneYear
            val (status, color, exp) = when {
                cr >= 1.5 && cr <= 2.6 -> Triple("Optimal Liquidity", Color(0xFF2EA043), "Sound cover for short-term trade creditors and HMRC VAT/PAYE liabilities.")
                cr in 1.0..1.5 -> Triple("Tight Working Capital", Color(0xFFD29922), "Able to meet creditors, but sensitive to late-paying trade debtors.")
                cr < 1.0 -> Triple("Insolvency Warning", Color(0xFFF85149), "Creditors due within 1 year exceed current assets. High risk of cash crunch!")
                else -> Triple("High Cash Surplus", Color(0xFF58A6FF), "Working capital ratio >2.6. Capital could be invested into qualifying AIA plant/machinery.")
            }
            list.add(FinancialHealthMetric("Current Ratio", String.format(Locale.UK, "%.2fx", cr), status, color, exp))
        }

        // 2. Gearing / Debt-to-Equity Ratio
        if (report.shareholderFunds > 0) {
            val de = report.totalCreditors / report.shareholderFunds
            val (status, color, exp) = when {
                de <= 1.0 -> Triple("Low Gearing", Color(0xFF2EA043), "Conservative balance sheet funded mainly through retained reserves.")
                de <= 2.0 -> Triple("Moderate Gearing", Color(0xFFD29922), "Typical UK commercial structure; keep monitor on loan interest cover.")
                else -> Triple("Highly Geared", Color(0xFFF85149), "Heavy reliance on debt finance; increases risk if UK base rates fluctuate.")
            }
            list.add(FinancialHealthMetric("Gearing Ratio", String.format(Locale.UK, "%.2fx", de), status, color, exp))
        }

        // 3. Pre-Tax Margin
        if (report.turnover > 0) {
            val margin = (report.netProfitBeforeTax / report.turnover) * 100.0
            val (status, color, exp) = when {
                margin >= 15.0 -> Triple("Superior UK Trading", Color(0xFF2EA043), "Strong gross-to-net conversion with robust pricing power.")
                margin in 5.0..15.0 -> Triple("Healthy Trading", Color(0xFF58A6FF), "Normal UK corporate trading operating margin.")
                margin in 0.0..5.0 -> Triple("Narrow Margin", Color(0xFFD29922), "Vulnerable to supply chain inflation or National Living Wage rises.")
                else -> Triple("Operating at a Loss", Color(0xFFF85149), "Trading loss. Check UK loss carry-back rules against prior year profits.")
            }
            list.add(FinancialHealthMetric("Net Margin", String.format(Locale.UK, "%.1f%%", margin), status, color, exp))
        }

        // 4. Net Current Assets (Working Capital)
        val workingCapital = report.currentAssets - report.creditorsDueWithinOneYear
        val (status, color, exp) = when {
            workingCapital > 0 -> Triple("Positive Buffer", Color(0xFF2EA043), "Surplus liquid assets available for business growth.")
            workingCapital == 0.0 -> Triple("Neutral", Color(0xFF8B949E), "No margin for operational delay.")
            else -> Triple("Deficit Alert", Color(0xFFF85149), "Negative working capital; creditors exceed short-term assets.")
        }
        list.add(FinancialHealthMetric("Net Current Assets", String.format(Locale.UK, "£%,.0f", workingCapital), status, color, exp))

        return list
    }

    fun generateAdvisory(report: BalanceSheetReport): List<AdvisoryRecommendation> {
        val adviceList = mutableListOf<AdvisoryRecommendation>()

        // 1. Balance Sheet Fundamental Equation (Total Assets = Total Creditors + Shareholder Funds)
        val expectedAssets = report.totalCreditors + report.shareholderFunds
        val discrepancy = report.totalAssets - expectedAssets
        if (report.totalAssets > 0 && Math.abs(discrepancy) > 1.0) {
            adviceList.add(
                AdvisoryRecommendation(
                    id = "adv_imbalance",
                    title = "Companies House Balance Sheet Imbalance",
                    severity = "CRITICAL AUDIT ITEM",
                    color = Color(0xFFF85149),
                    advice = "Total Assets (£%,.0f) do not equal Total Creditors + Shareholder Funds (£%,.0f). Discrepancy is £%,.0f.".format(Locale.UK, report.totalAssets, expectedAssets, discrepancy),
                    actionItem = "Audit unrecorded journal entries, unposted depreciation, or dividends before submitting annual accounts to Companies House."
                )
            )
        }

        // 2. Corporation Tax Liability Advisory with Marginal Rate insight
        if (report.netProfitBeforeTax > 0) {
            val estimatedCT = calculateCorporationTax(report.netProfitBeforeTax)
            val effectiveRate = (estimatedCT / report.netProfitBeforeTax) * 100.0
            val isMarginal = report.netProfitBeforeTax in 50000.0..250000.0

            adviceList.add(
                AdvisoryRecommendation(
                    id = "adv_tax_reserve",
                    title = if (isMarginal) "HMRC Corporation Tax (26.5% Marginal Taper Active)" else "HMRC Corporation Tax Reserve Estimate",
                    severity = if (isMarginal) "HIGH PRIORITY TAX PLANNING" else "TAX PLANNING",
                    color = Color(0xFF58A6FF),
                    advice = if (isMarginal) {
                        "Estimated Corp Tax is £%,.0f (Effective rate: %.1f%%). Because profits sit between £50k and £250k, each marginal pound of profit bears a 26.5%% effective tax charge.".format(Locale.UK, estimatedCT, effectiveRate)
                    } else {
                        "Estimated Corp Tax is £%,.0f (Effective rate: %.1f%%). HMRC payment deadline is 9 months and 1 day after accounting period end.".format(Locale.UK, estimatedCT, effectiveRate)
                    },
                    actionItem = if (isMarginal) {
                        "Accelerate allowable deductions (AIA equipment, employer pension contribution, director bonuses) to capture 26.5p of tax relief per £1 spent."
                    } else {
                        "Consider utilizing Annual Investment Allowance (AIA) or pension contributions before year-end to reduce taxable profit."
                    }
                )
            )
        }

        // 3. Liquidity & Working Capital Warning
        if (report.creditorsDueWithinOneYear > 0) {
            val cr = report.currentAssets / report.creditorsDueWithinOneYear
            if (cr < 1.1) {
                adviceList.add(
                    AdvisoryRecommendation(
                        id = "adv_liquidity",
                        title = "Creditor Default & Cash Shortfall Warning",
                        severity = "HIGH RISK",
                        color = Color(0xFFF85149),
                        advice = "Creditors due within 1 year are near or exceeding quick assets. A delayed debtor payment could risk a late VAT/PAYE settlement.",
                        actionItem = "Chase aged Trade Debtors (£%,.0f) and offer a 2%% early-settlement discount. Enforce 30-day payment terms.".format(Locale.UK, report.tradeDebtors)
                    )
                )
            }
        }

        // 4. Stagnant Stock Warning
        if (report.currentAssets > 0 && report.stockInventory > (report.currentAssets * 0.40)) {
            adviceList.add(
                AdvisoryRecommendation(
                    id = "adv_stock",
                    title = "Excess Capital Locked in Stock",
                    severity = "MODERATE RISK",
                    color = Color(0xFFD29922),
                    advice = "Stock (£%,.0f) makes up over 40%% of total current assets, restricting business liquidity.".format(Locale.UK, report.stockInventory),
                    actionItem = "Perform Net Realisable Value (NRV) review under FRS 102. Discount or write down aged lines to lower taxable profit and free working cash."
                )
            )
        }

        // 5. Audit Exemption Assessment
        if (report.turnover > 0 && report.totalAssets > 0) {
            val isExempt = report.turnover <= 15000000.0 && report.totalAssets <= 7500000.0
            if (isExempt) {
                adviceList.add(
                    AdvisoryRecommendation(
                        id = "adv_audit",
                        title = "Eligible for UK Small Company Audit Exemption",
                        severity = "COMPLIANCE BENEFIT",
                        color = Color(0xFF2EA043),
                        advice = "Turnover (£%,.0f) and Total Assets (£%,.0f) sit safely under the £15m / £7.5m statutory thresholds.".format(Locale.UK, report.turnover, report.totalAssets),
                        actionItem = "File unaudited FRS 102 Section 1A or FRS 105 Micro-entity accounts with Companies House to save external audit costs."
                    )
                )
            }
        }

        return adviceList
    }

    /**
     * Generates a concrete, dynamic Action Checklist tailored directly to the uploaded balance sheet numbers.
     */
    fun generateTasks(report: BalanceSheetReport): List<ActionTask> {
        val tasks = mutableListOf<ActionTask>()

        // Imbalance fix
        val expectedAssets = report.totalCreditors + report.shareholderFunds
        val discrepancy = report.totalAssets - expectedAssets
        if (Math.abs(discrepancy) > 1.0 && report.totalAssets > 0) {
            tasks.add(
                ActionTask(
                    id = "task_fix_imbalance",
                    title = "Reconcile Balance Sheet Difference of £%,.0f".format(Locale.UK, Math.abs(discrepancy)),
                    urgency = "CRITICAL",
                    urgencyColor = Color(0xFFF85149),
                    rationale = "Companies House requires Total Assets to strictly match Total Creditors + Shareholder Funds.",
                    procedure = "Review unposted general ledger journals: verify Retained Profit brought forward, depreciation accruals, and director dividend postings.",
                    statutoryReference = "Companies Act 2006 s.393 (True and Fair view)"
                )
            )
        }

        // Tax reserve task
        if (report.netProfitBeforeTax > 0) {
            val estimatedCT = calculateCorporationTax(report.netProfitBeforeTax)
            val isMarginal = report.netProfitBeforeTax in 50000.0..250000.0

            tasks.add(
                ActionTask(
                    id = "task_ct_reserve",
                    title = "Sweep £%,.0f to HMRC Tax Reserve Account".format(Locale.UK, estimatedCT),
                    urgency = if (isMarginal) "HIGH PRIORITY" else "ESSENTIAL",
                    urgencyColor = if (isMarginal) Color(0xFFD29922) else Color(0xFF58A6FF),
                    rationale = if (isMarginal) {
                        "Profits are subject to the 26.5% marginal taper. Ring-fencing cash prevents late HMRC payment interest."
                    } else {
                        "Ensure tax liability is ring-fenced 9 months and 1 day after your accounting period end."
                    },
                    procedure = "Transfer £%,.0f into a designated deposit account. Check pre-year-end capital allowance investments (AIA) to legally reduce this liability.".format(Locale.UK, estimatedCT),
                    statutoryReference = "TMA 1970 s.59D / Corporation Tax Act 2010"
                )
            )
        }

        // Aged debtors task
        if (report.tradeDebtors > 25000.0) {
            val potentialVatRelief = (report.tradeDebtors * 0.15) * (20.0 / 120.0)
            tasks.add(
                ActionTask(
                    id = "task_chase_debtors",
                    title = "Audit Trade Debtors Book (£%,.0f)".format(Locale.UK, report.tradeDebtors),
                    urgency = "CASH FLOW",
                    urgencyColor = Color(0xFF58A6FF),
                    rationale = "Aged trade debtors tie up working capital. Invoices unpaid past 6 months qualify for VAT Bad Debt Relief.",
                    procedure = "Issue 7-day payment demand letters. For balances older than 6 months already written off, reclaim approximately £%,.0f via Box 4 of next MTD VAT return.".format(Locale.UK, potentialVatRelief),
                    statutoryReference = "VATA 1994 Section 36 (Bad Debt Relief)"
                )
            )
        }

        // Working capital & liquidity task
        if (report.creditorsDueWithinOneYear > 0) {
            val cr = report.currentAssets / report.creditorsDueWithinOneYear
            if (cr < 1.15) {
                tasks.add(
                    ActionTask(
                        id = "task_working_capital",
                        title = "Implement Working Capital Buffer Plan",
                        urgency = "WARNING",
                        urgencyColor = Color(0xFFF85149),
                        rationale = "Current Ratio is %.2fx. Current liabilities (£%,.0f) threaten cash solvency.".format(Locale.UK, cr, report.creditorsDueWithinOneYear),
                        procedure = "Negotiate extended 60-day terms with key suppliers, defer non-essential capex, or arrange a formal director loan buffer.",
                        statutoryReference = "Insolvency Act 1986 s.123 (Cash-flow test)"
                    )
                )
            }
        }

        // Stock inventory NRV task
        if (report.stockInventory > 30000.0) {
            tasks.add(
                ActionTask(
                    id = "task_stock_nrv",
                    title = "Perform Stock Net Realisable Value (NRV) Audit",
                    urgency = "TAX SAVING",
                    urgencyColor = Color(0xFF2EA043),
                    rationale = "Stock carries a value of £%,.0f. Any damaged or slow-moving lines can be written down to reduce Corporation Tax.".format(Locale.UK, report.stockInventory),
                    procedure = "Run an inventory aging report (>90 days). Write down carrying values to expected net recovery price before balance sheet sign-off.",
                    statutoryReference = "FRS 102 Section 13 (Inventories)"
                )
            )
        }

        // Audit exemption confirmation
        if (report.turnover > 0 && report.turnover <= 15000000.0 && report.totalAssets <= 7500000.0) {
            tasks.add(
                ActionTask(
                    id = "task_file_exempt",
                    title = "Adopt Audit Exemption Statement on Balance Sheet",
                    urgency = "COMPLIANCE",
                    urgencyColor = Color(0xFF2EA043),
                    rationale = "Company qualifies as small under Companies Act 2006.",
                    procedure = "Include standard statutory audit exemption notes on the final balance sheet submitted to Companies House.",
                    statutoryReference = "Companies Act 2006 Section 477"
                )
            )
        }

        return tasks
    }

    /**
     * Dynamically generates customized journal entries that directly incorporate the numbers from the uploaded balance sheet!
     */
    fun generateDynamicJournalEntries(report: BalanceSheetReport): List<CheatSheetEntry> {
        val dynamicList = mutableListOf<CheatSheetEntry>()

        // 1. Corporation Tax Accrual Journal
        if (report.netProfitBeforeTax > 0) {
            val estimatedCT = calculateCorporationTax(report.netProfitBeforeTax)
            dynamicList.add(
                CheatSheetEntry(
                    id = "dyn_ct_accrual",
                    transaction = "Accrue Corporation Tax for the Period (£%,.0f)".format(Locale.UK, estimatedCT),
                    debit = "Taxation on Ordinary Activities (P&L): £%,.0f".format(Locale.UK, estimatedCT),
                    credit = "Corporation Tax Liability (Creditors < 1yr): £%,.0f".format(Locale.UK, estimatedCT),
                    explanation = "Accrues current tax obligation calculated on £%,.0f PBT before financial year close.".format(Locale.UK, report.netProfitBeforeTax),
                    isSuggestedForReport = true,
                    suggestedContext = "Derived directly from your uploaded Profit Before Tax"
                )
            )
        }

        // 2. Bad Debt Provision on Large Debtors Book
        if (report.tradeDebtors > 25000.0) {
            val provisionAmt = report.tradeDebtors * 0.05
            dynamicList.add(
                CheatSheetEntry(
                    id = "dyn_bad_debt",
                    transaction = "5% General/Specific Doubtful Debts Provision (£%,.0f)".format(Locale.UK, provisionAmt),
                    debit = "Bad & Doubtful Debts Expense (P&L): £%,.0f".format(Locale.UK, provisionAmt),
                    credit = "Provision for Doubtful Debts (Contra-Asset): £%,.0f".format(Locale.UK, provisionAmt),
                    explanation = "Prudence adjustment against £%,.0f trade debtors to reflect estimated non-recoverable accounts under FRS 102.".format(Locale.UK, report.tradeDebtors),
                    isSuggestedForReport = true,
                    suggestedContext = "Recommended for your £%,.0f Trade Debtors balance".format(Locale.UK, report.tradeDebtors)
                )
            )
        }

        // 3. Stock Write Down to NRV
        if (report.stockInventory > 30000.0) {
            val writeDownAmt = report.stockInventory * 0.08
            dynamicList.add(
                CheatSheetEntry(
                    id = "dyn_stock_write_down",
                    transaction = "Obsolete Stock Write-Down to NRV (£%,.0f)".format(Locale.UK, writeDownAmt),
                    debit = "Cost of Sales / Stock Write-Down (P&L): £%,.0f".format(Locale.UK, writeDownAmt),
                    credit = "Stock Inventory on Hand (Current Assets): £%,.0f".format(Locale.UK, writeDownAmt),
                    explanation = "Reduces £%,.0f stock carrying value to net realisable value, decreasing taxable trading profits.".format(Locale.UK, report.stockInventory),
                    isSuggestedForReport = true,
                    suggestedContext = "Recommended based on your £%,.0f stock holding".format(Locale.UK, report.stockInventory)
                )
            )
        }

        // 4. Working Capital Loan Reclassification
        if (report.creditorsDueWithinOneYear > 100000.0) {
            dynamicList.add(
                CheatSheetEntry(
                    id = "dyn_refinance_loan",
                    transaction = "Term-Loan Reclassification (Short to Long-Term)",
                    debit = "Bank Loans Due Within 1 Year (Current Creditors)",
                    credit = "Bank Loans Due After 1 Year (Non-Current Creditors)",
                    explanation = "Refinances short-term liabilities into long-term facility to restore current ratio from strained level.",
                    isSuggestedForReport = true,
                    suggestedContext = "Addresses £%,.0f in short-term creditors".format(Locale.UK, report.creditorsDueWithinOneYear)
                )
            )
        }

        // Append standard cheat-sheet items
        dynamicList.addAll(AccountancyRepository.getStandardCheatSheet())
        return dynamicList
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF0D1117),
                    surface = Color(0xFF161B22),
                    primary = Color(0xFF2EA043),
                    secondary = Color(0xFF58A6FF),
                    onBackground = Color(0xFFC9D1D9),
                    onSurface = Color(0xFFF0F6FC)
                )
            ) {
                UKAccountancyAppScreen()
            }
        }
    }
}

@Composable
fun UKAccountancyAppScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    // Central shared balance sheet state driving all tabs
    var rawInputText by remember {
        mutableStateOf(
            """
Cash at Bank: £55,000
Trade Debtors: £82,000
Stock: £40,000
Current Assets: £177,000
Total Assets: £340,000
Creditors Due Within One Year: £115,000
Creditors Due After One Year: £75,000
Total Creditors: £190,000
Shareholder Funds: £150,000
Turnover: £520,000
Profit Before Tax: £65,000
            """.trimIndent()
        )
    }

    var report by remember {
        mutableStateOf(UKReportAnalysisEngine.parseReportText(rawInputText, "UK FRS Sample"))
    }
    var importedFileName by remember { mutableStateOf("UK FRS Sample") }

    // Synchronized active tasks generated from the balance sheet
    var activeTasks by remember { mutableStateOf(UKReportAnalysisEngine.generateTasks(report)) }

    // Re-generate tasks whenever the active report updates
    LaunchedEffect(report) {
        activeTasks = UKReportAnalysisEngine.generateTasks(report)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF161B22)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Audit & Import") },
                    label = { Text("Audit & Import", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                val pending = activeTasks.count { !it.isDone }
                                if (pending > 0) {
                                    Badge(containerColor = Color(0xFFF85149)) {
                                        Text("$pending", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.PlaylistAddCheck, contentDescription = "Action Tasks")
                        }
                    },
                    label = { Text("Action Tasks", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "HMRC Tax") },
                    label = { Text("HMRC Tax", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Tips") },
                    label = { Text("UK Tips", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Journals") },
                    label = { Text("Journals", fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(innerPadding)
        ) {
            // Persistent Active Report Indicator Banner
            ActiveReportTopBanner(
                report = report,
                sourceName = importedFileName,
                onNavigateToAudit = { selectedTab = 0 }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> UKReportImportAuditView(
                        report = report,
                        rawInputText = rawInputText,
                        importedFileName = importedFileName,
                        onUpdateReport = { newReport, newRaw, newName ->
                            report = newReport
                            rawInputText = newRaw
                            importedFileName = newName
                        },
                        onNavigateToTasks = { selectedTab = 1 },
                        onNavigateToTax = { selectedTab = 2 }
                    )
                    1 -> UKActionTasksView(
                        report = report,
                        tasks = activeTasks,
                        onToggleTask = { taskId ->
                            activeTasks = activeTasks.map {
                                if (it.id == taskId) it.copy(isDone = !it.isDone) else it
                            }
                        }
                    )
                    2 -> UKTaxCalculatorView(report = report)
                    3 -> UKTipsView(report = report)
                    4 -> UKCheatsheetView(report = report)
                }
            }
        }
    }
}

@Composable
fun ActiveReportTopBanner(
    report: BalanceSheetReport,
    sourceName: String,
    onNavigateToAudit: () -> Unit
) {
    Surface(
        color = Color(0xFF161B22),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color(0xFF30363D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2EA043))
                )
                Column {
                    Text(
                        text = "ACTIVE REPORT: ${sourceName.take(20)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF0F6FC)
                    )
                    Text(
                        text = "Turnover: £%,.0f  •  PBT: £%,.0f".format(Locale.UK, report.turnover, report.netProfitBeforeTax),
                        fontSize = 10.sp,
                        color = Color(0xFF8B949E)
                    )
                }
            }

            AssistChip(
                onClick = onNavigateToAudit,
                label = { Text("Change", fontSize = 10.sp, color = Color(0xFF58A6FF)) },
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

@Composable
fun UKReportImportAuditView(
    report: BalanceSheetReport,
    rawInputText: String,
    importedFileName: String,
    onUpdateReport: (BalanceSheetReport, String, String) -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToTax: () -> Unit
) {
    val context = LocalContext.current
    var showManualEdit by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val content = reader.readText()
                    val fileName = it.lastPathSegment ?: "Imported UK File"
                    val parsed = UKReportAnalysisEngine.parseReportText(content, fileName)
                    onUpdateReport(parsed, content, fileName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val metrics = remember(report) { UKReportAnalysisEngine.computeMetrics(report) }
    val recommendations = remember(report) { UKReportAnalysisEngine.generateAdvisory(report) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "UK BALANCE SHEET AUDIT & IMPORT",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Data feeds HMRC tax planning, automated tasks, and journals across the app.",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )
            }
        }

        // Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { filePickerLauncher.launch("*/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showManualEdit = !showManualEdit },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC9D1D9)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(if (showManualEdit) Icons.Default.Close else Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (showManualEdit) "Close Input" else "Paste / Edit", fontSize = 12.sp)
                }
            }
        }

        // Quick Preset Templates
        item {
            Text("Sample UK Scenarios:", fontSize = 11.sp, color = Color(0xFF8B949E), fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(
                        onClick = {
                            val raw = """
Cash at Bank: £140,000
Trade Debtors: £60,000
Stock: £0
Current Assets: £200,000
Total Assets: £280,000
Creditors Due Within One Year: £45,000
Creditors Due After One Year: £20,000
Total Creditors: £65,000
Shareholder Funds: £215,000
Turnover: £750,000
Profit Before Tax: £180,000
                            """.trimIndent()
                            val name = "UK Tech Ltd (£180k PBT)"
                            onUpdateReport(UKReportAnalysisEngine.parseReportText(raw, name), raw, name)
                        },
                        label = { Text("UK Tech Ltd (Marginal Relief)", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            val raw = """
Cash at Bank: £8,000
Trade Debtors: £95,000
Stock: £160,000
Current Assets: £263,000
Total Assets: £380,000
Creditors Due Within One Year: £280,000
Creditors Due After One Year: £90,000
Total Creditors: £370,000
Shareholder Funds: £10,000
Turnover: £600,000
Profit Before Tax: -£25,000
                            """.trimIndent()
                            val name = "Distressed UK Retail"
                            onUpdateReport(UKReportAnalysisEngine.parseReportText(raw, name), raw, name)
                        },
                        label = { Text("Distressed Retail", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            val raw = """
Cash at Bank: £45,000
Trade Debtors: £35,000
Stock: £50,000
Current Assets: £130,000
Total Assets: £280,000
Creditors Due Within One Year: £90,000
Creditors Due After One Year: £50,000
Total Creditors: £140,000
Shareholder Funds: £110,000
Turnover: £350,000
Profit Before Tax: £32,000
                            """.trimIndent()
                            val name = "Imbalanced UK Audit"
                            onUpdateReport(UKReportAnalysisEngine.parseReportText(raw, name), raw, name)
                        },
                        label = { Text("Imbalanced Audit", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Raw Editor
        if (showManualEdit) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Raw Ledger / Balance Sheet Input:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF58A6FF)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                onUpdateReport(UKReportAnalysisEngine.parseReportText(it, "Manual Input"), it, "Manual Input")
                            },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color(0xFFC9D1D9),
                                focusedBorderColor = Color(0xFF2EA043),
                                unfocusedBorderColor = Color(0xFF30363D)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }
                }
            }
        }

        // Summary Card in GBP
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PARSED BALANCE SHEET (GBP)", color = Color(0xFF8B949E), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Assets:", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.totalAssets), color = Color(0xFF2EA043), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Assets (Bank + Debtors + Stock):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.currentAssets), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Creditors Due (< 1yr + > 1yr):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.totalCreditors), color = Color(0xFFF85149), fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shareholder Funds (Capital & Reserves):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.shareholderFunds), color = Color(0xFF58A6FF), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Shortcuts to Linked Sections
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onNavigateToTasks,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6FEB)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlaylistAddCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("View Tasks", fontSize = 11.sp)
                }
                Button(
                    onClick = onNavigateToTax,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF238636)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("HMRC Tax View", fontSize = 11.sp)
                }
            }
        }

        // Metrics
        items(metrics) { metric ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(metric.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(metric.explanation, color = Color(0xFF8B949E), fontSize = 11.sp, lineHeight = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(text = metric.status, color = metric.statusColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = metric.statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = metric.valueStr,
                            color = metric.statusColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Advisory Section
        item {
            Spacer(Modifier.height(4.dp))
            Text("STATUTORY ADVISORY & COMPLIANCE FINDINGS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2EA043), letterSpacing = 1.sp)
        }

        items(recommendations) { advice ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth().border(1.dp, advice.color.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(advice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFF0F6FC), modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(6.dp), color = advice.color.copy(alpha = 0.2f)) {
                            Text(advice.severity, color = advice.color, fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(advice.advice, color = Color(0xFFC9D1D9), fontSize = 12.sp, lineHeight = 17.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF0D1117), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text("ACTION: ", color = Color(0xFF58A6FF), fontWeight = FontWeight.Black, fontSize = 11.sp)
                            Text(advice.actionItem, color = Color(0xFFF0F6FC), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UKActionTasksView(
    report: BalanceSheetReport,
    tasks: List<ActionTask>,
    onToggleTask: (String) -> Unit
) {
    val completedCount = tasks.count { it.isDone }
    val progressPct = if (tasks.isNotEmpty()) completedCount.toFloat() / tasks.size else 1f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "ACTIONABLE AUDIT TASKS & FIXES",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Generated in real time from ${report.rawFileName}.",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )
            }
        }

        // Progress Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Task Completion Score", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("$completedCount / ${tasks.size} Done", fontSize = 12.sp, color = Color(0xFF58A6FF), fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = progressPct,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF2EA043),
                        trackColor = Color(0xFF21262D)
                    )
                }
            }
        }

        items(tasks) { task ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (task.isDone) Color(0xFF11161D) else Color(0xFF161B22)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (task.isDone) Color(0xFF2EA043).copy(alpha = 0.4f) else task.urgencyColor.copy(alpha = 0.5f),
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onToggleTask(task.id) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = task.isDone,
                        onCheckedChange = { onToggleTask(task.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF2EA043),
                            uncheckedColor = Color(0xFF8B949E)
                        )
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = if (task.isDone) Color(0xFF8B949E) else Color(0xFFF0F6FC)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = task.urgencyColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = task.urgency,
                                    color = task.urgencyColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))
                        Text(task.rationale, color = Color(0xFFC9D1D9), fontSize = 12.sp, lineHeight = 16.sp)

                        Spacer(Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF0D1117),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text("HOW TO FIX:", fontSize = 10.sp, color = Color(0xFF58A6FF), fontWeight = FontWeight.Bold)
                                Text(task.procedure, fontSize = 11.sp, color = Color(0xFFF0F6FC), lineHeight = 15.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("Ref: ${task.statutoryReference}", fontSize = 10.sp, color = Color(0xFF8B949E), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UKTaxCalculatorView(report: BalanceSheetReport) {
    var turnoverInput by remember {
        mutableStateOf(if (report.turnover > 0) report.turnover.toLong().toString() else "")
    }
    var allowableExpensesInput by remember {
        val calculatedExpenses = (report.turnover - report.netProfitBeforeTax).coerceAtLeast(0.0)
        mutableStateOf(if (calculatedExpenses > 0) calculatedExpenses.toLong().toString() else "")
    }
    var vatRateSelection by remember { mutableStateOf(20) }

    // Synchronize inputs whenever the parent active report updates (from upload, CSV import, or presets)
    LaunchedEffect(report) {
        if (report.turnover > 0) {
            turnoverInput = report.turnover.toLong().toString()
        }
        val calculatedExpenses = (report.turnover - report.netProfitBeforeTax).coerceAtLeast(0.0)
        if (calculatedExpenses > 0 || report.netProfitBeforeTax != 0.0) {
            allowableExpensesInput = calculatedExpenses.toLong().toString()
        }
    }

    val turnover = turnoverInput.toDoubleOrNull() ?: 0.0
    val expenses = allowableExpensesInput.toDoubleOrNull() ?: 0.0
    val taxableProfit = (turnover - expenses).coerceAtLeast(0.0)

    val corpTax = UKReportAnalysisEngine.calculateCorporationTax(taxableProfit)
    val effectiveRate = if (taxableProfit > 0) (corpTax / taxableProfit) * 100.0 else 0.0
    val retainedProfit = taxableProfit - corpTax
    val estimatedOutputVAT = turnover * (vatRateSelection / 100.0)

    val isMarginalRelief = taxableProfit in 50000.0..250000.0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "HMRC CORPORATION TAX & VAT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tax computation dynamically connected to your uploaded balance sheet.",
                    fontSize = 12.sp,
                    color = Color(0xFF8B949E)
                )
            }
        }

        // Live connection indicator & reset button
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF161B22),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2EA043))
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "LINKED: ${report.rawFileName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF0F6FC)
                            )
                        }
                        Text(
                            text = "Report Turnover: £%,.0f  •  PBT: £%,.0f".format(
                                Locale.UK,
                                report.turnover,
                                report.netProfitBeforeTax
                            ),
                            fontSize = 11.sp,
                            color = Color(0xFF8B949E)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            turnoverInput = if (report.turnover > 0) report.turnover.toLong().toString() else "0"
                            val exp = (report.turnover - report.netProfitBeforeTax).coerceAtLeast(0.0)
                            allowableExpensesInput = exp.toLong().toString()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF58A6FF)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Re-Sync", fontSize = 11.sp)
                    }
                }
            }
        }

        if (isMarginalRelief) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFD29922).copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD29922), RoundedCornerShape(10.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFD29922),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "26.5% MARGINAL RELIEF BAND ACTIVE: Profits sit between £50,000 and £250,000. Every extra £1 of allowable expense or pension contribution saves 26.5p in Corporation Tax!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE3B341),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = turnoverInput,
                onValueChange = { turnoverInput = it },
                label = { Text("Annual Turnover / Billings (£)") },
                placeholder = { Text("e.g. 520000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFC9D1D9),
                    focusedBorderColor = Color(0xFF2EA043),
                    unfocusedBorderColor = Color(0xFF30363D)
                )
            )
        }

        item {
            OutlinedTextField(
                value = allowableExpensesInput,
                onValueChange = { allowableExpensesInput = it },
                label = { Text("Allowable Business Expenses & AIA Deductions (£)") },
                placeholder = { Text("e.g. 455000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color(0xFFC9D1D9),
                    focusedBorderColor = Color(0xFF2EA043),
                    unfocusedBorderColor = Color(0xFF30363D)
                )
            )
        }

        item {
            Text(
                text = "VAT Rate Applied to Turnover:",
                fontSize = 12.sp,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(20 to "Standard 20%", 5 to "Reduced 5%", 0 to "Zero-Rated 0%").forEach { (rate, label) ->
                    FilterChip(
                        selected = vatRateSelection == rate,
                        onClick = { vatRateSelection = rate },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF238636),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "HMRC CT600 TAX COMPUTATION SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B949E),
                        letterSpacing = 1.sp
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxable Trading Profit:", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(
                            String.format(Locale.UK, "£%,.2f", taxableProfit),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("UK Corporation Tax Payable:", color = Color(0xFFF85149), fontSize = 13.sp)
                        Text(
                            String.format(Locale.UK, "-£%,.2f", corpTax),
                            color = Color(0xFFF85149),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Effective CT Rate:", color = Color(0xFF8B949E), fontSize = 12.sp)
                        Text(
                            text = when {
                                taxableProfit <= 50000.0 -> "19.0% (Small Profits Rate)"
                                taxableProfit >= 250000.0 -> "25.0% (Main Rate)"
                                else -> String.format(Locale.UK, "%.2f%% (Marginal Relief Taper)", effectiveRate)
                            },
                            color = Color(0xFF58A6FF),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Divider(color = Color(0xFF30363D))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Retained Distributable Reserves:",
                            color = Color(0xFF2EA043),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format(Locale.UK, "£%,.2f", retainedProfit),
                            color = Color(0xFF2EA043),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = "Est. Output VAT (${vatRateSelection}%):",
                            color = Color(0xFFD29922),
                            fontSize = 13.sp
                        )
                        Text(
                            text = String.format(Locale.UK, "£%,.2f", estimatedOutputVAT),
                            color = Color(0xFFD29922),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UKTipsView(report: BalanceSheetReport) {
    var selectedCategory by remember { mutableStateOf(TipCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedTipId by remember { mutableStateOf<Int?>(null) }
    var bookmarkedIds by remember { mutableStateOf(setOf<Int>()) }

    val filteredTips = remember(selectedCategory, searchQuery, report) {
        AccountancyRepository.tips.filter { tip ->
            val matchesCategory = when (selectedCategory) {
                TipCategory.ALL -> true
                TipCategory.TAILORED -> tip.triggerCondition(report)
                else -> tip.category == selectedCategory
            }
            matchesCategory &&
                    (tip.title.contains(searchQuery, ignoreCase = true) ||
                            tip.summary.contains(searchQuery, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UK KNOWLEDGE & STRATEGY",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "HMRC legislation, AIA capital allowances & VAT rules",
                    fontSize = 11.sp,
                    color = Color(0xFF8B949E)
                )
            }
            Badge(containerColor = Color(0xFF21262D)) {
                Text(
                    text = "${bookmarkedIds.size} Saved",
                    color = Color(0xFF58A6FF),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search AIA, marginal relief, VAT...", color = Color(0xFF6E7681), fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
        ) {
            items(TipCategory.values()) { category ->
                val active = selectedCategory == category
                FilterChip(
                    selected = active,
                    onClick = { selectedCategory = category },
                    label = { Text(category.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    leadingIcon = {
                        Icon(category.icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (category == TipCategory.TAILORED) Color(0xFF1F6FEB) else Color(0xFF238636),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredTips) { tip ->
                val isExpanded = expandedTipId == tip.id
                val isSaved = bookmarkedIds.contains(tip.id)
                val isTailored = tip.triggerCondition(report)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isTailored) Color(0xFF1F6FEB) else if (isExpanded) Color(0xFF2EA043) else Color(0xFF30363D),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { expandedTipId = if (isExpanded) null else tip.id }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (isTailored) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF1F6FEB).copy(alpha = 0.2f),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = "TRIGGERED BY YOUR BALANCE SHEET",
                                            color = Color(0xFF58A6FF),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = tip.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF0F6FC)
                                )
                            }
                            IconButton(
                                onClick = {
                                    bookmarkedIds = if (isSaved) bookmarkedIds - tip.id else bookmarkedIds + tip.id
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Save",
                                    tint = if (isSaved) Color(0xFF58A6FF) else Color(0xFF8B949E),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = tip.summary, fontSize = 12.sp, color = Color(0xFF8B949E))

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1F6FEB).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "★ ${tip.impact}",
                                color = Color(0xFF58A6FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                Divider(color = Color(0xFF30363D), thickness = 1.dp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "UK Practice Guidance & Statute:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2EA043)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = tip.deepDive,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFC9D1D9)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UKCheatsheetView(report: BalanceSheetReport) {
    val dynamicJournals = remember(report) {
        UKReportAnalysisEngine.generateDynamicJournalEntries(report)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DOUBLE-ENTRY CHEATSHEET & FIXES", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA043))
        Text("Customized journal entries matching your balance sheet figures.", fontSize = 12.sp, color = Color(0xFF8B949E))
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(dynamicJournals) { entry ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (entry.isSuggestedForReport) Color(0xFF1F6FEB) else Color(0xFF30363D),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        if (entry.isSuggestedForReport) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF1F6FEB).copy(alpha = 0.2f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            ) {
                                Text(
                                    text = "★ ${entry.suggestedContext.uppercase()}",
                                    color = Color(0xFF58A6FF),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(entry.transaction, fontWeight = FontWeight.Bold, color = Color(0xFFF0F6FC), fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth()) {
                            Text("DR: ", color = Color(0xFF2EA043), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text(entry.debit, color = Color(0xFFC9D1D9), fontSize = 12.sp)
                        }
                        Row(Modifier.fillMaxWidth()) {
                            Text("CR: ", color = Color(0xFFF85149), fontWeight = FontWeight.Black, fontSize = 12.sp)
                            Text(entry.credit, color = Color(0xFFC9D1D9), fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(entry.explanation, color = Color(0xFF8B949E), fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }
        }
    }
}
