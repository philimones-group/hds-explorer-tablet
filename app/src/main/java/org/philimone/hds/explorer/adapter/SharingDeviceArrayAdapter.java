package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.datasharing.SharingDevice;
import org.philimone.hds.explorer.io.datasharing.wifi.TcpIpSharingDevice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SharingDeviceArrayAdapter extends ArrayAdapter<SharingDevice> {
    private Context mContext;
     private List<SharingDevice> list = new ArrayList<>();

    public SharingDeviceArrayAdapter(@NonNull Context context, List<SharingDevice> deviceList) {
        super(context, R.layout.sharing_device_item);
        this.mContext = context;
        this.list.addAll(deviceList);
    }

    public SharingDeviceArrayAdapter(@NonNull Context context) {
        super(context, R.layout.sharing_device_item);
        this.mContext = context;
    }

    @Override
    public SharingDevice getItem(int position) {
        return this.list.get(position);
    }

    public SharingDevice getLastItem() {
        int s = getCount();
        return s==0 ? null : list.get(s-1);
    }

    public List<SharingDevice> getDevicesList() {
        return list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addDevice(SharingDevice sharingDevice){
        this.list.add(sharingDevice);
        notifyDataSetChanged();
    }

    public void addDevices(Collection<? extends SharingDevice> sharingDevices){
        this.list.addAll(sharingDevices);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sharing_device_item, parent, false);

        TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
        TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
        TextView txtItem3 = rowView.findViewById(R.id.txtItem3);

        SharingDevice device = list.get(position);

        if (device instanceof TcpIpSharingDevice) {
            txtItem1.setText(device.getUsername());
            txtItem2.setText(device.getUuid());
            txtItem3.setText(device.getAppVersion());
        } else {
            txtItem1.setText(device.getName());
            txtItem2.setText(device.getUsername());
            txtItem3.setText(device.getAppVersion());
        }



        return rowView;
    }
}
