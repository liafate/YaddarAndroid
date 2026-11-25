package ir.yaddar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import ir.yaddar.app.data.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import java.util.Locale

private val Teal = Color(0xFF167D76)
private val Background = Color(0xFFF5F7F6)
private val Muted = Color(0xFF667085)
private const val CUSTOMER_KIND = "CUSTOMER"
private const val EVENT_CUSTOMER_DEBT = "CUSTOMER_DEBT"
private const val EVENT_CUSTOMER_PAYMENT = "CUSTOMER_PAYMENT"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, YaddarDatabase::class.java, "yaddar.db")
            .addMigrations(YaddarDatabase.MIGRATION_1_2)
            .build()
        setContent { YaddarApp(db) }
    }
}

@Composable
fun YaddarApp(db: YaddarDatabase) {
    var selected by remember { mutableStateOf("خانه") }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Background,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    listOf("خانه", "کالا", "اشخاص", "گزارش", "بیشتر").forEach { label ->
                        NavigationBarItem(
                            selected = selected == label,
                            onClick = { selected = label },
                            label = { Text(label, fontSize = 11.sp) },
                            icon = {
                                Icon(
                                    when (label) {
                                        "خانه" -> Icons.Default.Home
                                        "کالا" -> Icons.Default.Inventory2
                                        "اشخاص" -> Icons.Default.People
                                        "گزارش" -> Icons.Default.BarChart
                                        else -> Icons.Default.Menu
                                    },
                                    contentDescription = label
                                )
                            }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                "خانه" -> Dashboard(
                    modifier = Modifier.padding(padding),
                    onCustomers = { selected = "اشخاص" }
                )
                "اشخاص" -> CustomerBook(db, Modifier.padding(padding))
                else -> ComingSoon(selected, Modifier.padding(padding))
            }
        }
    }
}

/* صفحهٔ اصلی با همان چیدمان نسخهٔ قبلی */
@Composable
private fun Dashboard(
    modifier: Modifier = Modifier,
    onCustomers: () -> Unit
) {
    Column(
        modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 24.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("یاددار", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
            Text("مدیریت ساده و روزانه مغازه", color = Muted)
        }

        Surface(shape = RoundedCornerShape(22.dp), color = Teal, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("سلام، روز بخیر", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("خلاصه وضعیت مغازه", color = Color.White.copy(alpha = .85f))
                Spacer(Modifier.height(6.dp))
                Text(
                    "اطلاعات واقعی پس از تکمیل ماژول‌ها در این بخش نمایش داده خواهد شد.",
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("فروش امروز", "—", Modifier.weight(1f))
            SummaryCard("دریافت امروز", "—", Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("طلب از مشتری", "—", Modifier.weight(1f), Color(0xFFB7791F))
            SummaryCard("بدهی به فروشنده", "—", Modifier.weight(1f), Color(0xFFB7791F))
        }

        Text("عملیات سریع", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("فروش جدید", Icons.Default.ShoppingCart, Modifier.weight(1f))
            ActionTile("ثبت خرید", Icons.Default.AddBox, Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionTile("مشتریان", Icons.Default.People, Modifier.weight(1f), onClick = onCustomers)
            ActionTile(
                "دریافت / پرداخت",
                Icons.Default.AccountBalanceWallet,
                Modifier.weight(1f),
                onClick = onCustomers
            )
        }

        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = "یادآوری‌ها", tint = Color(0xFFB7791F))
                Column {
                    Text("یادآوری‌ها", fontWeight = FontWeight.Bold)
                    Text(
                        "هشدارهای موجودی و حساب‌ها در نسخه‌های بعدی فعال می‌شوند.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Teal
) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Muted, fontSize = 13.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = accent)
        }
    }
}

@Composable
private fun ActionTile(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val tileModifier = if (onClick == null) modifier.height(100.dp)
        else modifier.height(100.dp).clickable(onClick = onClick)

    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = tileModifier) {
        Column(
            Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = Teal, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

private data class CustomerSummary(val person: PersonEntity, val balance: Long)

private fun calculateCustomerBalance(events: List<FinancialEventEntity>): Long =
    events.fold(0L) { balance, event ->
        when (event.eventType) {
            EVENT_CUSTOMER_DEBT -> balance + event.amount
            EVENT_CUSTOMER_PAYMENT -> balance - event.amount
            else -> balance
        }
    }

private fun moneyText(amount: Long): String = String.format(Locale.US, "%,d", amount)

private fun dateText(timestamp: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale("fa", "IR"))
        .format(Date(timestamp))

private fun asciiDigits(value: String): String = buildString {
    value.forEach { ch ->
        append(
            when (ch) {
                in '۰'..'۹' -> '0' + (ch - '۰')
                in '٠'..'٩' -> '0' + (ch - '٠')
                else -> ch
            }
        )
    }
}

@Composable
private fun CustomerBook(db: YaddarDatabase, modifier: Modifier = Modifier) {
    val dao = remember(db) { db.yaddarDao() }
    val scope = rememberCoroutineScope()
    var customers by remember { mutableStateOf<List<CustomerSummary>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf("") }
    var reloadKey by remember { mutableIntStateOf(0) }
    var selectedCustomer by remember { mutableStateOf<PersonEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var savingCustomer by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf("") }

    LaunchedEffect(reloadKey) {
        loading = true
        loadError = ""
        try {
            val people = dao.getPeopleByKind(CUSTOMER_KIND)
            customers = people.map { person ->
                CustomerSummary(person, calculateCustomerBalance(dao.getCustomerLedgerEventsForPerson(person.id)))
            }
        } catch (e: Exception) {
            loadError = "خواندن فهرست مشتریان انجام نشد: ${e.message ?: "خطای نامشخص"}"
        }
        loading = false
    }

    if (selectedCustomer != null) {
        CustomerLedger(
            db = db,
            customer = selectedCustomer!!,
            modifier = modifier,
            onBack = { selectedCustomer = null },
            onChanged = { reloadKey++ }
        )
        return
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("دفتر حساب مشتریان", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Teal)
                Text("مانده و گردش حساب هر مشتری", color = Muted, fontSize = 13.sp)
            }
            Icon(Icons.Default.People, contentDescription = null, tint = Teal, modifier = Modifier.size(30.dp))
        }

        Button(
            onClick = {
                customerName = ""
                customerPhone = ""
                formError = ""
                showAddDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("افزودن مشتری جدید")
        }

        if (loadError.isNotBlank()) {
            Text(loadError, color = MaterialTheme.colorScheme.error)
        } else if (loading) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
        } else if (customers.isEmpty()) {
            Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Teal, modifier = Modifier.size(36.dp))
                    Text("هنوز مشتری‌ای ثبت نشده است.", fontWeight = FontWeight.Bold)
                    Text("از دکمهٔ بالا اولین مشتری را اضافه کنید.", color = Muted, fontSize = 13.sp)
                }
            }
        } else {
            customers.forEach { summary -> CustomerCard(summary) { selectedCustomer = summary.person } }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { if (!savingCustomer) showAddDialog = false },
            title = { Text("افزودن مشتری") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it; formError = "" },
                        label = { Text("نام مشتری") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("شماره تماس (اختیاری)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (formError.isNotBlank()) {
                        Text(formError, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(enabled = !savingCustomer, onClick = {
                    if (customerName.isBlank()) {
                        formError = "نام مشتری را وارد کنید."
                    } else {
                        savingCustomer = true
                        scope.launch {
                            try {
                                dao.insertPerson(
                                    PersonEntity(
                                        name = customerName.trim(),
                                        phone = customerPhone.trim().ifBlank { null },
                                        kind = CUSTOMER_KIND
                                    )
                                )
                                showAddDialog = false
                                reloadKey++
                            } catch (e: Exception) {
                                formError = "ذخیره انجام نشد: ${e.message ?: "خطای نامشخص"}"
                            }
                            savingCustomer = false
                        }
                    }
                }) { Text(if (savingCustomer) "در حال ذخیره..." else "ذخیره") }
            },
            dismissButton = {
                TextButton(enabled = !savingCustomer, onClick = { showAddDialog = false }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun CustomerCard(summary: CustomerSummary, onClick: () -> Unit) {
    val balanceColor = when {
        summary.balance > 0 -> Color(0xFFB7791F)
        summary.balance < 0 -> Teal
        else -> Muted
    }
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(summary.person.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!summary.person.phone.isNullOrBlank()) {
                    Text(summary.person.phone.orEmpty(), color = Muted, fontSize = 12.sp)
                }
                Text(
                    when {
                        summary.balance > 0 -> "بدهکار به شما"
                        summary.balance < 0 -> "بستانکار از شما"
                        else -> "تسویه"
                    },
                    color = balanceColor,
                    fontSize = 12.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(moneyText(kotlin.math.abs(summary.balance)), fontWeight = FontWeight.Bold, color = balanceColor)
                Text("تومان", color = Muted, fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronLeft, contentDescription = "نمایش حساب", tint = Muted)
        }
    }
}

@Composable
private fun CustomerLedger(
    db: YaddarDatabase,
    customer: PersonEntity,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onChanged: () -> Unit
) {
    val dao = remember(db) { db.yaddarDao() }
    val scope = rememberCoroutineScope()
    var events by remember(customer.id) { mutableStateOf<List<FinancialEventEntity>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableIntStateOf(0) }
    var entryType by remember { mutableStateOf(EVENT_CUSTOMER_DEBT) }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(customer.id, reloadKey) {
        loading = true
        try {
            events = dao.getCustomerLedgerEventsForPerson(customer.id)
            message = ""
        } catch (e: Exception) {
            message = "خواندن گردش حساب انجام نشد: ${e.message ?: "خطای نامشخص"}"
        }
        loading = false
    }

    val balance = calculateCustomerBalance(events)
    val balanceColor = when {
        balance > 0 -> Color(0xFFB7791F)
        balance < 0 -> Teal
        else -> Muted
    }

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowForward, contentDescription = "بازگشت") }
            Column {
                Text(customer.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Teal)
                Text("دفتر گردش حساب مشتری", color = Muted, fontSize = 12.sp)
            }
        }

        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    when {
                        balance > 0 -> "مانده بدهی مشتری"
                        balance < 0 -> "مانده بستانکاری مشتری"
                        else -> "حساب تسویه است"
                    },
                    color = Muted,
                    fontSize = 13.sp
                )
                Text(
                    "${moneyText(kotlin.math.abs(balance))} تومان",
                    color = balanceColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                if (balance < 0) Text("این مبلغ به نفع مشتری است.", color = Muted, fontSize = 12.sp)
            }
        }

        Text("ثبت رویداد مالی", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = entryType == EVENT_CUSTOMER_DEBT,
                onClick = { entryType = EVENT_CUSTOMER_DEBT; message = "" },
                label = { Text("ثبت بدهی جدید") },
                leadingIcon = { if (entryType == EVENT_CUSTOMER_DEBT) Icon(Icons.Default.Check, contentDescription = null) }
            )
            FilterChip(
                selected = entryType == EVENT_CUSTOMER_PAYMENT,
                onClick = { entryType = EVENT_CUSTOMER_PAYMENT; message = "" },
                label = { Text("ثبت دریافت") },
                leadingIcon = { if (entryType == EVENT_CUSTOMER_PAYMENT) Icon(Icons.Default.Check, contentDescription = null) }
            )
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = asciiDigits(it).filter(Char::isDigit) },
            label = { Text("مبلغ (تومان)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("شرح / توضیحات (اختیاری)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val amount = amountText.toLongOrNull()
                if (amount == null || amount <= 0L) {
                    message = "مبلغ معتبر و بزرگ‌تر از صفر وارد کنید."
                } else {
                    saving = true
                    message = ""
                    scope.launch {
                        try {
                            dao.insertFinancialEvent(
                                FinancialEventEntity(
                                    eventType = entryType,
                                    amount = amount,
                                    personId = customer.id,
                                    timestamp = System.currentTimeMillis(),
                                    note = noteText.trim().ifBlank { null }
                                )
                            )
                            amountText = ""
                            noteText = ""
                            reloadKey++
                            onChanged()
                            message = "اطلاعات با موفقیت ثبت شد."
                        } catch (e: Exception) {
                            message = "ذخیره انجام نشد: ${e.message ?: "خطای نامشخص"}"
                        }
                        saving = false
                    }
                }
            },
            enabled = !saving,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Teal)
        ) {
            if (saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
            else Icon(Icons.Default.Save, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (saving) "در حال ذخیره..." else "ثبت در دفتر حساب")
        }
        if (message.isNotBlank()) {
            Text(
                message,
                color = if (message.startsWith("اطلاعات")) Teal else MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }

        HorizontalDivider()
        Text("تاریخچه حساب", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        if (loading) {
            Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Teal)
            }
        } else if (events.isEmpty()) {
            Text("هنوز رویدادی برای این مشتری ثبت نشده است.", color = Muted, fontSize = 13.sp)
        } else {
            events.forEach { event -> LedgerEventRow(event) }
        }
    }
}

@Composable
private fun LedgerEventRow(event: FinancialEventEntity) {
    val isDebt = event.eventType == EVENT_CUSTOMER_DEBT
    val title = when (event.eventType) {
        EVENT_CUSTOMER_DEBT -> "بدهی / خرید نسیه"
        EVENT_CUSTOMER_PAYMENT -> "دریافت از مشتری"
        else -> event.eventType
    }
    val amountColor = if (isDebt) Color(0xFFB7791F) else Teal
    Surface(color = Color.White, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontWeight = FontWeight.Medium)
                Text(dateText(event.timestamp), color = Muted, fontSize = 11.sp)
                if (!event.note.isNullOrBlank()) Text(event.note.orEmpty(), color = Muted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (isDebt) "+" else "−") + moneyText(event.amount),
                    color = amountColor,
                    fontWeight = FontWeight.Bold
                )
                Text("تومان", color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ComingSoon(name: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Construction, contentDescription = name, tint = Teal, modifier = Modifier.size(48.dp))
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("این بخش در مرحله توسعه قرار دارد.", color = Color.Gray)
        }
    }
}
