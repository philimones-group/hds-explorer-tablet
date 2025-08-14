package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.UploadCollectedDataItem;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class UploadCoreCollectedDataAdapter extends RecyclerView.Adapter<UploadCoreCollectedDataAdapter.CoreCollectedDataViewHolder> {
    private List<UploadCollectedDataItem> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable = true;
    private Context mContext;
    private OnItemActionListener listener;
    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public UploadCoreCollectedDataAdapter(Context context, List<UploadCollectedDataItem> objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
        this.listener = listener;
    }

    public UploadCoreCollectedDataAdapter(Context context, UploadCollectedDataItem[] objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        for (UploadCollectedDataItem cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
        this.listener = listener;
    }

    public List<UploadCollectedDataItem> getCollectedDataList(){
        return this.collectedDataList;
    }

    public UploadCollectedDataItem getItem(int position) {
        return collectedDataList.get(position);
    }

    public void setCheckedOrUnchecked(int position) {
        CoreCollectedData cd = collectedDataList.get(position).getCoreCollectedData();

        if (cd.uploaded==false || cd.uploadedWithError) {
            selectedList[position] = !selectedList[position];
            notifyDataSetChanged();

            if (listener != null) {
                listener.onCheckedStatusChanged(position, selectedList[position], areAllChecked(), hasAnyChecked());
            }
        }

    }

    public void setAllChecked(boolean checked) {

        for (int position = 0; position < selectedList.length; position++) {
            CoreCollectedData cd = collectedDataList.get(position).getCoreCollectedData();
            if (!cd.uploaded || cd.uploadedWithError) {
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

    public List<UploadCollectedDataItem> getSelectedCollectedData() {
        List<UploadCollectedDataItem> list = new ArrayList<>();

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
        View rowView = inflater.inflate(R.layout.core_form_collected_item, parent, false);
        return new CoreCollectedDataViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(@NonNull CoreCollectedDataViewHolder holder, int position) {
        UploadCollectedDataItem coreCollectedData = getItem(position);
        holder.setValues(coreCollectedData);
    }

    @Override
    public int getItemCount() {
        return this.collectedDataList.size();
    }

    public interface OnItemActionListener {
        public void onInfoButtonClicked(CoreCollectedData collectedData);

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

        public void setValues(UploadCollectedDataItem dataItem) {

            CoreCollectedData  cd = dataItem.getCoreCollectedData();
            CoreFormExtension formExtension = cd.extension.getTarget();

            TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
            TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
            TextView txtItem3 = rowView.findViewById(R.id.txtItem3);
            TextView txtItem4 = rowView.findViewById(R.id.txtItem4);
            TextView txtItem5 = rowView.findViewById(R.id.txtItem5);
            Button button = rowView.findViewById(R.id.btnItemInfo);
            CheckBox chkProcessed = rowView.findViewById(R.id.chkProcessed);

            int position = collectedDataList.indexOf(dataItem);
            boolean checked = selectedList[position];
            boolean checkable = (cd.uploaded && !cd.uploadedWithError);

            button.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInfoButtonClicked(cd);
                }
            });

            String createdDate = cd.createdDate==null ? "" : dateUtil.formatYMDHMS(cd.createdDate);
            String updatedDate = cd.updatedDate==null ? "" : dateUtil.formatYMDHMS(cd.updatedDate);
            String uploadedDate = cd.uploadedDate==null ? "" : dateUtil.formatYMDHMS(cd.uploadedDate);
            String code = StringUtil.isBlank(cd.formEntityCode) ? "" : cd.formEntityCode + " - ";

            txtItem1.setText(cd.formEntityName);
            txtItem2.setText(code + mContext.getString(cd.formEntity.name));
            txtItem3.setText(mContext.getString(R.string.core_entity_collected_date_lbl)+createdDate+", " + mContext.getString(R.string.core_entity_uploaded_date_lbl)+uploadedDate);
            chkProcessed.setChecked(checked);
            button.setEnabled(cd.uploadedWithError);

            if (cd.recordType == CoreFormRecordType.UPDATE_RECORD) {
                //DO SOMETHING DIFFERENT WITH EDIT/UPDATES
                txtItem2.setPaintFlags(txtItem2.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            } else {
                txtItem2.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            }

            if (formExtension != null) {

                if (formExtension.enabled){
                    String message = "";

                    if (dataItem.isExtensionCollected()) {
                        message = mContext.getString(R.string.core_form_group_item_extension_collected_lbl);

                        if (dataItem.odkFormStatus == FormUtilities.FormStatus.UNFINALIZED) {
                            message += " (" + mContext.getString(R.string.odk_unfinished_extension_notfinalized) + ")";
                        } else if (dataItem.odkFormStatus == FormUtilities.FormStatus.NOT_FOUND) {
                            message += " (" + mContext.getString(R.string.odk_unfinished_extension_notfound) + ")";
                        }

                    } else if(formExtension.required){
                        message = mContext.getString(R.string.core_form_group_item_extension_not_collected_lbl);
                    } else {
                        message = mContext.getString(R.string.core_form_group_item_extension_not_required_lbl);
                    }

                    txtItem4.setText(mContext.getString(R.string.core_form_group_item_extension_lbl));
                    txtItem5.setText(message);
                } else {
                    txtItem4.setVisibility(View.GONE);
                    txtItem5.setVisibility(View.GONE);
                }
            } else {
                txtItem4.setVisibility(View.GONE);
                txtItem5.setVisibility(View.GONE);
            }

            chkProcessed.setVisibility(checkable ? View.INVISIBLE : View.VISIBLE);

            //rowView.setBackgroundColor(checkable ? ContextCompat.getColor(mContext, R.color.zxing_transparent) : ContextCompat.getColor(mContext, R.color.nui_sync_lists_selected_item_color_1));

            if (!dataItem.isFormExtensionValid()){
                rowView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.nui_sync_lists_selected_item_color_1));
            }
        }
    }
}
