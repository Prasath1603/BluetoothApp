package com.example.bluetoothmanagerapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Bluetooth permissions at runtime
        requestBluetoothPermissions()

        setContent {
            BluetoothApp(this)
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothApp(context: Context) {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val currentlyConnectedDevice = remember { mutableStateOf<BluetoothDevice?>(null) }
    val previouslyConnectedDevices = remember { mutableStateOf(listOf<BluetoothDevice>()) }
    val availableDevices = remember { mutableStateOf(listOf<BluetoothDevice>()) }

    if (bluetoothAdapter == null) {
        Text("Bluetooth is not supported on this device", fontSize = 20.sp)
        return
    }

    if (!bluetoothAdapter.isEnabled) {
        Text("Please enable Bluetooth", fontSize = 20.sp)
        return
    }

    LaunchedEffect(Unit) {
        if (hasBluetoothPermissions(context)) {
            val bondedDevices = bluetoothAdapter.bondedDevices ?: emptySet()
            currentlyConnectedDevice.value = bondedDevices.find { it.isConnected() }
            previouslyConnectedDevices.value = bondedDevices.filter { !it.isConnected() }
            availableDevices.value = bluetoothAdapter.scanForDevices(context)
        } else {
            Log.e("BluetoothApp", "Missing Bluetooth permissions")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Currently Connected Device", fontSize = 20.sp)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text(
                    currentlyConnectedDevice.value?.name ?: "No device connected",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Previously Connected Devices", fontSize = 20.sp)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(previouslyConnectedDevices.value) { device ->
                Text(device.name ?: "Unnamed device", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Other Available Devices", fontSize = 20.sp)
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(availableDevices.value) { device ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(device.name ?: "Unnamed device", fontSize = 16.sp)
                    Button(onClick = { /* Connect to this device */ }) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun BluetoothAdapter.scanForDevices(context: Context): List<BluetoothDevice> {
    val devices = mutableListOf<BluetoothDevice>()
    val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let { devices.add(it) }
                }
            }
        }
    }

    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    context.registerReceiver(discoveryReceiver, filter)
    startDiscovery()

    return devices
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.isConnected(): Boolean {
    // Implement logic to check if the device is currently connected
    // Placeholder return value; replace with actual connection check
    return false
}

fun hasBluetoothPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
}
