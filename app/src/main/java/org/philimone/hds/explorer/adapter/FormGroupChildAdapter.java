package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.FormGroupChildItem;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.Form;
import org.philimone.hds.explorer.model.FormGroupMapping;
import org.philimone.hds.explorer.model.enums.FormCollectType;
import org.philimone.hds.explorer.model.enums.FormType;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class FormGroupChildAdapter extends RecyclerView.Adapter<FormGroupChildAdapter.FormGroupChildViewHolder> {

    private List<FormGroupChildItem> childItems;
    private Context mContext;
    private String filterText;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public FormGroupChildAdapter(Context context, List<FormGroupChildItem> objects){
        this.childItems = new ArrayList<>();
        this.childItems.addAll(objects);
        this.mContext = context;
    }

    public FormGroupChildAdapter(Context context, FormGroupChildItem[] objects){
        this.childItems = new ArrayList<>();
        for (FormGroupChildItem fdl : objects) this.childItems.add(fdl);
        this.mContext = context;
    }

    public List<FormGroupChildItem> getChildItems(){
        return filterList(this.childItems);
    }

    public FormGroupChildItem getItem(int position) {
        return filterList(childItems).get(position);
    }

    @NonNull
    @Override
    public FormGroupChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.form_group_selector_item, parent, false);

        return new FormGroupChildViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull FormGroupChildViewHolder holder, int position) {
        FormGroupChildItem ci = getItem(position);
        holder.setValues(ci);
    }

    @Override
    public int getItemCount() {
        return filterList(this.childItems).size();
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

    private List<FormGroupChildItem> filterList(List<FormGroupChildItem> itemList){
        if (filterText==null) return itemList;

        List<FormGroupChildItem> filtered = new ArrayList<>();
        String codeRegex = ".*" + filterText.toLowerCase() + ".*";

        for (FormGroupChildItem item : itemList) {
            String formName = item.getForm().getFormName();
            if (formName.toLowerCase().matches(codeRegex)){
                filtered.add(item);
            }
        }

        return filtered;
    }

    class FormGroupChildViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup rowView;

        public FormGroupChildViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(FormGroupChildItem childItem) {
            Form form = childItem.getForm();
            CollectedData collectedData = childItem.getCollectedData();

            TextView txtName = rowView.findViewById(R.id.txtItem1);
            TextView txtInstanceName = rowView.findViewById(R.id.txtItem2);
            TextView txtCollected = rowView.findViewById(R.id.txtCollected);
            TextView txtRequired = rowView.findViewById(R.id.txtRequired);
            TextView txtOptional = rowView.findViewById(R.id.txtOptional);
            TextView txtConditional = rowView.findViewById(R.id.txtConditional);
            CheckBox chkProcessed = rowView.findViewById(R.id.chkProcessed);

            txtName.setText(form.getFormName());

            //collected status
            if (collectedData==null) {
                txtCollected.setText(R.string.form_group_selector_not_collected_lbl);
                txtInstanceName.setText("");
            } else {

                String date = dateUtil.formatYMDHMS(collectedData.getFormLastUpdatedDate()); //agnostic date format - used for display date
                txtCollected.setText(mContext.getText(R.string.form_group_selector_collected_lbl)+" "+date);
                txtInstanceName.setText(collectedData.formInstanceName);
                chkProcessed.setChecked(collectedData.isFormFinalized());
            }
            //form requirements
            if (childItem.getFormGroupMapping() != null) {
                FormGroupMapping formGroupMapping = childItem.getFormGroupMapping();

                txtRequired.setVisibility(formGroupMapping.formRequired ? View.VISIBLE : View.GONE);
                txtOptional.setVisibility(formGroupMapping.formRequired ? View.GONE : View.VISIBLE);
                txtConditional.setVisibility(formGroupMapping.formCollectType != FormCollectType.NORMAL_COLLECT ? View.VISIBLE : View.GONE);
            }

        }
    }
}
