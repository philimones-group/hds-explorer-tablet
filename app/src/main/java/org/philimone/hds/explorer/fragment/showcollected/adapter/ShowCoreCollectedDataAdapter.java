package org.philimone.hds.explorer.fragment.showcollected.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.fragment.showcollected.adapter.model.CoreCollectedDataItem;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class ShowCoreCollectedDataAdapter extends RecyclerView.Adapter<ShowCoreCollectedDataAdapter.CoreCollectedDataViewHolder> {
    private List<CoreCollectedDataItem> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable = true;
    private Context mContext;
    private OnItemActionListener listener;
    private String filterText;
    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public ShowCoreCollectedDataAdapter(Context context, List<CoreCollectedDataItem> objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
        this.listener = listener;
    }

    public ShowCoreCollectedDataAdapter(Context context, CoreCollectedDataItem[] objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        for (CoreCollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
        this.listener = listener;
    }

    public List<CoreCollectedDataItem> getCollectedDataList(){
        return this.collectedDataList;
    }



    public void setCheckedOrUnchecked(int position) {
        CoreCollectedDataItem dataItem = collectedDataList.get(position);
        if (dataItem.collectedData.uploaded==false || dataItem.collectedData.uploadedWithError) {
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
            if (!collectedDataList.get(position).collectedData.uploaded || collectedDataList.get(position).collectedData.uploadedWithError) {
                selectedList[position] = checked;
            }
        }

        notifyDataSetChanged();

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
        notifyDataSetChanged();
    }

    public List<CoreCollectedDataItem> getSelectedCollectedData() {
        List<CoreCollectedDataItem> list = new ArrayList<>();

        for (int i = 0; i < selectedList.length; i++) {
            if (selectedList[i] == true) {
                list.add(collectedDataList.get(i));
            }
        }

        return list;
    }

    @NonNull
    @Override
    public CoreCollectedDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.core_form_item, parent, false);
        return new CoreCollectedDataViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull CoreCollectedDataViewHolder holder, int position) {
        CoreCollectedDataItem coreCollectedData = getItem(position);
        holder.setValues(coreCollectedData);
    }

    @Override
    public int getItemCount() {
        return filterList(this.collectedDataList).size();
    }

    public CoreCollectedDataItem getItem(int position) {
        return filterList(collectedDataList).get(position);
    }

    public void filterSubjects(String text){
        Log.d("filtering", ""+text);

        if (StringUtil.isBlank(text)){
            this.filterText = null;
        } else {
            this.filterText = text;
        }

        notifyDataSetChanged();
    }

    public boolean codeMatches(CoreCollectedDataItem item, String code){
        String codeRegex = ".*" + code.toLowerCase() + ".*";
        String formEntityNameText = mContext.getString(item.collectedData.formEntity.name);
        return item.contentMatches(codeRegex, formEntityNameText);
    }

    private List<CoreCollectedDataItem> filterList(List<CoreCollectedDataItem> itemList){
        if (filterText==null) return itemList;

        List<CoreCollectedDataItem> filtered = new ArrayList<>();

        for (CoreCollectedDataItem item : itemList) {
            if (codeMatches(item, filterText)){
                filtered.add(item);
            }
        }

        return filtered;
    }

    public interface OnItemActionListener {
        public void onInfoButtonClicked(CoreCollectedDataItem collectedData);

        /*
         * position = -1 - means selectall
         */
        void onCheckedStatusChanged(int position, boolean checkedStatus, boolean allChecked, boolean anyChecked);
    }

    class CoreCollectedDataViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup rowView;

        public CoreCollectedDataViewHolder(@NonNull View itemView) {
            super(itemView);
            this.rowView = (ViewGroup) itemView;
        }

        public void setValues(CoreCollectedDataItem dataItem) {

            CoreCollectedData cd = dataItem.collectedData;
            Household hh = dataItem.household;
            Member mm = dataItem.member;
            Region rr = dataItem.region;

            ImageView iconView = rowView.findViewById(R.id.iconView);
            ImageView iconView2Reg = rowView.findViewById(R.id.iconView2);
            ImageView iconView3Hou = rowView.findViewById(R.id.iconView3);
            ImageView iconView4Mem = rowView.findViewById(R.id.iconView4);
            TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
            TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
            TextView txtItem3 = rowView.findViewById(R.id.txtItem3);
            TextView txtItem4 = rowView.findViewById(R.id.txtItem4);
            CheckBox chkSelected = rowView.findViewById(R.id.chkProcessed);

            String createdDate = cd.createdDate==null ? "" : dateUtil.formatYMDHMS(cd.createdDate);
            String updatedDate = cd.updatedDate==null ? createdDate : dateUtil.formatYMDHMS(cd.updatedDate);
            String uploadedDate = cd.uploadedDate==null ? "" : dateUtil.formatYMDHMS(cd.uploadedDate);
            String code = StringUtil.isBlank(cd.formEntityCode) ? "" : cd.formEntityCode + " - ";
            String name = cd.formEntityName; //hh!=null ? hh.name : mm!=null ? mm.name : rr!=null ? rr.name : "";


            txtItem1.setText(mContext.getString(cd.formEntity.name) + " (" + cd.formEntityCode + ")");
            txtItem2.setText(name);
            txtItem3.setText(mContext.getString(R.string.core_entity_updated_date_lbl) + " " + updatedDate);
            txtItem4.setText(mContext.getString(R.string.core_entity_uploaded_date_lbl)  + " " + uploadedDate);

            chkSelected.setChecked(dataItem.selected);
            //Visit based collected data
            //Non-visit based collected data
            //New Region
            //Edited Region
            //Edited Household
            //Edited Member

            iconView.setVisibility(View.GONE);
            iconView2Reg.setVisibility(View.GONE);
            iconView3Hou.setVisibility(View.GONE);
            iconView4Mem.setVisibility(View.GONE);

            if (cd.visitId != 0) { //visit based
                iconView.setVisibility(View.VISIBLE);
                //txtItem4.setVisibility(View.VISIBLE);
            } else { //non visit based
                iconView2Reg.setVisibility(rr != null ? View.VISIBLE : View.GONE);
                iconView3Hou.setVisibility(hh != null ? View.VISIBLE : View.GONE);
                iconView4Mem.setVisibility(mm != null ? View.VISIBLE : View.GONE);
            }

            if (dataItem.collectedData.uploaded) {
                chkSelected.setEnabled(false);
            }

            if (cd.recordType == CoreFormRecordType.UPDATE_RECORD) {
                //DO SOMETHING DIFFERENT WITH EDIT/UPDATES
                txtItem2.setPaintFlags(txtItem2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                txtItem2.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            }

            //rowView.setBackgroundColor(checkable ? ContextCompat.getColor(mContext, R.color.zxing_transparent) : ContextCompat.getColor(mContext, R.color.nui_sync_lists_selected_item_color_1));
        }
    }
}
