package ir.yaddar.app

import android.content.Intent
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
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

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
                listOf("خانه", "کالا", "اشخاص", "گزارش", "بیشتر").forEach { label ->
                    NavigationBarItem(selected == label, { selected = label }, label = { Text(label, fontSize = 11.sp) },
                        icon = { Icon(when(label) {
                            "خانه" -> Icons.Default.Home
                            "کالا" -> Icons.Default.Inventory2
                            "اشخاص" -> Icons.Default.People
                            "گزارش" -> Icons.Default.BarChart
                            else -> Icons.Default.Menu
                        }, contentDescription=label) })
                }
            }
        }) { padding ->
            when(selected) {
                "خانه" -> Dashboard(Modifier.padding(padding)) { selected = "اشخاص" }
                "اشخاص" -> CustomerBook(db, Modifier.padding(padding))
                else -> ComingSoon(selected, Modifier.padding(padding))
            }
        }
    }
}
@Composable private fun Dashboard(modifier: Modifier=Modifier,onCustomers:()->Unit) {
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement=Arrangement.spacedBy(16.dp)) {
        Text("یاددار",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Teal)
        Text("مدیریت ساده و روزانه مغازه",color=Muted)
        Surface(color=Teal,shape=RoundedCornerShape(22.dp),modifier=Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Text("سلام، روز بخیر",color=Color.White,fontSize=20.sp,fontWeight=FontWeight.Bold)
                Text("خلاصه وضعیت مغازه",color=Color.White.copy(alpha=.85f))
                Text("اطلاعات واقعی پس از تکمیل ماژول‌ها در این بخش نمایش داده خواهد شد.",color=Color.White,fontSize=13.sp)
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){SummaryCard("فروش امروز","—",Modifier.weight(1f));SummaryCard("دریافت امروز","—",Modifier.weight(1f))}
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){SummaryCard("طلب از مشتری","—",Modifier.weight(1f),Color(0xFFB7791F));SummaryCard("بدهی به فروشنده","—",Modifier.weight(1f),Color(0xFFB7791F))}
        Text("عملیات سریع",fontWeight=FontWeight.Bold,fontSize=18.sp)
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){ActionTile("فروش جدید",Icons.Default.ShoppingCart,Modifier.weight(1f));ActionTile("ثبت خرید",Icons.Default.AddBox,Modifier.weight(1f))}
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){ActionTile("مشتریان",Icons.Default.People,Modifier.weight(1f),onCustomers);ActionTile("دریافت / پرداخت",Icons.Default.AccountBalanceWallet,Modifier.weight(1f),onCustomers)}
    }
}
@Composable private fun SummaryCard(title:String,value:String,modifier:Modifier=Modifier,accent:Color=Teal){
    Surface(color=Color.White,shape=RoundedCornerShape(18.dp),modifier=modifier){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(title,color=Muted,fontSize=13.sp);Text(value,fontWeight=FontWeight.Bold,fontSize=20.sp,color=accent)}}
}
@Composable private fun ActionTile(title:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier=Modifier,onClick:(()->Unit)?=null){
    Surface(color=Color.White,shape=RoundedCornerShape(18.dp),modifier=if(onClick==null)modifier.height(100.dp) else modifier.height(100.dp).clickable{onClick()}) {
        Column(Modifier.fillMaxSize().padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(icon,contentDescription=title,tint=Teal,modifier=Modifier.size(30.dp));Spacer(Modifier.height(8.dp));Text(title,fontWeight=FontWeight.Medium,fontSize=14.sp)}
    }
}

private data class CustomerSummary(val person:PersonEntity,val balance:Long)
private fun balanceOf(events:List<FinancialEventEntity>)=events.fold(0L){b,e->when(e.eventType){EVENT_CUSTOMER_DEBT->b+e.amount;EVENT_CUSTOMER_PAYMENT->b-e.amount;else->b}}
private fun digits(s:String)=buildString { s.forEach{append(when(it){in '۰'..'۹'->'0'+(it-'۰');in '٠'..'٩'->'0'+(it-'٠');else->it})} }
private fun numeric(s:String)=digits(s).filter(Char::isDigit)
private fun money(n:Long)=String.format(Locale.US,"%,d",n)
private fun formatInput(s:String):String { val d=numeric(s); return if(d.isEmpty()) "" else try{money(d.toLong())}catch(_:Exception){d} }

/* Gregorian/Jalali conversion helpers; timestamps remain Unix milliseconds. */
private data class JDate(val y:Int,val m:Int,val d:Int)
private fun gregorianToJalali(gy:Int,gm:Int,gd:Int):JDate {
    val gdm=intArrayOf(0,31,59,90,120,151,181,212,243,273,304,334)
    var jy=gy-1600; var jm=gm-1; var jd=gd-1
    var gyear=gy
    val gy2=if(gm>2) gyear+1 else gyear
    var days=365*jy+(jy/4)-(jy/100)+(jy/400)+gdm[jm]+jd
    if(gm>2 && ((gy2%4==0 && gy2%100!=0)||gy2%400==0)) days++
    var jdays=days-79
    val jnp=jdays/12053
    jdays%=12053
    jy=979+33*jnp+4*(jdays/1461)
    jdays%=1461
    if(jdays>=366){jy+= (jdays-1)/365;jdays=(jdays-1)%365}
    if(jdays<186){jm=1+jdays/31;jd=1+jdays%31}else{jm=7+(jdays-186)/30;jd=1+(jdays-186)%30}
    return JDate(jy,jm,jd)
}
private fun jalaliToGregorian(jyIn:Int,jmIn:Int,jdIn:Int):Triple<Int,Int,Int>{
    var jy=jyIn-979; var jm=jmIn-1; var jd=jdIn-1
    var jday=365*jy+(jy/33)*8+(jy%33+3)/4
    jday+=if(jm<6) jm*31 else 186+(jm-6)*30
    jday+=jd
    var gday=jday+79
    var gy=1600+400*(gday/146097);gday%=146097
    var leap=true
    if(gday>=36525){gday--;gy+=100*(gday/36524);gday%=36524;if(gday>=365)gday++}
    gy+=4*(gday/1461);gday%=1461
    if(gday>=366){leap=false;gday--;gy+=gday/365;gday%=365}
    val monthDays=intArrayOf(31,if(leap)29 else 28,31,30,31,30,31,31,30,31,30,31)
    var gm=0
    while(gm<12 && gday>=monthDays[gm]){gday-=monthDays[gm];gm++}
    return Triple(gy,gm+1,gday+1)
}
private fun jDateFromTimestamp(ts:Long):JDate{
    val c=Calendar.getInstance();c.timeInMillis=ts
    return gregorianToJalali(c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH))
}
private fun jDateText(ts:Long):String{val j=jDateFromTimestamp(ts);return "%04d/%02d/%02d".format(Locale.US,j.y,j.m,j.d)}
private fun startTimestamp(j:JDate):Long?{
    if(j.y<1200||j.m !in 1..12||j.d !in 1..31)return null
    val (gy,gm,gd)=jalaliToGregorian(j.y,j.m,j.d)
    val c=Calendar.getInstance();c.clear();c.set(gy,gm-1,gd,0,0,0);c.set(Calendar.MILLISECOND,0)
    // Round-trip validation rejects impossible dates.
    if(jDateFromTimestamp(c.timeInMillis)!=j)return null
    return c.timeInMillis
}
private fun nextDay(ts:Long):Long{val c=Calendar.getInstance();c.timeInMillis=ts;c.add(Calendar.DAY_OF_MONTH,1);return c.timeInMillis}
private fun todayJ():JDate=jDateFromTimestamp(System.currentTimeMillis())

@Composable private fun JalaliDateDialog(initial:JDate,onSelect:(JDate)->Unit,onDismiss:()->Unit){
    var y by remember{mutableStateOf(initial.y.toString())};var m by remember{mutableStateOf(initial.m.toString())};var d by remember{mutableStateOf(initial.d.toString())};var err by remember{mutableStateOf("")}
    AlertDialog(onDismissRequest=onDismiss,title={Text("انتخاب تاریخ شمسی")},text={
        Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(y,{y=numeric(it)},label={Text("سال")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));OutlinedTextField(m,{m=numeric(it)},label={Text("ماه")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number));OutlinedTextField(d,{d=numeric(it)},label={Text("روز")},modifier=Modifier.weight(1f),keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))}
            Text("مثال: 1405 / 8 / 4",color=Muted,fontSize=12.sp)
            if(err.isNotBlank())Text(err,color=MaterialTheme.colorScheme.error)
        }
    },confirmButton={TextButton(onClick={val jd=JDate(y.toIntOrNull()?:0,m.toIntOrNull()?:0,d.toIntOrNull()?:0);if(startTimestamp(jd)==null)err="تاریخ واردشده معتبر نیست." else onSelect(jd)}){Text("تأیید")}},dismissButton={TextButton(onClick=onDismiss){Text("انصراف")}})
}
private fun dayStart(j:JDate)=startTimestamp(j)
private fun endExclusive(j:JDate):Long?=dayStart(j)?.let(::nextDay)

@Composable private fun CustomerBook(db:YaddarDatabase,modifier:Modifier=Modifier){
    val dao=remember(db){db.yaddarDao()};val scope=rememberCoroutineScope()
    var summaries by remember{mutableStateOf<List<CustomerSummary>>(emptyList())};var loading by remember{mutableStateOf(true)}
    var error by remember{mutableStateOf("")};var reload by remember{mutableIntStateOf(0)}
    var selected by remember{mutableStateOf<PersonEntity?>(null)};var add by remember{mutableStateOf(false)}
    var name by remember{mutableStateOf("")};var phone by remember{mutableStateOf("")};var formError by remember{mutableStateOf("")}
    var search by remember{mutableStateOf("")};var sort by remember{mutableStateOf("name")}
    var allReport by remember{mutableStateOf(false)}
    LaunchedEffect(reload){loading=true;try{summaries=dao.getPeopleByKind(CUSTOMER_KIND).map{CustomerSummary(it,balanceOf(dao.getCustomerLedgerEventsForPerson(it.id)))};error=""}catch(e:Exception){error=e.message?: "خطا"};loading=false}
    if(allReport){AllCustomerReport(summaries,onBack={allReport=false}) ;return}
    if(selected!=null){CustomerLedger(db,selected!!,modifier,{selected=null},{reload++});return}
    val filtered=summaries.filter{it.person.name.contains(search,true)||(it.person.phone?.contains(search)==true)}.let{list->
        when(sort){"debt"->list.sortedByDescending{it.balance.coerceAtLeast(0)};"credit"->list.sortedByDescending{(-it.balance).coerceAtLeast(0)};"status"->list.sortedBy{when{it.balance>0->0;it.balance<0->1;else->2}};else->list.sortedBy{it.person.name.lowercase()}}
    }
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("دفتر حساب مشتریان",fontSize=23.sp,fontWeight=FontWeight.Bold,color=Teal)
        OutlinedTextField(search,{search=it},label={Text("جستجو نام یا شماره تماس")},leadingIcon={Icon(Icons.Default.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){FilterChip(sort=="name",{sort="name"},label={Text("نام")});FilterChip(sort=="debt",{sort="debt"},label={Text("بیشترین بدهی")});FilterChip(sort=="credit",{sort="credit"},label={Text("بستانکاری")});FilterChip(sort=="status",{sort="status"},label={Text("وضعیت")})}
        Button(onClick={add=true},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Teal)){Icon(Icons.Default.PersonAdd,null);Spacer(Modifier.width(6.dp));Text("افزودن مشتری")}
        OutlinedButton(onClick={allReport=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Assessment,null);Spacer(Modifier.width(6.dp));Text("گزارش کلی مشتریان")}
        if(error.isNotBlank())Text(error,color=MaterialTheme.colorScheme.error)
        if(loading)CircularProgressIndicator()
        else if(filtered.isEmpty())Text(if(search.isNotEmpty())"نتیجه‌ای یافت نشد." else "هنوز مشتری ثبت نشده است.",color=Muted)
        else filtered.forEach{CustomerCard(it){selected=it.person}}
    }
    if (add) AlertDialog(
        onDismissRequest = { add = false },
        title = { Text("مشتری جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("نام") })
                OutlinedTextField(phone, { phone = it }, label = { Text("شماره تماس") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                if (formError.isNotBlank()) Text(formError, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank()) {
                    formError = "نام الزامی است."
                } else {
                    scope.launch {
                        try {
                            dao.insertPerson(PersonEntity(name = name.trim(), phone = phone.trim().ifBlank { null }, kind = CUSTOMER_KIND))
                            add = false
                            name = ""
                            phone = ""
                            reload++
                        } catch (e: Exception) {
                            formError = e.message ?: "خطا"
                        }
                    }
                }
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = { add = false }) { Text("لغو") } }
    )
}
@Composable private fun CustomerCard(s:CustomerSummary,onClick:()->Unit){
    val col=if(s.balance>0)Color(0xFFB7791F) else if(s.balance<0)Teal else Muted
    Surface(color=Color.White,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth().clickable{onClick()}){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
        Column(Modifier.weight(1f)){Text(s.person.name,fontWeight=FontWeight.Bold);s.person.phone?.takeIf{it.isNotBlank()}?.let{Text(it,color=Muted,fontSize=12.sp)};Text(if(s.balance>0)"بدهکار" else if(s.balance<0)"بستانکار" else "تسویه",color=col,fontSize=12.sp)}
        Column(horizontalAlignment=Alignment.End){Text(money(kotlin.math.abs(s.balance)),color=col,fontWeight=FontWeight.Bold);Text("تومان",fontSize=11.sp,color=Muted)}
        Icon(Icons.Default.ChevronLeft,null,tint=Muted)
    }}
}

@Composable private fun CustomerLedger(db:YaddarDatabase,customer:PersonEntity,modifier:Modifier=Modifier,onBack:()->Unit,onChanged:()->Unit){
    val dao=remember(db){db.yaddarDao()};val scope=rememberCoroutineScope()
    var events by remember(customer.id){mutableStateOf<List<FinancialEventEntity>>(emptyList())};var reload by remember{mutableIntStateOf(0)};var loading by remember{mutableStateOf(true)}
    var type by remember{mutableStateOf(EVENT_CUSTOMER_DEBT)};var amount by remember{mutableStateOf("")};var note by remember{mutableStateOf("")};var msg by remember{mutableStateOf("")};var busy by remember{mutableStateOf(false)}
    var editEvent by remember{mutableStateOf<FinancialEventEntity?>(null)};var deleteEvent by remember{mutableStateOf<FinancialEventEntity?>(null)}
    var report by remember{mutableStateOf(false)}
    LaunchedEffect(customer.id,reload){loading=true;try{events=dao.getCustomerLedgerEventsForPerson(customer.id);msg=""}catch(e:Exception){msg=e.message?: "خطا"};loading=false}
    val balance=balanceOf(events)
    if(report){CustomerPeriodReport(dao,customer,onBack={report=false});return}
    Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowForward,"بازگشت")};Column{Text(customer.name,fontSize=22.sp,fontWeight=FontWeight.Bold,color=Teal);Text("دفتر گردش حساب",color=Muted)}}
        Surface(color=Color.White,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(16.dp)){Text(if(balance>0)"مانده بدهی" else if(balance<0)"مانده بستانکاری" else "تسویه",color=Muted);Text("${money(kotlin.math.abs(balance))} تومان",fontSize=24.sp,fontWeight=FontWeight.Bold,color=if(balance>0)Color(0xFFB7791F) else Teal)}}
        Button(onClick={report=true},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.DateRange,null);Spacer(Modifier.width(6.dp));Text("گزارش دوره‌ای")}
        Text("ثبت تراکنش",fontWeight=FontWeight.Bold)
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(type==EVENT_CUSTOMER_DEBT,{type=EVENT_CUSTOMER_DEBT},label={Text("بدهی")});FilterChip(type==EVENT_CUSTOMER_PAYMENT,{type=EVENT_CUSTOMER_PAYMENT},label={Text("دریافت")})}
        OutlinedTextField(amount,{amount=formatInput(it)},label={Text("مبلغ (تومان)")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth())
        OutlinedTextField(note,{note=it},label={Text("شرح")},modifier=Modifier.fillMaxWidth())
        Button(enabled=!busy,onClick={val n=numeric(amount).toLongOrNull();if(n==null||n<=0)msg="مبلغ باید مثبت باشد." else{busy=true;scope.launch{try{dao.insertFinancialEvent(FinancialEventEntity(eventType=type,amount=n,personId=customer.id,timestamp=System.currentTimeMillis(),note=note.trim().ifBlank{null}));amount="";note="";reload++;onChanged();msg="ثبت شد."}catch(e:Exception){msg=e.message?: "خطا"};busy=false}}},modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Teal)){Text(if(busy)"..." else "ثبت تراکنش")}
        if(msg.isNotBlank())Text(msg,color=if(msg=="ثبت شد.")Teal else MaterialTheme.colorScheme.error,fontSize=12.sp)
        HorizontalDivider();Text("تاریخچه",fontWeight=FontWeight.Bold)
        if(loading)CircularProgressIndicator() else events.forEach{LedgerRow(it,{editEvent=it},{deleteEvent=it})}
    }
    editEvent?.let { event ->
        EditEventDialog(
            e = event,
            onDismiss = { editEvent = null },
            onSave = { updated ->
                scope.launch {
                    try {
                        dao.updateFinancialEvent(updated)
                        editEvent = null
                        reload++
                        onChanged()
                    } catch (e: Exception) {
                        msg = "ویرایش ناموفق: ${e.message}"
                    }
                }
            }
        )
    }
    deleteEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { deleteEvent = null },
            title = { Text("حذف تراکنش") },
            text = { Text("این تراکنش حذف شود؟") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            dao.deleteFinancialEvent(event.id)
                            deleteEvent = null
                            reload++
                            onChanged()
                        } catch (e: Exception) {
                            msg = e.message ?: "خطا"
                        }
                    }
                }) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { deleteEvent = null }) { Text("لغو") }
            }
        )
    }
}
@Composable private fun LedgerRow(e:FinancialEventEntity,onEdit:()->Unit,onDelete:()->Unit){
    Surface(color=Color.White,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
        Column(Modifier.weight(1f)){Text(if(e.eventType==EVENT_CUSTOMER_DEBT)"بدهی / خرید نسیه" else "دریافت",fontWeight=FontWeight.Medium);Text(jDateText(e.timestamp),color=Muted,fontSize=12.sp);e.note?.let{Text(it,color=Muted,fontSize=12.sp)};Text((if(e.eventType==EVENT_CUSTOMER_DEBT)"+" else "−")+money(e.amount)+" تومان",color=if(e.eventType==EVENT_CUSTOMER_DEBT)Color(0xFFB7791F) else Teal,fontWeight=FontWeight.Bold)}
        IconButton(onClick=onEdit){Icon(Icons.Default.Edit,"ویرایش")};IconButton(onClick=onDelete){Icon(Icons.Default.Delete,"حذف",tint=MaterialTheme.colorScheme.error)}
    }}
}
@Composable private fun EditEventDialog(e:FinancialEventEntity,onDismiss:()->Unit,onSave:(FinancialEventEntity)->Unit){
    var amount by remember { mutableStateOf(formatInput(e.amount.toString())) }
    var note by remember { mutableStateOf(e.note.orEmpty()) }
    var date by remember { mutableStateOf(jDateFromTimestamp(e.timestamp)) }
    var pick by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ویرایش تراکنش") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (e.eventType == EVENT_CUSTOMER_DEBT) "نوع: بدهی" else "نوع: دریافت")
                OutlinedTextField(value = amount, onValueChange = { amount = formatInput(it) }, label = { Text("مبلغ (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("شرح") })
                OutlinedButton(onClick = { pick = true }) { Text("تاریخ: ${date.y}/${date.m}/${date.d}") }
                if (err.isNotBlank()) Text(err, color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val n = numeric(amount).toLongOrNull()
                val ts = startTimestamp(date)
                if (n == null || n <= 0 || ts == null) err = "مبلغ یا تاریخ نامعتبر"
                else onSave(e.copy(amount = n, note = note.trim().ifBlank { null }, timestamp = ts))
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } }
    )
    if (pick) JalaliDateDialog(date, { date = it; pick = false }, { pick = false })
}
@Composable private fun CustomerPeriodReport(dao:YaddarDao,customer:PersonEntity,onBack:()->Unit){
    val scope=rememberCoroutineScope();var from by remember{mutableStateOf(todayJ())};var to by remember{mutableStateOf(todayJ())};var pickFrom by remember{mutableStateOf(false)};var pickTo by remember{mutableStateOf(false)};var data by remember{mutableStateOf<List<FinancialEventEntity>?>(null)};var opening by remember{mutableStateOf(0L)};var err by remember{mutableStateOf("")}
    val start=startTimestamp(from);val end=endExclusive(to)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
        Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowForward,null)};Text("گزارش دوره‌ای ${customer.name}",fontWeight=FontWeight.Bold)}
        OutlinedButton(onClick = { pickFrom = true }, modifier = Modifier.fillMaxWidth()) {
            Text("شروع: ${from.y}/${from.m}/${from.d}")
        }
        OutlinedButton(onClick = { pickTo = true }, modifier = Modifier.fillMaxWidth()) {
            Text("پایان: ${to.y}/${to.m}/${to.d}")
        }
        Button(
            onClick = {
                if (start == null || end == null || end <= start) {
                    err = "بازه تاریخ معتبر نیست."
                } else {
                    scope.launch {
                        try {
                            opening = dao.customerOpeningBalance(customer.id, start)
                            data = dao.getCustomerLedgerEventsBetween(customer.id, start, end)
                            err = ""
                        } catch (e: Exception) {
                            err = e.message ?: "خطا"
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("محاسبه گزارش") }
        if(err.isNotBlank())Text(err,color=MaterialTheme.colorScheme.error)
        data?.let{list->val debts=list.filter{it.eventType==EVENT_CUSTOMER_DEBT}.sumOf{it.amount};val payments=list.filter{it.eventType==EVENT_CUSTOMER_PAYMENT}.sumOf{it.amount};val closing=opening+debts-payments
            ReportValue("مانده ابتدای دوره",opening);ReportValue("مجموع بدهی",debts);ReportValue("مجموع دریافت",payments);ReportValue("مانده پایان دوره",closing)
            Text("تراکنش‌های دوره",fontWeight=FontWeight.Bold);list.forEach{ReportEventRow(it)}
            ShareButton("اشتراک گزارش",buildString{append("گزارش حساب ${customer.name}\nاز ${from.y}/${from.m}/${from.d} تا ${to.y}/${to.m}/${to.d}\nمانده ابتدای دوره: ${money(opening)}\nبدهی: ${money(debts)}\nدریافت: ${money(payments)}\nمانده پایان: ${money(closing)} تومان\n\n");list.forEach{append("${jDateText(it.timestamp)} | ${if(it.eventType==EVENT_CUSTOMER_DEBT)"بدهی" else "دریافت"} | ${money(it.amount)} | ${it.note.orEmpty()}\n")}})
        }
    }
    if(pickFrom)JalaliDateDialog(from,{from=it;pickFrom=false;data=null},{pickFrom=false});if(pickTo)JalaliDateDialog(to,{to=it;pickTo=false;data=null},{pickTo=false})
}
@Composable private fun ReportValue(label:String,value:Long){Surface(color=Color.White,shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(12.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(label);Text("${money(kotlin.math.abs(value))} تومان",fontWeight=FontWeight.Bold,color=if(value<0)Teal else Color(0xFFB7791F))}}}
@Composable private fun ShareButton(label:String,content:String){val ctx=androidx.compose.ui.platform.LocalContext.current;Button(onClick={val i=Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,content);ctx.startActivity(Intent.createChooser(i,label))},modifier=Modifier.fillMaxWidth()){Icon(Icons.Default.Share,null);Spacer(Modifier.width(6.dp));Text(label)}}
@Composable private fun AllCustomerReport(summaries:List<CustomerSummary>,onBack:()->Unit){
    var sort by remember{mutableStateOf(false)}
    val ordered=if(sort)summaries.sortedByDescending{it.balance}else summaries.sortedBy{it.person.name.lowercase()}
    val debt=ordered.sumOf{it.balance.coerceAtLeast(0)};val credit=ordered.sumOf{(-it.balance).coerceAtLeast(0)}
    val report=buildString{append("گزارش کلی مشتریان\n\n");ordered.forEach{append("${it.person.name} | ${it.person.phone.orEmpty()} | ${if(it.balance>0)"بدهکار" else if(it.balance<0)"بستانکار" else "تسویه"} | ${money(kotlin.math.abs(it.balance))} تومان\n")};append("\nمجموع بدهکاری: ${money(debt)}\nمجموع بستانکاری: ${money(credit)}")}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Row(verticalAlignment=Alignment.CenterVertically){IconButton(onClick=onBack){Icon(Icons.Default.ArrowForward,null)};Text("گزارش کلی مشتریان",fontWeight=FontWeight.Bold)};Button(onClick={sort=!sort},modifier=Modifier.fillMaxWidth()){Text(if(sort)"مرتب‌سازی نام" else "مرتب‌سازی مانده")};ReportValue("مجموع بدهکاری",debt);ReportValue("مجموع بستانکاری",credit);ordered.forEach{CustomerCard(it,{})};ShareButton("اشتراک گزارش",report)}
}

@Composable private fun ReportEventRow(e:FinancialEventEntity){
    Surface(color=Color.White,shape=RoundedCornerShape(12.dp),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){Text(if(e.eventType==EVENT_CUSTOMER_DEBT)"بدهی" else "دریافت",fontWeight=FontWeight.Medium);Text(jDateText(e.timestamp),color=Muted,fontSize=12.sp);Text((if(e.eventType==EVENT_CUSTOMER_DEBT)"+" else "−")+money(e.amount)+" تومان");e.note?.takeIf{it.isNotBlank()}?.let{Text(it,color=Muted,fontSize=12.sp)}}}
}
@Composable private fun ComingSoon(name:String,modifier:Modifier=Modifier){Box(modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.Center,verticalArrangement=Arrangement.spacedBy(12.dp)){Icon(Icons.Default.Construction,null,tint=Teal,modifier=Modifier.size(48.dp));Text(name,fontSize=22.sp,fontWeight=FontWeight.Bold);Text("این بخش در مرحله توسعه قرار دارد.",color=Color.Gray)}}}
