package org.philimone.hds.explorer.io.datasharing.wifi;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
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

public class TcpIpDeviceArrayAdapter extends ArrayAdapter<TcpIpDevice> {
    private Context mContext;
    private List<TcpIpDevice> list = new ArrayList<>();

    public TcpIpDeviceArrayAdapter(@NonNull Context context, List<TcpIpDevice> deviceList) {
        super(context, R.layout.bluetooth_device_item);
        this.mContext = context;
        this.list.addAll(deviceList);
    }

    public TcpIpDeviceArrayAdapter(@NonNull Context context) {
        super(context, R.layout.tcp_device_item);
        this.mContext = context;
    }

    @Override
    public TcpIpDevice getItem(int position) {
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

    public void addDevice(TcpIpDevice device){
        if (!this.list.contains(device)) {
            this.list.add(device);
            notifyDataSetChanged();
        }
    }

    public void addDevices(Collection<? extends TcpIpDevice> devices){
        this.list.addAll(devices);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.tcp_device_item, parent, false);

        TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
        TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
        TextView txtItem3 = rowView.findViewById(R.id.txtItem3);

        TcpIpDevice device = list.get(position);

        txtItem1.setText(device.getName());
        txtItem2.setText(device.getHostname());
        txtItem3.setText(device.getPort()+"");

        return rowView;
    }
}
