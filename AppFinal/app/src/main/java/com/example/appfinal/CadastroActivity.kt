package com.example.appfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfinal.database.AppDatabase
import com.example.appfinal.database.User
import com.example.appfinal.database.UserDao
import com.example.appfinal.databinding.ActivityCadastroBinding // Assuma que o layout é activity_cadastro.xml
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializa o View Binding
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o DAO
        userDao = AppDatabase.getDatabase(this).userDao()

        // Ajuste os IDs conforme o seu layout XML de cadastro
        binding.buttonCadastrar.setOnClickListener {
            handleCadastro()
        }
    }

    private fun handleCadastro() {
        // Ajuste os IDs dos EditTexts conforme o seu layout
        val email = binding.editTextEmail.text.toString().trim()
        val senha = binding.editTextSenha.text.toString()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        // Usa CoroutineScope para operação assíncrona de banco de dados
        CoroutineScope(Dispatchers.IO).launch {
            // 1. Verifica se o email já está cadastrado
            val existingUser = userDao.findUserByEmail(email)

            // Voltar para a Thread principal (Main) para exibir mensagens de UI
            withContext(Dispatchers.Main) {
                if (existingUser != null) {
                    Toast.makeText(this@CadastroActivity, "E-mail já cadastrado.", Toast.LENGTH_LONG).show()
                } else {
                    // 2. Insere o novo usuário
                    val newUser = User(email = email, password = senha)
                    // Volta para o Dispatchers.IO para inserir
                    launch(Dispatchers.IO) {
                        userDao.insertUser(newUser)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@CadastroActivity, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            // Após o cadastro, volta para a tela de login
                            finish()
                        }
                    }
                }
            }
        }
    }
}