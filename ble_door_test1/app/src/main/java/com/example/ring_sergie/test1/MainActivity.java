package com.example.ring_sergie.test1;

import android.Manifest;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "sergei.ka";
    private static final boolean START_SCAN = true;
    private static final boolean STOP_SCAN = false;
    private Button mButton;
    private TextView helloTextView;
    private TextView mTextView;
    private CheckBox mBLEscanCheckBox;
    private BluetoothAdapter mBluetoothAdapter;
    private final static int REQUEST_ENABLE_BT = 1;
    private BluetoothDevice mDevice = null;
    private boolean bFound = false;
    public String msUUID = java.util.UUID.randomUUID().toString();
    private boolean bScanInProgress = false;
    boolean mBLEConnected;
    private BluetoothLeService mBleService = null;

    // GATT att results visualization
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    List<String> mScanBTResultList;
    List<String> mScanLEResultList;
    List<String> mCharacteristics;

    // Defines several constants used when transmitting messages between the
    // service and the UI.

    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;

        // ... (Add other message types here as needed.)
    }

    private class MyBluetoothService {
        private static final String TAG = "MY_APP_DEBUG_TAG";
        private Handler mHandler; // handler that gets info from Bluetooth service

        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;
            private byte[] mmBuffer; // mmBuffer store for the stream

            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

//            public void run() {
//                mmBuffer = new byte[1024];
//                int numBytes; // bytes returned from read()
//
//                // Keep listening to the InputStream until an exception occurs.
//                while (true) {
//                    try {
//                        // Read from the InputStream.
//                        numBytes = mmInStream.read(mmBuffer);
//
//                        // Send the obtained bytes to the UI activity.
//                        Notification.MessagingStyle.Message readMsg = mHandler.obtainMessage((int) MessageConstants.MESSAGE_READ, numBytes, -1,
//                                mmBuffer);
//                        readMsg.sendToTarget();
//                    } catch (IOException e) {
//                        Log.d(TAG, "Input stream was disconnected", e);
//                        break;
//                    }
//                }
//            }
//
//            // Call this from the main activity to send data to the remote device.
//            public void write(byte[] bytes) {
//                try {
//                    mmOutStream.write(bytes);
//
//                    // Share the sent message with the UI activity.
//                    Notification.MessagingStyle.Message writtenMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
//                    writtenMsg.sendToTarget();
//                } catch (IOException e) {
//                    Log.e(TAG, "Error occurred when sending data", e);
//
//                    // Send a failure message back to the activity.
//                    NotificationCompat.MessagingStyle.Message writeErrorMsg =
//                            mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("toast",
//                            "Couldn't send data to the other device");
//                    writeErrorMsg.setData(bundle);
//                    mHandler.sendMessage(writeErrorMsg);
//                }
//            }

            // Call this method from the main activity to shut down the connection.
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }
    }
    // this it standard BT connection class
    private class ConnectThread extends Thread {
        private static final String TAG = "skazik-socket";
        public BluetoothSocket mmSocket = null;
        private final UUID MY_UUID = java.util.UUID.fromString(msUUID);

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                debugout("Unable to connect, exception " + connectException.toString() + "\r\n" + "close the socket and return.");
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            debugout("connected - manageMyConnectedSocket");

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            manageMyConnectedSocket(mmSocket);
        }

        private void manageMyConnectedSocket(BluetoothSocket mmSocket) {
            Toast.makeText(getBaseContext(), "CONNECTED!!!", Toast.LENGTH_SHORT).show();
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
    // end of Thread Connection (BT) class


    public void debugout(String text)
    {
        Log.d(TAG, text);
        helloTextView.setText(text);
    }

    public void debugout(String text, Boolean bLong)
    {
        Log.d(TAG, text);
        helloTextView.setText(text);
        Toast.makeText(getBaseContext(), text, bLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleFound(BluetoothDevice device)
    {
        String deviceName = device.getName() != null ? device.getName() : "UNKNOWN";
        String deviceHardwareAddress = device.getAddress(); // MAC address

        String text = deviceName;
        text += "\r\n";
        text += deviceHardwareAddress;
        debugout(text);

        if (device.getName() != null)
        {
            if (!bFound)
                mTextView.setText(text);

            if (mBLEscanCheckBox.isChecked())
                mScanLEResultList.add(text);
            else
                mScanBTResultList.add(text);
        }

        if (!bFound && (deviceName.contains("Ampak") || deviceName.contains("WL18")))
        {
            scan_discover(STOP_SCAN);
            bFound = true;
            mDevice = device;
            mButton.setText("Connect");
            mTextView.setText(text);

            if (mBLEscanCheckBox.isChecked())
                listDataChild.put(listDataHeader.get(1), mScanLEResultList);
            else
                listDataChild.put(listDataHeader.get(0), mScanBTResultList); // Header, Child data
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        mButton = (Button) findViewById(R.id.button1);
        helloTextView = (TextView) findViewById(R.id.text_view_id);
        mTextView =  (TextView) findViewById(R.id.textView1);
        mBLEscanCheckBox = (CheckBox) findViewById(R.id.checkBox1);
        mBLEscanCheckBox.setChecked(true);

        // Bluetooth section
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        // check for BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "ble_not_supported", Toast.LENGTH_SHORT).show();
        }

        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(); // (BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        filter.addAction(BluetoothLeService.EXTRA_DATA);

        registerReceiver(mReceiver, filter);

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        prepareListData();

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("BT scan results");
        listDataHeader.add("Bluetooth LE scan");
        listDataHeader.add("Results");
        listDataHeader.add("Help");

        // Adding child data
        mScanBTResultList = new ArrayList<String>();
        mScanBTResultList.add("...");

        mScanLEResultList = new ArrayList<String>();
        mScanLEResultList.add("...");

        mCharacteristics = new ArrayList<String>();
        mCharacteristics.add("--up                    hciconfig hci0 up");
        mCharacteristics.add("--down                  hciconfig hci0 down");
        mCharacteristics.add("--piscan                hciconfig hci0 piscan");
        mCharacteristics.add("--noscan                hciconfig hci0 noscan");
        mCharacteristics.add("--leadv                 hciconfig hci0 leadv");
        mCharacteristics.add("--noleadv               hciconfig hci0 leadv");
        mCharacteristics.add("--class                 hciconfig hci0 class 0x280430");
        mCharacteristics.add("--hciinit               up, piscan, class 0x280430, leadv");
        mCharacteristics.add("--hcishutdown           noleadv, noscan, down");

        listDataChild.put(listDataHeader.get(0), mScanBTResultList); // Header, Child data
        listDataChild.put(listDataHeader.get(1), mScanLEResultList);
        listDataChild.put(listDataHeader.get(2), mCharacteristics);
        listDataChild.put(listDataHeader.get(3), mCharacteristics);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();

        scan_discover(STOP_SCAN);

        if (mBleService != null ) {
            mBleService.BluetoothGatt_close();
        }
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            handleFound(device);
                        }
                    });
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scan_discover(boolean bStart)
    {
        if (bStart && !bScanInProgress)
        {
            bScanInProgress = true;
            if (mBLEscanCheckBox.isChecked()) {
                mScanLEResultList.clear();
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
            else {
                mScanBTResultList.clear();
                mBluetoothAdapter.startDiscovery();
            }

            mButton.setText("scanning... Click to stop");
        }
        else if (!bStart && bScanInProgress)
        {
            bScanInProgress = false;
            if (mBLEscanCheckBox.isChecked()) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            else
            {
                mBluetoothAdapter.cancelDiscovery();
            }
            mButton.setText(bFound ? "Connect" : "Scan");
        }
        mBLEscanCheckBox.setEnabled(!bScanInProgress);
    }


    public void OnCheckBoxClick(View v) {
        bFound = false;
        mButton.setText("Scan");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void OnButtonClick(View v) throws IOException {

        if (!bFound) {
            if (bScanInProgress) {
                scan_discover(STOP_SCAN);
            } else {
                scan_discover(START_SCAN);
            }
        }
        else
        {
            debugout("trying to connect " + mDevice.getAddress() + "\r\n" + msUUID);
            if (mDevice != null) {
                if (mBLEscanCheckBox.isChecked()) {
                    mBleService = new BluetoothLeService();
                    mBleService.BluetoothGatt_connectGatt(mDevice, this, false);
                }
                else
                {
                    ConnectThread mConnect = new ConnectThread(mDevice);
                    if (mConnect.mmSocket != null) {
                        mConnect.run();
                    }
                }
            }
            else
            {
                debugout("mDevice == null", false);
            }
        }
    }
    private static final String LIST_NAME = "NAME_LIST";
    private static final String LIST_UUID = "UUID_LIST";

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;

        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


        mCharacteristics.clear();
        listDataChild.put(listDataHeader.get(1), mScanLEResultList);

        debugout("Loops through available GATT Services.");
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();

            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            debugout("service: " + uuid + " type " + gattService.getType());

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put( LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                String wr = "";
                if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    wr += "R";
                }
                if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                        (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                    wr += "W";
                }
                String ch = "- charas: " + gattCharacteristic.toString() + " prop: " + gattCharacteristic.getProperties() + " " + wr;
                mCharacteristics.add(ch);

                if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                        (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                    debugout(ch);
                    wr += "W";
                    String str = "Hello WL18xx and hpcam2";
                    byte[] strBytes = str.getBytes();
                    byte[] bytes = gattCharacteristic.getValue();

                    if (bytes == null) {
                        debugout("Cannot get Values from mWriteCharacteristic.");
                        bytes = new byte[str.length()];
                    }

                    for(int i = 0; i < (bytes.length < strBytes.length ? bytes.length : strBytes.length); i++) {
                        bytes[i] = strBytes[i];
                    }
                    gattCharacteristic.setValue(bytes);
                    mBleService.BluetoothGatt_writeCharacteristic(gattCharacteristic);
                    debugout("Sending " + str + " " + bytes.length + " bytes", true);
                }
                else {
                    debugout("- characteristics: " + uuid + " prop: " + gattCharacteristic.getProperties() + " " + wr);
                }

                mBleService.BluetoothGatt_setNotify(gattCharacteristic);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        listDataChild.put(listDataHeader.get(2), mCharacteristics);
        can_disconnect_now();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void can_disconnect_now() {
        if (mBleService != null ) {
            debugout("--disconnected--");
            mTextView.setText(mDevice.getName() + "\r\n" + mDevice.getAddress());
            mBleService.BluetoothGatt_close();
            mBleService = null;
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            debugout("mReceiver: " + action.toString());
            switch (action)
            {
                case BluetoothAdapter.ACTION_STATE_CHANGED: debugout("ACTION_STATE_CHANGED"); break;
                case BluetoothDevice.ACTION_FOUND: debugout("ACTION_FOUND"); break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED: debugout("ACTION_DISCOVERY_STARTED"); break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED: debugout("ACTION_DISCOVERY_FINISHED"); break;
                default:
                    mGattUpdateReceiver.onReceive(context, intent);
                    break;
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                handleFound(device);
            }
        }
    };

    // Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            debugout("mGattUpdateReceiver: " + action.toString(), false);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mBLEConnected = true;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBLEConnected = false;
                invalidateOptionsMenu();
                // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                mTextView.setText("trying to send data....");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBleService.BluetoothGatt_getServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                debugout(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
}


