package ir.yaddar.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ProductEntity::class, PersonEntity::class, FinancialEventEntity::class], version = 1, exportSchema = true)
abstract class YaddarDatabase : RoomDatabase()
