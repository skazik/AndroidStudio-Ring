package com.example.ring_sergie.test1;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.IntentSender;
import android.os.ParcelUuid;
import android.text.method.ScrollingMovementMethod;
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
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private CompanionDeviceManager mDeviceManager;
    private AssociationRequest mPairingRequest;
    private BluetoothDeviceFilter mDeviceFilter;
    private static final int SELECT_DEVICE_REQUEST_CODE = 42;
    private static final String TAG = "sergei.ka";
    private static final boolean START_SCAN = true;
    private static final boolean STOP_SCAN = false;
    private Button mButton;
    private TextView scrollDisplay;
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
    List <BluetoothDevice> mScanBLEDevices;
    int mBLEDeviceIndex = 0;

    List<String> mCharacteristics;
    Boolean mbInteractiveReadOnce; // vs. batch-mode pairing simulation

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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void CompanionDevicePairing() {

        Log.d(TAG, "Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT);
        Log.d(TAG, " Build.VERSION_CODES.O: " +  Build.VERSION_CODES.O);
        Log.d(TAG, "(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "getting system service for mDeviceManager");
            mDeviceManager = getSystemService(CompanionDeviceManager.class);
        }
        // To skip filtering based on name and supported feature flags (UUIDs),
        // don't include calls to setNamePattern() and addServiceUuid(),
        // respectively. This example uses Bluetooth.
        BluetoothDeviceFilter.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG,"making builder for koko");
            builder = new BluetoothDeviceFilter.Builder();
            builder.setNamePattern(Pattern.compile("koko"));
            builder.build();
        }

        // The argument provided in setSingleDevice() determines whether a single
        // device name or a list of device names is presented to the user as
        // pairing options.
        if ((builder != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)) {
            Log.d(TAG, "requesting assotiation with builder");
            mPairingRequest = new AssociationRequest.Builder()
                    .addDeviceFilter(mDeviceFilter)
                    .setSingleDevice(true)
                    .build();
        }

        // When the app tries to pair with the Bluetooth device, show the
        // appropriate pairing request dialog to the user.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "assotiating ...");

            mDeviceManager.associate(mPairingRequest,
                    new CompanionDeviceManager.Callback() {
                        @Override
                        public void onDeviceFound(IntentSender chooserLauncher) {
                            try {
                                startIntentSenderForResult(chooserLauncher,
                                        SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(CharSequence charSequence) {

                        }
                    },
                    null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_DEVICE_REQUEST_CODE &&
                resultCode == Activity.RESULT_OK) {
            // User has chosen to pair with the Bluetooth device.
            BluetoothDevice deviceToPair =
                    data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            deviceToPair.createBond();

            // ... Continue interacting with the paired device.
        }
    }

    public void debugout(String text)
    {
        Log.d(TAG, text);
        scrollDisplay.append(text + "\n");
    }

    public void debugout(String text, Boolean bLong)
    {
        Log.d(TAG, text);
        scrollDisplay.append(text + "\n");
//        scrollDisplay.setText(text);
//        Toast.makeText(getBaseContext(), text, bLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void handleFound(BluetoothDevice device, String strUUID)
    {
        if (!bFound) {
            String deviceName = device.getName() != null ? device.getName() : "UNKNOWN";
            String deviceHardwareAddress = device.getAddress(); // MAC address

            String text = deviceName;
            text += "\r\nADDR:";
            text += deviceHardwareAddress;
            if (!strUUID.isEmpty()) {
                text += "\r\nUUID:" + strUUID;
            }

            debugout(text);

            if (device.getName() != null) {
                if (!bFound)
                    mTextView.setText(text);

                if (mBLEscanCheckBox.isChecked() && !mScanLEResultList.contains(text)) {
                    mScanLEResultList.add(text);
                    mScanBLEDevices.add(device);
                }
                else if (!mBLEscanCheckBox.isChecked() && !mScanBTResultList.contains(text))
                    mScanBTResultList.add(text);
            }

            if (!bFound &&
                    (deviceName.contains("RingTEST") ||
                    deviceName.contains("koko") ||
                    deviceName.contains("Ambarella") ))
            {
                bFound = true;
                mDevice = device;
                mButton.setText("Connect");
                mTextView.setText(text);
            }
        }

        if (bFound) {
            scan_discover(STOP_SCAN);
//            if (mBLEscanCheckBox.isChecked())
//                listDataChild.put(listDataHeader.get(1), mScanLEResultList);
//            else
//                listDataChild.put(listDataHeader.get(0), mScanBTResultList); // Header, Child data
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void readCharacteristicByUUID(String uuid)
    {
        BluetoothGattCharacteristic gattCharacteristic = getCharacteristic(uuid);
        if (gattCharacteristic != null)
        {
            Log.d(TAG, "reading " + uuid + " waiting for read complete...");
            mBleService.BluetoothGatt_readCharacteristic(gattCharacteristic);
        }
        else
            Log.d(TAG, "gattCharacteristic " + uuid+ " not found... abort.");
    }

    public List<BluetoothDevice> getConnectedDevices() {
        BluetoothManager btManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        return btManager.getConnectedDevices(BluetoothProfile.GATT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mbInteractiveReadOnce = false;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Getting conn.&.paired devs", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                List<BluetoothDevice> btdevices = getConnectedDevices();
                Log.d(TAG, "found " + btdevices.size() + " connected devices");
                for(int i=0; i<btdevices.size(); i++)
                {
                    //match your device here
                    Log.d(TAG, "connected devices:" + " BLE Name:"+btdevices.get(i).getName());
                }

                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                if (pairedDevices.size() > 0) {
                    for (BluetoothDevice device : pairedDevices) {
                        Log.d("paired devices:"," BLE Name:"+device.getName());
                        try {
                            if(device.getName().contains("koko")){
                                Method m = device.getClass()
                                        .getMethod("removeBond", (Class[]) null);
                                m.invoke(device, (Object[]) null);
                            }
                        } catch (Exception e) {
                            Log.e("fail", e.getMessage());
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8 SDK 26
                    CompanionDevicePairing();
                }

            }
        });

        SimpleDateFormat df = new SimpleDateFormat("(MM-dd HH:mm)");
        String formattedDate = df.format(Calendar.getInstance().getTime());
        String new_title = /*(String) getTitle()*/ "Ring BLEst1 " + formattedDate;
        setTitle(new_title);

        mButton = (Button) findViewById(R.id.button1);

        scrollDisplay = (TextView) findViewById(R.id.text_view_id);
        scrollDisplay.setMovementMethod(ScrollingMovementMethod.getInstance());
        scrollDisplay.setSelected(true);
        scrollDisplay.append("ready to start?..\n");

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
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(
                    ExpandableListView parent, View v,
                    int groupPosition, int childPosition,
                    long id) {
                List<String> list = listDataChild.get(listDataHeader.get(groupPosition ));

                String content = list.get(childPosition);
                if (bScanInProgress)
                {
                    Toast.makeText(getBaseContext(), "Stop scan first...", Toast.LENGTH_SHORT).show();
                }
                else if (groupPosition != 1 && groupPosition != 2)
                {
                    Toast.makeText(getBaseContext(), "this list is not implemented... ", Toast.LENGTH_SHORT).show();
                }
                else if (content.contains("UUID:")) // characteristics list
                {
                    content = content.substring(content.indexOf("UUID:")+"UUID:".length());
                    if (content.contains("\n"))
                        content = content.substring(0, content.indexOf('\n'));
                    final String uuid = content;

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            switch (choice) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    // TODO: check properties W R - maybe to write vs. to read
                                    mbInteractiveReadOnce = true;
                                    readCharacteristicByUUID(uuid);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("READ " + content + "?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                else if (content.contains("ADDR:"))
                {
                    content = content.substring(content.indexOf("ADDR:")+"ADDR:".length());
                    if (content.contains("\n"))
                        content = content.substring(0, content.indexOf('\n'));

                    mBLEDeviceIndex = childPosition;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int choice) {
                            switch (choice) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    MainActivity.this.bFound = true;
                                    mDevice = mScanBLEDevices.get(mBLEDeviceIndex);
                                    mButton.setText("Connect");
                                    mTextView.setText(mScanLEResultList.get(mBLEDeviceIndex));
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("connect to " + content + "?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();
                }
                else {
                    Toast.makeText(getBaseContext(), "click on " + groupPosition + " " + childPosition + " " + list.get(childPosition), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });
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
        listDataHeader.add("Characteristics");

        // Adding child data
        mScanBTResultList = new ArrayList<String>();
        mScanBTResultList.add("hpcam2 ADDR:38:D2:69:F2:7E:00");
        mScanBTResultList.add("doorbe ADDR:43:34:1B:00:1F:AC");

        mScanBLEDevices = new ArrayList<BluetoothDevice>();
        mScanLEResultList = new ArrayList<String>();
        mScanLEResultList.add("all you need is ⓛⓞⓥⓔ");

        mCharacteristics = new ArrayList<String>();
        mScanLEResultList.add("ⓛⓞⓥⓔ");

        listDataChild.put(listDataHeader.get(0), mScanBTResultList); // Header, Child data
        listDataChild.put(listDataHeader.get(1), mScanLEResultList);
        listDataChild.put(listDataHeader.get(2), mCharacteristics);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onDestroy() {
        super.onDestroy();

        scan_discover(STOP_SCAN);
        disconnectAndRelease();
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

    private String parseAdvertisementPacket(final byte[] scanRecord) {

        byte[] advertisedData = Arrays.copyOf(scanRecord, scanRecord.length);

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++] & 0xFF;
                        uuid16 |= (advertisedData[offset++] << 8);
                        len -= 2;
                        Log.d(TAG, "UUID:" + UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            String sret = new UUID(leastSignificantBit, mostSignificantBit).toString();
                            Log.d(TAG, "UUID:" + sret);
                            return sret;

                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            Log.e(TAG, "BlueToothDeviceFilter.parseUUID");
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                case 0xFF:  // Manufacturer Specific Data
                    char MfgData[] = new char[32];
                    int i=0;
                    Log.d(TAG, "Manufacturer Specific Data size:" + len + " bytes");
                    while (len > 1) {
                        if (i < 32) {
                            MfgData[i++] = (char) advertisedData[offset++];
                        }
                        len -= 1;
                    }
                    Log.d(TAG, "Manufacturer Specific Data saved." + MfgData.toString());
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return new String("");
    }

    public ScanCallback mLeScanFilteredCb = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            handleFound(result.getDevice(), "");
        }
    };
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                        @Override
                        public void run() {
                            handleFound(device, parseAdvertisementPacket(scanRecord));
                        }
                    });
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startLeScanNow()
    {
        List<ScanFilter> filters = new ArrayList<>();
        // filters.add(new ScanFilter.Builder().setDeviceName("RingSetup-").build());
        UUID uuidChime = UUID.fromString("0000face-0000-1000-8000-00805f9b34fb");
        ScanFilter chimeUuidFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(uuidChime)).build();
        filters.add(chimeUuidFilter);

        ScanSettings.Builder builderScanSettings = new ScanSettings.Builder();
        builderScanSettings.setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        mBluetoothAdapter.getBluetoothLeScanner().startScan(filters, builderScanSettings.build(), mLeScanFilteredCb);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scan_discover(boolean bStart)
    {
        if (bStart && !bScanInProgress)
        {
            bScanInProgress = true;
            if (mBLEscanCheckBox.isChecked()) {
                mScanLEResultList.clear();
                mScanBLEDevices.clear();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startLeScanNow();
                }
                else {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                }
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
        else if (mBLEConnected && mButton.getText().equals("Discover"))
        {
            mButton.setText("Discovering...");
            mBleService.BluetoothGatt_discoverServices();
        }
        else if (mBLEConnected && mButton.getText().equals("Disconnect")) {
            mButton.setText("Connect");
            disconnectAndRelease();
        }
        else
        {
            debugout("trying to connect " + mDevice.getAddress() + "\r\n" + msUUID);
            if (mDevice != null) {
                mButton.setText("connecting...");
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void writeCharacteristicValue(String str, String charname, BluetoothGattCharacteristic gattCharacteristic)
    {
        // TODO: check it is writable
        // if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
        //        (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {

        debugout("writing " + str + " to " + charname);

        byte[] strBytes = str.getBytes();
        byte[] bytes = gattCharacteristic.getValue();

        if (bytes == null) {
            // debugout("Cannot get Values from mWriteCharacteristic.");
            bytes = new byte[str.length()];
        }

        for (int i = 0; i < (bytes.length < strBytes.length ? bytes.length : strBytes.length); i++) {
            bytes[i] = strBytes[i];
        }
        gattCharacteristic.setValue(bytes);
        mBleService.BluetoothGatt_writeCharacteristic(gattCharacteristic);
        debugout("Sent " + str + " " + bytes.length + " bytes", true);
    }

    private void writeCharacteristicValue(byte[] strBytes, String charname, BluetoothGattCharacteristic gattCharacteristic)
    {
        // TODO: check it is writable
        // if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
        //        (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {

        debugout("writing BINARY to " + charname);

        byte[] bytes = gattCharacteristic.getValue();

        if (bytes == null) {
            // debugout("Cannot get Values from mWriteCharacteristic.");
            bytes = new byte[strBytes.length];
        }

        for (int i = 0; i < (bytes.length < strBytes.length ? bytes.length : strBytes.length); i++) {
            bytes[i] = strBytes[i];
        }
        gattCharacteristic.setValue(bytes);
        mBleService.BluetoothGatt_writeCharacteristic(gattCharacteristic);
        debugout("Sent BINARY " + bytes.length + " bytes", true);
    }

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

        debugout("Loops through available " + gattServices.size() + " GATT Services.");
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();

            uuid = gattService.getUuid().toString();

            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            debugout("service: " + uuid + " type " + gattService.getType() + " attr: " + gattCharacteristics.size());

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {

                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();

                uuid = gattCharacteristic.getUuid().toString();
                String uuid_human = SampleGattAttributes.lookup(uuid, unknownCharaString);

                currentCharaData.put( LIST_NAME, uuid_human);
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);

                String wr = "";
                if ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    wr += "READ";
                }
                if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) |
                        (gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) > 0) {
                    wr += "WRITE";
                }
                debugout("characteristic: " + uuid_human + " " + uuid + " " + wr);

                String ch = uuid_human + " " + wr + " UUID:" + uuid;
                mCharacteristics.add(ch);

                if (!unknownCharaString.equals(uuid_human)) {
                    // request for notifications
                    if (uuid_human.contains(SampleGattAttributes.GET_PUBLIC_PAYLOAD)) {
                        mBleService.BluetoothGatt_setNotify(gattCharacteristic);
                    }
                    else if (uuid_human.contains(SampleGattAttributes.GET_PAIRING_STATE)) {
                        mBleService.BluetoothGatt_setNotify(gattCharacteristic);
                    }
                }

                if (uuid_human.contains(SampleGattAttributes.SET_PUBLIC_KEY)) {
                    //writeCharacteristicValue("this IS long CHARACTERISTIC written to SET_PUBLIC_KEY", SampleGattAttributes.SET_PUBLIC_KEY, gattCharacteristic);
                    byte[] strBytes = hexStringToByteArray("EECB38776FDD076A86BF57C6E42FCB1B878BE2E84187C9A9E3433BCF8185045B");
                    writeCharacteristicValue(strBytes, SampleGattAttributes.SET_PUBLIC_KEY, gattCharacteristic);
                }
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void disconnectAndRelease() {
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
                handleFound(device, "");
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattCharacteristic getCharacteristic(String attr_uuid)
    {
        Log.d(TAG, "attr_uuid from Value = " + attr_uuid);
        List<BluetoothGattService> gattServices = mBleService.BluetoothGatt_getServices();

        for (BluetoothGattService gattService : gattServices) {
            Log.d(TAG, "gattService: " + gattService.toString());
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                Log.d(TAG, "gattCharacteristic " + gattCharacteristic.getUuid().toString().toUpperCase() + " vs. " + attr_uuid.toUpperCase());
                if (gattCharacteristic.getUuid().toString().toUpperCase().equals(attr_uuid.toUpperCase())) {
                    Log.d(TAG, "gattCharacteristic FOUND! " + gattCharacteristic.getUuid() + " == " + attr_uuid);
                    return gattCharacteristic;
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private BluetoothGattCharacteristic findCharacteristic(String name)
    {
        String attr_uuid = SampleGattAttributes.uuidByVal(name);
        Log.d(TAG, "attr_uuid from Name [" + name + " = " + attr_uuid);
        return getCharacteristic(attr_uuid);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean findAndWriteCharacteristic(String txt, String name, String val)
    {
        Log.d(TAG, txt + ": looking for gattCharacteristic...");
        BluetoothGattCharacteristic gattCharacteristic = findCharacteristic(name);
        if (gattCharacteristic != null) {
            Log.d(TAG, name + ": found! sending write");
            writeCharacteristicValue(val, name, gattCharacteristic);
        }
        else {
            Log.d(TAG, name + ": gattCharacteristic not found... abort.");
            return false;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean findAndReadCharacteristic(String txt, String name) {
        Log.d(TAG, txt + ": looking for gattCharacteristic to read");
        BluetoothGattCharacteristic gattCharacteristic = findCharacteristic(name);
        if (gattCharacteristic != null) {
            Log.d(TAG, name + ": found! reading the char - waiting for read complete...");
            mBleService.BluetoothGatt_readCharacteristic(gattCharacteristic);
        } else {
            Log.d(TAG, name + ": gattCharacteristic not found... abort.");
            return false;
        }
        return true;
    }

    private String NetworktoJSON(String ssid, String pass){

        JSONObject jsonObject= new JSONObject();
        try {
            jsonObject.put("ssid", ssid);
            jsonObject.put("pass", pass);

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void HandleReadNotify(String aStr)
    {
        if (aStr == null)
        {
            debugout("null data read - abort.");
            return;
        }

        BluetoothGattCharacteristic gattCharacteristic;
        debugout(aStr);
        Log.d(TAG, "---------------------[" + aStr + "]------------------");

        if (mbInteractiveReadOnce) {
            mbInteractiveReadOnce = false; // just display it
        }
        else if (aStr.contains("att:GET_MAC_ADDRESS")) {
            findAndReadCharacteristic("GET SERIAL", SampleGattAttributes.GET_SERIAL_NUMBER);
        }
        else if (aStr.contains("att:GET_SERIAL_NUMBER")) {
            findAndWriteCharacteristic("ZIP", SampleGattAttributes.SET_ZIPCODE, "91234");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            findAndWriteCharacteristic("ENG", SampleGattAttributes.SET_LANGUAGE, "ENG");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            findAndWriteCharacteristic("US", SampleGattAttributes.SET_COUNTRY_CODE, "US/GB");
        }
        else if (aStr.contains("att:GET_PAIRING_STATE")) {
            if (aStr.contains("val:PAYLOAD_READY")) {
                findAndReadCharacteristic("PAYLOAD_READ", SampleGattAttributes.GET_PUBLIC_PAYLOAD);
            }
            else if (aStr.contains("val:WIFI_CONNECTED"))
            {
                Log.d(TAG, "WIFI_CONNECTED: WiFi Setup done!!");
                Log.d(TAG, "WIFI_CONNECTED: setting other values!!");
                findAndReadCharacteristic("GET MAC", SampleGattAttributes.GET_MAC_ADDRESS);
            }
            else if (aStr.contains("val:WIFI_CONNECT_FAILED"))
            {
                Log.d(TAG, "WIFI_CONNECT_FAILED: bad bad bad... abort and restart");
                disconnectAndRelease();
                mButton.setText("Re-x-Connect");
            }
        }
        else if (aStr.contains("att:GET_PUBLIC_PAYLOAD")) {
            Log.d(TAG, "got PUBLIC_PAYLOAD, ready for encrypted data echange");

            // request for networks
            findAndReadCharacteristic("GET_NETWORKS", SampleGattAttributes.GET_NETWORKS);
        }
        else if (aStr.contains("att:GET_NETWORKS")) {
            Log.d(TAG, "on GET_NETWORKS, ready to present to UI");

            boolean started = false;
            // let's assume the SSID and PASSWORD selected
            String network_credentials = NetworktoJSON("Doorbells", "Parola d'ordine");
            debugout("sending " + network_credentials);

            if (!findAndWriteCharacteristic("on GET_NETWORKS", SampleGattAttributes.SET_WIFI_NETWORK, network_credentials))
            {
                debugout("oops.. something went wrong...abort and restart");
                disconnectAndRelease();
                mButton.setText("Re-x-Connect");
            }
        }
    }

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
                Log.i(TAG, "MainAct informed Connected to GATT server.");
                mButton.setText("Discover");
                // mButton.setText("discovering...");
                // boolean ret = mBleService.BluetoothGatt_discoverServices();
                // Log.i(TAG, "Attempting to start service discovery:" + ret);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mBLEConnected = false;
                mButton.setText("Connect");
                invalidateOptionsMenu();
                // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // debugout("trying to send data....");
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBleService.BluetoothGatt_getServices());
                mButton.setText("Disconnect");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                HandleReadNotify(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };
}


/*
Querying paired devices
Before performing device discovery, it's worth querying the set of paired devices to see if the desired device is already known. To do so, call getBondedDevices(). This returns a set of BluetoothDevice objects representing paired devices. For example, you can query all paired devices and get the name and MAC address of each device, as the following code snippet demonstrates:

Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

if (pairedDevices.size() > 0) {
    // There are paired devices. Get the name and address of each paired device.
    for (BluetoothDevice device : pairedDevices) {
        String deviceName = device.getName();
        String deviceHardwareAddress = device.getAddress(); // MAC address
    }
}
 */