package com.example.appfinal

import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.InputStream
import java.util.ArrayList
// Importações adicionadas para formatar data e hora
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TelaLog : AppCompatActivity() {

    private lateinit var statusTextView: TextView
    private lateinit var rfidLogListView: ListView
    private lateinit var logAdapter: ArrayAdapter<String>
    private val rfidLogList = ArrayList<String>()

    // Use um companion object para o socket, como na resposta anterior.
    companion object {
        var bluetoothSocket: BluetoothSocket? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_log)

        statusTextView = findViewById(R.id.textView4)
        rfidLogListView = findViewById(R.id.listViewRfidLog)

        // Configura o adapter para o ListView
        logAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rfidLogList)
        rfidLogListView.adapter = logAdapter

        if (bluetoothSocket != null && bluetoothSocket!!.isConnected) {
            statusTextView.text = "Status da Conexão: Conectado"
            startReadThread()
        } else {
            statusTextView.text = "Status da Conexão: Desconectado"
        }
    }

    private fun startReadThread() {
        // Objeto para formatar a hora (ex: 14:30:59)
        // O formato "HH:mm:ss" mostra Hora, Minuto e Segundo.
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        // Usa um buffer para ler os dados do socket
        val buffer = ByteArray(1024)
        var bytes: Int
        val inputStream: InputStream

        try {
            inputStream = bluetoothSocket!!.inputStream
        } catch (e: IOException) {
            runOnUiThread { statusTextView.text = "Erro: Conexão perdida" }
            return
        }

        // Inicia a thread para leitura dos dados
        Thread {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    bytes = inputStream.read(buffer)
                    val receivedData = String(buffer, 0, bytes).trim()

                    if (receivedData.isNotEmpty()) {
                        // 1. Obtém a hora atual do sistema formatada
                        val currentTime = timeFormatter.format(Date())

                        runOnUiThread {
                            // 2. Cria a nova entrada de log combinando hora e leitura
                            // Exemplo: "14:30:59 - Cartão lido: 1A2B3C4D"
                            val logEntry = "$currentTime - Cartão lido: $receivedData"

                            // 3. Adiciona a leitura à lista e notifica o adapter
                            rfidLogList.add(logEntry)
                            logAdapter.notifyDataSetChanged()

                            // Rola para a última leitura
                            rfidLogListView.smoothScrollToPosition(rfidLogList.size - 1)
                        }
                    }
                } catch (e: IOException) {
                    runOnUiThread { statusTextView.text = "Conexão perdida com o dispositivo" }
                    break
                }
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fecha o socket e a conexão quando a tela é destruída
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}