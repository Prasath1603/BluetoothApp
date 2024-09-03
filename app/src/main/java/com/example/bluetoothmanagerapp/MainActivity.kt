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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext

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
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController)
        }
        composable("device/{deviceName}") { backStackEntry ->
            DeviceDetailsScreen(
                deviceName = backStackEntry.arguments?.getString("deviceName") ?: "Unknown Device"
            )
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(navController: NavHostController) {
    val context = LocalContext.current
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
            availableDevices.value = scanForDevices(context)
        } else {
            Log.e("BluetoothApp", "Missing Bluetooth permissions")
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Header for Currently Connected Device
        Text(
            text = "Currently Connected Device: ${currentlyConnectedDevice.value?.name ?: "None"}",
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

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
                    Button(onClick = {
                        navController.navigate("device/${device.name}")
                    }) {
                        Text("View Details")
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun DeviceDetailsScreen(deviceName: String) {
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val device = bluetoothAdapter?.bondedDevices?.find { it.name == deviceName }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Device Details", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (device == null) {
            Text("Device not found", fontSize = 20.sp)
            return
        }

        Text("Name: ${device.name}", fontSize = 20.sp)
        Text("Address: ${device.address}", fontSize = 20.sp)
        // Add more details if needed

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Implement disconnect logic */ }) {
            Text("Disconnect")
        }
    }
}

@SuppressLint("MissingPermission")
fun scanForDevices(context: Context): List<BluetoothDevice> {
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
    BluetoothAdapter.getDefaultAdapter().startDiscovery()

    // Unregister receiver after discovery is done
    // You might want to unregister it when you stop discovery or in onDestroy of activity
    context.unregisterReceiver(discoveryReceiver)

    return devices
}

@SuppressLint("MissingPermission")
fun BluetoothDevice.isConnected(): Boolean {
    // Implement logic to check if the device is currently connected
    // This is a placeholder return value; replace with actual connection check
    return false
}

fun hasBluetoothPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
}
