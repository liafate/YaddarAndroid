package ir.yaddar.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface YaddarDao {
    @Insert suspend fun insertProduct(product: ProductEntity): Long
    @Query("SELECT * FROM products ORDER BY name") suspend fun getProducts(): List<ProductEntity>

    @Insert suspend fun insertPerson(person: PersonEntity): Long
    @Update suspend fun updatePerson(person: PersonEntity)
    @Query("SELECT * FROM people ORDER BY name") suspend fun getPeople(): List<PersonEntity>
    @Query("SELECT * FROM people WHERE kind = :kind ORDER BY name") suspend fun getPeopleByKind(kind: String): List<PersonEntity>

    @Insert suspend fun insertFinancialEvent(event: FinancialEventEntity): Long
    @Update suspend fun updateFinancialEvent(event: FinancialEventEntity)
    @Query("DELETE FROM financial_events WHERE id = :eventId") suspend fun deleteFinancialEvent(eventId: Long)
    @Query("SELECT * FROM financial_events ORDER BY timestamp DESC") suspend fun getFinancialEvents(): List<FinancialEventEntity>
    @Query("SELECT * FROM financial_events WHERE personId = :personId ORDER BY timestamp DESC") suspend fun getEventsForPerson(personId: Long): List<FinancialEventEntity>
    @Query("SELECT * FROM financial_events WHERE personId = :personId AND eventType IN ('CUSTOMER_DEBT', 'CUSTOMER_PAYMENT') ORDER BY timestamp DESC") suspend fun getCustomerLedgerEventsForPerson(personId: Long): List<FinancialEventEntity>
    @Query("SELECT * FROM financial_events WHERE personId = :personId AND eventType IN ('CUSTOMER_DEBT', 'CUSTOMER_PAYMENT') AND timestamp >= :start AND timestamp < :end ORDER BY timestamp ASC") suspend fun getCustomerLedgerEventsBetween(personId: Long, start: Long, end: Long): List<FinancialEventEntity>
    @Query("SELECT COALESCE(SUM(CASE WHEN eventType = 'CUSTOMER_DEBT' THEN amount WHEN eventType = 'CUSTOMER_PAYMENT' THEN -amount ELSE 0 END), 0) FROM financial_events WHERE personId = :personId AND eventType IN ('CUSTOMER_DEBT', 'CUSTOMER_PAYMENT') AND timestamp < :start") suspend fun customerOpeningBalance(personId: Long, start: Long): Long

    @Insert suspend fun insertDocument(document: FinancialDocumentEntity): Long
    @Insert suspend fun insertDocumentItems(items: List<DocumentItemEntity>)
    @Query("SELECT * FROM documents ORDER BY createdAt DESC") suspend fun getDocuments(): List<FinancialDocumentEntity>
    @Query("SELECT * FROM document_items WHERE documentId = :documentId") suspend fun getDocumentItems(documentId: Long): List<DocumentItemEntity>
    @Query("SELECT * FROM documents WHERE type = :type ORDER BY createdAt DESC") suspend fun getDocumentsByType(type: String): List<FinancialDocumentEntity>
    @Query("SELECT COALESCE(SUM(total - paid), 0) FROM documents WHERE type = 'SALE'") suspend fun totalSalesReceivable(): Long
    @Query("SELECT COALESCE(SUM(total - paid), 0) FROM documents WHERE type = 'PURCHASE'") suspend fun totalPurchasePayable(): Long
    @Query("SELECT COALESCE(SUM(total), 0) FROM documents WHERE type = 'SALE' AND createdAt >= :start AND createdAt < :end") suspend fun salesBetween(start: Long, end: Long): Long
    @Query("SELECT COALESCE(SUM(paid), 0) FROM documents WHERE type = 'SALE' AND createdAt >= :start AND createdAt < :end") suspend fun receiptsBetween(start: Long, end: Long): Long
    @Query("SELECT COALESCE(SUM(amount), 0) FROM financial_events WHERE eventType = 'CUSTOMER_PAYMENT' AND timestamp >= :start AND timestamp < :end") suspend fun customerPaymentsBetween(start: Long, end: Long): Long
}
