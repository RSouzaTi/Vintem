package br.edu.utfpr.vintem.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.vintem.R
import br.edu.utfpr.vintem.databinding.ActivityCadastroBinding
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.viewmodel.LancamentoViewModel
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val viewModel: LancamentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Chamadas organizadas
        configurarMascaraMoeda()
        configurarSpinnerCategoria() // Chamar uma vez no onCreate
        configurarData()
        configurarBotaoSalvar()
    }

    private fun configurarSpinnerCategoria() {
        // Correção: O Context correto aqui é 'this' (a Activity)
        ArrayAdapter.createFromResource(
            this,
            R.array.categorias_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategoria.adapter = adapter
        }
    }

    private fun configurarMascaraMoeda() {
        binding.etValor.addTextChangedListener(object : TextWatcher {
            private var atual = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != atual) {
                    val limpo = s.toString().replace("[^\\d]".toRegex(), "")
                    if (limpo.isEmpty()) return

                    val parsed = limpo.toDouble() / 100
                    val formatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed)

                    atual = formatado
                    binding.etValor.setText(formatado)
                    binding.etValor.setSelection(formatado.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarData() {
        binding.etData.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, ano, mes, dia ->
                val dataFormatada = String.format("%02d/%02d/%d", dia, mes + 1, ano)
                binding.etData.setText(dataFormatada)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun configurarBotaoSalvar() {
        binding.btnSalvar.setOnClickListener {
            val valorTexto = binding.etValor.text.toString()
            val desc = binding.etDescricao.text.toString()
            val data = binding.etData.text.toString()
            val categoria = binding.spinnerCategoria.selectedItem.toString() // Pega a categoria!

            // Validações
            if (valorTexto.isEmpty() || valorTexto == "R$ 0,00") {
                binding.etValor.error = "Campo obrigatório"
                return@setOnClickListener
            }
            if (desc.trim().isEmpty()) {
                binding.etDescricao.error = "Dê uma descrição"
                return@setOnClickListener
            }
            if (data.isEmpty()) {
                binding.etData.error = "Selecione a data"
                return@setOnClickListener
            }

            try {
                // Converte o texto da moeda para Double puro
                val valorFinal = valorTexto
                    .replace("[R$\\s]".toRegex(), "")
                    .replace(".", "")
                    .replace(",", ".")
                    .toDouble()

                val tipo = if (binding.rbReceita.isChecked) "Receita" else "Despesa"

                // Cria o objeto com a CATEGORIA incluída (Passo 01)
                val novo = Lancamento(
                    valor = valorFinal,
                    descricao = desc,
                    data = data,
                    tipo = tipo,
                    categoria = categoria
                )

                viewModel.inserir(novo)
                finish()

            } catch (e: Exception) {
                AlertDialog.Builder(this)
                    .setTitle("Erro")
                    .setMessage("Erro ao processar valor: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}