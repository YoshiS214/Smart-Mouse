package com.example.smartmouse.bluetooth

import android.app.Application
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
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.smartmouse.R
import java.lang.Exception
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

typealias gattService = BluetoothGattService
typealias gattChar = BluetoothGattCharacteristic
typealias gattDesc = BluetoothGattDescriptor

class BLE: Application() {
    companion object{
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)

        private const val uuidLast24: String = "-0000-1000-8000-00805F9B34FB"

        private val deviceName: String = android.os.Build.DEVICE
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

        var reportMap: List<Byte> = emptyList()
        var outputReport: ByteArray = byteArrayOf()
        private var intentFilter : IntentFilter = IntentFilter()


        lateinit var appContext: Context
        lateinit var handler: Handler
        lateinit var advertiser : BluetoothLeAdvertiser
        lateinit var adSettings: android.bluetooth.le.AdvertiseSettings
        lateinit var inputReportChar: BluetoothGattCharacteristic
        lateinit var gattServer: BluetoothGattServer
        lateinit var adData: AdvertiseData
        lateinit var scanResult: AdvertiseData
        lateinit var callback: AdvertiseCallback
        lateinit var bleDevices: Array<BluetoothDevice>

        private var gattCallback: BluetoothGattServerCallback = object : BluetoothGattServerCallback(){
            override fun onConnectionStateChange(
                device: BluetoothDevice?,
                status: Int,
                newState: Int
            ) {
                super.onConnectionStateChange(device, status, newState);
                when (newState){
                    BluetoothProfile.STATE_CONNECTED ->{
                        if (device != null) {
                            if (device.bondState == BluetoothDevice.BOND_NONE){
                                try{
                                    device.setPairingConfirmation(true)
                                }catch(e:Exception){
                                    Log.d(null, e.toString())
                                }

                                device.createBond()
                            }
                        }
                        handler.post {
                            if (gattServer != null){
                                gattServer.connect(device, true)
                            }
                        }
                        if (!bleDevices.contains(device) && device != null){
                            bleDevices.plus(device)
                        }
                    }
                    BluetoothProfile.STATE_DISCONNECTED ->{
                        handler.post {
                            if (gattServer != null){
                                gattServer.connect(device, true)
                            }
                        }
                        if (!bleDevices.contains(device) && device != null){
                            bleDevices = bleDevices.filterNot { x -> x==device }.toTypedArray()
                        }
                    }
                    else -> {}
                }
            }

            override fun onCharacteristicReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                characteristic: BluetoothGattCharacteristic?
            ) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                if(gattServer != null && characteristic != null){
                    handler.post {
                        var value: ByteArray = when (characteristic.uuid){
                            hidInfoChar -> hidInfoResponse
                            reportMapChar -> reportMap.toByteArray()
                            hidControlPointChar -> byteArrayOf(0)
                            reportChar -> byteArrayOf()
                            reportChar -> manufactureName.toByteArray()
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
                device: BluetoothDevice?,
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
                            if (characteristic != null) {
                                if (characteristic.properties == (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_WRITE_NO_RESPONSE)){
                                    if (value != null) {
                                        outputReport = value
                                    }
                                }
                            }
                            gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, byteArrayOf())
                        }
                    }
                }
            }

            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor?
            ) {
                super.onDescriptorReadRequest(device, requestId, offset, descriptor)
                if (gattServer != null){
                    handler.post {
                        if (descriptor != null) {
                            if (descriptor.uuid == reportReferenceDescriptor){
                                var value: ByteArray = when (descriptor.characteristic.properties){
                                    (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_NOTIFY) -> byteArrayOf(0,1)
                                    (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE or gattChar.PROPERTY_WRITE_NO_RESPONSE) -> byteArrayOf(0,2)
                                    (gattChar.PROPERTY_READ or gattChar.PROPERTY_WRITE) -> byteArrayOf(0,3)
                                    else -> byteArrayOf()
                                }
                                gattServer.sendResponse(device,requestId,BluetoothGatt.GATT_SUCCESS,0,value)
                            }
                        }
                    }
                }
            }

            override fun onDescriptorWriteRequest(
                device: BluetoothDevice?,
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
                if (descriptor != null) {
                    descriptor.value = value
                }
                if (responseNeeded){
                    if (descriptor != null) {
                        if(descriptor.uuid == clientCharConfigDescriptor){
                            if (gattServer != null){
                                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,0,
                                    byteArrayOf())
                            }
                        }
                    }
                }
            }
        }

        private val getBatteryPercentage : BroadcastReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    if (intent.action == Intent.ACTION_BATTERY_CHANGED){
                        batteryPercentage = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,-1)
                    }
                }
            }
        }

        // Reports
        private var hidInputs: Queue<ByteArray> = LinkedList<ByteArray>()



        fun initialise(context: Context, reports: BooleanArray, rate:Long):String? {
            appContext = context.applicationContext
            handler = Handler(appContext.mainLooper)

            val bluetoothManager: BluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = BluetoothAdapter.getDefaultAdapter()

            if (adapter == null){
                return "Bluetooth is not available"
            }else if(!adapter.isEnabled){
                return "Please enable Bluetooth"
            }else if (!adapter.isMultipleAdvertisementSupported) {
                return "BLE advertising is not supported"
            }
            advertiser = adapter.bluetoothLeAdvertiser
            if (adapter == null){
                return "BLE advertising is not supported"
            }

            gattServer = bluetoothManager.openGattServer(appContext, gattCallback)
            if(gattServer == null){
                return "GATT server is off, please check Bluetooth is on"
            }

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

            callback = object : AdvertiseCallback() {
                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                    super.onStartSuccess(settingsInEffect)
                }

                override fun onStartFailure(errorCode: Int) {
                    Log.e("BLE", "Advertising onStartFailure: $errorCode")
                    super.onStartFailure(errorCode)
                }
            }

            addService(getHIDService(reports))
            addService(getDeviceInfoService())
            addService(getBatteryService())

            var task: TimerTask = object: TimerTask(){
                override fun run() {
                    val input: ByteArray = hidInputs.poll()
                    if (input.isNotEmpty() && inputReportChar != null){
                        inputReportChar.value = input
                        handler.post {
                            for (x in bleDevices){
                                if (gattServer != null){
                                    try{
                                        gattServer.notifyCharacteristicChanged(
                                            x,
                                            inputReportChar,
                                            false
                                        )
                                    }catch (e: Exception){
                                        Log.d(null, e.toString())
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Timer().scheduleAtFixedRate(
                task,
                0,
                rate
            )

            return null
        }

        private fun addService(service: gattService){
            var added = false
            if (gattServer != null){
                while (added){
                    try{
                        added = gattServer.addService(service)
                    }catch (e : Exception){
                        Log.d(null,e.toString())
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

        private fun setAdditionalCharacteristic(service: gattService , needs: BooleanArray): gattService{

            var characteristic: gattChar
            var descriptor: gattDesc
            val characteristics: IntArray = intArrayOf(gattChar.PROPERTY_NOTIFY, gattChar.PROPERTY_WRITE_NO_RESPONSE, 0)

            for (x in 0..3){
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
                        characteristic.addDescriptor(descriptor)
                        inputReportChar = characteristic
                    }
                    while (!service.addCharacteristic(characteristic)){}
                }
            }

            return service
        }

        private fun getDeviceInfoService(): gattService{
            val services: ArrayList<UUID> = arrayListOf(manufactureNameChar, modelNumChar, serialNumChar)
            var service: gattService = gattService(deviceInfoService, gattService.SERVICE_TYPE_SECONDARY)
            var characteristic: gattChar

            for (x in 0..3){
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
            var service: gattService = gattService(deviceInfoService, gattService.SERVICE_TYPE_SECONDARY)
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
            handler.post {
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
                appContext.registerReceiver(getBatteryPercentage, intentFilter)
                this.advertiser.startAdvertising(adSettings, adData, scanResult, callback)
            }
        }

        fun stop(){
            handler.post {
                appContext.unregisterReceiver(getBatteryPercentage)
                try{
                    advertiser.stopAdvertising(callback)
                }catch (e: Exception){
                    Log.d(null, e.toString())
                }
                if (gattServer != null){
                    try{
                        for(x in bleDevices){
                            gattServer.cancelConnection(x)
                        }
                        gattServer.close()
                        //gattServer.clearServices()
                    }catch (e: Exception){
                        Log.d(null, e.toString())
                    }
                }
            }

        }

        private fun getUUID(uuid_short: Int): UUID{
            return UUID.fromString("%08X".format(uuid_short)+uuidLast24)
        }

    }

}

