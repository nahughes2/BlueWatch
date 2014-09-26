package gappy.bluewatch;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothList extends Activity {

    private Set<BluetoothDevice> mDeviceSet;
    private ListView mBluetoothList;
    ArrayList<BluetoothDevice> mDeviceList;
    private Menu actionMenu;
    private static final String TAG = "BluetoothList";
    private static final int DISCOVERY_REQUEST = 1;

    private BluetoothAdapter bluetoothAdapter;

    private static String UUID;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //create UUID
        if (UUID == null) {
            SharedPreferences sharedPrefs = getSharedPreferences(
                    UUID, Context.MODE_PRIVATE);
            UUID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (UUID == null) {
                UUID = java.util.UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, UUID);
                editor.commit();
            }
        }

        setContentView(R.layout.activity_bluetooth_list);

        //get bluetooth devices
        mBluetoothList = (ListView) findViewById(R.id.bluetoothList);

        mDeviceSet = bluetoothAdapter.getBondedDevices();

        //if device list is empty
        if (mDeviceSet.size() < 1) {
            //display empty page
        } else {
            mDeviceList = new ArrayList<BluetoothDevice>();
            mDeviceList.addAll(mDeviceSet);

            mBluetoothList.setAdapter(new BluetoothArrayAdapter(this.getBaseContext(), mDeviceList));
            mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    if(true/*TODO connect */) {
                        //connected
                    } else {
                        //connection error
                    }
                }
            });
        }

        mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO connect socket
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(bluetoothState);
    }

    public void refreshBluetoothList() {
        mDeviceSet = bluetoothAdapter.getBondedDevices();
        ListAdapter listAdapter = mBluetoothList.getAdapter();
        ((BaseAdapter)listAdapter).notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.bluetooth_list, menu);
        MenuItem bluetoothEnabledButton = menu.getItem(0);
        actionMenu = menu;

        if(bluetoothAdapter.isEnabled()) {
            bluetoothEnabledButton.setIcon(R.drawable.ic_action_bluetooth_connected);
        } else {
            bluetoothEnabledButton.setIcon(R.drawable.ic_action_bluetooth);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.refresh) {
            //bluetooth intent
            return true;
        } else if (id == R.id.onOffButton) {
            turnBlueToothOn();

        } else if (id == R.id.addDevice) {
            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            registerReceiver(bluetoothState, filter);
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE), DISCOVERY_REQUEST);

        }
        return super.onOptionsItemSelected(item);
    }

    public class BluetoothArrayAdapter extends ArrayAdapter<BluetoothDevice> {

        List<BluetoothDevice> mDeviceList;
        List<String> deviceNames = new ArrayList<String>();

        public BluetoothArrayAdapter(Context context, ArrayList<BluetoothDevice> deviceList) {
            super(context, R.layout.activity_bluetooth_list, deviceList);
            mDeviceList = deviceList;
            for(BluetoothDevice deviceItem : mDeviceList) {
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
            }

            if(false /* TODO if socket exists */) {
                textView.setTextColor(getResources().getColor(R.color.holo_blue_bright));
            }

            return rowView;
        }
    }

    private void turnBlueToothOn() {

        if(!bluetoothAdapter.isEnabled()) {
            //turn bluetooth on
            String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
            String actionRequestEnabled = BluetoothAdapter.ACTION_REQUEST_ENABLE;
            IntentFilter filter = new IntentFilter(actionStateChanged);
            registerReceiver(bluetoothState, filter);
            startActivityForResult(new Intent(actionRequestEnabled), 0);
        } else {
            bluetoothAdapter.disable();
            actionMenu.getItem(0).setIcon(R.drawable.ic_action_bluetooth);
        }
    }

    private void pairDevice() {
        if(bluetoothAdapter.startDiscovery()) {
            registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        }
    }

    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            String toastText;
            switch (state) {
                case(BluetoothAdapter.STATE_TURNING_ON):
                    toastText = "Turning on Bluetooth";
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    actionMenu.getItem(0).setIcon(R.drawable.ic_action_bluetooth_connected);
                    break;
                case(BluetoothAdapter.STATE_TURNING_OFF):
                    toastText = "Turning off Bluetooth";
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    actionMenu.getItem(0).setIcon(R.drawable.ic_action_bluetooth);
                    break;
                case(BluetoothAdapter.STATE_ON):
                    toastText = "Bluetooth on";
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    actionMenu.getItem(0).setIcon(R.drawable.ic_action_bluetooth_connected);
                    break;
                case(BluetoothAdapter.STATE_OFF):
                    toastText = "Bluetooth off";
                    Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT).show();
                    actionMenu.getItem(0).setIcon(R.drawable.ic_action_bluetooth);
                    break;
            }
            mBluetoothList.invalidate();
        }
    };

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String toastText = "Discovered: " + intent.getStringExtra(BluetoothDevice.EXTRA_NAME);

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DISCOVERY_REQUEST) {
            Toast.makeText(getApplicationContext(), "Discovering Devices",
                    Toast.LENGTH_SHORT).show();
        }
        pairDevice();
    }
}
