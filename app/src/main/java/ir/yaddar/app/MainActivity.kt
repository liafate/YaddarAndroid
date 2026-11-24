package ir.yaddar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Teal = Color(0xFF167D76)
private val Background = Color(0xFFF5F7F6)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { YaddarApp() }
    }
}

@Composable
fun YaddarApp() {
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
                                Icon(when(label) {
                                    "خانه" -> Icons.Default.Home
                                    "کالا" -> Icons.Default.Inventory2
                                    "اشخاص" -> Icons.Default.People
                                    "گزارش" -> Icons.Default.BarChart
                                    else -> Icons.Default.Menu
                                }, contentDescription = label)
                            }
                        )
                    }
                }
            }
        ) { padding ->
            when (selected) {
                "خانه" -> Dashboard(Modifier.padding(padding))
                else -> ComingSoon(selected, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun Dashboard(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(horizontal = 18.dp).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column {
            Text("یاددار", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Teal)
            Text("مدیریت ساده و روزانه مغازه", color = Color(0xFF667085))
        }
        Surface(shape = RoundedCornerShape(22.dp), color = Teal, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("سلام، روز بخیر", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("خلاصه وضعیت مغازه", color = Color.White.copy(alpha = .85f))
                Spacer(Modifier.height(6.dp))
                Text("اطلاعات واقعی پس از تکمیل ماژول‌ها در این بخش نمایش داده خواهد شد.", color = Color.White, fontSize = 13.sp)
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
            ActionTile("مشتریان", Icons.Default.People, Modifier.weight(1f))
            ActionTile("دریافت / پرداخت", Icons.Default.AccountBalanceWallet, Modifier.weight(1f))
        }
        Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.NotificationsActive, tint = Color(0xFFB7791F))
                Column {
                    Text("یادآوری‌ها", fontWeight = FontWeight.Bold)
                    Text("هشدارهای موجودی و حساب‌ها در نسخه‌های بعدی فعال می‌شوند.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier, accent: Color = Teal) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, color = Color(0xFF667085), fontSize = 13.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = accent)
        }
    }
}

@Composable
private fun ActionTile(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Surface(color = Color.White, shape = RoundedCornerShape(18.dp), modifier = modifier.height(100.dp)) {
        Column(Modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, tint = Teal, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ComingSoon(name: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Default.Construction, tint = Teal, modifier = Modifier.size(48.dp))
            Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("این بخش در مرحله توسعه قرار دارد.", color = Color.Gray)
        }
    }
}
