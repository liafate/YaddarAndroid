package ir.yaddar.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String = "عدد",
    val purchasePrice: Long = 0,
    val salePrice: Long = 0,
    val quantity: Double = 0.0,
    val minimumStock: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val kind: String // CUSTOMER, SUPPLIER, BOTH
)

@Entity(tableName = "financial_events")
data class FinancialEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String,
    val amount: Long,
    val personId: Long? = null,
    val accountId: Long? = null,
    val relatedDocumentId: Long? = null,
    val timestamp: Long,
    val note: String? = null
)
