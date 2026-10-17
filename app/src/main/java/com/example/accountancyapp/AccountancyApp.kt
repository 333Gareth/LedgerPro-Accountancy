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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

// -------------------------------------------------------------
// UK Models & Enums
// -------------------------------------------------------------
enum class TipCategory(val title: String, val icon: ImageVector) {
    ALL("All", Icons.Default.AllInclusive),
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
    val impact: String
)

data class CheatSheetEntry(
    val transaction: String,
    val debit: String,
    val credit: String,
    val explanation: String
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
    val rawFileName: String = ""
)

data class FinancialHealthMetric(
    val name: String,
    val valueStr: String,
    val status: String,
    val statusColor: Color,
    val explanation: String
)

data class AdvisoryRecommendation(
    val title: String,
    val severity: String,
    val color: Color,
    val advice: String,
    val actionItem: String
)

// -------------------------------------------------------------
// UK Knowledge Base (HMRC, FRS 102/105, Companies House)
// -------------------------------------------------------------
object AccountancyRepository {
    val tips = listOf(
        AccountancyTip(
            id = 1,
            title = "Annual Investment Allowance (AIA) & Full Expensing",
            summary = "Write off 100% of qualifying plant & machinery up to £1,000,000 upfront.",
            deepDive = "Under UK tax law, the Annual Investment Allowance allows companies and unincorporated businesses to deduct 100% of the cost of qualifying plant and machinery up to the £1m cap. Companies can also utilize permanent Full Expensing (100% first-year allowance for main rate expenditure) to slash taxable profits immediately.",
            category = TipCategory.TAX,
            impact = "Immediate 25% Corporation Tax Shield"
        ),
        AccountancyTip(
            id = 2,
            title = "UK Corporation Tax Marginal Relief Taper",
            summary = "Profits between £50,000 and £250,000 receive an effective tapered tax rate.",
            deepDive = "Companies with taxable profits below £50,000 pay the 19% small profits rate, while profits over £250,000 pay the full 25% main rate. If your profit is in the £50k-£250k bracket, you claim Marginal Relief using the standard fraction (3/400). Note that associated companies reduce these thresholds proportionately.",
            category = TipCategory.TAX,
            impact = "Avoid Overpaying HMRC Corp Tax"
        ),
        AccountancyTip(
            id = 3,
            title = "VAT Bad Debt Relief (6-Month Rule)",
            summary = "Reclaim output VAT on unpaid customer invoices older than 6 months.",
            deepDive = "Under Section 36 of VATA 1994, if a UK trade debtor hasn't paid you after 6 months from the date of the invoice (and has been written off in your day-to-day accounts), you can reclaim the VAT already accounted for on Box 4 of your next Making Tax Digital (MTD) VAT return.",
            category = TipCategory.VAT,
            impact = "Recovers 20% Cash on Defaulted Invoices"
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
            title = "Companies House Audit Exemption Limits",
            summary = "Check if your small company qualifies for audit exemption under CA 2006.",
            deepDive = "Under Section 477 of the Companies Act 2006, a small private limited company is exempt from statutory audit if it meets at least 2 of the following: annual turnover not more than £10.2m, balance sheet total not more than £5.1m, and average employees not more than 50.",
            category = TipCategory.PRO,
            impact = "Saves £5,000 - £15,000 in External Audit Fees"
        ),
        AccountancyTip(
            id = 7,
            title = "Segregated HMRC Tax Reservoir Account",
            summary = "Isolate PAYE/NIC and collected Output VAT weekly.",
            deepDive = "Never retain collected VAT or PAYE/National Insurance payroll deductions in your operational account. Sweep 20% of net trade receipts and monthly payroll deductions to an interest-bearing business savings account every Friday so HMRC direct debits never cause overdraft charges.",
            category = TipCategory.TAX,
            impact = "Zero HMRC Surcharge Notice Defaults"
        )
    )

    val cheatSheet = listOf(
        CheatSheetEntry(
            transaction = "Depreciation on Plant & Machinery",
            debit = "Depreciation Charge (Profit & Loss)",
            credit = "Accumulated Depreciation (Balance Sheet)",
            explanation = "Reduces carrying value under FRS 102/105. Disallowed for Corp Tax (replaced by Capital Allowances)."
        ),
        CheatSheetEntry(
            transaction = "VAT Quarter Settlement (HMRC Payment)",
            debit = "HMRC VAT Control Account (Creditors)",
            credit = "Bank Current Account (Cash)",
            explanation = "Clears net liability of Box 5 (Output VAT minus Input VAT) paid via MTD direct debit."
        ),
        CheatSheetEntry(
            transaction = "Customer Advance / Deposit with VAT",
            debit = "Bank Current Account (Cash)",
            credit = "Deferred Income (Creditors) & Output VAT",
            explanation = "Creates a tax point immediately for VAT even though turnover is not yet recognized in P&L."
        ),
        CheatSheetEntry(
            transaction = "Director's Loan Account (DLA) Dividend",
            debit = "Retained Earnings / Equity (Dividends Paid)",
            credit = "Director's Loan Account (Creditors)",
            explanation = "Credits the director after formal board approval, provided sufficient distributable reserves exist."
        ),
        CheatSheetEntry(
            transaction = "PAYE & Employee NIC Monthly Sweep",
            debit = "Gross Wages & Employer's NIC Expense",
            credit = "Net Wages Payable & PAYE/NIC Liability",
            explanation = "Recognizes operational staffing cost and sets up liabilities payable to HMRC by the 22nd."
        )
    )
}

// -------------------------------------------------------------
// UK Financial Analysis & Advisory Engine
// -------------------------------------------------------------
object UKReportAnalysisEngine {

    /**
     * Calculates UK Corporation Tax using current HMRC rate bands:
     * - Profit <= £50k: 19% (Small profits rate)
     * - Profit >= £250k: 25% (Main rate)
     * - £50k - £250k: 25% minus Marginal Relief: (3/400) * (£250,000 - Profit)
     */
    fun calculateCorporationTax(profit: Double): Double {
        if (profit <= 0.0) return 0.0
        return when {
            profit <= 50000.0 -> profit * 0.19
            profit >= 250000.0 -> profit * 0.25
            else -> {
                val fullTax = profit * 0.25
                val marginalRelief = (3.0 / 400.0) * (250000.0 - profit)
                (fullTax - marginalRelief).coerceAtLeast(0.0)
            }
        }
    }

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

        val lines = rawText.lines()
        for (line in lines) {
            val parts = if (line.contains(",")) line.split(",") else line.split(":", "=")
            if (parts.size >= 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].replace("£", "").replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0

                when {
                    key.contains("cash") || key.contains("bank") -> cash = value
                    key.contains("debtor") || key.contains("receivable") || key.contains("ar") -> debtors = value
                    key.contains("stock") || key.contains("inventor") -> stock = value
                    key.contains("current asset") -> curAssets = value
                    key.contains("total asset") -> totalAssets = value
                    key.contains("creditor") && key.contains("within") || key.contains("current liabilit") -> curCreditors = value
                    key.contains("creditor") && key.contains("after") || key.contains("long-term debt") || key.contains("long term") -> ltCreditors = value
                    key.contains("total creditor") || key.contains("total liabilit") -> totalCreditors = value
                    key.contains("shareholder") || key.contains("equity") || key.contains("retained") || key.contains("capital") -> equity = value
                    key.contains("turnover") || key.contains("revenue") || key.contains("sales") -> turnover = value
                    key.contains("profit before tax") || key.contains("net profit") || key.contains("net income") -> netProfit = value
                }
            }
        }

        // Auto-reconciliation fallback for UK FRS formatting
        if (curAssets == 0.0 && (cash > 0 || debtors > 0 || stock > 0)) {
            curAssets = cash + debtors + stock
        }
        if (totalAssets == 0.0 && curAssets > 0) {
            totalAssets = curAssets
        }
        if (totalCreditors == 0.0 && (curCreditors > 0 || ltCreditors > 0)) {
            totalCreditors = curCreditors + ltCreditors
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
                    title = "Companies House Balance Sheet Imbalance",
                    severity = "CRITICAL AUDIT ITEM",
                    color = Color(0xFFF85149),
                    advice = "Total Assets (£%,.0f) do not equal Total Creditors + Shareholder Funds (£%,.0f). Discrepancy is £%,.0f.".format(Locale.UK, report.totalAssets, expectedAssets, discrepancy),
                    actionItem = "Audit unrecorded journal entries, unposted depreciation, or dividends before submitting annual accounts to Companies House."
                )
            )
        }

        // 2. Corporation Tax Liability Advisory
        if (report.netProfitBeforeTax > 0) {
            val estimatedCT = calculateCorporationTax(report.netProfitBeforeTax)
            val effectiveRate = (estimatedCT / report.netProfitBeforeTax) * 100.0
            adviceList.add(
                AdvisoryRecommendation(
                    title = "HMRC Corporation Tax Reserve Estimate",
                    severity = "TAX PLANNING",
                    color = Color(0xFF58A6FF),
                    advice = "Estimated Corp Tax is £%,.0f (Effective rate: %.1f%%). HMRC payment deadline is 9 months and 1 day after accounting period end.".format(Locale.UK, estimatedCT, effectiveRate),
                    actionItem = "Consider utilizing Annual Investment Allowance (AIA) or pension contributions before year-end to reduce taxable profit."
                )
            )
        }

        // 3. Liquidity & Working Capital Warning
        if (report.creditorsDueWithinOneYear > 0) {
            val cr = report.currentAssets / report.creditorsDueWithinOneYear
            if (cr < 1.1) {
                adviceList.add(
                    AdvisoryRecommendation(
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
        if (report.currentAssets > 0 && report.stockInventory > (report.currentAssets * 0.50)) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "Excess Capital Locked in Stock",
                    severity = "MODERATE RISK",
                    color = Color(0xFFD29922),
                    advice = "Stock (£%,.0f) makes up over 50%% of total current assets, restricting business liquidity.".format(Locale.UK, report.stockInventory),
                    actionItem = "Carry out a stock turn analysis. Discount lines aged >90 days to free up cash and reduce insurance/storage overhead."
                )
            )
        }

        // 5. Audit Exemption Assessment
        if (report.turnover > 0 && report.totalAssets > 0) {
            val isExempt = report.turnover <= 10200000.0 && report.totalAssets <= 5100000.0
            if (isExempt) {
                adviceList.add(
                    AdvisoryRecommendation(
                        title = "Eligible for UK Small Company Audit Exemption",
                        severity = "COMPLIANCE BENEFIT",
                        color = Color(0xFF2EA043),
                        advice = "Turnover (£%,.0f) and Total Assets (£%,.0f) sit safely under the £10.2m / £5.1m statutory thresholds.".format(Locale.UK, report.turnover, report.totalAssets),
                        actionItem = "File unaudited FRS 102 Section 1A or FRS 105 Micro-entity accounts with Companies House to save audit costs."
                    )
                )
            }
        }

        return adviceList
    }
}

// -------------------------------------------------------------
// Main Android Activity
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// UI Navigation
// -------------------------------------------------------------
@Composable
fun UKAccountancyAppScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF161B22)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Tips") },
                    label = { Text("UK Tips", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Report Audit") },
                    label = { Text("Balance Audit", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "HMRC Calc") },
                    label = { Text("HMRC Tax", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Double Entry") },
                    label = { Text("Cheatsheet", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D1117))
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> UKTipsView()
                1 -> UKReportImportAuditView()
                2 -> UKTaxCalculatorView()
                3 -> UKCheatsheetView()
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: UK Tips & HMRC Strategy Browser
// -------------------------------------------------------------
@Composable
fun UKTipsView() {
    var selectedCategory by remember { mutableStateOf(TipCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedTipId by remember { mutableStateOf<Int?>(null) }
    var bookmarkedIds by remember { mutableStateOf(setOf<Int>()) }

    val filteredTips = remember(selectedCategory, searchQuery) {
        AccountancyRepository.tips.filter { tip ->
            (selectedCategory == TipCategory.ALL || tip.category == selectedCategory) &&
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
                    text = "LEDGER PRO UK",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "HMRC & Companies House Accounting Hub",
                    fontSize = 12.sp,
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
            placeholder = { Text("Search HMRC, AIA, VAT, reverse charge...", color = Color(0xFF6E7681), fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF8B949E)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF2EA043),
                unfocusedBorderColor = Color(0xFF30363D),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
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
                        selectedContainerColor = Color(0xFF238636),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF161B22),
                        labelColor = Color(0xFFC9D1D9)
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

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isExpanded) Color(0xFF2EA043) else Color(0xFF30363D), RoundedCornerShape(14.dp))
                        .clickable { expandedTipId = if (isExpanded) null else tip.id }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = tip.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFFF0F6FC),
                                modifier = Modifier.weight(1f)
                            )
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
                        Text(text = tip.summary, fontSize = 13.sp, color = Color(0xFF8B949E))

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

// -------------------------------------------------------------
// TAB 2: UK Balance Sheet Audit & Advisory
// -------------------------------------------------------------
@Composable
fun UKReportImportAuditView() {
    val context = LocalContext.current

    // Default UK balance sheet sample in GBP
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

    var report by remember { mutableStateOf(UKReportAnalysisEngine.parseReportText(rawInputText, "UK FRS Sample")) }
    var importedFileName by remember { mutableStateOf("UK FRS Sample") }
    var showManualEdit by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val content = reader.readText()
                    rawInputText = content
                    importedFileName = it.lastPathSegment ?: "Imported UK File"
                    report = UKReportAnalysisEngine.parseReportText(content, importedFileName)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "UK BALANCE SHEET AUDIT",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2EA043),
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "FRS 102/105 Review & HMRC Corporation Tax Advisory",
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF21262D)
                ) {
                    Text(
                        text = importedFileName.take(16),
                        color = Color(0xFF58A6FF),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
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

        // UK Preset Templates
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
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
                            importedFileName = "UK Tech Ltd (Profitable)"
                            report = UKReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
                        },
                        label = { Text("UK Tech Ltd (£180k PBT)", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
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
                            importedFileName = "Distressed UK Retail"
                            report = UKReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
                        },
                        label = { Text("Distressed Retail", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
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
                            importedFileName = "Imbalanced UK Audit"
                            report = UKReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Raw Ledger Input (Key: Value or CSV in £):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF58A6FF)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                rawInputText = it
                                report = UKReportAnalysisEngine.parseReportText(it, "Manual Input")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
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
            Text(
                text = "PARSED BALANCE SHEET METRICS (GBP)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B949E),
                letterSpacing = 1.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Assets:", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.totalAssets), color = Color(0xFF2EA043), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Assets (Bank + Debtors + Stock):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.currentAssets), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Creditors Due (Within & After 1 Year):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.totalCreditors), color = Color(0xFFF85149), fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Shareholder Funds (Capital & Reserves):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.0f", report.shareholderFunds), color = Color(0xFF58A6FF), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Metrics
        items(metrics) { metric ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(metric.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(metric.explanation, color = Color(0xFF8B949E), fontSize = 11.sp, lineHeight = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = metric.status,
                            color = metric.statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
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
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2EA043), modifier = Modifier.size(18.dp))
                Text(
                    text = "UK ADVISORY & STATUTORY COMPLIANCE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.sp
                )
            }
        }

        items(recommendations) { advice ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, advice.color.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = advice.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFF0F6FC),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = advice.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = advice.severity,
                                color = advice.color,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = advice.advice,
                        color = Color(0xFFC9D1D9),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0D1117),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Text(
                                text = "ACTION: ",
                                color = Color(0xFF58A6FF),
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                            Text(
                                text = advice.actionItem,
                                color = Color(0xFFF0F6FC),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: UK Corporation Tax & VAT Calculator
// -------------------------------------------------------------
@Composable
fun UKTaxCalculatorView() {
    var turnoverInput by remember { mutableStateOf("") }
    var allowableExpensesInput by remember { mutableStateOf("") }
    var vatRateSelection by remember { mutableStateOf(20) } // Standard 20%, Reduced 5%, Zero 0%

    val turnover = turnoverInput.toDoubleOrNull() ?: 0.0
    val expenses = allowableExpensesInput.toDoubleOrNull() ?: 0.0
    val taxableProfit = (turnover - expenses).coerceAtLeast(0.0)

    val corpTax = UKReportAnalysisEngine.calculateCorporationTax(taxableProfit)
    val effectiveRate = if (taxableProfit > 0) (corpTax / taxableProfit) * 100.0 else 0.0
    val retainedProfit = taxableProfit - corpTax

    // VAT estimate on turnover
    val estimatedOutputVAT = turnover * (vatRateSelection / 100.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("HMRC CORPORATION TAX & VAT CALCULATOR", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA043))
            Text("Accurate UK Marginal Relief taper (19% - 25%) and VAT estimation.", fontSize = 12.sp, color = Color(0xFF8B949E))
        }

        item {
            OutlinedTextField(
                value = turnoverInput,
                onValueChange = { turnoverInput = it },
                label = { Text("Annual Turnover / Billings (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = allowableExpensesInput,
                onValueChange = { allowableExpensesInput = it },
                label = { Text("Allowable Business Expenses & AIA (£)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Text("VAT Rate Selection:", fontSize = 12.sp, color = Color(0xFF8B949E), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Taxable Trading Profit:", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.2f", taxableProfit), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("UK Corporation Tax (CT600):", color = Color(0xFFF85149), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "-£%,.2f", corpTax), color = Color(0xFFF85149), fontWeight = FontWeight.Bold)
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
                        Text("Retained Distributable Reserves:", color = Color(0xFF2EA043), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(String.format(Locale.UK, "£%,.2f", retainedProfit), color = Color(0xFF2EA043), fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Est. Output VAT (${vatRateSelection}%):", color = Color(0xFFD29922), fontSize = 13.sp)
                        Text(String.format(Locale.UK, "£%,.2f", estimatedOutputVAT), color = Color(0xFFD29922), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFF161B22),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("HMRC Filing Deadlines Note:", fontWeight = FontWeight.Bold, color = Color(0xFF58A6FF), fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• Corporation Tax payment is due 9 months and 1 day following your accounting period end.\n" +
                        "• Company Tax Return (CT600) is due 12 months after your period end.\n" +
                        "• VAT Returns & payments under MTD are due 1 calendar month and 7 days after the quarter end.",
                        fontSize = 11.sp,
                        color = Color(0xFF8B949E),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: UK Double-Entry Cheatsheet (FRS 102/105)
// -------------------------------------------------------------
@Composable
fun UKCheatsheetView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("UK DOUBLE-ENTRY CHEATSHEET", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA043))
        Text("Standard journal entries conforming to UK FRS 102 / FRS 105 principles.", fontSize = 12.sp, color = Color(0xFF8B949E))
        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(AccountancyRepository.cheatSheet) { entry ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
