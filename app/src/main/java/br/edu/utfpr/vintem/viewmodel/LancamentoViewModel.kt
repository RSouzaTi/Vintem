package br.edu.utfpr.vintem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.vintem.model.AppDatabase
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.model.ResumoMensal
import br.edu.utfpr.vintem.repository.LancamentoRepository
import kotlinx.coroutines.launch

// Usamos AndroidViewModel para ter acesso ao 'application'
class LancamentoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LancamentoRepository

    // 1. Variável para a lista principal (Resolve o erro 'allLancamentos')
    val allLancamentos: LiveData<List<Lancamento>>

    // 2. Variável para o resumo mensal
    val resumoMensal: LiveData<List<ResumoMensal>>

    init {
        val dao = AppDatabase.getDatabase(application).lancamentoDao()
        repository = LancamentoRepository(dao)

        // Inicializamos as variáveis buscando do repositório
        allLancamentos = repository.allLancamentos.asLiveData()
        resumoMensal = repository.resumoMensal.asLiveData()
    }

    // 3. Funções de ação (Resolve 'inserir' e 'deletar')
    // Nota: Use 'insert' ou 'inserir' conforme definiu no Repository
    fun insert(lancamento: Lancamento) = viewModelScope.launch {
        repository.insert(lancamento)
    }

    fun deletar(lancamento: Lancamento) = viewModelScope.launch {
        repository.deletar(lancamento)
    }
}