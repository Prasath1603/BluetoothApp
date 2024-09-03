package com.example.bluetoothmanagerapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DetailsActivity : ComponentActivity() {
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get the Bluetooth device passed from the MainActivity
        val device: BluetoothDevice? = intent.getParcelableExtra("DEVICE")

        setContent {
            device?.let {
                DeviceDetailsScreen(it)
            }
        }
    }
}

@Composable
fun DeviceDetailsScreen(device: BluetoothDevice) {
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {

        Text(
            text = "Device Details",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Name: ${device.name}", fontSize = 18.sp)
        Text(text = "Address: ${device.address}", fontSize = 18.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Mode: Normal",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Battery: 85%",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sound Control:",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Slider(
            value = 0.5f,
            onValueChange = { /* Handle sound level change */ },
            valueRange = 0f..1f,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
