package com.example.covid_19

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.covid_19.network.RetrofitService
import com.example.covid_19.utils.MessageParser
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Distance
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.MessagesClient
import com.google.gson.JsonObject
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var broadcastMessage: Message? = null
    private lateinit var locationManager: LocationManager
    private lateinit var phoneNumber: String
    private lateinit var nearbyCloent: MessagesClient
    val list: MutableList<Pair<Long, String>> = ArrayList()
    private val REQUEST_ENABLE_BT = 1
    private lateinit var bluetoothAdapter: BluetoothAdapter
    val listOfNearby: MutableMap<String, String> = mutableMapOf()
    val listOfContactDevices: MutableMap<String, Long> = mutableMapOf()
    lateinit var macAddress: String

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String = intent.action!!
            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name
                    val deviceHardwareAddress = device.address // MAC address
                    Log.d(TAG, "Mac Address of discovered device = $deviceHardwareAddress \n $deviceName")
                    if(listOfNearby.containsKey(deviceName)) {
                        Log.d(TAG, "List of nearby contains $deviceHardwareAddress")
                        if(!(listOfContactDevices.containsKey(listOfNearby[deviceName]))) {
                            Log.d(TAG, "Adding to contact Device $deviceHardwareAddress")
                            listOfContactDevices[listOfNearby[deviceName]!!] = System.currentTimeMillis()
                            val body = JsonObject().apply {
                                this.addProperty("PhoneNo", listOfNearby[deviceName])
                                this.addProperty("TimeSpent", 0)
                            }
                            val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
                            val jwt = sharedPreferences.getString("JWT", "")
                            RetrofitService().gerRetrofitApiService().meetUser(body, jwt.toString()).subscribeOn(Schedulers.io()).subscribe(
                                    { response ->
                                        Log.d(TAG, "Response of meeting people = ${response.body()}")
                                        if(response.isSuccessful) {
                                            Log.d(TAG, "Meeting people has been sucessful")
                                        } else {
                                            Log.d(TAG, "Response code for meeting people = ${response.code()}")
                                            Log.d(TAG, "Response body = ${response.body()}")
                                        }
                                    },
                                    {
                                        Log.e(TAG, "Error in meeting people = ${it.message}")
                                    }
                            )
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "Entered else. Starting discovery again")
                }
            }
            bluetoothAdapter.startDiscovery()
        }
    }

    val messageListener = object: MessageListener() {
        override fun onFound(message: Message?) {
            Log.d(TAG, "Messsage found = ${String(message!!.content)}")
            val jsonObject = MessageParser.parseBluetoothMessage(String(message.content))
            listOfNearby[jsonObject["Mac"].toString()] = jsonObject["Phone"].toString()
            super.onFound(message)
        }

        override fun onDistanceChanged(p0: Message?, p1: Distance?) {
            super.onDistanceChanged(p0, p1)
            Log.d(TAG, "Distance changed for message ${String(p0!!.content)} to ${p1.toString()}")
        }

        @SuppressLint("CheckResult")
        override fun onLost(message: Message?) {
            Log.d(TAG, "Messsage lost = ${String(message!!.content)}")
            val jsonObject = MessageParser.parseBluetoothMessage(String(message.content))
            listOfNearby.remove(jsonObject["Mac"].toString())
            super.onLost(message)
        }
    }

    @SuppressLint("MissingPermission")
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        macAddress = bluetoothAdapter.name
        Log.d(TAG, "BLE NAME = ${bluetoothAdapter.name}")
        nearbyCloent = Nearby.getMessagesClient(this)

        checkPermissions()
    }

    private fun checkPermissions() {
        if(!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))) {
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
        else if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 5)
        }
        else if (bluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
        initializeActivity()
    }

    private fun initializeActivity() {
        setUpBluetooth()
        bluetoothAdapter.startDiscovery()
        Log.d(TAG, "Adding listener to nerby messages")
        val sharedPreferences = applicationContext.getSharedPreferences("MySharedPreferences", Context.MODE_PRIVATE)
        phoneNumber = sharedPreferences.getString("Phone", "0000000000")!!
        Log.d(TAG, "Recived PhoneNumber = $phoneNumber")
        if(broadcastMessage == null) {
            Log.d(TAG, "Entered 1 If")
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Entered 2 If")
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 5)
            } else {
                Log.d(TAG, "Entered 2 else")
                broadcastNearbyMessage("$macAddress\n$phoneNumber")
            }
        }
        try {
            nearbyCloent.subscribe(messageListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error in publishing message = ${e.toString()}")
        }
    }

    private fun broadcastNearbyMessage(message: String) {
        if(broadcastMessage != null) {
            Log.d(TAG, "Unpublishing the previous message")
            nearbyCloent.unpublish(broadcastMessage!!)
        }
        broadcastMessage = Message(message.toByteArray())
        Log.d(TAG, "Publishing the new message ${String(broadcastMessage!!.content)}")
        try {
            nearbyCloent.publish(broadcastMessage!!)
        } catch (e: Exception) {
            Log.e(TAG, "Error in publishing message = ${e.toString()}")
        }
    }

    private fun setUpBluetooth() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)
        bluetoothAdapter.startDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        checkPermissions()
    }
}