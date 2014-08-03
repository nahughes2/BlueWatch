package gappy.bluewatch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
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

    private static final String TAG = "BluetoothList";

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
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        if(mBluetoothAdapter.isEnabled() != true) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        //if device list is empty
        if (mDeviceSet.size() < 1) {
            //display empty page
        } else {
            ArrayList<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
            deviceList.addAll(mDeviceSet);

            mBluetoothList.setAdapter(new BluetoothArrayAdapter(this.getBaseContext(), deviceList));
            mBluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    //connect to bluetooth device
                }
            });
        }
    }

    public void refreshBluetoothList() {
        mDeviceSet = mBluetoothAdapter.getBondedDevices();
        ListAdapter listAdapter = mBluetoothList.getAdapter();
        ((BaseAdapter)listAdapter).notifyDataSetChanged();
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
            if(mDeviceList.get(position).getBondState() == BluetoothDevice.ACTION_ACL_CONNECTED) {
                textView.setTextColor(getResources().getColor(R.color.holo_blue_bright);
            }
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

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }


}
