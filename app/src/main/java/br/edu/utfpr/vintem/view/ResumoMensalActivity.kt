package br.edu.utfpr.vintem.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.vintem.databinding.ActivityResumoMensalBinding
import br.edu.utfpr.vintem.viewmodel.LancamentoViewModel
import kotlin.collections.emptyList

class ResumoMensalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoMensalBinding
    private val viewModel: LancamentoViewModel by viewModels()
    private lateinit var adapter: ResumoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoMensalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar o botão de voltar na barra superior
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Resumo por Mês"

        // Inicializar o Adapter com lista vazia
        adapter = ResumoAdapter(emptyList())
        binding.rvResumo.layoutManager = LinearLayoutManager(this)
        binding.rvResumo.adapter = adapter

        // Observar os dados do resumo mensal
        // Usamos .asLiveData() porque o DAO retorna um Flow
        viewModel.resumoMensal.observe(this) { lista ->
            adapter.lista = lista
            adapter.notifyDataSetChanged()
        }
    }

    // Faz o botão de voltar da barra superior funcionar
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}