package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class FormLoaderAdapter extends RecyclerView.Adapter<FormLoaderAdapter.FormLoaderViewHolder> {
    private List<FormDataLoader> dataLoaders;
    private Context mContext;
    private String filterText;

    public FormLoaderAdapter(Context context, List<FormDataLoader> objects){
        this.dataLoaders = new ArrayList<>();
        this.dataLoaders.addAll(objects);
        this.mContext = context;
    }

    public FormLoaderAdapter(Context context, FormDataLoader[] objects){
        this.dataLoaders = new ArrayList<>();
        for (FormDataLoader fdl : objects) this.dataLoaders.add(fdl);
        this.mContext = context;
    }

    public List<FormDataLoader> getDataLoaders(){
        return filterList(this.dataLoaders);
    }

    public FormDataLoader getItem(int position) {
        return filterList(dataLoaders).get(position);
    }

    @NonNull
    @Override
    public FormLoaderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.form_item, parent, false);
        return new FormLoaderViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull FormLoaderViewHolder holder, int position) {
        FormDataLoader fd = getItem(position);
        holder.setValues(fd);
    }

    @Override
    public int getItemCount() {
        return filterList(this.dataLoaders).size();
    }

    public void filterForms(String name){
        Log.d("filtering", ""+name);

        if (StringUtil.isBlank(name)){
            this.filterText = null;
        } else {
            this.filterText = name;
        }

        notifyDataSetChanged();
    }

    private List<FormDataLoader> filterList(List<FormDataLoader> itemList){
        if (filterText==null) return itemList;

        List<FormDataLoader> filtered = new ArrayList<>();
        String codeRegex = ".*" + filterText.toLowerCase() + ".*";

        for (FormDataLoader item : itemList) {
            String formName = item.getForm().getFormName();
            if (formName.toLowerCase().matches(codeRegex)){
                filtered.add(item);
            }
        }

        return filtered;
    }

    class FormLoaderViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup rowView;

        public FormLoaderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(FormDataLoader fd) {
            TextView txtName = (TextView) rowView.findViewById(R.id.txtFormItemName);
            String processed = "0";
            txtName.setText(fd.getForm().getFormName());
        }
    }
}
