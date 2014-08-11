package gappy.bluewatch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class BluetoothList extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mDeviceSet;
    private ListView mBluetoothList;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private String mBluetoothDeviceAddress;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final String TAG = "BluetoothList";

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
        }
    };

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //test
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);
        //get bluetooth devices
        mBluetoothList = (ListView) findViewById(R.id.bluetoothList);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mDeviceSet = mBluetoothAdapter.getBondedDevices();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
  //      registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        if (mBluetoothAdapter.isEnabled() != true) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        //if device list is empty
        if (mDeviceSet.size() < 1) {
            //display empty page
        } else {
            final ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
            deviceList.addAll(mDeviceSet);

            mBluetoothList.setAdapter(new BluetoothArrayAdapter(this.getBaseContext(), deviceList));
            mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    //TODO connect to bluetooth device
                    connect(deviceList.get(position).getAddress());
                }
            });
        }
    }

    public void refreshBluetoothList() {
        mDeviceSet = mBluetoothAdapter.getBondedDevices();
        ListAdapter listAdapter = mBluetoothList.getAdapter();
        ((BaseAdapter) listAdapter).notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            //bluetooth intent
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class BluetoothArrayAdapter extends ArrayAdapter<BluetoothDevice> {

        List<BluetoothDevice> mDeviceList;
        List<String> deviceNames = new ArrayList<String>();

        public BluetoothArrayAdapter(Context context, ArrayList<BluetoothDevice> deviceList) {
            super(context, R.layout.activity_bluetooth_list, deviceList);
            mDeviceList = deviceList;
            for (BluetoothDevice deviceItem : mDeviceList) {
                deviceNames.add(deviceItem.getName());
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.layout_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.deviceName);
            textView.setText(deviceNames.get(position));
            ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
//            if (mDeviceList.get(position)) {
//                textView.setTextColor(getResources().getColor(R.color.holo_blue_bright);
//            }
            BluetoothClass itemClass = mDeviceList.get(position).getBluetoothClass();
            if (itemClass != null) {
                switch (itemClass.getMajorDeviceClass()) {
                    case BluetoothClass.Device.Major.AUDIO_VIDEO:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bt_headphones_a2dp));
                        break;
                    case BluetoothClass.Device.Major.COMPUTER:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bt_laptop));
                        break;
                    case BluetoothClass.Device.Major.PHONE:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bt_cellphone));
                        break;
                    case BluetoothClass.Device.Major.WEARABLE:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.perm_group_personal_info));
                        break;
                    case BluetoothClass.Device.Major.IMAGING:
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_bt_imaging));
                        break;

                    default:
                        // unrecognized device class; continue
                }
            } else {
                Log.w(TAG, "mBtClass is null");
            }

            return rowView;
        }
    }

}
