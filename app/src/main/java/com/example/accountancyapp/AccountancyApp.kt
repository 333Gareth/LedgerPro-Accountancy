package com.example.accountancyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    label = { Text("Tips & Hacks") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculators") },
                    label = { Text("Tax Calc") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Double Entry") },
                    label = { Text("Cheatsheet") }
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
                1 -> FinancialCalculatorView()
                2 -> CheatsheetView()
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
// TAB 2: Quick Tax & Margin Calculator
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
// TAB 3: Double-Entry Cheatsheet
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
