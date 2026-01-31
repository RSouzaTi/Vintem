package br.edu.utfpr.vintem

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
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
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale


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
            val intent = Intent(
                this,
                CadastroActivity::class.java
            )
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
        binding.btnImprimir.setOnClickListener {
            val listaAtual = viewModel.allLancamentos.value ?: emptyList()
            if (listaAtual.isNotEmpty()) {
                imprimirRelatorioPDF(listaAtual)
            } else {
                Toast.makeText(this, "Não há lançamentos para imprimir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun imprimirRelatorioPDF(lista: List<Lancamento>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()

        // Configura a página (A4 aprox. 595x842 pontos)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Título
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("Relatório do Vintém", 40f, 50f, paint)

        // Cabeçalho da tabela
        paint.textSize = 14f
        paint.isFakeBoldText = false
        canvas.drawText("Data", 40f, 100f, paint)
        canvas.drawText("Descrição", 150f, 100f, paint)
        canvas.drawText("Valor", 450f, 100f, paint)

        // Linha divisória
        canvas.drawLine(40f, 110f, 550f, 110f, paint)

        // Itens da lista
        var yPos = 140f
        lista.forEach {
            canvas.drawText(it.data, 40f, yPos, paint)
            canvas.drawText(it.descricao, 150f, yPos, paint)

            val valorTexto = if (it.tipo == "Receita") "+ ${it.valor}" else "- ${it.valor}"
            canvas.drawText(valorTexto, 450f, yPos, paint)

            yPos += 30f // Pula para a próxima linha
        }

        pdfDocument.finishPage(page)

        // Salva o arquivo na pasta de Downloads
        val file =
            File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Relatorio_Vintem.pdf")

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(this, "PDF salvo em Downloads!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Erro ao gerar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
    private fun atualizarSaldo(lista: List<Lancamento>) {
        var total = 0.0
        // Calcula a soma (Receitas somam, Despesas subtraem)
        lista.forEach {
            if (it.tipo == "Receita") total += it.valor else total -= it.valor
        }

        // Formata o valor final para o padrão R$ 0.000,00
        val formatador = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("pt", "BR"))
        binding.tvSaldoTotal.text = formatador.format(total)

        // Opcional: Mudar a cor do saldo (Verde se positivo, Vermelho se negativo)
        if (total >= 0) {
            binding.tvSaldoTotal.setTextColor(getColor(R.color.receita_green))
        } else {
            binding.tvSaldoTotal.setTextColor(getColor(R.color.despesa_red))
        }
    }

    private fun configurarSwipeParaExcluir() {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 1. Identifica a posição e o item que foi deslizado
                val posicao = viewHolder.adapterPosition
                val itemExcluido = adapter.lista[posicao]

                // 2. Remove do Banco de Dados através da ViewModel
                viewModel.deletar(itemExcluido)

                // 3. Cria o Snackbar (a barrinha que sobe)
                Snackbar.make(binding.root, "Vintém excluído!", Snackbar.LENGTH_LONG)
                    .setAction("DESFAZER") {
                        // 4. Se o usuário clicar em DESFAZER, inserimos o item de volta
                        viewModel.inserir(itemExcluido)

                        // Dica: Como estamos usando LiveData/Flow, a lista no
                        // RecyclerView vai atualizar sozinha quando o item for reinserido!
                    }
                    .setActionTextColor(getColor(R.color.receita_green)) // Cor do texto do botão
                    .show()
            }
        }


         fun atualizarSaldo(lista: List<Lancamento>) {
            var total = 0.0
            lista.forEach {
                if (it.tipo == "Receita") total += it.valor else total -= it.valor
            }

            // Usa a mesma formatação aqui!
            val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            binding.tvSaldoTotal.text = formatador.format(total)
        }
    }
}