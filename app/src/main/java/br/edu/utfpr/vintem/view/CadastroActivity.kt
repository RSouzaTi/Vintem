package br.edu.utfpr.vintem.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import br.edu.utfpr.vintem.databinding.ActivityCadastroBinding
import br.edu.utfpr.vintem.model.Lancamento
import br.edu.utfpr.vintem.viewmodel.LancamentoViewModel
import java.util.*

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private val viewModel: LancamentoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Chamamos as funções que organizam o código
        configurarData()
        configurarBotaoSalvar()
    }

    private fun configurarData() {
        binding.etData.setOnClickListener {
            val cal = Calendar.getInstance()
            val ano = cal.get(Calendar.YEAR)
            val mes = cal.get(Calendar.MONTH)
            val dia = cal.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, year, month, day ->
                val dataFormatada = String.format("%02d/%02d/%d", day, month + 1, year)
                binding.etData.setText(dataFormatada)
            }, ano, mes, dia).show()
        }
    }

    private fun configurarBotaoSalvar() {
        binding.btnSalvar.setOnClickListener {
            val valorString = binding.etValor.text.toString()
            val desc = binding.etDescricao.text.toString()
            val data = binding.etData.text.toString()
            val tipo = if (binding.rbReceita.isChecked) "Receita" else "Despesa"

            if (validarCampos(valorString, desc, data)) {
                val novoLancamento = Lancamento(
                    valor = valorString.toDouble(),
                    descricao = desc,
                    data = data,
                    tipo = tipo
                )

                // Aqui é onde a mágica do MVVM acontece:
                viewModel.inserir(novoLancamento)

                Toast.makeText(this, "Vintém salvo com sucesso!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun validarCampos(valor: String, desc: String, data: String): Boolean {
        if (valor.isEmpty() || desc.isEmpty() || data.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos do Vintém!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}