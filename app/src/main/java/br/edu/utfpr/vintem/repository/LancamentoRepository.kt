package br.edu.utfpr.vintem.repository

import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.model.LancamentoDao

class LancamentoRepository(private val dao: LancamentoDao) {
    val allLancamentos = dao.getAll()

    suspend fun insert(lancamento: Lancamento) {
        dao.insert(lancamento)
    }
}