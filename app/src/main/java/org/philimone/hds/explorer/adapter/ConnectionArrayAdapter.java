package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.io.datasharing.ConnectionType;

public class ConnectionArrayAdapter extends ArrayAdapter<ConnectionType> {
    private Context mContext;
    private ConnectionType[] list;

    public ConnectionArrayAdapter(@NonNull Context context) {
        super(context, R.layout.sync_data_sharing_connections_item, R.id.txtItem1);
        this.mContext = context;
        this.list = ConnectionType.values();

        Log.d("list", "created "+this.list.length);
    }

    @Override
    public ConnectionType getItem(int position) {
        return this.list[position];
    }

    @Override
    public int getCount() {
        return list.length;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sync_data_sharing_connections_item, parent, false);

        TextView text1 = rowView.findViewById(R.id.txtItem1);
        ConnectionType type = list[position];
        text1.setText(mContext.getString(type.name));

        return rowView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.sync_data_sharing_connections_item, parent, false);

        TextView text1 = rowView.findViewById(R.id.txtItem1);
        ConnectionType type = list[position];
        text1.setText(mContext.getString(type.name));

        return rowView;
    }
}
