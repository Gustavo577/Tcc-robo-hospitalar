package com.example.appfinal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appfinal.databinding.ActivityMainBinding // Classe de binding gerada

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializa o View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Exemplo de ação para o botão de Logout
        binding.btLogout.setOnClickListener {
            fazerLogout()
        }
    }

    private fun fazerLogout() {
        // Lógica de logout simples:
        // 1. Redireciona de volta para a tela de Login
        val intent = Intent(this, LoginActivity::class.java)

        // Estas flags garantem que o usuário não possa usar o botão 'Voltar'
        // para retornar à MainActivity (área logada).
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}