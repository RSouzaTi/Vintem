package br.edu.utfpr.vintem.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.vintem.R
import br.edu.utfpr.vintem.databinding.ItemLancamentoBinding
import br.edu.utfpr.vintem.model.Lancamento
import java.text.NumberFormat
import java.util.Locale

class LancamentoAdapter(var lista: List<Lancamento>) :
    RecyclerView.Adapter<LancamentoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLancamentoBinding) : RecyclerView.ViewHolder(binding.root)

    // Formatador instanciado uma única vez para economizar memória
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLancamentoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lancamento = lista[position]
        val context = holder.itemView.context

        with(holder.binding) {
            tvDescricao.text = lancamento.descricao
            tvData.text = lancamento.data

            // Exibe a categoria (Certifique-se que esse ID existe no seu XML)
            tvCategoria.text = lancamento.categoria

            val valorFormatado = formatador.format(lancamento.valor)

            if (lancamento.tipo == "Receita") {
                tvValor.text = "+ $valorFormatado"
                tvValor.setTextColor(ContextCompat.getColor(context, R.color.receita_green))
            } else {
                tvValor.text = "- $valorFormatado"
                tvValor.setTextColor(ContextCompat.getColor(context, R.color.despesa_red))
            }
        }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Lancamento>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}