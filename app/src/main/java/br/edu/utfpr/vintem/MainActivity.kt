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
import br.edu.utfpr.vintem.view.ResumoMensalActivity
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

        binding.cardResumo.setOnClickListener {
            val intent = Intent(this, ResumoMensalActivity::class.java)
            startActivity(intent)
        }
        binding.btnImprimir.setOnClickListener {
            // Verifica se já tem permissão (para Android 9 ou inferior)
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }

            val listaAtual = viewModel.allLancamentos.value ?: emptyList()
            if (listaAtual.isNotEmpty()) {
                imprimirRelatorioPDF(listaAtual)
            } else {
                Toast.makeText(this, "Não há lançamentos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarGrafico(lista: List<Lancamento>) {
        val despesas = lista.filter { it.tipo == "Despesa" }
        val gastosPorCategoria = despesas.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.valor } }

        val entradas = ArrayList<PieEntry>()
        gastosPorCategoria.forEach { (cat, valor) ->
            entradas.add(PieEntry(valor.toFloat(), cat))
        }

        val dataSet = PieDataSet(entradas, "") // Criando o dataSet aqui
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f

        // --- LÓGICA PARA MODO ESCURO ---
        val isNightMode = (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        if (isNightMode) {
            val corBranca = getColor(R.color.white)

            // Cor do texto dos valores nas fatias
            dataSet.valueTextColor = corBranca

            // Cor da legenda e do texto central
            binding.pieChart.legend.textColor = corBranca
            binding.pieChart.setCenterTextColor(corBranca)
            binding.pieChart.setEntryLabelColor(corBranca) // Nomes das categorias nas fatias
        }

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
        val callback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val posicao = viewHolder.adapterPosition
                val item = adapter.lista[posicao]

                viewModel.deletar(item)

                Snackbar.make(binding.root, "Item removido", Snackbar.LENGTH_LONG)
                    .setAction("DESFAZER") { viewModel.insert(item) }
                    .show()
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.rvLancamentos)
    }

    private fun imprimirRelatorioPDF(lista: List<Lancamento>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val tituloPaint = Paint()

        // Configura a página (A4: 595 x 842 pontos)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Título do Relatório
        tituloPaint.textSize = 24f
        tituloPaint.isFakeBoldText = true
        canvas.drawText("Relatório Vintém", 40f, 60f, tituloPaint)

        // 2. Cabeçalho da Tabela
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Data", 40f, 110f, paint)
        canvas.drawText("Descrição (Categoria)", 120f, 110f, paint)
        canvas.drawText("Valor", 480f, 110f, paint)

        // Linha divisória após o cabeçalho
        canvas.drawLine(40f, 120f, 550f, 120f, paint)

        // 3. Listagem dos Itens
        paint.isFakeBoldText = false
        var yPos = 150f
        val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        lista.forEach { item ->
            // Se a página estiver cheia, você precisaria criar uma nova (simplificado aqui)
            if (yPos > 800f) return@forEach

            canvas.drawText(item.data, 40f, yPos, paint)
            canvas.drawText("${item.descricao} (${item.categoria})", 120f, yPos, paint)

            val valorTexto = if (item.tipo == "Receita") {
                "+ ${formatador.format(item.valor)}"
            } else {
                "- ${formatador.format(item.valor)}"
            }
            canvas.drawText(valorTexto, 480f, yPos, paint)

            yPos += 30f // Espaçamento entre linhas
        }

        pdfDocument.finishPage(page)

        // 4. Gravação do Arquivo
        // Usamos a pasta de Downloads pública para facilitar o acesso do usuário
        val diretorio =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(diretorio, "Relatorio_Vintem_${System.currentTimeMillis()}.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF salvo em Downloads!", Toast.LENGTH_LONG).show()

            // Abre o PDF logo após salvar
            abrirPDF(file)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }

    private fun abrirPDF(file: File) {
        try {
            // O "packageName + .provider" deve ser igual ao declarado no Manifest
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY

            startActivity(Intent.createChooser(intent, "Abrir Relatório com:"))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Instale um leitor de PDF para visualizar o arquivo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}