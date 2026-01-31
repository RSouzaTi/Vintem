package br.edu.utfpr.vintem.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
        // Inicializa o View Binding corretamente
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarData()
        configurarBotaoSalvar()
    }

    private fun configurarData() {
        binding.etData.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, ano, mes, dia ->
                    val dataFormatada = String.format("%02d/%02d/%d", dia, mes + 1, ano)
                    binding.etData.setText(dataFormatada)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun configurarMascaraMoeda() {
        binding.etValor.addTextChangedListener(object : TextWatcher {
            private var atual = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != atual) {
                    // Remove tudo que não é número
                    val limpo = s.toString().replace("[^\\d]".toRegex(), "")
                    if (limpo.isEmpty()) return

                    val parsed = limpo.toDouble() / 100
                    // Força o padrão brasileiro: R$ 1.000,00
                    val formatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(parsed)

                    atual = formatado
                    binding.etValor.setText(formatado)
                    binding.etValor.setSelection(formatado.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun configurarBotaoSalvar() {
        binding.btnSalvar.setOnClickListener {
            val valorBruto = binding.etValor.text.toString()
            val desc = binding.etDescricao.text.toString()
            // Pega o texto: "R$ 1.250,50"
            val valorTexto = binding.etValor.text.toString()

        // Transforma em: "1250.50"
            val valorParaBanco = valorTexto
                .replace("[R$\\s]".toRegex(), "") // Remove R$ e espaços
                .replace(".", "")                 // Remove os pontos de milhar
                .replace(",", ".")                // Troca a vírgula decimal por ponto

            val valorFinal = valorParaBanco.toDoubleOrNull() ?: 0.0

            // 1. VALIDAÇÃO PROATIVA (UX Melhor)
            if (valorBruto.isEmpty() || valorBruto == "R$ 0,00") {
                binding.etValor.error = "Campo obrigatório"
                return@setOnClickListener
            }
            if (desc.trim().isEmpty()) {
                binding.etDescricao.error = "Dê uma descrição"
                return@setOnClickListener
            }

            // 2. TENTATIVA DE CONVERSÃO COM TRY-CATCH (Segurança Extra)
            try {
                // Limpa a máscara para converter
                val valorFormatado = valorBruto
                    .replace("[R$\\s]".toRegex(), "")
                    .replace(".", "")
                    .replace(",", ".")

                val valorFinal = valorFormatado.toDouble()

                val tipo = if (binding.rbReceita.isChecked) "Receita" else "Despesa"
                val data = binding.etData.text.toString()

                val novo =
                    Lancamento(valor = valorFinal, descricao = desc, data = data, tipo = tipo)
                viewModel.inserir(novo)
                finish()

            } catch (e: Exception) {
                // ALERTA DE ERRO (Caso algo muito bizarro aconteça na conversão)
                AlertDialog.Builder(this)
                    .setTitle("Erro ao salvar")
                    .setMessage("Verifique os dados digitados. Erro: ${e.message}")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }
}