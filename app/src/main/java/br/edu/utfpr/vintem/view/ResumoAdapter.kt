package br.edu.utfpr.vintem.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.vintem.R
import br.edu.utfpr.vintem.databinding.ItemResumoMensalBinding
import br.edu.utfpr.vintem.model.ResumoMensal // IMPORTANTE: Importar a sua model
import java.text.NumberFormat
import java.util.Locale

// Trocamos List<T> por List<ResumoMensal>
class ResumoAdapter(var lista: List<ResumoMensal>) :
    RecyclerView.Adapter<ResumoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemResumoMensalBinding) : RecyclerView.ViewHolder(binding.root)

    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResumoMensalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val context = holder.itemView.context

        // Agora o Kotlin reconhece totalReceita e totalDespesa porque sabe que o item é ResumoMensal
        val saldoMes = item.totalReceita - item.totalDespesa

        with(holder.binding) {
            tvMesAno.text = item.mesAno
            tvReceitasValor.text = "Receitas: ${formatador.format(item.totalReceita)}"
            tvDespesasValor.text = "Despesas: ${formatador.format(item.totalDespesa)}"
            tvSaldoMes.text = "Balanço: ${formatador.format(saldoMes)}"

            // Cor do Balanço Mensal
            val corSaldo = if (saldoMes >= 0) R.color.receita_green else R.color.despesa_red
            tvSaldoMes.setTextColor(ContextCompat.getColor(context, corSaldo))
        }
    }

    override fun getItemCount() = lista.size
}