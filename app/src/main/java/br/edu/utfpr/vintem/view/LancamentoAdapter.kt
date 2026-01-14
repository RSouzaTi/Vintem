package br.edu.utfpr.vintem.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.edu.utfpr.vintem.databinding.ItemLancamentoBinding
import br.edu.utfpr.vintem.model.Lancamento

class LancamentoAdapter(private var lista: List<Lancamento>) :
    RecyclerView.Adapter<LancamentoAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemLancamentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLancamentoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lancamento = lista[position]
        holder.binding.tvDescricao.text = lancamento.descricao
        holder.binding.tvData.text = lancamento.data

        // Formata o valor e a cor
        holder.binding.tvValor.text = String.format("R$ %.2f", lancamento.valor)

        if (lancamento.tipo == "Despesa") {
            holder.binding.tvValor.setTextColor(Color.RED)
        } else {
            holder.binding.tvValor.setTextColor(Color.parseColor("#2E7D32")) // Verde escuro
        }
    }

    override fun getItemCount() = lista.size

    fun atualizarLista(novaLista: List<Lancamento>) {
        lista = novaLista
        notifyDataSetChanged()
    }
}