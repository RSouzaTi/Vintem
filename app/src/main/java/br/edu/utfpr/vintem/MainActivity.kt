package br.edu.utfpr.vintem

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.View
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
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LancamentoViewModel by viewModels()
    private lateinit var adapter: LancamentoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialização do Adapter
        adapter = LancamentoAdapter(listOf())
        binding.rvLancamentos.layoutManager = LinearLayoutManager(this)
        binding.rvLancamentos.adapter = adapter

        // Configurações Iniciais
        configurarSwipeParaExcluir()

        // Observador dos dados
        viewModel.allLancamentos.observe(this) { lista ->
            adapter.atualizarLista(lista)
            atualizarSaldo(lista)
            configurarGrafico(lista)

            // Empty State
            binding.tvAvisoVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            binding.pieChart.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        }

        // Cliques
        binding.fabAdicionar.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }

        binding.btnImprimir.setOnClickListener {
            val listaAtual = viewModel.allLancamentos.value ?: emptyList()
            if (listaAtual.isNotEmpty()) {
                imprimirRelatorioPDF(listaAtual)
            } else {
                Toast.makeText(this, "Não há lançamentos para imprimir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarGrafico(lista: List<Lancamento>) {
        // Mostra gastos por categoria (Passo 01)
        val despesas = lista.filter { it.tipo == "Despesa" }
        val gastosPorCategoria = despesas.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.valor } }

        val entradas = ArrayList<PieEntry>()
        gastosPorCategoria.forEach { (cat, valor) ->
            entradas.add(PieEntry(valor.toFloat(), cat))
        }

        val dataSet = PieDataSet(entradas, "")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.centerText = "Gastos"
        binding.pieChart.invalidate()
    }

    private fun atualizarSaldo(lista: List<Lancamento>) {
        var total = 0.0
        lista.forEach {
            if (it.tipo == "Receita") total += it.valor else total -= it.valor
        }

        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        binding.tvSaldoTotal.text = formatador.format(total)

        val cor = if (total >= 0) R.color.receita_green else R.color.despesa_red
        binding.tvSaldoTotal.setTextColor(getColor(cor))
    }

    private fun configurarSwipeParaExcluir() {
        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicao = viewHolder.adapterPosition
                val item = adapter.lista[posicao]

                viewModel.deletar(item)

                Snackbar.make(binding.root, "Item removido", Snackbar.LENGTH_LONG)
                    .setAction("DESFAZER") { viewModel.inserir(item) }
                    .show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.rvLancamentos)
    }

    private fun imprimirRelatorioPDF(lista: List<Lancamento>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        paint.textSize = 20f
        canvas.drawText("Relatório Vintém", 40f, 50f, paint)

        paint.textSize = 12f
        var y = 100f
        lista.forEach {
            canvas.drawText("${it.data} - ${it.descricao} (${it.categoria})", 40f, y, paint)
            val valorTxt = if(it.tipo == "Receita") "+${it.valor}" else "-${it.valor}"
            canvas.drawText(valorTxt, 480f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Vintem_Relatorio.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF Gerado: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}