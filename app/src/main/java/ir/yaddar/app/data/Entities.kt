package ir.yaddar.app.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="products") data class ProductEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val unit:String="عدد",val purchasePrice:Long=0,val salePrice:Long=0,val quantity:Double=0.0,val minimumStock:Double=0.0,val isActive:Boolean=true)
@Entity(tableName="people") data class PersonEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val name:String,val phone:String?=null,val kind:String,val customerType:String="حقیقی")
@Entity(tableName="financial_events") data class FinancialEventEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val eventType:String,val amount:Long,val personId:Long?=null,val accountId:Long?=null,val relatedDocumentId:Long?=null,val timestamp:Long,val note:String?=null)
@Entity(tableName="documents") data class FinancialDocumentEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val type:String,val personId:Long?=null,val subtotal:Long,val discount:Long=0,val total:Long,val paid:Long=0,val createdAt:Long=System.currentTimeMillis(),val note:String?=null)
@Entity(tableName="document_items") data class DocumentItemEntity(@PrimaryKey(autoGenerate=true) val id:Long=0,val documentId:Long,val productId:Long?=null,val name:String,val quantity:Double,val unitPrice:Long,val lineTotal:Long)
