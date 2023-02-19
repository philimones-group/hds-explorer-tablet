package org.philimone.hds.explorer.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;

import org.philimone.hds.explorer.R;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BluetoothDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {
    private Context mContext;
     private List<BluetoothDevice> list = new ArrayList<>();

    public BluetoothDeviceArrayAdapter(@NonNull Context context, List<BluetoothDevice> deviceList) {
        super(context, R.layout.bluetooth_device_item);
        this.mContext = context;
        this.list.addAll(deviceList);
    }

    public BluetoothDeviceArrayAdapter(@NonNull Context context) {
        super(context, R.layout.bluetooth_device_item);
        this.mContext = context;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return this.list.get(position);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addDevice(BluetoothDevice bluetoothDevice){
        this.list.add(bluetoothDevice);
        notifyDataSetChanged();
    }

    public void addDevices(Collection<? extends BluetoothDevice> bluetoothDevices){
        this.list.addAll(bluetoothDevices);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bluetooth_device_item, parent, false);

        TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
        TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
        TextView txtItem3 = rowView.findViewById(R.id.txtItem3);

        BluetoothDevice device = list.get(position);

        txtItem1.setText(device.getName());
        txtItem2.setText(device.getAddress());
        txtItem3.setText(device.toString());

        return rowView;
    }
}
