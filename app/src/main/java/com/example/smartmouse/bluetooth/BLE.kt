package com.example.smartmouse.bluetooth

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmouse.DataStore
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.properties.Delegates

typealias gattService = BluetoothGattService
typealias gattChar = BluetoothGattCharacteristic
typealias gattDesc = BluetoothGattDescriptor
typealias bDevice = BluetoothDevice

abstract class BLE{
    companion object{
        fun INPUT(size: Int): Byte {
            return (0x80 or size).toByte()
        }

        fun OUTPUT(size: Int): Byte {
            return (0x90 or size).toByte()
        }

        fun COLLECTION(size: Int): Byte {
            return (0xA0 or size).toByte()
        }

        fun FEATURE(size: Int): Byte {
            return (0xB0 or size).toByte()
        }

        fun END_COLLECTION(size: Int): Byte {
            return (0xC0 or size).toByte()
        }

        fun USAGE_PAGE(size: Int): Byte {
            return (0x04 or size).toByte()
        }

        fun LOGICAL_MINIMUM(size: Int): Byte {
            return (0x14 or size).toByte()
        }

        fun LOGICAL_MAXIMUM(size: Int): Byte {
            return (0x24 or size).toByte()
        }

        fun PHYSICAL_MINIMUM(size: Int): Byte {
            return (0x34 or size).toByte()
        }

        fun PHYSICAL_MAXIMUM(size: Int): Byte {
            return (0x44 or size).toByte()
        }

        fun UNIT_EXPONENT(size: Int): Byte {
            return (0x54 or size).toByte()
        }

        fun UNIT(size: Int): Byte {
            return (0x64 or size).toByte()
        }

        fun REPORT_SIZE(size: Int): Byte {
            return (0x74 or size).toByte()
        }

        fun REPORT_ID(size: Int): Byte {
            return (0x84 or size).toByte()
        }

        fun REPORT_COUNT(size: Int): Byte {
            return (0x94 or size).toByte()
        }

        fun USAGE(size: Int): Byte {
            return (0x08 or size).toByte()
        }

        fun USAGE_MINIMUM(size: Int): Byte {
            return (0x18 or size).toByte()
        }

        fun USAGE_MAXIMUM(size: Int): Byte {
            return (0x28 or size).toByte()
        }

        fun LSB(value: Int): Byte {
            return (value and 0xff).toByte()
        }

        fun MSB(value: Int): Byte {
            return (value shr 8 and 0xff).toByte()
        }

        fun enableBLE(){
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            adapter.apply {
                val intent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                AppCompatActivity().startActivityForResult(intent, 1)
            }
        }

        fun disableBLE(){
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            adapter.apply {
                disable()
            }
        }

        fun isEnabled(): Boolean{
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return adapter.isEnabled
        }

    }

    private val uuidLast24: String = "-0000-1000-8000-00805F9B34FB"

    private val deviceName: String = android.os.Build.DEVICE.plus("-SmartMouse")
    private val manufactureName: String = android.os.Build.MANUFACTURER
    private val serialNumber: String = android.os.Build.ID
    private var batteryPercentage: Int = 0
    private val maxInfoLength: Int = 20

    private val deviceInfoService: UUID = getUUID(0x180A)
    private val manufactureNameChar: UUID = getUUID(0x2A29)
    private val modelNumChar: UUID = getUUID(0x2A24)
    private val serialNumChar: UUID = getUUID(0x2A25)

    private val batteryService: UUID = getUUID(0x180F)
    private val batteryLevel: UUID = getUUID(0x2A19)

    private val hidService: UUID = getUUID(0x1812)
    private val hidInfoChar: UUID = getUUID(0x2A4A)
    private val reportMapChar: UUID = getUUID(0x2A4B)
    private val hidControlPointChar: UUID = getUUID(0x2A4C)
    private val reportChar: UUID = getUUID(0x2A4D)
    private val protocolModeChar: UUID = getUUID(0x2A4E)

    private val reportReferenceDescriptor: UUID = getUUID(0x2908)
    private val clientCharConfigDescriptor: UUID = getUUID(0x2902)

    private val hidInfoResponse: ByteArray = byteArrayOf(0x11, 0x01, 0x00, 0x03)
    private val MouseKeyboardMap: ByteArray = byteArrayOf(
        0x05, 0x01,                         // USAGE_PAGE (Generic Desktop)
        0x09, 0x02,                         // USAGE (Mouse)
        0xa1.toByte(), 0x01,                         // COLLECTION (Application)
        0x05, 0x01,                         // USAGE_PAGE (Generic Desktop)
        0x09, 0x02,                         // USAGE (Mouse)
        0xa1.toByte(), 0x02,                         //       COLLECTION (Logical)
        0x85.toByte(), 0x04,                         //   REPORT_ID (Mouse)
        0x09, 0x01,                         //   USAGE (Pointer)
        0xa1.toByte(), 0x00,                         //   COLLECTION (Physical)
        0x05, 0x09,                         //     USAGE_PAGE (Button)
        0x19, 0x01,                         //     USAGE_MINIMUM (Button 1)
        0x29, 0x02,                         //     USAGE_MAXIMUM (Button 2)
        0x15, 0x00,                         //     LOGICAL_MINIMUM (0)
        0x25, 0x01,                         //     LOGICAL_MAXIMUM (1)
        0x75, 0x01,                         //     REPORT_SIZE (1)
        0x95.toByte(), 0x02,                         //     REPORT_COUNT (2)
        0x81.toByte(), 0x02,                         //     INPUT (Data,Var,Abs)
        0x95.toByte(), 0x01,                         //     REPORT_COUNT (1)
        0x75, 0x06,                         //     REPORT_SIZE (6)
        0x81.toByte(), 0x03,                         //     INPUT (Cnst,Var,Abs)
        0x05, 0x01,                         //     USAGE_PAGE (Generic Desktop)
        0x09, 0x30,                         //     USAGE (X)
        0x09, 0x31,                         //     USAGE (Y)
        0x16, 0x01, 0xf8.toByte(),                   //     LOGICAL_MINIMUM (-2047)
        0x26, 0xff.toByte(), 0x07,                   //     LOGICAL_MAXIMUM (2047)
        0x75, 0x10,                         //     REPORT_SIZE (16)
        0x95.toByte(), 0x02,                         //     REPORT_COUNT (2)
        0x81.toByte(), 0x06,                         //     INPUT (Data,Var,Rel)
        0xa1.toByte(), 0x02,                         //       COLLECTION (Logical)
        0x85.toByte(), 0x06,                         //   REPORT_ID (Feature)
        0x09, 0x48,                         //         USAGE (Resolution Multiplier)
        0x15, 0x00,                         //         LOGICAL_MINIMUM (0)
        0x25, 0x01,                         //         LOGICAL_MAXIMUM (1)
        0x35, 0x01,                         //         PHYSICAL_MINIMUM (1)
        0x45, 0x04,                         //         PHYSICAL_MAXIMUM (4)
        0x75, 0x02,                         //         REPORT_SIZE (2)
        0x95.toByte(), 0x01,                         //         REPORT_COUNT (1)
        0xb1.toByte(), 0x02,                         //         FEATURE (Data,Var,Abs)
        0x85.toByte(), 0x04,                         //   REPORT_ID (Mouse)
        //0x05, 0x01,                       //     USAGE_PAGE (Generic Desktop)
        0x09, 0x38,                         //         USAGE (Wheel)
        0x15, 0x81.toByte(),                         //         LOGICAL_MINIMUM (-127)
        0x25, 0x7f,                         //         LOGICAL_MAXIMUM (127)
        0x35, 0x00,                         //         PHYSICAL_MINIMUM (0)        - reset physical
        0x45, 0x00,                         //         PHYSICAL_MAXIMUM (0)
        0x75, 0x08,                         //         REPORT_SIZE (8)
        0x95.toByte(), 0x01,                         //     REPORT_COUNT (1)
        0x81.toByte(), 0x06,                         //     INPUT (Data,Var,Rel)
        0xc0.toByte(),                               //       END_COLLECTION

        0xa1.toByte(), 0x02,                         //       COLLECTION (Logical)
        0x85.toByte(), 0x06,                         //   REPORT_ID (Feature)
        0x09, 0x48,                         //         USAGE (Resolution Multiplier)
        0x15, 0x00,                         //         LOGICAL_MINIMUM (0)
        0x25, 0x01,                         //         LOGICAL_MAXIMUM (1)
        0x35, 0x01,                         //         PHYSICAL_MINIMUM (1)
        0x45, 0x04,                         //         PHYSICAL_MAXIMUM (4)
        0x75, 0x02,                         //         REPORT_SIZE (2)
        0x95.toByte(), 0x01,                         //         REPORT_COUNT (1)
        0xb1.toByte(), 0x02,                         //         FEATURE (Data,Var,Abs)
        0x35, 0x00,                         //         PHYSICAL_MINIMUM (0)        - reset physical
        0x45, 0x00,                         //         PHYSICAL_MAXIMUM (0)
        0x75, 0x04,                         //         REPORT_SIZE (4)
        0xb1.toByte(), 0x03,                         //         FEATURE (Cnst,Var,Abs)
        0x85.toByte(), 0x04,                         //   REPORT_ID (Mouse)
        0x05, 0x0c,                         //         USAGE_PAGE (Consumer Devices)
        0x0a, 0x38, 0x02,                   //         USAGE (AC Pan)
        0x15, 0x81.toByte(),                         //         LOGICAL_MINIMUM (-127)
        0x25, 0x7f,                         //         LOGICAL_MAXIMUM (127)
        0x75, 0x08,                         //         REPORT_SIZE (8)
        0x95.toByte(), 0x01,                         //         REPORT_COUNT (1)
        0x81.toByte(), 0x06,                         //         INPUT (Data,Var,Rel)
        0xc0.toByte(),                               //       END_COLLECTION
        0xc0.toByte(),                               //       END_COLLECTION

        0xc0.toByte(),                               //   END_COLLECTION
        0xc0.toByte(),                               //END_COLLECTION

        0x05, 0x01,                         // USAGE_PAGE (Generic Desktop)
        0x09, 0x06,                         // Usage (Keyboard)
        0xA1.toByte(), 0x01,                         // Collection (Application)
        0x85.toByte(), 0x08,                         //   REPORT_ID (Keyboard)
        0x05, 0x07,                         //     Usage Page (Key Codes)
        0x19, 0xe0.toByte(),                         //     Usage Minimum (224)
        0x29, 0xe7.toByte(),                         //     Usage Maximum (231)
        0x15, 0x00,                         //     Logical Minimum (0)
        0x25, 0x01,                         //     Logical Maximum (1)
        0x75, 0x01,                         //     Report Size (1)
        0x95.toByte(), 0x08,                         //     Report Count (8)
        0x81.toByte(), 0x02,                         //     Input (Data, Variable, Absolute)
        0x95.toByte(), 0x01,                         //     Report Count (1)
        0x75, 0x08,                         //     Report Size (8)
        0x81.toByte(), 0x01,                         //     Input (Constant) reserved byte(1)
        0x95.toByte(), 0x01,                         //     Report Count (1)
        0x75, 0x08,                         //     Report Size (8)
        0x15, 0x00,                         //     Logical Minimum (0)
        0x25, 0x65,                         //     Logical Maximum (101)
        0x05, 0x07,                         //     Usage Page (Key codes)
        0x19, 0x00,                         //     Usage Minimum (0)
        0x29, 0x65,                         //     Usage Maximum (101)
        0x81.toByte(), 0x00,                         //     Input (Data, Array) Key array(6 bytes)
        0xc0.toByte()                                // End Collection (Application)

    )

    private var intentFilter : IntentFilter = IntentFilter()

    abstract fun getReportMap(): ByteArray

    abstract fun getOutputReport(output: ByteArray)

    var bleEnabled: Boolean = true
    var hidDevice: BluetoothHidDevice? = null
    var connectedHosts: Array<bDevice> = arrayOf()
    var hostDevices: Array<bDevice> = arrayOf()

    lateinit var appContext: Context
    lateinit var handler: Handler
    lateinit var thread: HandlerThread
    lateinit var timer: Timer
    lateinit var adapter: BluetoothAdapter
    lateinit var advertiser : BluetoothLeAdvertiser
    lateinit var adSettings: android.bluetooth.le.AdvertiseSettings
    lateinit var inputReportChar: BluetoothGattCharacteristic
    lateinit var gattServer: BluetoothGattServer
    lateinit var adData: AdvertiseData
    lateinit var scanResult: AdvertiseData
    lateinit var callback: AdvertiseCallback
    var sendingRate by Delegates.notNull<Long>()

    private val serverListener: BluetoothProfile.ServiceListener = object: BluetoothProfile.ServiceListener{
        @RequiresApi(Build.VERSION_CODES.P)
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
            if (profile == BluetoothProfile.HID_DEVICE){
                if (proxy != null) {
                    hidDevice = proxy as BluetoothHidDevice?
                    hidDevice?.registerApp(sdpSettings, null, qosSettings, {it.run()}, hidCallback)
                }
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.HID_DEVICE){
                hidDevice = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private val sdpSettings: BluetoothHidDeviceAppSdpSettings = BluetoothHidDeviceAppSdpSettings(
        deviceName,
        "Smart Mouse",
        manufactureName,
        BluetoothHidDevice.SUBCLASS1_COMBO,
        MouseKeyboardMap
    )

    @RequiresApi(Build.VERSION_CODES.P)
    private val qosSettings: BluetoothHidDeviceAppQosSettings = BluetoothHidDeviceAppQosSettings(
        BluetoothHidDeviceAppQosSettings.SERVICE_BEST_EFFORT,
        800,
        9,
        0,
        11250,
        BluetoothHidDeviceAppQosSettings.MAX
    )

    @RequiresApi(Build.VERSION_CODES.P)
    private val hidCallback: BluetoothHidDevice.Callback = object: BluetoothHidDevice.Callback(){
        override fun onConnectionStateChanged(device: BluetoothDevice?, state: Int) {
            super.onConnectionStateChanged(device, state)
            when(state){
                BluetoothProfile.STATE_CONNECTED ->{
                    if (device != null){
                        connectedHosts.plus(device)
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED ->{
                    if (device != null){
                        connectedHosts = connectedHosts.filterNot { x -> x == device }.toTypedArray()
                    }
                }
            }
        }

        override fun onAppStatusChanged(pluggedDevice: BluetoothDevice?, registered: Boolean) {
            super.onAppStatusChanged(pluggedDevice, registered)
            if (hidDevice?.getConnectionState(pluggedDevice) == BluetoothProfile.STATE_DISCONNECTED && pluggedDevice!= null){
                hidDevice?.connect(pluggedDevice)
                hostDevices.plus(pluggedDevice)
            }else{
                hidDevice?.connect(hidDevice?.getDevicesMatchingConnectionStates(intArrayOf(BluetoothProfile.STATE_CONNECTING,BluetoothProfile.STATE_CONNECTED,BluetoothProfile.STATE_DISCONNECTED,BluetoothProfile.STATE_DISCONNECTING))
                    ?.get(0))
            }
        }

        override fun onGetReport(device: BluetoothDevice?, type: Byte, id: Byte, bufferSize: Int) {
            super.onGetReport(device, type, id, bufferSize)
        }

        override fun onSetReport(device: BluetoothDevice?, type: Byte, id: Byte, data: ByteArray?) {
            super.onSetReport(device, type, id, data)
        }
    }

    private var gattCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback(){
        override fun onConnectionStateChange(
            device: bDevice?,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(device, status, newState);
            when (newState){
                BluetoothProfile.STATE_CONNECTED -> {
                    if (device != null) {
                        when (device.bondState) {
                            bDevice.BOND_NONE -> {
                                appContext.registerReceiver(
                                    object : BroadcastReceiver() {
                                        override fun onReceive(context: Context?, intent: Intent?) {
                                            if (intent != null) {
                                                if (intent.action == bDevice.ACTION_BOND_STATE_CHANGED) {
                                                    when (intent.getIntExtra(
                                                        bDevice.EXTRA_BOND_STATE,
                                                        bDevice.ERROR
                                                    )) {
                                                        bDevice.BOND_BONDED -> {
                                                            context?.unregisterReceiver(this)
                                                            handler.post {
                                                                if (gattServer != null) {
                                                                    gattServer.connect(device, true)
                                                                }
                                                            }
                                                        }


                                                    }
                                                }

                                            }
                                        }
                                    },
                                    IntentFilter(bDevice.ACTION_BOND_STATE_CHANGED)
                                )

                                try {
                                    device.setPairingConfirmation(true)
                                } catch (e: Exception) {
                                    Log.d(null, e.toString())
                                }

                                device.createBond()
                            }
                            bDevice.BOND_BONDED -> {
                                handler.post {
                                    if (gattServer != null) {
                                        gattServer.connect(device, true)
                                    }
                                }
                                if (!hostDevices.contains(device)) {
                                    hostDevices = hostDevices.plus(device)
                                    Log.d("DEVICE", "追加したよ")
                                }
                                if (!connectedHosts.contains(device)) {
                                    connectedHosts = connectedHosts.plus(device)
                                }
                            }
                        }
                    }
                }
                /*
                BluetoothProfile.STATE_CONNECTING ->{
                    if (device != null){
                        handler.post {
                            if (gattServer != null){
                                gattServer.connect(device, true)
                                Log.d("DEVICE", "追加してるよ")
                            }
                        }
                        if (!bleDevices.contains(device)){
                            //bleDevices = bleDevices.filterNot { x -> x==device }.toTypedArray()
                        }
                        if (!connectedDevices.contains(device)){
                            connectedDevices = connectedDevices.plus(device)
                        }
                    }
                }

                BluetoothProfile.STATE_DISCONNECTING ->{
                    if (device != null){
                        handler.post {
                            if (gattServer != null){
                                gattServer.connect(device, true)
                            }
                        }
                        if (!bleDevices.contains(device)){
                            //bleDevices = bleDevices.filterNot { x -> x==device }.toTypedArray()
                        }
                        if (connectedDevices.contains(device)){
                            connectedDevices = connectedDevices.filterNot { x -> x==device }.toTypedArray()
                        }
                    }
                }
                */
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (device != null) {
                        handler.post {
                            if (gattServer != null) {
                                gattServer.connect(device, true)
                            }
                        }
                        if (!hostDevices.contains(device)) {
                            //bleDevices = bleDevices.filterNot { x -> x==device }.toTypedArray()
                        }
                        if (connectedHosts.contains(device)) {
                            connectedHosts =
                                connectedHosts.filterNot { x -> x == device }.toTypedArray()
                        }
                    }
                }
                else -> {}
            }
        }

        override fun onCharacteristicReadRequest(
            device: bDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            if(gattServer != null && characteristic != null){
                handler.post {
                    var value: ByteArray = when (characteristic.uuid){
                        hidInfoChar -> hidInfoResponse
                        reportMapChar ->
                            (if (offset == 0) {
                                getReportMap()
                            } else {
                                (if (getReportMap().size - offset > 0) {
                                    getReportMap().copyOfRange(offset, getReportMap().size - 1)
                                } else {
                                    null
                                }) as ByteArray
                            })
                        hidControlPointChar -> byteArrayOf(0)
                        reportChar -> byteArrayOf()
                        manufactureNameChar -> manufactureName.toByteArray()
                        serialNumChar -> serialNumber.toByteArray()
                        modelNumChar -> deviceName.toByteArray()
                        batteryLevel -> byteArrayOf(batteryPercentage.toByte())
                        else -> characteristic.value
                    }
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value)

                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: bDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (gattServer != null){
                handler.post {
                    if (responseNeeded){
                        var result: Int = BluetoothGatt.GATT_FAILURE
                        if (characteristic != null) {
                            if (characteristic.properties == (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_WRITE_NO_RESPONSE)){
                                if (value != null) {
                                    getOutputReport(value)
                                    result = BluetoothGatt.GATT_SUCCESS
                                }
                            }
                        }
                        gattServer.sendResponse(
                            device,
                            requestId,
                            result,
                            0,
                            byteArrayOf()
                        )
                    }
                }
            }
        }

        override fun onDescriptorReadRequest(
            device: bDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            if (gattServer != null){
                handler.post {
                    var value: ByteArray = byteArrayOf()
                    if (descriptor != null) {
                        if (descriptor.uuid == reportReferenceDescriptor){
                            value = when (descriptor.characteristic.properties){
                                (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_NOTIFY) -> byteArrayOf(
                                    0,
                                    1
                                )
                                (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_WRITE_NO_RESPONSE) -> byteArrayOf(
                                    0,
                                    2
                                )
                                (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE) -> byteArrayOf(
                                    0,
                                    3
                                )
                                else -> byteArrayOf()
                            }
                        }
                        gattServer.sendResponse(
                            device,
                            requestId,
                            (if (value != byteArrayOf())
                            {BluetoothGatt.GATT_SUCCESS}else{ BluetoothGatt.GATT_FAILURE }) as Int,
                            0,
                            value
                        )
                    }
                }
            }
        }

        override fun onDescriptorWriteRequest(
            device: bDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            var result: Int = BluetoothGatt.GATT_FAILURE
            if (descriptor != null) {
                descriptor.value = value
            }
            if (responseNeeded){
                if (descriptor != null) {
                    if(descriptor.uuid == clientCharConfigDescriptor){
                        result = BluetoothGatt.GATT_SUCCESS
                    }
                }
                if (gattServer != null){
                    gattServer.sendResponse(
                        device, requestId, result, 0,
                        byteArrayOf()
                    )
                }
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
        }
    }

    private val getBatteryPercentage : BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED){
                    batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                }
            }
        }
    }

    // Reports
    private var hidInputs: Queue<Report> = ConcurrentLinkedQueue()

    private class Report(device: bDevice, report: ByteArray){
        private val device: bDevice = device
        private val report: ByteArray = report
        fun getDevice(): bDevice {return device}
        fun getReport(): ByteArray {return report}
    }

    interface Listener{
        fun Connected(device: bDevice)
        fun Disconnected(device: bDevice)
    }

    fun initialise(context: Context, reports: BooleanArray, rate: Long):String? {
        appContext = context.applicationContext
        thread = HandlerThread("BLE")
        thread.start()
        handler = Handler(thread.looper)
        sendingRate = rate

        val bluetoothManager: BluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter

        if (adapter == null){
            return "Bluetooth is not available"
        }else if(!adapter.isEnabled){
            bleEnabled = false
            return "Please enable Bluetooth"
        }else if (!adapter.isMultipleAdvertisementSupported) {
            return "BLE advertising is not supported"
        }
        adapter.getProfileProxy(appContext, serverListener, BluetoothProfile.HID_DEVICE)
        advertiser = adapter.bluetoothLeAdvertiser
        if (adapter == null){
            return "BLE advertising is not supported"
        }

        callback = object : AdvertiseCallback() {
            /*
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                super.onStartSuccess(settingsInEffect)
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BLE", "Advertising onStartFailure: $errorCode")
                super.onStartFailure(errorCode)
            }
             */
        }

        hostDevices = DataStore.getBleDevices(appContext)
        connectedHosts = arrayOf()

        return null
    }

    private fun addService(service: gattService){
        var added = false
        if (gattServer != null){
            while (added){
                try{
                    Thread.sleep(500)
                    added = gattServer.addService(service)
                }catch (e: Exception){
                    Log.d(null, e.toString())
                }
            }
        }
    }

    private fun getHIDService(reports: BooleanArray): gattService{
        var service: gattService = gattService(hidService, gattService.SERVICE_TYPE_PRIMARY)
        var characteristic: gattChar

        do {
            characteristic = gattChar(
                hidInfoChar,
                gattChar.PROPERTY_READ,
                gattChar.PERMISSION_READ_ENCRYPTED
            )
        }while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                reportMapChar,
                gattChar.PROPERTY_READ,
                gattChar.PERMISSION_READ_ENCRYPTED
            )
        }while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                protocolModeChar,
                gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE_NO_RESPONSE,
                gattChar.PERMISSION_READ_ENCRYPTED or gattChar.PERMISSION_WRITE_ENCRYPTED
            )
            characteristic.writeType = gattChar.WRITE_TYPE_NO_RESPONSE
        }while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                hidControlPointChar,
                gattChar.PROPERTY_WRITE_NO_RESPONSE,
                gattChar.PERMISSION_WRITE_ENCRYPTED
            )
            characteristic.writeType = gattChar.WRITE_TYPE_NO_RESPONSE
        }while (!service.addCharacteristic(characteristic))

        service = setAdditionalCharacteristic(service, reports)

        return service
    }

    private fun setAdditionalCharacteristic(service: gattService, needs: BooleanArray): gattService{

        var characteristic: gattChar
        var descriptor: gattDesc
        val characteristics: IntArray = intArrayOf(
            gattChar.PROPERTY_NOTIFY,
            gattChar.PROPERTY_WRITE_NO_RESPONSE,
            0
        )

        for (x in needs.indices){
            if (needs[x]){
                characteristic = gattChar(
                    reportChar,
                    gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or characteristics[x],
                    gattChar.PERMISSION_WRITE_ENCRYPTED or gattChar.PERMISSION_READ_ENCRYPTED
                )
                descriptor = gattDesc(
                    reportReferenceDescriptor,
                    gattDesc.PERMISSION_READ_ENCRYPTED or gattDesc.PERMISSION_WRITE_ENCRYPTED
                )
                characteristic.addDescriptor(descriptor)
                if (x == 0){
                    descriptor = gattDesc(
                        clientCharConfigDescriptor,
                        gattDesc.PERMISSION_READ_ENCRYPTED or gattDesc.PERMISSION_WRITE_ENCRYPTED
                    )
                    descriptor.value = gattDesc.ENABLE_NOTIFICATION_VALUE
                    characteristic.addDescriptor(descriptor)
                    inputReportChar = characteristic
                }
                while (!service.addCharacteristic(characteristic)){}
            }
        }

        return service
    }

    private fun getDeviceInfoService(): gattService{
        val services: ArrayList<UUID> = arrayListOf(
            manufactureNameChar,
            modelNumChar,
            serialNumChar
        )
        var service: gattService = gattService(
            deviceInfoService,
            gattService.SERVICE_TYPE_PRIMARY
        )
        var characteristic: gattChar

        for (x in services.indices){
            characteristic = gattChar(
                services[x],
                gattChar.PROPERTY_READ,
                gattChar.PROPERTY_WRITE
            )
            while (!service.addCharacteristic(characteristic)){}
        }

        return service
    }

    private fun getBatteryService(): gattService{
        var service: gattService = gattService(
            batteryService,
            gattService.SERVICE_TYPE_PRIMARY
        )
        var characteristic: gattChar = gattChar(
            batteryLevel,
            gattChar.PROPERTY_NOTIFY or gattChar.PROPERTY_READ,
            gattChar.PERMISSION_READ_ENCRYPTED
        )

        var descriptor: gattDesc = gattDesc(
            clientCharConfigDescriptor,
            gattDesc.PERMISSION_READ or gattDesc.PERMISSION_WRITE
        )
        descriptor.value = gattDesc.ENABLE_NOTIFICATION_VALUE
        characteristic.addDescriptor(descriptor)

        while(!service.addCharacteristic(characteristic)){}

        return service
    }

    fun start(){
        gattServer = (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).openGattServer(appContext, gattCallback)

        thread = HandlerThread("BLE")
        thread.start()
        handler = Handler(thread.looper)

        Thread {
            addService(getHIDService(booleanArrayOf(true, true, true)))
            addService(getDeviceInfoService())
            addService(getBatteryService())
        }.start()

        var task: TimerTask = object: TimerTask(){
            @RequiresApi(Build.VERSION_CODES.P)
            override fun run() {
                if (hidInputs.size != 0){
                    val input: Report = hidInputs.poll()
                    if (input != null && inputReportChar != null){
                        inputReportChar.value = input.getReport()
                        var device = input.getDevice()
                        handler.post {
                            if (gattServer != null){
                                try{
                                    gattServer.notifyCharacteristicChanged(
                                        device,
                                        inputReportChar,
                                        false
                                    )
                                }catch (e: Exception){
                                    Log.d("Report", e.toString())
                                }
                            }
                            hidDevice?.sendReport(device,4,input.getReport())

                        }
                    }
                }
            }
        }

        timer = Timer()
        timer.scheduleAtFixedRate(
            task,
            0,
            sendingRate
        )

        handler.post {
            adSettings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .setTimeout(0)
                .build()
            adData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(ParcelUuid(deviceInfoService))
                .addServiceUuid(ParcelUuid(hidService))
                .addServiceUuid(ParcelUuid(batteryService))
                .build()

            scanResult = AdvertiseData.Builder()
                .addServiceUuid(ParcelUuid(deviceInfoService))
                .addServiceUuid(ParcelUuid(hidService))
                .addServiceUuid(ParcelUuid(batteryService))
                .build()

            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
            appContext.registerReceiver(getBatteryPercentage, intentFilter)
            advertiser.startAdvertising(adSettings, adData, scanResult, callback)
        }
    }

    fun stop(){
        handler.post {
            try {
                appContext.unregisterReceiver(getBatteryPercentage)
            } catch (e: Exception){
            Log.d(null, e.toString())
            }

            try{
                advertiser.stopAdvertising(callback)
            }catch (e: Exception){
                Log.d(null, e.toString())
            }
            if (timer != null){
                try{
                    timer.cancel()
                }catch (e: Exception){
                    Log.d(null, e.toString())
                }
            }
            if (gattServer != null){
                try{
                    for(x in hostDevices){
                        gattServer.cancelConnection(x)
                    }
                    gattServer.clearServices()
                    gattServer.close()

                }catch (e: Exception){
                    Log.d(null, e.toString())
                }
            }

            thread.quit()
        }

    }

    fun addHidInput(device: bDevice, report: ByteArray){
        if (hidInputs != null){
            hidInputs.offer(Report(device, report))
        }
    }

    open fun isReady(): Boolean{
        var ready = true

        when {
            appContext == null -> {
                ready = false
                Log.d(null, "Empty appContext")
            }
            handler == null -> {
                ready = false
                Log.d(null, "Empty handler")
            }
            adapter == null -> {
                ready = false
                Log.d(null, "Empty adapter")
            }
            advertiser == null -> {
                ready = false
                Log.d(null, "Empty advertiser")
            }
            adSettings == null -> {
                ready = false
                Log.d(null, "Empty adsettings")
            }
            inputReportChar == null -> {
                ready = false
                Log.d(null, "Empty inputReportChar")
            }
            gattServer == null -> {
                ready = false
                Log.d(null, "Empty gattServer")
            }
            adData == null -> {
                ready = false
                Log.d(null, "Empty adData")
            }
            scanResult == null -> {
                ready = false
                Log.d(null, "Empty scanResult")
            }
            callback == null -> {
                ready = false
                Log.d(null, "Empty callback")
            }
            hostDevices == null -> {
                ready = false
                Log.d(null, "Empty bleDevices")
            }
        }

        return ready
    }

    open fun getDevicesName(): Array<String>{
        var names: Array<String> = arrayOf()
        try{
            for (device: bDevice in hostDevices){
                names = names.plus(device.name)
            }
        }catch (e: Exception){
            names = arrayOf("Error: No device found")
        }


        return names
    }

    open fun connect(name: String){
        var device: bDevice? = null

        for (x in hostDevices){
            if (x.name == name){
                device = x
            }
        }

        if (device != null){
            if (device.bondState == bDevice.BOND_NONE){
                try{
                    device.setPairingConfirmation(true)
                }catch (e: Exception){
                    Log.d(null, e.toString())
                }

                device.createBond()
            }

            if (gattServer != null){
                try {
                    device.createInsecureRfcommSocketToServiceRecord(hidService).connect()
                }catch (e: Exception){
                    gattServer.connect(device, true)
                }

                Log.d("Device", "${device.name}に繋げるよ")
            }
        }
    }

    open fun connectedDevice():Array<bDevice>{
        return connectedHosts
    }

    open fun saveData(){
        DataStore.writeBleDevices(appContext, hostDevices)
    }

    open fun deleteData(){
        DataStore.writeBleDevices(appContext, arrayOf())
    }

    private fun getUUID(uuid_short: Int): UUID{
        return UUID.fromString("%08X".format(uuid_short) + uuidLast24)
    }


}

