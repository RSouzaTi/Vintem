package br.edu.utfpr.vintem.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lancamentos")
data class Lancamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val valor: Double,
    val descricao: String,
    val data: String,
    val tipo: String,// "Receita" ou "Despesa"
    val categoria: String = "Geral" // Novo campo
)