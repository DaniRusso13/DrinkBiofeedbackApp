package com.example.drinkbiofeedback20.main.connectivity

import android.app.Activity
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.*
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.ExpandableListView.OnChildClickListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.example.drinkbiofeedback20.R
import com.example.drinkbiofeedback20.main.DataStoreActivity
import com.example.drinkbiofeedback20.main.database.DrinkDataBase
import com.example.drinkbiofeedback20.main.database.DrinkViewModel
import com.example.drinkbiofeedback20.main.database.DrinkVolume
import com.example.drinkbiofeedback20.main.database.DrinkVolumeViewModelFactory

import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.collections.HashMap


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with `bluetoothClasses.BluetoothLeService`, which in turn interacts with the
 * Bluetooth LE API.
 */
class DeviceControlActivity : FragmentActivity() {
    //Constants
    private val TAG = DeviceControlActivity::class.java.simpleName
    private val LIST_NAME = "NAME"
    private val LIST_UUID = "UUID"
    private var stored: String = 72.toString()
    private var volume: Int = 210
    private var count = 0
    private var mConnected = false

    companion object {
        const val EXTRAS_DEVICE_NAME = "DEVICE_NAME"
        const val EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS"
    }

    //Other variables
    private lateinit var mConnectionState: TextView
    private lateinit var mDataField: TextView
    private lateinit var mDeviceName: String
    private lateinit var mGattServicesList: ExpandableListView
    private lateinit var mDeviceAddress: String
    private var mBluetoothLeService: BluetoothLeService? = null
    private var mGattCharacteristics: ArrayList<ArrayList<BluetoothGattCharacteristic>>? =
        ArrayList()
    private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
    private lateinit var drinkViewModel: DrinkViewModel

    // Code to manage Service lifecycle.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mBluetoothLeService = (service as BluetoothLeService.LocalBinder).getService()
            if (!mBluetoothLeService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService!!.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.


    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private val servicesListClickListener =
        OnChildClickListener { parent, v, groupPosition, childPosition, id ->
            if (mGattCharacteristics != null) {
                val characteristic = mGattCharacteristics!![groupPosition][childPosition]
                val charaProp = characteristic.properties
                if (charaProp or BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                    // If there is an active notification on a characteristic, clear
                    // it first so it doesn't update the data field on the user interface.
                    if (mNotifyCharacteristic != null) {
                        mBluetoothLeService?.setCharacteristicNotification(
                            mNotifyCharacteristic!!, false
                        )
                        mNotifyCharacteristic = null
                    }
                    mBluetoothLeService?.readCharacteristic(characteristic)
                }
                if (charaProp or BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {
                    mNotifyCharacteristic = characteristic
                    mBluetoothLeService?.setCharacteristicNotification(
                        characteristic, true
                    )
                }
                return@OnChildClickListener true
            }
            false
        }

    init {

    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME).toString()
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS).toString()
        setContentView(R.layout.gatt_services_characteristics)

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            startActivity(Intent(this@DeviceControlActivity, DataStoreActivity::class.java))
        }


        val application = requireNotNull(this@DeviceControlActivity).application

        val dataSource = DrinkDataBase.getInstance(application).drinkDataBaseDao()

        val viewModelFactory = DrinkVolumeViewModelFactory(dataSource, application)

        val drinkViewModel =
            ViewModelProvider(this, viewModelFactory).get(DrinkViewModel::class.java)

        val button3 = findViewById<Button>(R.id.button3)
        button3.setOnClickListener() {
            drinkViewModel.deleteAllData()
            Toast.makeText(
                applicationContext,
                "Your data has gone forever",
                Toast.LENGTH_SHORT
            ).show()
        }
        // Sets up UI references.
        //(findViewById<View>(R.id.device_address) as TextView).text = mDeviceAddress
        mGattServicesList = (findViewById<View>(R.id.gatt_services_list) as ExpandableListView)
        mGattServicesList.setOnChildClickListener(servicesListClickListener)
        mConnectionState = findViewById<View>(R.id.connection_state) as TextView
        mDataField = findViewById<View>(R.id.data_value) as TextView
        actionBar!!.title = mDeviceName
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, mServiceConnection, Activity.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        if (mBluetoothLeService != null) {
            val result: Boolean = mBluetoothLeService!!.connect(mDeviceAddress)
            Log.d(TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(mGattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        mBluetoothLeService = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_connect -> {
                mBluetoothLeService?.connect(mDeviceAddress)
                return true
            }
            R.id.menu_disconnect -> {
                mBluetoothLeService?.disconnect()
                return true
            }
            R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearUI() {
        mGattServicesList.setAdapter(null as SimpleExpandableListAdapter?)
        mDataField.setText(R.string.no_data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gatt_services, menu)
        if (mConnected) {
            menu.findItem(R.id.menu_connect).isVisible = false
            menu.findItem(R.id.menu_disconnect).isVisible = true
        } else {
            menu.findItem(R.id.menu_connect).isVisible = true
            menu.findItem(R.id.menu_disconnect).isVisible = false
        }
        return true
    }

    fun updateConnectionState(resourceId: Int) {
        runOnUiThread { mConnectionState.setText(resourceId) }
    }

    private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeService.ACTION_GATT_CONNECTED == action) {
                mConnected = true
                updateConnectionState(R.string.connected)
                invalidateOptionsMenu()
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED == action) {
                mConnected = false
                updateConnectionState(R.string.disconnected)
                invalidateOptionsMenu()
                clearUI()
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED == action) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService?.getSupportedGattServices())
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE == action) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displayData(data: String?) {

        Log.i("DATA", "displayData: $data")

        val application = requireNotNull(this@DeviceControlActivity).application

        val dataSource = DrinkDataBase.getInstance(application).drinkDataBaseDao()

        val viewModelFactory = DrinkVolumeViewModelFactory(dataSource, application)

        val drinkViewModel =
            ViewModelProvider(this, viewModelFactory).get(DrinkViewModel::class.java)

        if (data != null) {


            if (stored == data && (stored.toInt() <= 80) && (stored.toInt() != 0)) {
                count += 1
                //    Log.i("DATA", "displayData__1: $stored, $data")
            }

            if (count == 5) {
               // Log.i("DATA", "displayData___2: $stored, $data")
                val y = 2.917 * (stored.toInt()) - 23.3  //relation between pressure and volume
                val volume = volume - (y.toInt())
                mDataField.text = volume.toString() //DATA I WANT TO STORE IN THE DATABASE
                count = 0
                val sp = SoundPool(5, AudioManager.STREAM_MUSIC, 0)
                val soundId = sp.load(this, R.raw.ping, 1)
                sp.play(soundId, 1f, 1f, 0, 0, 1f)
                val mPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.ping)
                mPlayer.start()


                val button1 = findViewById<Button>(R.id.button1)
                button1.setOnClickListener {
                    val localDate = LocalDate.now().toString()
                    val localTime = LocalTime.now().toString()
                    val drink = DrinkVolume(0, localDate, localTime, volume)
                    drinkViewModel.addVolume(drink)
                    Toast.makeText(
                        applicationContext,
                        "Your current volume has saved in the database",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            //store += data.toInt()
            //mDataField.text = store.toString()
            stored = data
        }
    }

    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String? = null
        /*val unknownServiceString = resources.getString(R.string.unknown_service)
        val unknownCharaString = resources.getString(R.string.unknown_characteristic)*/
        val gattServiceData = ArrayList<HashMap<String, String?>>()
        val gattCharacteristicData = ArrayList<ArrayList<HashMap<String, String?>>>()
        mGattCharacteristics = ArrayList()

        // Loops through available GATT Services.

        for (gattService in gattServices) {
            val currentServiceData = HashMap<String, String?>()

            uuid = gattService.uuid.toString()
            currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(uuid)
            currentServiceData[LIST_UUID] = uuid
            if (currentServiceData[LIST_NAME] != null) {
                gattServiceData.add(currentServiceData)

                val gattCharacteristicGroupData = ArrayList<HashMap<String, String?>>()
                val gattCharacteristics = gattService.characteristics
                val charas = ArrayList<BluetoothGattCharacteristic>()

                // Loops through available Characteristics.
                for (gattCharacteristic in gattCharacteristics) {
                    charas.add(gattCharacteristic)
                    val currentCharaData = HashMap<String, String?>()
                    uuid = gattCharacteristic.uuid.toString()
                    currentCharaData[LIST_NAME] =
                        SampleGattAttributes.lookup(uuid)
                    currentCharaData[LIST_UUID] = uuid
                    gattCharacteristicGroupData.add(currentCharaData)
                }
                mGattCharacteristics!!.add(charas)
                gattCharacteristicData.add(gattCharacteristicGroupData)
            }
        }
        val gattServiceAdapter = SimpleExpandableListAdapter(
            this,
            gattServiceData,
            android.R.layout.simple_expandable_list_item_2,
            arrayOf(LIST_NAME, LIST_UUID),
            intArrayOf(android.R.id.text1, R.id.text2),
            gattCharacteristicData,
            android.R.layout.simple_expandable_list_item_2,
            arrayOf(LIST_NAME, LIST_UUID),
            intArrayOf(android.R.id.text1, R.id.text2)
        )
        mGattServicesList.setAdapter(gattServiceAdapter)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE)
        return intentFilter
    }
}