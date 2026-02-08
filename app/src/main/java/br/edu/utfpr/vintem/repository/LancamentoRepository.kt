package br.edu.utfpr.vintem.repository

import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.model.LancamentoDao
import br.edu.utfpr.vintem.model.ResumoMensal
import kotlinx.coroutines.flow.Flow

class LancamentoRepository(private val lancamentoDao: LancamentoDao) {

    // Trocado 'dao' por 'lancamentoDao'
    val allLancamentos: Flow<List<Lancamento>> = lancamentoDao.getAll()

    // Este já estava correto
    val resumoMensal: Flow<List<ResumoMensal>> = lancamentoDao.getResumoMensal()

    suspend fun insert(lancamento: Lancamento) {
        // Trocado 'dao' por 'lancamentoDao'
        lancamentoDao.insert(lancamento)
    }

    suspend fun deletar(lancamento: Lancamento) {
        // Trocado 'dao' por 'lancamentoDao'
        lancamentoDao.deletar(lancamento)
    }
}