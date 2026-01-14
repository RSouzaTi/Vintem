package br.edu.utfpr.vintem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import br.edu.utfpr.vintem.model.AppDatabase
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.repository.LancamentoRepository
import kotlinx.coroutines.launch

class LancamentoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LancamentoRepository
    val allLancamentos: LiveData<List<Lancamento>>

    init {
        val dao = AppDatabase.getDatabase(application).lancamentoDao()
        repository = LancamentoRepository(dao)
        allLancamentos = repository.allLancamentos.asLiveData()
    }

    fun inserir(lancamento: Lancamento) = viewModelScope.launch {
        repository.insert(lancamento)
    }
}