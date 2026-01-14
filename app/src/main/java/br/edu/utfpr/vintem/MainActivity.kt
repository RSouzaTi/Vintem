package br.edu.utfpr.vintem

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.utfpr.vintem.databinding.ActivityMainBinding
import br.edu.utfpr.vintem.view.CadastroActivity
import br.edu.utfpr.vintem.view.LancamentoAdapter
import br.edu.utfpr.vintem.viewmodel.LancamentoViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Instancia a ViewModel para pegar os dados do banco
    private val viewModel: LancamentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar o RecyclerView
        val adapter = LancamentoAdapter(listOf()) // Começa com lista vazia
        binding.rvLancamentos.layoutManager = LinearLayoutManager(this)
        binding.rvLancamentos.adapter = adapter

        // 2. Observar os dados da ViewModel (A mágica do MVVM)
        // Sempre que o banco mudar, este bloco será executado automaticamente
        viewModel.allLancamentos.observe(this) { lista ->
            adapter.atualizarLista(lista)
            atualizarSaldo(lista)
        }

        // 3. Configurar o botão para abrir a tela de cadastro
        binding.fabAdicionar.setOnClickListener {
            val intent = Intent(this, CadastroActivity::class.java)
            startActivity(intent)
        }
    }

    // Função simples para calcular e exibir o saldo no Card
    private fun atualizarSaldo(lista: List<br.edu.utfpr.vintem.model.Lancamento>) {
        var total = 0.0
        lista.forEach {
            if (it.tipo == "Receita") total += it.valor else total -= it.valor
        }
        binding.tvSaldoTotal.text = String.format("R$ %.2f", total)
    }
}