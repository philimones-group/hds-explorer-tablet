package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.CollectedDataItem;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.User;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class CollectedDataAdapter extends RecyclerView.Adapter<CollectedDataAdapter.CollectedDataViewHolder> {
    private List<CollectedDataItem> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable;
    private Context mContext;
    private User currentUser;
    private int highlightedIndex = -1;

    public CollectedDataAdapter(Context context, List<CollectedDataItem> objects){
        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
    }

    public CollectedDataAdapter(Context context, CollectedDataItem[] objects){
        this.collectedDataList = new ArrayList<>();
        for (CollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
    }

    /*
     * Will allow the adapter items to be selectable or not,
     * Enable or Disables selectability of the List
     */
    public void setMultiSelection(boolean value){
        this.multiSelectable = value;
        //update views by enabling first Checkbox


    }

    public List<CollectedDataItem> getCollectedDataList(){
        return this.collectedDataList;
    }

    public CollectedDataItem getItem(int position) {
        return collectedDataList.get(position);
    }

    @NonNull
    @Override
    public CollectedDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.form_collected_item, parent, false);
        return new CollectedDataViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectedDataViewHolder holder, int position) {
        CollectedDataItem item = getItem(position);
        holder.setValues(item, position);
    }

    @Override
    public int getItemCount() {
        return this.collectedDataList.size();
    }

    public void setHighlightedIndex(int index) {
        this.highlightedIndex = index;
        notifyDataSetChanged();
    }

    public int getPositionOf(CollectedData collectedData) {
        for (int i=0; i < this.collectedDataList.size(); i++) {
            CollectedDataItem dataItem = this.collectedDataList.get(i);

            if (dataItem.getCollectedData().id == collectedData.id) {
                return i;
            }
        }

        return -1;
    }

    class CollectedDataViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup rowView;

        public CollectedDataViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(CollectedDataItem cdi, int position) {
            TextView txtName = (TextView) rowView.findViewById(R.id.txtItem1);
            TextView txtForm = (TextView) rowView.findViewById(R.id.txtItem2);
            TextView txtExtra = (TextView) rowView.findViewById(R.id.txtItem3);
            CheckBox chkProcessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);
            ImageView iconView = rowView.findViewById(R.id.iconView);
            ImageView groupIconView = rowView.findViewById(R.id.groupIconView);
            ImageView hdsIconView = rowView.findViewById(R.id.hdsIconView);

            //Member mb = cdi.getMember();
            CollectedData cd = cdi.getCollectedData();
            String sdate = StringUtil.format(cd.formLastUpdatedDate, "yyyy-MM-dd HH:mm:ss");
            String formName = cdi.getFormName(mContext); //cdi.isFormNull() ? cd.getFormId() : cdi.getForm().getFormName());
            String formGroup = cd.formGroupName;

            txtName.setText(cd.getFormInstanceName());
            txtExtra.setText(sdate);
            chkProcessed.setChecked(cd.isFormFinalized());

            txtForm.setText(cd.formGroupCollected ? (formName + " -> " + formGroup) : formName);

            if (highlightedIndex == position) {
                //highlight record
                rowView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.nui_sync_lists_selected_item_color_1));
            } else {
                rowView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.zxing_transparent));
            }

            iconView.setVisibility(cdi.isFormExtension() || cd.formGroupCollected ? View.GONE : View.VISIBLE);
            groupIconView.setVisibility(cd.formGroupCollected && !cdi.isFormExtension() ? View.VISIBLE : View.GONE);
            hdsIconView.setVisibility(cdi.isFormExtension() ? View.VISIBLE : View.GONE);
        }
    }
}
