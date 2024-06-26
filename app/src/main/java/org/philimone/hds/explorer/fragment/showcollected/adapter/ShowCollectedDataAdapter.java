package org.philimone.hds.explorer.fragment.showcollected.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.OdkCollectedDataItem;
import org.philimone.hds.explorer.model.CollectedData;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class ShowCollectedDataAdapter extends RecyclerView.Adapter<ShowCollectedDataAdapter.CollectedDataViewHolder> {
    private List<OdkCollectedDataItem> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable;
    private Context mContext;
    private OnItemActionListener listener;
    private User currentUser;
    private String filterText;

    public ShowCollectedDataAdapter(Context context, List<OdkCollectedDataItem> objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
        this.listener = listener;
    }

    public ShowCollectedDataAdapter(Context context, OdkCollectedDataItem[] objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        for (OdkCollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
        this.currentUser = Bootstrap.getCurrentUser();
        this.listener = listener;
    }

    /*
     * Will allow the adapter items to be selectable or not,
     * Enable or Disables selectability of the List
     */
    public void setMultiSelection(boolean value){
        this.multiSelectable = value;
        //update views by enabling first Checkbox
    }

    public List<OdkCollectedDataItem> getCollectedDataList(){
        return this.collectedDataList;
    }

    public void setCheckedOrUnchecked(int position) {
        OdkCollectedDataItem dataItem = collectedDataList.get(position);
        if (!dataItem.isFormExtension()) { /* not uploaded odk forms should be considered */
            selectedList[position] = !selectedList[position];
            dataItem.selected = selectedList[position];
            notifyDataSetChanged();

            if (listener != null) {
                listener.onCheckedStatusChanged(position, selectedList[position], areAllChecked(), hasAnyChecked());
            }
        }

    }

    public void setAllChecked(boolean checked) {

        for (int position = 0; position < selectedList.length; position++) {
            if (!collectedDataList.get(position).isFormExtension()) {
                selectedList[position] = checked;
            }
        }

        notifyItemRangeChanged(0, selectedList.length);

        if (listener != null) {
            listener.onCheckedStatusChanged(-1, checked, areAllChecked(), hasAnyChecked());
        }
    }

    public boolean areAllChecked(){
        for (boolean selected : selectedList) {
            if (!selected) return false;
        }

        return true;
    }

    public boolean hasAnyChecked(){
        for (boolean selected : selectedList) {
            if (selected) return true;
        }

        return false;
    }

    public void setChecked(int position, boolean checked) {
        selectedList[position] = checked;
        notifyItemChanged(position);
    }

    public List<OdkCollectedDataItem> getSelectedCollectedData() {
        List<OdkCollectedDataItem> list = new ArrayList<>();

        for (int i = 0; i < selectedList.length; i++) {
            if (selectedList[i] == true) {
                list.add(collectedDataList.get(i));
            }
        }

        return list;
    }

    @NonNull
    @Override
    public CollectedDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.form_odk_collected_item, parent, false);
        return new CollectedDataViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull CollectedDataViewHolder holder, int position) {
        OdkCollectedDataItem item = getItem(position);
        holder.setValues(item);
    }

    @Override
    public int getItemCount() {
        return filterList(collectedDataList).size();
    }

    public OdkCollectedDataItem getItem(int position) {
        return filterList(collectedDataList).get(position);
    }

    public void filterSubjects(String text){
        //Log.d("filtering", ""+text);

        if (StringUtil.isBlank(text)){
            this.filterText = null;
        } else {
            this.filterText = text;
        }

        notifyDataSetChanged();
    }

    public boolean codeMatches(OdkCollectedDataItem item, String code){
        String codeRegex = ".*" + code.toLowerCase() + ".*";
        return item.contentMatches(codeRegex);
    }

    private List<OdkCollectedDataItem> filterList(List<OdkCollectedDataItem> itemList){
        if (filterText==null) return itemList;

        List<OdkCollectedDataItem> filtered = new ArrayList<>();

        for (OdkCollectedDataItem item : itemList) {
            if (codeMatches(item, filterText)){
                filtered.add(item);
            }
        }

        return filtered;
    }

    class CollectedDataViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup rowView;

        public CollectedDataViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(OdkCollectedDataItem cdi) {
            TextView txtName = rowView.findViewById(R.id.txtItem1);
            TextView txtForm = rowView.findViewById(R.id.txtItem2);
            TextView txtSubject = rowView.findViewById(R.id.txtItem3);
            TextView txtDate = rowView.findViewById(R.id.txtItem4);
            CheckBox chkSelected = rowView.findViewById(R.id.chkSelected);
            ImageView iconView = rowView.findViewById(R.id.iconView);
            ImageView groupIconView = rowView.findViewById(R.id.groupIconView);
            ImageView hdsIconView = rowView.findViewById(R.id.hdsIconView);
            TextView txtFinalized = rowView.findViewById(R.id.txtFinalized);
            TextView txtNotFinalized = rowView.findViewById(R.id.txtNotFinalized);

            //Member mb = cdi.getMember();
            CollectedData cd = cdi.getCollectedData();
            String instanceName = cd.getFormInstanceName();
            String formName = cdi.getFormName(mContext);
            String formGroupName = cd.formGroupName;
            String subjectText = getFormText(cdi);
            String updatedDate = StringUtil.format(cd.formLastUpdatedDate, "yyyy-MM-dd HH:mm:ss");
            String processed = "0";

            txtName.setText(instanceName);
            txtForm.setText(cd.formGroupCollected ? (formName + " -> " + formGroupName) : formName);
            txtSubject.setText(subjectText);
            txtDate.setText(mContext.getString(R.string.core_entity_updated_date_lbl) + " " +updatedDate);
            txtFinalized.setVisibility(cd.isFormFinalized() ? View.VISIBLE : View.GONE);
            txtNotFinalized.setVisibility(cd.isFormFinalized() ? View.GONE : View.VISIBLE);

            chkSelected.setChecked(cdi.selected);
            chkSelected.setEnabled(!cdi.isFormExtension());

            iconView.setVisibility(cd.formGroupCollected || cdi.isFormExtension() ? View.GONE : View.VISIBLE);
            groupIconView.setVisibility(cd.formGroupCollected ? View.VISIBLE : View.GONE);
            hdsIconView.setVisibility(cdi.isFormExtension() ? View.VISIBLE : View.GONE);
        }

        private String getFormText(OdkCollectedDataItem cdi) {

            if (cdi.isRegionItem()) {
                return "["+ mContext.getString(R.string.form_subject_region_lbl) +"]: " + cdi.getRegion().code + " -> " + cdi.getRegion().name;
            }
            if (cdi.isHouseholdItem()) {
                return "["+ mContext.getString(R.string.form_subject_household_lbl) +"]: " + cdi.getHousehold().code + " -> " + cdi.getHousehold().name;
            }
            if (cdi.isMemberItem()) {
                return "["+ mContext.getString(R.string.form_subject_member_lbl) +"]: " + cdi.getMember().code + " -> " + cdi.getMember().name;
            }

            return "SUBJECT NOT AVAILABLE";
        }
    }

    public interface OnItemActionListener {
        public void onInfoButtonClicked(OdkCollectedDataItem collectedData);

        /*
         * position = -1 - means selectall
         */
        void onCheckedStatusChanged(int position, boolean checkedStatus, boolean allChecked, boolean anyChecked);
    }
}
