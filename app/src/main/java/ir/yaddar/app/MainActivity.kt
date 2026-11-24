package ir.yaddar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.room.Room
import ir.yaddar.app.data.*
import kotlinx.coroutines.launch

private val Teal = Color(0xFF167D76)
private val Background = Color(0xFFF5F7F6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(applicationContext, YaddarDatabase::class.java, "yaddar.db")
            .addMigrations(YaddarDatabase.MIGRATION_1_2).build()
        setContent { YaddarApp(db) }
    }
}

@Composable
fun YaddarApp(db: YaddarDatabase) {
    var selected by remember { mutableStateOf("خانه") }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(containerColor = Background, bottomBar = {
            NavigationBar(containerColor = Color.White) {
                listOf("خانه", "فروش", "کالا", "اشخاص", "بیشتر").forEach { label ->
                    NavigationBarItem(selected = selected == label, onClick = { selected = label },
                        label = { Text(label, fontSize = 11.sp) }, icon = {
                            Icon(when(label) {
                                "خانه" -> Icons.Default.Home
                                "فروش" -> Icons.Default.ShoppingCart
                                "کالا" -> Icons.Default.Inventory2
                                "اشخاص" -> Icons.Default.People
                                else -> Icons.Default.Menu
                            }, contentDescription = label)
                        })
                }
            }
        }) { padding ->
            when(selected) {
                "خانه" -> Dashboard(Modifier.padding(padding), onSale = { selected = "فروش" })
                "فروش" -> SaleScreen(db, Modifier.padding(padding))
                else -> ComingSoon(selected, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun Dashboard(modifier: Modifier = Modifier, onSale: () -> Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp).padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("یاددار", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
        Text("مدیریت ساده و روزانه مغازه", color = Color(0xFF667085))
        Surface(shape = RoundedCornerShape(22.dp), color = Teal, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("خوش آمدید", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("ثبت فروش را از اینجا شروع کنید.", color = Color.White)
                Button(onClick = onSale, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Teal)) {
                    Icon(Icons.Default.Add, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("ثبت فروش جدید")
                }
            }
        }
        Text("عملیات سریع", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Button(onClick = onSale, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("فروش جدید")
        }
    }
}

@Composable
private fun SaleScreen(db: YaddarDatabase, modifier: Modifier = Modifier) {
    var itemName by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var paid by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val quantity = qty.toDoubleOrNull() ?: 0.0
    val unitPrice = price.toLongOrNull() ?: 0L
    val paidAmount = paid.toLongOrNull() ?: 0L
    val total = if(quantity >= 0 && unitPrice >= 0) (quantity * unitPrice.toDouble()).toLong() else 0L
    val balance = total - paidAmount

    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("ثبت فروش", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Teal)
        Text("اطلاعات کالا و پرداخت را وارد کنید.")
        OutlinedTextField(itemName, { itemName = it }, label = { Text("نام کالا") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(qty, { qty = it }, label = { Text("تعداد") }, modifier = Modifier.weight(1f), singleLine = true)
            OutlinedTextField(price, { price = it }, label = { Text("قیمت واحد") }, modifier = Modifier.weight(2f), singleLine = true)
        }
        OutlinedTextField(paid, { paid = it }, label = { Text("مبلغ پرداخت‌شده") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        OutlinedTextField(note, { note = it }, label = { Text("توضیحات (اختیاری)") }, modifier = Modifier.fillMaxWidth())
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AmountRow("جمع فروش", total)
                AmountRow("پرداخت", paidAmount)
                HorizontalDivider()
                AmountRow("مانده", balance, Teal, true)
            }
        }
        Button(onClick = {
            if(itemName.isBlank() || quantity <= 0 || unitPrice <= 0 || paidAmount < 0 || paidAmount > total) {
                message = "لطفاً اطلاعات را بررسی کنید. پرداخت نباید از مبلغ فروش بیشتر باشد."
            } else {
                saving = true; message = ""
                scope.launch {
                    try {
                        val docId = db.yaddarDao().insertDocument(FinancialDocumentEntity(type="SALE", subtotal=total, total=total, paid=paidAmount, note=note.ifBlank { null }))
                        db.yaddarDao().insertDocumentItems(listOf(DocumentItemEntity(documentId=docId, name=itemName.trim(), quantity=quantity, unitPrice=unitPrice, lineTotal=total)))
                        itemName = ""; qty = "1"; price = ""; paid = "0"; note = ""
                        message = "فروش با موفقیت ذخیره شد."
                    } catch(e: Exception) { message = "ذخیره انجام نشد: " + (e.message ?: "") }
                    saving = false
                }
            }
        }, enabled = !saving, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Teal)) {
            if(saving) CircularProgressIndicator(modifier=Modifier.size(20.dp), color=Color.White) else Icon(Icons.Default.Save, contentDescription=null)
            Spacer(Modifier.width(8.dp)); Text(if(saving) "در حال ذخیره..." else "ذخیره فروش")
        }
        if(message.isNotBlank()) Text(message, color=if(message.startsWith("فروش")) Teal else Color.Red)
    }
}

@Composable
private fun AmountRow(label: String, value: Long, color: Color = Color.DarkGray, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = if(bold) FontWeight.Bold else FontWeight.Normal)
        Text(value.toString(), color=color, fontWeight=if(bold) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun ComingSoon(name: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Construction, contentDescription=name, tint=Teal, modifier=Modifier.size(48.dp))
            Text(name, fontSize=22.sp, fontWeight=FontWeight.Bold)
            Text("این بخش در مرحله توسعه قرار دارد.", color=Color.Gray)
        }
    }
}
