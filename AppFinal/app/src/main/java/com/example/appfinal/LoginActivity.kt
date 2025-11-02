package com.example.appfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

// ⭐️ IMPORTS NECESSÁRIOS
import com.example.appfinal.database.AppDatabase
import com.example.appfinal.database.UserDao
import com.example.appfinal.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    // ⭐️ Declaração do DAO
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ⭐️ Inicializa o DAO
        userDao = AppDatabase.getDatabase(this).userDao()

        // 3. Atribui a função de login ao botão (btLogin do seu layout)
        binding.btLogin.setOnClickListener {
            validarLogin()
        }

        // 💡 tela de cadastro
        binding.btCadastro.setOnClickListener {
         IrParaCadastro() }

    }

    private fun IrParaCadastro(){
        val intent = Intent(this, CadastroActivity::class.java)
        startActivity(intent)
    }

    private fun validarLogin() {
        val email = binding.etEmail.text.toString().trim()
        val senha = binding.etSenha.text.toString()

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha o email e a senha.", Toast.LENGTH_SHORT).show()
            return
        }

        // ⭐️ Lança a Coroutine para operação de banco de dados em background
        CoroutineScope(Dispatchers.IO).launch {

            // Busca o usuário com as credenciais fornecidas
            val user = userDao.login(email, senha)

            // ⭐️ Retorna para a Thread principal para atualizar a UI
            withContext(Dispatchers.Main) {
                if (user != null) {
                    // Login bem-sucedido
                    Toast.makeText(this@LoginActivity, "Login efetuado. Bem-vindo!", Toast.LENGTH_LONG).show()
                    irParaSegundaTela()
                } else {
                    // Credenciais inválidas
                    Toast.makeText(this@LoginActivity, "Email ou senha incorretos.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun irParaSegundaTela(){
        // Altere 'SegundaTela::class.java' para a sua Activity principal
        val segundaTela = Intent(this, SegundaTela::class.java)
        startActivity(segundaTela)
        finish() // Impede o retorno à tela de login
    }
}