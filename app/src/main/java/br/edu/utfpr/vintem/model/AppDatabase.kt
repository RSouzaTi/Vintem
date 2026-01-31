package br.edu.utfpr.vintem.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Lancamento::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lancamentoDao(): LancamentoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vintem_database"
                )
                    .fallbackToDestructiveMigration() // <-- Adicione isso para evitar o crash
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
