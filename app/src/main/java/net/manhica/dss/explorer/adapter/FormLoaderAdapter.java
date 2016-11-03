package net.manhica.dss.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.manhica.clip.explorer.R;
import net.manhica.dss.explorer.data.FormDataLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 8/10/16.
 */
public class FormLoaderAdapter extends ArrayAdapter {
    private List<FormDataLoader> dataLoaders;
    private Context mContext;

    public FormLoaderAdapter(Context context, List<FormDataLoader> objects){
        super(context, R.layout.member_item, objects);

        this.dataLoaders = new ArrayList<>();
        this.dataLoaders.addAll(objects);
        this.mContext = context;
    }

    public FormLoaderAdapter(Context context, FormDataLoader[] objects){
        super(context, R.layout.member_item, objects);

        this.dataLoaders = new ArrayList<>();
        for (FormDataLoader fdl : objects) this.dataLoaders.add(fdl);
        this.mContext = context;
    }

    public List<FormDataLoader> getDataLoaders(){
        return this.dataLoaders;
    }

    @Override
    public FormDataLoader getItem(int position) {
        return dataLoaders.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.form_item, parent, false);

        TextView txtName = (TextView) rowView.findViewById(R.id.txtFormItemName);

        FormDataLoader fd = dataLoaders.get(position);

        String processed = "0";

        txtName.setText(fd.getForm().getFormName());

        return rowView;
    }
}
