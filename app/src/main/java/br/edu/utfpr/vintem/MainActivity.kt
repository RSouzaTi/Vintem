package br.edu.utfpr.vintem

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.vintem.databinding.ActivityMainBinding
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.view.CadastroActivity
import br.edu.utfpr.vintem.view.LancamentoAdapter
import br.edu.utfpr.vintem.viewmodel.LancamentoViewModel



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LancamentoViewModel by viewModels()

    // 1. Declare aqui para que toda a classe possa ver o adapter
    private lateinit var adapter: LancamentoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.fabAdicionar.setOnClickListener {
            val intent = Intent(this,
                CadastroActivity::class.java)
            startActivity(intent)
        }

        // 2. Inicialize (sem o "val" na frente!)
        adapter = LancamentoAdapter(listOf())

        binding.rvLancamentos.layoutManager = LinearLayoutManager(this)
        binding.rvLancamentos.adapter = adapter

        // Agora o código do Swipe (deslizar) vai funcionar!
        configurarSwipeParaExcluir()

        viewModel.allLancamentos.observe(this) { lista ->
            adapter.atualizarLista(lista)
            atualizarSaldo(lista)
        }

        binding.fabAdicionar.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    private fun configurarSwipeParaExcluir() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicao = viewHolder.adapterPosition
                // Agora o 'adapter' é reconhecido aqui!
                val lancamentoParaDeletar = adapter.lista[posicao]
                viewModel.deletar(lancamentoParaDeletar)
                Toast.makeText(this@MainActivity, "Vintém excluído!", Toast.LENGTH_SHORT).show()
            }
        }
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvLancamentos)
    }

    private fun atualizarSaldo(lista: List<Lancamento>) {
        var total = 0.0
        lista.forEach {
            if (it.tipo == "Receita") total += it.valor else total -= it.valor
        }
        binding.tvSaldoTotal.text = String.format("R$ %.2f", total)
    }
}