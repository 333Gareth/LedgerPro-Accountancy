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

// -------------------------------------------------------------
// Models
// -------------------------------------------------------------
enum class TipCategory(val title: String, val icon: ImageVector) {
    ALL("All", Icons.Default.AllInclusive),
    TAX("Tax Strategy", Icons.Default.ReceiptLong),
    BOOKKEEPING("Bookkeeping", Icons.Default.MenuBook),
    EXCEL("Excel Hacks", Icons.Default.GridOn),
    AUDIT("Audit & Compliance", Icons.Default.FactCheck),
    PRO("Career & Firm", Icons.Default.TrendingUp)
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
    val cash: Double = 0.0,
    val accountsReceivable: Double = 0.0,
    val inventory: Double = 0.0,
    val currentAssets: Double = 0.0,
    val totalAssets: Double = 0.0,
    val currentLiabilities: Double = 0.0,
    val longTermDebt: Double = 0.0,
    val totalLiabilities: Double = 0.0,
    val totalEquity: Double = 0.0,
    val revenue: Double = 0.0,
    val netIncome: Double = 0.0,
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
// Sample Knowledge Base
// -------------------------------------------------------------
object AccountancyRepository {
    val tips = listOf(
        AccountancyTip(
            id = 1,
            title = "Section 179 & Capital Allowances Acceleration",
            summary = "Write off 100% of qualified business equipment in the year acquired.",
            deepDive = "Rather than depreciating computers, software, vehicles, or machinery over 3 to 7 years, leverage upfront full-expensing provisions. Ensure the equipment is placed in service before fiscal year-end, and cross-check state/provincial conformance.",
            category = TipCategory.TAX,
            impact = "Massive Year-1 Cash Flow Shield"
        ),
        AccountancyTip(
            id = 2,
            title = "The Golden Rule of Index-Match / XLOOKUP",
            summary = "Ditch VLOOKUP for XLOOKUP to prevent broken reconciliations.",
            deepDive = "VLOOKUP breaks whenever a new column is inserted into a ledger. XLOOKUP is non-positional, looks left or right, and defaults to exact match. Formula syntax: =XLOOKUP(lookup_value, lookup_array, return_array, \"Not Found\").",
            category = TipCategory.EXCEL,
            impact = "Eliminates Silent Formula Corruption"
        ),
        AccountancyTip(
            id = 3,
            title = "Three-Way Match Verification",
            summary = "Prevent fraud and overpayment by enforcing PO = Receiving Slip = Invoice.",
            deepDive = "Never approve vendor disbursements based solely on an invoice. Ensure line-item quantity matches the warehouse bill of lading (receiving doc) and unit pricing matches the original approved Purchase Order (PO).",
            category = TipCategory.AUDIT,
            impact = "Stops Phantom Invoicing & Double Billing"
        ),
        AccountancyTip(
            id = 4,
            title = "Deadweight Accrual Flush",
            summary = "Reconcile trailing unbilled accruals at every quarter close.",
            deepDive = "Stale accrued expense balances artificially depress current period earnings and distort EBITDA. Always implement an automatic reversal date or mandate documentation before rolling accruals beyond 90 days.",
            category = TipCategory.BOOKKEEPING,
            impact = "Clean EBITDA & True Balance Sheet Health"
        ),
        AccountancyTip(
            id = 5,
            title = "Dynamic Excel SUMIFS with EOMONTH",
            summary = "Automate rolling monthly general ledger summaries without manual edits.",
            deepDive = "Combine SUMIFS with =EOMONTH(Date, 0) to aggregate journal entries into neat monthly columns without creating brittle pivot tables or retyping dates every close cycle.",
            category = TipCategory.EXCEL,
            impact = "Saves 4+ Hours on Monthly Close"
        ),
        AccountancyTip(
            id = 6,
            title = "Pre-Close Cut-Off Review",
            summary = "Scrutinize invoices dated within 5 days before and after month-end.",
            deepDive = "Auditors immediately check revenue and expense cut-off. Ensure billings belong strictly to the performance obligation period, preventing unearned revenue recognition errors.",
            category = TipCategory.AUDIT,
            impact = "Prevents Primary Year-End Audit Adjustments"
        ),
        AccountancyTip(
            id = 7,
            title = "Separate Corporate Cash Reservoirs",
            summary = "Implement the Sub-Account allocation method for VAT / Sales Tax.",
            deepDive = "Never leave collected sales tax or payroll withholdings in operational checking. Sweep tax liabilities into a segregated high-yield holding account every Friday so tax day is never a liquidity crisis.",
            category = TipCategory.TAX,
            impact = "Zero Tax-Day Liquidity Shortfalls"
        )
    )

    val cheatSheet = listOf(
        CheatSheetEntry(
            transaction = "Depreciation Expense",
            debit = "Depreciation Expense (Income Stmt)",
            credit = "Accumulated Depreciation (Contra Asset)",
            explanation = "Reduces asset carrying value without directly touching cash."
        ),
        CheatSheetEntry(
            transaction = "Prepaid Insurance Usage",
            debit = "Insurance Expense",
            credit = "Prepaid Insurance (Asset)",
            explanation = "Recognizes monthly consumption of upfront annual premiums."
        ),
        CheatSheetEntry(
            transaction = "Customer Advance / Deposit",
            debit = "Cash / Bank (Asset)",
            credit = "Unearned / Deferred Revenue (Liability)",
            explanation = "Cash arrived, but obligation is not yet earned."
        ),
        CheatSheetEntry(
            transaction = "Bad Debt Write-Off (Allowance Method)",
            debit = "Allowance for Doubtful Accounts",
            credit = "Accounts Receivable",
            explanation = "Cleans customer balance without hitting current period expense."
        )
    )
}

// -------------------------------------------------------------
// Financial Report Parser & Advisory Engine
// -------------------------------------------------------------
object ReportAnalysisEngine {

    fun parseReportText(rawText: String, sourceName: String = "Manual Import"): BalanceSheetReport {
        var cash = 0.0
        var ar = 0.0
        var inv = 0.0
        var curAssets = 0.0
        var totalAssets = 0.0
        var curLiab = 0.0
        var ltDebt = 0.0
        var totalLiab = 0.0
        var totalEquity = 0.0
        var revenue = 0.0
        var netIncome = 0.0

        val lines = rawText.lines()
        for (line in lines) {
            val parts = if (line.contains(",")) line.split(",") else line.split(":", "=")
            if (parts.size >= 2) {
                val key = parts[0].trim().lowercase()
                val value = parts[1].replace("$", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0

                when {
                    key.contains("cash") -> cash = value
                    key.contains("receivable") || key.contains("ar") -> ar = value
                    key.contains("inventory") || key.contains("stock") -> inv = value
                    key.contains("current asset") -> curAssets = value
                    key.contains("total asset") -> totalAssets = value
                    key.contains("current liabilit") -> curLiab = value
                    key.contains("long term debt") || key.contains("long-term debt") -> ltDebt = value
                    key.contains("total liabilit") -> totalLiab = value
                    key.contains("equity") || key.contains("retained") -> totalEquity = value
                    key.contains("revenue") || key.contains("sales") || key.contains("turnover") -> revenue = value
                    key.contains("net income") || key.contains("net profit") -> netIncome = value
                }
            }
        }

        // Auto-reconcile fallback calculations if line totals were omitted
        if (curAssets == 0.0 && (cash > 0 || ar > 0 || inv > 0)) {
            curAssets = cash + ar + inv
        }
        if (totalAssets == 0.0 && curAssets > 0) {
            totalAssets = curAssets
        }
        if (totalLiab == 0.0 && (curLiab > 0 || ltDebt > 0)) {
            totalLiab = curLiab + ltDebt
        }

        return BalanceSheetReport(
            cash = cash,
            accountsReceivable = ar,
            inventory = inv,
            currentAssets = curAssets,
            totalAssets = totalAssets,
            currentLiabilities = curLiab,
            longTermDebt = ltDebt,
            totalLiabilities = totalLiab,
            totalEquity = totalEquity,
            revenue = revenue,
            netIncome = netIncome,
            rawFileName = sourceName
        )
    }

    fun computeMetrics(report: BalanceSheetReport): List<FinancialHealthMetric> {
        val list = mutableListOf<FinancialHealthMetric>()

        // 1. Current Ratio (Liquidity)
        if (report.currentLiabilities > 0) {
            val cr = report.currentAssets / report.currentLiabilities
            val (status, color, exp) = when {
                cr >= 1.5 && cr <= 2.8 -> Triple("Optimal Liquidity", Color(0xFF2EA043), "Solid capability to pay short-term obligations.")
                cr in 1.0..1.5 -> Triple("Fair / Tight", Color(0xFFD29922), "Able to cover debts, but sensitive to collection delays.")
                cr < 1.0 -> Triple("Insolvent Warning", Color(0xFFF85149), "Current obligations exceed liquid assets! Cash crunch alert.")
                else -> Triple("Idle Working Capital", Color(0xFF58A6FF), "Very high ratio (>2.8). Cash may be unproductively trapped.")
            }
            list.add(FinancialHealthMetric("Current Ratio", String.format(Locale.US, "%.2fx", cr), status, color, exp))
        }

        // 2. Debt-to-Equity (Solvency & Financial Leverage)
        if (report.totalEquity > 0) {
            val de = report.totalLiabilities / report.totalEquity
            val (status, color, exp) = when {
                de <= 1.2 -> Triple("Conservative Leverage", Color(0xFF2EA043), "Healthy debt load funded primarily through equity.")
                de <= 2.2 -> Triple("Moderate Leverage", Color(0xFFD29922), "Typical corporate structure, maintain monitor on interest cover.")
                else -> Triple("Highly Leveraged", Color(0xFFF85149), "Heavy reliance on debt; elevates risk during rate hikes.")
            }
            list.add(FinancialHealthMetric("Debt-to-Equity", String.format(Locale.US, "%.2fx", de), status, color, exp))
        }

        // 3. Net Profit Margin
        if (report.revenue > 0) {
            val npm = (report.netIncome / report.revenue) * 100.0
            val (status, color, exp) = when {
                npm >= 15.0 -> Triple("Superior Margin", Color(0xFF2EA043), "High bottom-line profitability and pricing power.")
                npm in 5.0..15.0 -> Triple("Standard Commercial", Color(0xFF58A6FF), "Healthy corporate operating profit margin.")
                npm in 0.0..5.0 -> Triple("Thin Margins", Color(0xFFD29922), "Vulnerable to cost surges or supplier price increases.")
                else -> Triple("Operating At Loss", Color(0xFFF85149), "Negative profit; draining cash reserves.")
            }
            list.add(FinancialHealthMetric("Net Margin", String.format(Locale.US, "%.1f%%", npm), status, color, exp))
        }

        // 4. Working Capital
        val wc = report.currentAssets - report.currentLiabilities
        val (status, color, exp) = when {
            wc > 0 -> Triple("Positive Buffer", Color(0xFF2EA043), "Surplus liquid assets available to deploy.")
            wc == 0.0 -> Triple("Neutral", Color(0xFF8B949E), "No margin for operational error.")
            else -> Triple("Negative Working Capital", Color(0xFFF85149), "Immediate operational liability deficit.")
        }
        list.add(FinancialHealthMetric("Net Working Capital", String.format(Locale.US, "$%,.0f", wc), status, color, exp))

        return list
    }

    fun generateAdvisory(report: BalanceSheetReport): List<AdvisoryRecommendation> {
        val adviceList = mutableListOf<AdvisoryRecommendation>()

        // Check Balance Sheet Equation: Assets = Liabilities + Equity
        val expectedAssets = report.totalLiabilities + report.totalEquity
        val discrepancy = report.totalAssets - expectedAssets
        if (report.totalAssets > 0 && Math.abs(discrepancy) > 1.0) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "Fundamental Ledger Imbalance Detected",
                    severity = "CRITICAL AUDIT ITEM",
                    color = Color(0xFFF85149),
                    advice = "Assets ($%,.0f) do not balance with Total Liabilities + Equity ($%,.0f). Discrepancy is $%,.0f.".format(Locale.US, report.totalAssets, expectedAssets, discrepancy),
                    actionItem = "Audit unrecorded journal entries, unposted depreciation, or dividends before reporting to external stakeholders."
                )
            )
        }

        // Liquidity advisory
        if (report.currentLiabilities > 0) {
            val cr = report.currentAssets / report.currentLiabilities
            if (cr < 1.1) {
                adviceList.add(
                    AdvisoryRecommendation(
                        title = "Emergency Cash-Flow & Liquidity Shield",
                        severity = "HIGH RISK",
                        color = Color(0xFFF85149),
                        advice = "Current liabilities are near or exceeding quick assets. A single slow-paying debtor could provoke a payroll default.",
                        actionItem = "Offer 2/10 net 30 prompt payment discounts on Accounts Receivable ($%,.0f) to accelerate collections, and delay discretionary CapEx.".format(Locale.US, report.accountsReceivable)
                    )
                )
            } else if (cr > 3.0 && report.cash > (report.currentLiabilities * 1.5)) {
                adviceList.add(
                    AdvisoryRecommendation(
                        title = "Cash Drag / Inflation Optimization",
                        severity = "STRATEGY TIP",
                        color = Color(0xFF58A6FF),
                        advice = "High volume of unallocated cash ($%,.0f) is losing purchasing power to inflation while yielding minimal return.".format(Locale.US, report.cash),
                        actionItem = "Sweep 40% of dormant operational cash into high-yield 30-day Treasury Bills or automated interest-bearing overnight repurchase sweeps."
                    )
                )
            }
        }

        // Inventory Risk Advisory
        if (report.currentAssets > 0 && report.inventory > (report.currentAssets * 0.55)) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "Excessive Working Capital Trapped in Stock",
                    severity = "MODERATE RISK",
                    color = Color(0xFFD29922),
                    advice = "Inventory ($%,.0f) comprises over 55%% of current assets. Elevated exposure to obsolescence and storage overhead.".format(Locale.US, report.inventory),
                    actionItem = "Conduct an aged inventory audit (FIFO analysis). Discount stock stagnant over 120 days to free liquid capital."
                )
            )
        }

        // High Leverage Advisory
        if (report.totalEquity > 0 && (report.totalLiabilities / report.totalEquity) > 2.0) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "De-Leveraging & Debt Service Optimization",
                    severity = "FINANCIAL RISK",
                    color = Color(0xFFD29922),
                    advice = "Debt-to-Equity is elevated. Future borrowing capacity will be penalized with higher risk premiums from lenders.",
                    actionItem = "Prioritize paying down floating-rate credit facilities before fixed term debt, or evaluate converting partner debt to preferred equity."
                )
            )
        }

        // Tax Shield Suggestion if Profitable
        if (report.netIncome > 50000.0) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "Year-End Net Earnings Tax Defense",
                    severity = "TAX SAVINGS",
                    color = Color(0xFF2EA043),
                    advice = "Healthy net earnings ($%,.0f) will incur significant corporate tax liabilities unless strategic deductions are triggered.".format(Locale.US, report.netIncome),
                    actionItem = "Review pending CapEx hardware/software requisitions for Section 179 accelerated depreciation before the close of the current fiscal period."
                )
            )
        }

        // Fallback default tip if report looks very healthy
        if (adviceList.isEmpty()) {
            adviceList.add(
                AdvisoryRecommendation(
                    title = "Sound Financial Health Detected",
                    severity = "EXCELLENT",
                    color = Color(0xFF2EA043),
                    advice = "The balance sheet exhibits balanced liquidity ratios, controlled debt covenants, and positive operating equity.",
                    actionItem = "Maintain the current quarterly review cadence and consider establishing an automated three-way matching AP process."
                )
            )
        }

        return adviceList
    }
}

// -------------------------------------------------------------
// Main Activity
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
                AccountancyAppScreen()
            }
        }
    }
}

// -------------------------------------------------------------
// UI Layout & Navigation
// -------------------------------------------------------------
@Composable
fun AccountancyAppScreen() {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF161B22)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Lightbulb, contentDescription = "Tips") },
                    label = { Text("Tips & Hacks", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Report Audit") },
                    label = { Text("Report Audit", fontSize = 11.sp) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculators") },
                    label = { Text("Tax Calc", fontSize = 11.sp) }
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
                0 -> TipsView()
                1 -> ReportImportAuditView()
                2 -> FinancialCalculatorView()
                3 -> CheatsheetView()
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: Balance Sheet Import & AI-Style Advisory View
// -------------------------------------------------------------
@Composable
fun ReportImportAuditView() {
    val context = LocalContext.current

    // Sample default data
    var rawInputText by remember {
        mutableStateOf(
            """
Cash: 45,000
Accounts Receivable: 68,000
Inventory: 32,000
Current Assets: 145,000
Total Assets: 280,000
Current Liabilities: 92,000
Long-Term Debt: 60,000
Total Liabilities: 152,000
Total Equity: 128,000
Revenue: 420,000
Net Income: 48,000
            """.trimIndent()
        )
    }

    var report by remember { mutableStateOf(ReportAnalysisEngine.parseReportText(rawInputText, "Default Ledger Sample")) }
    var importedFileName by remember { mutableStateOf("Default Ledger Sample") }
    var showManualEdit by remember { mutableStateOf(false) }

    // File picker launcher for CSV or text financial statements
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val content = reader.readText()
                    rawInputText = content
                    importedFileName = it.lastPathSegment ?: "Imported File"
                    report = ReportAnalysisEngine.parseReportText(content, importedFileName)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val metrics = remember(report) { ReportAnalysisEngine.computeMetrics(report) }
    val recommendations = remember(report) { ReportAnalysisEngine.generateAdvisory(report) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BALANCE SHEET AUDIT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2EA043),
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Import Reports & Instant CPA Advisory",
                        fontSize = 12.sp,
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

        // Import & Template Action Buttons
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

        // Quick Preset Templates to test
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
Cash: 120,000
Accounts Receivable: 45,000
Inventory: 0
Current Assets: 165,000
Total Assets: 210,000
Current Liabilities: 30,000
Long-Term Debt: 15,000
Total Liabilities: 45,000
Total Equity: 165,000
Revenue: 600,000
Net Income: 140,000
                            """.trimIndent()
                            importedFileName = "SaaS Healthy Template"
                            report = ReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
                        },
                        label = { Text("Healthy SaaS", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
Cash: 12,000
Accounts Receivable: 85,000
Inventory: 140,000
Current Assets: 237,000
Total Assets: 350,000
Current Liabilities: 240,000
Long-Term Debt: 90,000
Total Liabilities: 330,000
Total Equity: 20,000
Revenue: 520,000
Net Income: -18,000
                            """.trimIndent()
                            importedFileName = "Distressed Retail"
                            report = ReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
                        },
                        label = { Text("Distressed Retail", fontSize = 11.sp) }
                    )
                }
                item {
                    AssistChip(
                        onClick = {
                            rawInputText = """
Cash: 50,000
Accounts Receivable: 40,000
Inventory: 60,000
Current Assets: 150,000
Total Assets: 300,000
Current Liabilities: 100,000
Long-Term Debt: 50,000
Total Liabilities: 150,000
Total Equity: 110,000
Revenue: 300,000
Net Income: 25,000
                            """.trimIndent()
                            importedFileName = "Unbalanced Ledger"
                            report = ReportAnalysisEngine.parseReportText(rawInputText, importedFileName)
                        },
                        label = { Text("Imbalanced Audit", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Expandable Raw Report Editor
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
                            text = "Raw Report Input (Key: Value or CSV):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF58A6FF)
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                rawInputText = it
                                report = ReportAnalysisEngine.parseReportText(it, "Manual Input")
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

        // Parsed Balance Sheet Summary Cards
        item {
            Text(
                text = "PARSED BALANCE SHEET METRICS",
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
                        Text(String.format(Locale.US, "$%,.0f", report.totalAssets), color = Color(0xFF2EA043), fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current Assets (Cash + AR + Inv):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.US, "$%,.0f", report.currentAssets), color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Liabilities (Curr + LT Debt):", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.US, "$%,.0f", report.totalLiabilities), color = Color(0xFFF85149), fontWeight = FontWeight.SemiBold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Stockholder / Owner Equity:", color = Color(0xFF8B949E), fontSize = 13.sp)
                        Text(String.format(Locale.US, "$%,.0f", report.totalEquity), color = Color(0xFF58A6FF), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Financial Health Ratios (Current Ratio, Debt to Equity, Margins)
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

        // Automated CPA Advice & Strategic Recommendations
        item {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2EA043), modifier = Modifier.size(18.dp))
                Text(
                    text = "ADVISORY & ACTIONABLE INTELLIGENCE",
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
// TAB 1: Tips & Tricks Browser
// -------------------------------------------------------------
@Composable
fun TipsView() {
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
        // App Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LEDGER PRO",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2EA043),
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Accountancy & Tax Intelligence",
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

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tax, formulas, audit...", color = Color(0xFF6E7681), fontSize = 13.sp) },
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

        // Category Pills
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

        // Tips List
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
                                    text = "Deep Dive & Execution:",
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
// TAB 3: Quick Tax & Margin Calculator
// -------------------------------------------------------------
@Composable
fun FinancialCalculatorView() {
    var grossRevenue by remember { mutableStateOf("") }
    var expenses by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf("20") }

    val rev = grossRevenue.toDoubleOrNull() ?: 0.0
    val exp = expenses.toDoubleOrNull() ?: 0.0
    val rate = (taxRate.toDoubleOrNull() ?: 0.0) / 100.0

    val netProfitBeforeTax = (rev - exp).coerceAtLeast(0.0)
    val estimatedTax = netProfitBeforeTax * rate
    val netProfitAfterTax = netProfitBeforeTax - estimatedTax
    val margin = if (rev > 0) (netProfitAfterTax / rev) * 100.0 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("TAX & MARGIN AUDITOR", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA043))
        Text("Estimate your net liability and real profit margin in real-time.", fontSize = 12.sp, color = Color(0xFF8B949E))

        OutlinedTextField(
            value = grossRevenue,
            onValueChange = { grossRevenue = it },
            label = { Text("Gross Revenue / Billings ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = expenses,
            onValueChange = { expenses = it },
            label = { Text("Deductible Business Expenses ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        OutlinedTextField(
            value = taxRate,
            onValueChange = { taxRate = it },
            label = { Text("Estimated Tax Bracket (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF30363D), RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pre-Tax Profit:", color = Color(0xFF8B949E), fontSize = 13.sp)
                    Text(String.format(Locale.US, "$%,.2f", netProfitBeforeTax), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Tax Liability:", color = Color(0xFFF85149), fontSize = 13.sp)
                    Text(String.format(Locale.US, "-$%,.2f", estimatedTax), color = Color(0xFFF85149), fontWeight = FontWeight.Bold)
                }
                Divider(color = Color(0xFF30363D))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Retained Earnings:", color = Color(0xFF2EA043), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(String.format(Locale.US, "$%,.2f", netProfitAfterTax), color = Color(0xFF2EA043), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Profit Margin:", color = Color(0xFF58A6FF), fontSize = 13.sp)
                    Text(String.format(Locale.US, "%.1f%%", margin), color = Color(0xFF58A6FF), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 4: Double-Entry Cheatsheet
// -------------------------------------------------------------
@Composable
fun CheatsheetView() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("DOUBLE-ENTRY CHEATSHEET", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF2EA043))
        Text("Standard journal entry guide for tricky adjustments.", fontSize = 12.sp, color = Color(0xFF8B949E))
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
