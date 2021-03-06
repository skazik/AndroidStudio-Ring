package com.example.ring_sergie.test1;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

// A service that interacts with the BLE device via the Android BLE API.
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Context mContext = null;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {
                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        // Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }

                @Override
                // Characteristic notification
                public void onCharacteristicChanged(BluetoothGatt gatt,
                                                    BluetoothGattCharacteristic characteristic) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            };

    private void broadcastUpdate(final String action) {
        if (mContext != null) {
            final Intent intent = new Intent(action);
            Log.d(TAG, "broadcastUpdate sending " + action);
            mContext.sendBroadcast(intent);
        }
        else
            Log.d(TAG, "broadcastUpdate mContext == null!");
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            final String Name = SampleGattAttributes.lookup(characteristic.getUuid().toString());
            final StringBuilder stringBuilder = new StringBuilder();

            Log.d(TAG, "broadcastUpdate name = " + Name);
            if (Name != null)
                stringBuilder.append("att:" + Name + "\n");
            if (data != null && data.length > 0) {
                Log.d(TAG, "data len = " + data.length);
                // stringBuilder.append("val:" + new String(data) + "\n");
                for(byte byteChar : data) {
                    // stringBuilder.append((byteChar > ' ') && (byteChar < 0x7F) ? byteChar : ".");
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
            }
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
        }
        if (mContext != null) mContext.sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean BluetoothGatt_discoverServices() {
        // refreshDeviceCache(mBluetoothGatt);
//        try {
//            Method localMethod = mBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
//            if (localMethod != null) {
//                Log.d(TAG, "...refreshing device");
//                localMethod.invoke(mBluetoothGatt, new Object[0]);
//            }
//            else
//                Log.e(TAG, "---Failed to refresh device");
//        }
//        catch (Exception localException) {
//            Log.e(TAG, "An exception occured while refreshing device");
//        }

        return mBluetoothGatt.discoverServices();
    }

    // additional heler functions
    public void BluetoothGatt_close() {
        if (mBluetoothGatt != null)
        {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void BluetoothGatt_connectGatt(BluetoothDevice device, Context ctx, boolean autoconnect)
    {
        final int TRANSPORT_LE = 2;
        mContext = ctx;
        mBluetoothGatt = device.connectGatt(mContext, autoconnect, mGattCallback, TRANSPORT_LE);
    }

    public List<BluetoothGattService> BluetoothGatt_getServices()
    {
        return mBluetoothGatt.getServices();
    }

    public void BluetoothGatt_writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public void BluetoothGatt_readCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.readCharacteristic(characteristic);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void BluetoothGatt_setNotify(BluetoothGattCharacteristic characteristic)
    {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.requestMtu(512);
            mBluetoothGatt.setCharacteristicNotification(characteristic, true);

            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }
    }

}
