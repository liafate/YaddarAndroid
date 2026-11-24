
package ir.yaddar.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface YaddarDao {

    @Insert
    suspend fun insertProduct(product: ProductEntity): Long

    @Query("SELECT * FROM products ORDER BY name")
    suspend fun getProducts(): List<ProductEntity>

    @Insert
    suspend fun insertPerson(person: PersonEntity): Long

    @Query("SELECT * FROM people ORDER BY name")
    suspend fun getPeople(): List<PersonEntity>

    @Insert
    suspend fun insertFinancialEvent(event: FinancialEventEntity): Long

    @Query("SELECT * FROM financial_events ORDER BY timestamp DESC")
    suspend fun getFinancialEvents(): List<FinancialEventEntity>

    @Insert
    suspend fun insertDocument(document: FinancialDocumentEntity): Long

    @Insert
    suspend fun insertDocumentItems(items: List<DocumentItemEntity>)

    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    suspend fun getDocuments(): List<FinancialDocumentEntity>

    @Query("SELECT * FROM document_items WHERE documentId = :documentId")
    suspend fun getDocumentItems(documentId: Long): List<DocumentItemEntity>

    @Query("SELECT * FROM documents WHERE type = :type ORDER BY createdAt DESC")
    suspend fun getDocumentsByType(type: String): List<FinancialDocumentEntity>

    @Query("SELECT COALESCE(SUM(total - paid), 0) FROM documents WHERE type = 'SALE'")
    suspend fun totalSalesReceivable(): Long

    @Query("SELECT COALESCE(SUM(total - paid), 0) FROM documents WHERE type = 'PURCHASE'")
    suspend fun totalPurchasePayable(): Long

    @Query("SELECT COALESCE(SUM(total), 0) FROM documents WHERE type = 'SALE' AND createdAt >= :start AND createdAt < :end")
    suspend fun salesBetween(start: Long, end: Long): Long

    @Query("SELECT COALESCE(SUM(paid), 0) FROM documents WHERE type = 'SALE' AND createdAt >= :start AND createdAt < :end")
    suspend fun receiptsBetween(start: Long, end: Long): Long
}
