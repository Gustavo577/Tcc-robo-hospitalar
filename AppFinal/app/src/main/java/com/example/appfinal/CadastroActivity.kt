package com.example.appfinal

import android.content.Intent
import android.os.Bundle
import android.util.Patterns // Necessário para validar o formato do e-mail
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appfinal.database.AppDatabase
import com.example.appfinal.database.User
import com.example.appfinal.database.UserDao
import com.example.appfinal.databinding.ActivityCadastroBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var userDao: UserDao

    // --- Funções de Validação ---

    /**
     * Verifica se a string de e-mail corresponde a um formato válido (ex: user@dominio.com).
     */
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Verifica se a senha atende ao critério de tamanho mínimo (ex: 6 caracteres).
     */
    private fun isPasswordValid(password: String): Boolean {
        // Requisito de senha: pelo menos 6 caracteres.
        return password.length >= 6
    }

    // --- Ciclo de Vida da Activity ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = AppDatabase.getDatabase(this).userDao()

        binding.buttonCadastrar.setOnClickListener {
            handleCadastro()
        }
    }

    // --- Lógica de Cadastro com Validação ---

    private fun handleCadastro() {
        // Obter e Limpar (trim) os dados
        val email = binding.editTextEmail.text.toString().trim()
        val senha = binding.editTextSenha.text.toString()

        // 1. Validação de campo vazio
        if (email.isEmpty()) {
            binding.editTextEmail.error = "O campo E-mail é obrigatório."
            return
        }
        if (senha.isEmpty()) {
            binding.editTextSenha.error = "O campo Senha é obrigatório."
            return
        }

        // 2. Validação de formato de E-mail
        if (!isEmailValid(email)) {
            binding.editTextEmail.error = "Por favor, insira um formato de e-mail válido."
            return
        }

        // 3. Validação de Senha (Tamanho Mínimo)
        if (!isPasswordValid(senha)) {
            binding.editTextSenha.error = "A senha deve ter no mínimo 6 caracteres."
            return
        }

        // Se chegou até aqui, os dados de entrada são válidos localmente.

        // Usa CoroutineScope para operação assíncrona de banco de dados
        CoroutineScope(Dispatchers.IO).launch {
            // 4. Verifica se o e-mail já está cadastrado no banco de dados
            val existingUser = userDao.findUserByEmail(email)

            // Voltar para a Thread principal (Main) para exibir mensagens de UI
            withContext(Dispatchers.Main) {

                // === BLOCO CORRIGIDO AQUI ===
                if (existingUser != null) {
                    // E-mail já existe
                    binding.editTextEmail.error = "Este e-mail já está cadastrado."
                    Toast.makeText(
                        this@CadastroActivity,
                        "E-mail já cadastrado.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // E-mail livre, inserir novo usuário
                    val newUser = User(email = email, password = senha)

                    launch(Dispatchers.IO) {
                        userDao.insertUser(newUser)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CadastroActivity,
                                "Cadastro realizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Após o cadastro, retorna para a tela anterior (Login)
                            finish()
                        }
                    }
                }
                // ============================
            }
        }
    }
}