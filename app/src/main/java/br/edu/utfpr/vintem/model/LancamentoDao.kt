package br.edu.utfpr.vintem.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.model.ResumoMensal
import kotlinx.coroutines.flow.Flow

@Dao
interface LancamentoDao {
    @Query("SELECT * FROM lancamentos ORDER BY id DESC")
    fun getAll(): Flow<List<Lancamento>>

    @Insert
    suspend fun insert(lancamento: Lancamento)

    @Delete
    suspend fun deletar(lancamento: Lancamento)

    // NOVA CONSULTA PARA O RESUMO
    @Query("""
        SELECT 
            substr(data, 4, 7) as mesAno, 
            SUM(CASE WHEN tipo = 'Receita' THEN valor ELSE 0 END) as totalReceita,
            SUM(CASE WHEN tipo = 'Despesa' THEN valor ELSE 0 END) as totalDespesa
        FROM lancamentos
        GROUP BY mesAno
        ORDER BY substr(data, 7, 4) DESC, substr(data, 4, 2) DESC
    """)
    fun getResumoMensal(): Flow<List<ResumoMensal>>
}