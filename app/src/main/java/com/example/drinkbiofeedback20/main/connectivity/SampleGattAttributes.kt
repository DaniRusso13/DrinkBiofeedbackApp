package com.example.drinkbiofeedback20.main.connectivity

import java.util.HashMap

object SampleGattAttributes {
    private val attributes : HashMap<String,String> = HashMap()

    var UUID_PRESSURE_MEASUREMENT = "00002aca-0000-1000-8000-00805f9b34fb"
    var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

    init{
        // Sample Services.
        attributes["00001813-0000-1000-8000-00805f9b34fb"] = "Smart Glass service"
        //attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service")
        // Sample Characteristics.
        attributes[UUID_PRESSURE_MEASUREMENT] = "Double-click to start measurement"
        // attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String")
    }
    fun lookup(uuid:String): String? {
        return attributes[uuid]
    }
}