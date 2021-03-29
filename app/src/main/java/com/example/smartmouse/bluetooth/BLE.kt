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
import android.os.BatteryManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.smartmouse.DataStore
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.properties.Delegates

// Create shorthands
typealias gattService = BluetoothGattService
typealias gattChar = BluetoothGattCharacteristic
typealias gattDesc = BluetoothGattDescriptor
typealias bDevice = BluetoothDevice

abstract class BLE {
    companion object {
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

        fun enableBLE() {
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            adapter.apply {
                val intent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                AppCompatActivity().startActivityForResult(intent, 1)
            }
        }

        fun disableBLE() {
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            adapter.apply {
                disable()
            }
        }

        fun isEnabled(): Boolean {
            var adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return adapter.isEnabled
        }

    }

    private val uuidFirst4: String = "0000"
    private val uuidLast24: String = "-0000-1000-8000-00805F9B34FB"

    private val deviceName: String = android.os.Build.DEVICE.plus("-SmartMouse")
    private val manufactureName: String = android.os.Build.MANUFACTURER
    private val serialNumber: String = android.os.Build.ID
    private var batteryPercentage: Int = 0

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
    private val emptyByte: ByteArray = byteArrayOf()

    private var intentFilter: IntentFilter = IntentFilter()

    abstract fun getReportMap(): ByteArray

    abstract fun getOutputReport(output: ByteArray)

    private val inputReport: Queue<Report> = ConcurrentLinkedQueue()
    fun addInputReports(input: Report) {
        if (input != null && input.getReport().isNotEmpty()) {
            inputReport.add(input)
        }
    }

    private var bleEnabled: Boolean = true
    private var bleDeviceMap: MutableMap<String, bDevice> = HashMap()
    private var connectionAllowed = false

    private lateinit var appContext: Context
    private lateinit var handler: Handler
    private lateinit var timer: Timer
    private lateinit var adapter: BluetoothAdapter
    private lateinit var advertiser: BluetoothLeAdvertiser
    private lateinit var adSettings: android.bluetooth.le.AdvertiseSettings
    private var inputReportChar: BluetoothGattCharacteristic? = null
    private lateinit var gattServer: BluetoothGattServer
    private lateinit var adData: AdvertiseData
    private lateinit var scanResult: AdvertiseData
    private lateinit var callback: AdvertiseCallback
    private lateinit var task: TimerTask
    private var sendingRate by Delegates.notNull<Long>()


    private var gattCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(
            device: bDevice?,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (device != null) {
                        when (device.bondState) {
                            bDevice.BOND_NONE -> { // When it find new host device, start pairing then create bond
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
                                                            intent.getParcelableExtra<bDevice>(
                                                                bDevice.EXTRA_DEVICE
                                                            )
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
                                DataStore.writeBleDevice(appContext, device)
                            }
                            bDevice.BOND_BONDED -> { // When it find a known device, create bond
                                handler.post {
                                    if (gattServer != null && connectionAllowed) {
                                        gattServer.connect(device, true)
                                    }
                                }
                                synchronized(bleDeviceMap) {
                                    bleDeviceMap.put(device.address, device)
                                }
                            }
                        }
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> { // When disconnected, try to reconnect once
                    if (device != null && connectionAllowed) {
                        handler.post {
                            if (gattServer != null) {
                                gattServer.connect(device, true)
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }

        override fun onCharacteristicReadRequest(
            device: bDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            if (gattServer == null) {
                return
            }
            if (gattServer != null && characteristic != null) {
                val uuids: Array<UUID> = arrayOf(
                    hidInfoChar,
                    reportMapChar,
                    hidControlPointChar,
                    reportChar,
                    manufactureNameChar,
                    serialNumChar,
                    modelNumChar,
                    batteryLevel
                )
                handler.post {
                    var match: UUID? = isUUIDListMatch(characteristic.uuid, uuids)
                    var value: ByteArray? = when (match) {
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
            if (gattServer != null) {
                handler.post {
                    if (responseNeeded) {
                        var result: Int = BluetoothGatt.GATT_FAILURE
                        if (characteristic != null) {
                            if (characteristic.properties == (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_WRITE_NO_RESPONSE)) {
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
            if (gattServer != null) {
                handler.post {
                    var value: ByteArray = byteArrayOf()
                    if (descriptor != null) {
                        if (isUUIDMatch(descriptor.uuid, reportReferenceDescriptor)) {
                            value = when (descriptor.characteristic.properties) {
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
                            (if (value != byteArrayOf()) {
                                BluetoothGatt.GATT_SUCCESS
                            } else {
                                BluetoothGatt.GATT_FAILURE
                            }),
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
            var result: Int = BluetoothGatt.GATT_FAILURE
            if (descriptor != null) {
                descriptor.value = value
            }
            if (responseNeeded) {
                if (descriptor != null) {
                    if (isUUIDMatch(descriptor.uuid, clientCharConfigDescriptor)) {
                        result = BluetoothGatt.GATT_SUCCESS
                    }
                }
                if (gattServer != null) {
                    gattServer.sendResponse(
                        device, requestId, result, 0,
                        byteArrayOf()
                    )
                }
            }
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        }
    }

    private val getBatteryPercentage: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                    batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                }
            }
        }
    }

    class Report(device: bDevice, report: ByteArray) {
        private val device: bDevice = device
        private val report: ByteArray = report
        fun getDevice(): bDevice {
            return device
        }

        fun getReport(): ByteArray {
            return report
        }
    }


    fun initialise(context: Context, reports: BooleanArray, rate: Long): String? {
        appContext = context.applicationContext
        handler = Handler(appContext.mainLooper)
        sendingRate = rate

        val bluetoothManager: BluetoothManager =
            appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        adapter = bluetoothManager.adapter

        if (!isEnabled()) {
            return "Bluetooth is not enabled"
        }

        if (adapter == null) {
            return "Bluetooth is not available"
        } else if (!adapter.isEnabled) {
            bleEnabled = false
            return "Please enable Bluetooth"
        } else if (!adapter.isMultipleAdvertisementSupported) {
            return "BLE advertising is not supported"
        }

        advertiser = adapter.bluetoothLeAdvertiser
        if (adapter == null) {
            return "BLE advertising is not supported"
        }

        callback = object : AdvertiseCallback() {}

        DataStore.getBleDevices(appContext).forEach { x ->
            synchronized(bleDeviceMap) {
                bleDeviceMap.put(x.address, x)
            }
        }

        gattServer = bluetoothManager.openGattServer(appContext, gattCallback)

        addService(getHIDService(reports))
        addService(getDeviceInfoService())
        addService(getBatteryService())

        task = object : TimerTask() { // Dequeue report which has host device and value, and send value to host device
            override fun run() {
                if (inputReport.size != 0 && !inputReport.isNullOrEmpty()) {
                    val input: Report = inputReport.poll()
                    if (input != null && inputReportChar != null) {
                        inputReportChar!!.value = input.getReport()
                        var device = input.getDevice()
                        handler.post {
                            if (gattServer != null) {
                                try {
                                    gattServer.notifyCharacteristicChanged(
                                        device,
                                        inputReportChar,
                                        false
                                    )
                                    Log.d("DataSent", inputReportChar!!.value.toString())
                                } catch (e: Exception) {
                                    Log.e("Report", e.toString())
                                }
                            }
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

        return null
    }

    private fun addService(service: gattService) {
        assert(gattServer != null)
        var added = false
        if (gattServer != null) {
            while (!added) {
                try {
                    Thread.sleep(500)
                    added = gattServer.addService(service)
                } catch (e: Exception) {
                    Log.e("addService", e.toString())
                }
            }
        }
    }

    private fun getHIDService(reports: BooleanArray): gattService {
        var service: gattService = gattService(hidService, gattService.SERVICE_TYPE_PRIMARY)
        var characteristic: gattChar

        do {
            characteristic = gattChar(
                hidInfoChar,
                gattChar.PROPERTY_READ,
                gattChar.PERMISSION_READ_ENCRYPTED
            )
        } while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                reportMapChar,
                gattChar.PROPERTY_READ,
                gattChar.PERMISSION_READ_ENCRYPTED
            )
        } while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                protocolModeChar,
                gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE_NO_RESPONSE,
                gattChar.PERMISSION_READ_ENCRYPTED or gattChar.PERMISSION_WRITE_ENCRYPTED
            )
            characteristic.writeType = gattChar.WRITE_TYPE_NO_RESPONSE
        } while (!service.addCharacteristic(characteristic))

        do {
            characteristic = gattChar(
                hidControlPointChar,
                gattChar.PROPERTY_WRITE_NO_RESPONSE,
                gattChar.PERMISSION_WRITE_ENCRYPTED
            )
            characteristic.writeType = gattChar.WRITE_TYPE_NO_RESPONSE
        } while (!service.addCharacteristic(characteristic))

        service = setAdditionalCharacteristic(service, reports)

        return service
    }

    private fun setAdditionalCharacteristic(
        service: gattService,
        needs: BooleanArray
    ): gattService {

        var characteristic: gattChar
        var descriptor: gattDesc
        val characteristics: IntArray = intArrayOf(
            gattChar.PROPERTY_NOTIFY,
            gattChar.PROPERTY_WRITE_NO_RESPONSE,
            0
        )

        for (x in needs.indices) {
            if (needs[x]) {
                characteristic = gattChar(
                    reportChar,
                    gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or characteristics[x],
                    gattChar.PERMISSION_WRITE_ENCRYPTED or gattChar.PERMISSION_READ_ENCRYPTED
                )
                if (x == 0) {
                    descriptor = gattDesc(
                        clientCharConfigDescriptor,
                        gattDesc.PERMISSION_READ_ENCRYPTED or gattDesc.PERMISSION_WRITE_ENCRYPTED
                    )
                    descriptor.value = gattDesc.ENABLE_NOTIFICATION_VALUE
                    characteristic.addDescriptor(descriptor)
                } else if (x == 1) {
                    characteristic.writeType = gattChar.WRITE_TYPE_NO_RESPONSE
                }
                descriptor = gattDesc(
                    reportReferenceDescriptor,
                    gattDesc.PERMISSION_READ_ENCRYPTED or gattDesc.PERMISSION_WRITE_ENCRYPTED
                )
                characteristic.addDescriptor(descriptor)
                while (!service.addCharacteristic(characteristic)) {
                }
                if (x == 0) {
                    inputReportChar = characteristic
                }
            }
        }

        return service
    }

    private fun getDeviceInfoService(): gattService {

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

        for (x in services.indices) {
            run {
                characteristic = gattChar(
                    services[x],
                    gattChar.PROPERTY_READ,
                    gattChar.PROPERTY_WRITE
                )
                service.addCharacteristic(characteristic)
            }
        }

        return service
    }

    private fun getBatteryService(): gattService {
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

        while (!service.addCharacteristic(characteristic)) {
        }

        return service
    }

    fun start() { // Start advertising
        connectionAllowed = true
        handler.post {
            adSettings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                .setConnectable(true)
                .setTimeout(0)
                .build()
            adData = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(true)
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

    fun stop() {
        handler.post {
            try {
                appContext.unregisterReceiver(getBatteryPercentage)
            } catch (e: Exception) {
                Log.d(null, e.toString())
            }

            try {
                advertiser.stopAdvertising(callback)
            } catch (e: Exception) {
                Log.d(null, e.toString())
            }
            try {
                for (x in devices) {
                    gattServer.cancelConnection(x)
                }
                gattServer.close()

            } catch (e: Exception) {
                Log.d("Stop", e.toString())
            }

        }
        connectionAllowed = false

    }

    private val devices: Set<bDevice>
        private get() {
            val temp: MutableSet<bDevice> = HashSet()
            synchronized(bleDeviceMap) { temp.addAll(bleDeviceMap.values) }
            return Collections.unmodifiableSet(temp)
        }


    open fun isReady(): Boolean {
        var ready = true

        try {
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
            }
        } catch (e: Exception) {
            ready = false
            Log.e("isReady", e.toString())
        }


        return ready
    }

    open fun getDevicesName(): Array<String> {
        var names: Array<String> = arrayOf()
        try {
            for (device: bDevice in devices) {
                names = names.plus(device.name)
            }
        } catch (e: Exception) {
            Log.d("getDeviceName", e.message!!)
            names = arrayOf("Error: No device found")
        }

        return names
    }

    open fun connect(name: String) {
        var device: bDevice? = null

        for (x in devices) {
            if (x.name == name) {
                device = x
            }
        }

        if (device != null) {

            if (gattServer != null) {
                handler.post {
                    if (gattServer != null && connectionAllowed) {
                        gattServer.connect(device, true)
                    }
                }
            }
        }
    }

    open fun connectedDevice(): Array<bDevice> {
        return try {
            devices.filter { x -> x.bondState == bDevice.BOND_BONDED }.toTypedArray()
        } catch (e: Exception) {
            gattServer.connectedDevices.toTypedArray()
        }

    }

    private fun getUUID(uuid_short: Int): UUID {
        return UUID.fromString(
            uuidFirst4 + String.format(
                "%04X",
                uuid_short and 0xffff
            ) + uuidLast24
        )
    }

    private fun isUUIDMatch(uuid1: UUID, uuid2: UUID): Boolean {
        return if ((uuid1.mostSignificantBits and -0xffff00000001L == 0L && uuid1.leastSignificantBits == 0L) || (uuid2.mostSignificantBits and -0xffff00000001L == 0L && uuid2.leastSignificantBits == 0L)) {
            ((uuid1.mostSignificantBits and 0x0000ffff00000000L) == (uuid2.mostSignificantBits and 0x0000ffff00000000L))
        } else {
            uuid1 == uuid2
        }
    }

    private fun isUUIDListMatch(uuid1: UUID, uuids: Array<UUID>): UUID? {
        for (x in uuids) {
            if (isUUIDMatch(uuid1, x)) {
                return x
            }
        }
        return null
    }

}

