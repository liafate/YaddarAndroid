package ir.yaddar.app.data
import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities=[ProductEntity,PersonEntity,FinancialEventEntity,FinancialDocumentEntity,DocumentItemEntity],version=2,exportSchema=true)
abstract class YaddarDatabase:RoomDatabase(){
 abstract fun yaddarDao():YaddarDao
 companion object {
  val MIGRATION_1_2=androidx.room.migration.Migration(1,2){db->
   db.execSQL("CREATE TABLE IF NOT EXISTS documents (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,type TEXT NOT NULL,personId INTEGER,subtotal INTEGER NOT NULL,discount INTEGER NOT NULL,total INTEGER NOT NULL,paid INTEGER NOT NULL,createdAt INTEGER NOT NULL,note TEXT)")
   db.execSQL("CREATE TABLE IF NOT EXISTS document_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,documentId INTEGER NOT NULL,productId INTEGER,name TEXT NOT NULL,quantity REAL NOT NULL,unitPrice INTEGER NOT NULL,lineTotal INTEGER NOT NULL)")
  }
 }
}
