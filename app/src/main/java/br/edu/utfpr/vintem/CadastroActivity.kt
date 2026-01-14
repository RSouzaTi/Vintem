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
        // Inicializa o View Binding corretamente
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarData()
        configurarBotaoSalvar()
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
            val valorString = binding.etValor.text.toString()
            val desc = binding.etDescricao.text.toString()
            val data = binding.etData.text.toString()
            val tipo = if (binding.rbReceita.isChecked) "Receita" else "Despesa"

            if (valorString.isNotEmpty() && desc.isNotEmpty() && data.isNotEmpty()) {
                val novoLancamento = Lancamento(
                    valor = valorString.toDouble(),
                    descricao = desc,
                    data = data,
                    tipo = tipo
                )

                viewModel.inserir(novoLancamento)
                Toast.makeText(this, "Vintém salvo!", Toast.LENGTH_SHORT).show()
                finish() // Volta para a tela principal
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}