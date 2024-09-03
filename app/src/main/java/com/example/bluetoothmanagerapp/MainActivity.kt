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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request Bluetooth permissions at runtime
        requestBluetoothPermissions()

        setContent {
            BluetoothApp()
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 1)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun BluetoothApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val currentlyConnectedDevice = remember { mutableStateOf<BluetoothDevice?>(null) }
    val availableDevices = remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }

    // BroadcastReceiver to handle discovered devices
    val discoveryReceiver = rememberUpdatedState(
        object : BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            // Update available devices list if it's not already present
                            if (!availableDevices.value.contains(device)) {
                                availableDevices.value = availableDevices.value + device
                            }
                        }
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (hasBluetoothPermissions(context)) {
            // Register receiver for Bluetooth device discovery
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(discoveryReceiver.value, filter)
            bluetoothAdapter?.startDiscovery()

            // Cleanup: Stop discovery and unregister receiver when activity is destroyed
            lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    bluetoothAdapter?.cancelDiscovery()
                    context.unregisterReceiver(discoveryReceiver.value)
                }
            })

            // Update the currently connected device
            currentlyConnectedDevice.value = bluetoothAdapter.bondedDevices
                ?.find { it.isConnected() }
        } else {
            Log.e("BluetoothApp", "Missing Bluetooth permissions")
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {

        Text(
            text = "Currently Connected Device",
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier
                .background(Color.Blue)
                .padding(8.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentlyConnectedDevice.value?.name ?: "No device connected",
            fontSize = 18.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Available Devices",
            fontSize = 20.sp,
            color = Color.White,
            modifier = Modifier
                .background(Color.Blue)
                .padding(8.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(availableDevices.value) { device ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(device.name ?: "Unnamed device", fontSize = 16.sp)
                    Button(onClick = { connectToDevice(device) }) {
                        Text("Connect")
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.isConnected(): Boolean {
    // This is a placeholder; replace with actual connection check logic
    // A better approach is to use BluetoothGatt and check connection state
    // For simplicity, we'll assume a device is connected if it is bonded
    return bondState == BluetoothDevice.BOND_BONDED
}

fun hasBluetoothPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun connectToDevice(device: BluetoothDevice) {
    // Implement your connection logic here
    // This typically involves creating a BluetoothGatt object and connecting to the device
    Log.d("BluetoothApp", "Attempting to connect to ${device.name}")
}
