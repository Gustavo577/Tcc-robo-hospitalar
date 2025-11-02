package com.example.appfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.appfinal.databinding.ActivitySegundaTelaBinding

class SegundaTela : AppCompatActivity() {

    //Código para importar biblioteca Binding, que permite reconhecer e usar os componentes presentes na tela

    private lateinit var  binding: ActivitySegundaTelaBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySegundaTelaBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Função para ir para a tela de Logs ao clicar no botão "btLog"

        binding.btLog.setOnClickListener(){

            val IrTelaLog = Intent(this, TelaLog::class.java)
            startActivity(IrTelaLog)
        }

        //Função para ir para a tela de Registros ao clicar no botão "btReg"

        binding.btReg.setOnClickListener(){

            val IrTelaReg = Intent(this, UserListActivity::class.java)
            startActivity(IrTelaReg)
        }

        //Função para ir para a tela de Bluetooth ao clicar no botão "btBlu"

        binding.btBlu.setOnClickListener(){

            val IrTelaBlu = Intent(this, TelaBlu::class.java)
            startActivity(IrTelaBlu)
        }


    }
}