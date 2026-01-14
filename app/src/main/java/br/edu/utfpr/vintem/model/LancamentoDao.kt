package br.edu.utfpr.vintem.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LancamentoDao {
    @Query("SELECT * FROM lancamentos ORDER BY id DESC")
    fun getAll(): Flow<List<Lancamento>>

    @Insert
    suspend fun insert(lancamento: Lancamento)
}