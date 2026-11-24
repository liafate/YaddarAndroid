package ir.yaddar.app.data
/** Shared financial arithmetic. Currency values use integer units (Long). */
object FinancialCalculator {
 data class Line(val quantity:Double,val unitPrice:Long){init{require(quantity>=0.0);require(unitPrice>=0)};val total:Long get()=(quantity*unitPrice.toDouble()).toLong()}
 data class Result(val subtotal:Long,val discount:Long,val total:Long,val paid:Long,val balance:Long)
 fun calculate(lines:List<Line>,discount:Long=0,paid:Long=0):Result{
  require(discount>=0&&paid>=0)
  val subtotal=lines.sumOf{it.total}
  require(discount<=subtotal){"Discount exceeds subtotal"}
  val total=subtotal-discount
  require(paid<=total){"Payment exceeds total"}
  return Result(subtotal,discount,total,paid,total-paid)
 }
}
