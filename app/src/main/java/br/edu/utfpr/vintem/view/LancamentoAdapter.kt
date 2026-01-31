package br.edu.utfpr.vintem.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.vintem.R
import br.edu.utfpr.vintem.databinding.ItemLancamentoBinding
import br.edu.utfpr.vintem.model.Lancamento
import java.text.NumberFormat
import java.util.Locale

class LancamentoAdapter(var lista: List<Lancamento>) :
    RecyclerView.Adapter<LancamentoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLancamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLancamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // O formatador está certinho aqui fora por performance!
    private val formatador = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lancamento = lista[position]

        // --- A LINHA QUE FALTAVA ESTÁ AQUI EMBAIXO ---
        val valorFormatado = formatador.format(lancamento.valor)

        // Preenche os textos básicos
        holder.binding.tvDescricao.text = lancamento.descricao
        holder.binding.tvData.text = lancamento.data

        // Aplica a lógica de cor e o valor formatado
        holder.binding.tvValor.apply {
            if (lancamento.tipo == "Receita") {
                text = "+ $valorFormatado"
                setTextColor(context.getColor(R.color.receita_green))
            } else {
                text = "- $valorFormatado"
                setTextColor(context.getColor(R.color.despesa_red))
            }
        }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Lancamento>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}