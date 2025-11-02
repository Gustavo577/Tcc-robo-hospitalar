package com.example.appfinal

import android.Manifest
import android.bluetooth.*
import android.content.*
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*

class TelaBlu : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var connectButton: Button
    private lateinit var statusText: TextView

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var selectedDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null

    private val uuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val PERMISSION_REQUEST_CODE = 101

    private lateinit var discoveryReceiver: BroadcastReceiver
    private val deviceList = ArrayList<String>()
    private val deviceMap = HashMap<String, BluetoothDevice>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_blu)

        listView = findViewById(R.id.listViewDevices)
        connectButton = findViewById(R.id.buttonConnect)
        statusText = findViewById(R.id.textViewStatus)

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não é suportado neste dispositivo", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceList)
        listView.adapter = adapter

        discoveryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            ContextCompat.checkSelfPermission(this@TelaBlu, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            return
                        }

                        val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        }

                        device?.let {
                            val name = it.name ?: "Desconhecido"
                            val deviceInfo = "$name - ${it.address}"
                            if (!deviceMap.containsKey(deviceInfo)) {
                                deviceList.add(deviceInfo)
                                deviceMap[deviceInfo] = it
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Toast.makeText(applicationContext, "Busca finalizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        registerReceiver(discoveryReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
        registerReceiver(discoveryReceiver, IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))

        connectButton.setOnClickListener {
            if (!temPermissoesBluetooth()) {
                Toast.makeText(this, "Permissões Bluetooth não concedidas", Toast.LENGTH_SHORT).show()
                pedirPermissoesBluetooth()
                return@setOnClickListener
            }

            if (selectedDevice == null) {
                Toast.makeText(this, "Selecione um dispositivo para conectar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Thread {
                try {
                    val device = selectedDevice!!

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        runOnUiThread {
                            Toast.makeText(this, "Permissão BLUETOOTH_CONNECT não concedida", Toast.LENGTH_SHORT).show()
                        }
                        return@Thread
                    }

                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    bluetoothAdapter?.cancelDiscovery()
                    bluetoothSocket?.connect()

                    runOnUiThread {
                        Toast.makeText(this, "Conectado com ${device.name}", Toast.LENGTH_SHORT).show()
                        statusText.text = "Conectado com ${device.name}"

                        // Atribui o socket Bluetooth para a classe TelaLog
                        TelaLog.bluetoothSocket = bluetoothSocket

                        // Cria um Intent para iniciar a TelaLog
                        val intent = Intent(this, TelaLog::class.java)

                        // Inicia a nova Activity
                        startActivity(intent)

                        // Finaliza a TelaBlu
                        finish()
                    }
                } catch (e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this, "Erro na conexão: ${e.message}", Toast.LENGTH_LONG).show()
                        statusText.text = "Erro na conexão"
                    }
                    e.printStackTrace()
                }
            }.start()
        }


        listView.setOnItemClickListener { _, _, position, _ ->
            val deviceInfo = deviceList[position]
            selectedDevice = deviceMap[deviceInfo]
            statusText.text = "Selecionado: ${selectedDevice?.name}"
        }

        if (!temPermissoesBluetooth()) {
            pedirPermissoesBluetooth()
        } else {
            verificarLocalizacaoAtivada()
            inicializarBluetooth()
            iniciarBuscaBluetooth()
        }
    }

    override fun onResume() {
        super.onResume()
        if (temPermissoesBluetooth()) {
            verificarLocalizacaoAtivada()
            inicializarBluetooth()
            iniciarBuscaBluetooth()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(discoveryReceiver)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Permissões concedidas", Toast.LENGTH_SHORT).show()
                verificarLocalizacaoAtivada()
                inicializarBluetooth()
                iniciarBuscaBluetooth()
            } else {
                Toast.makeText(this, "Permissões negadas", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun temPermissoesBluetooth(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun pedirPermissoesBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun verificarLocalizacaoAtivada() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Ative a localização para detectar dispositivos", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    private fun inicializarBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissão BLUETOOTH_CONNECT não concedida", Toast.LENGTH_SHORT).show()
            return
        }

        if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, PERMISSION_REQUEST_CODE)
        }

        val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
        deviceList.clear()
        deviceMap.clear()

        pairedDevices.forEach { device ->
            val name = device.name ?: "Desconhecido"
            val deviceInfo = "$name - ${device.address}"
            deviceList.add(deviceInfo)
            deviceMap[deviceInfo] = device
        }
        adapter.notifyDataSetChanged()
    }

    private fun iniciarBuscaBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val hasScanPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            if (hasScanPermission) {
                try {
                    if (bluetoothAdapter?.isDiscovering == true) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                    bluetoothAdapter?.startDiscovery()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Permissão BLUETOOTH_SCAN não concedida", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            val hasLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (hasLocationPermission) {
                try {
                    if (bluetoothAdapter?.isDiscovering == true) {
                        bluetoothAdapter.cancelDiscovery()
                    }
                    bluetoothAdapter?.startDiscovery()
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}