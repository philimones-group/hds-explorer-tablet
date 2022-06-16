package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.enums.CoreFormRecordType;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.utilities.StringUtil;

/**
 * Created by paul on 8/10/16.
 */
public class CoreCollectedDataAdapter extends RecyclerView.Adapter<CoreCollectedDataAdapter.CoreCollectedDataViewHolder> {
    private List<CoreCollectedData> collectedDataList;
    private boolean[] selectedList;
    private boolean multiSelectable = true;
    private Context mContext;
    private OnItemActionListener listener;

    public CoreCollectedDataAdapter(Context context, List<CoreCollectedData> objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        this.collectedDataList.addAll(objects);
        this.selectedList = new boolean[objects.size()];
        this.mContext = context;
        this.listener = listener;
    }

    public CoreCollectedDataAdapter(Context context, CoreCollectedData[] objects, OnItemActionListener listener){
        this.collectedDataList = new ArrayList<>();
        for (CoreCollectedData cd : objects) this.collectedDataList.add(cd);
        this.selectedList = new boolean[objects.length];
        this.mContext = context;
        this.listener = listener;
    }

    public List<CoreCollectedData> getCollectedDataList(){
        return this.collectedDataList;
    }

    public CoreCollectedData getItem(int position) {
        return collectedDataList.get(position);
    }

    public void setCheckedOrUnchecked(int position) {

        if (collectedDataList.get(position).uploaded==false || collectedDataList.get(position).uploadedWithError) {
            selectedList[position] = !selectedList[position];
            notifyDataSetChanged();

            if (listener != null) {
                listener.onCheckedStatusChanged(position, selectedList[position], areAllChecked(), hasAnyChecked());
            }
        }

    }

    public void setAllChecked(boolean checked) {

        for (int position = 0; position < selectedList.length; position++) {
            if (!collectedDataList.get(position).uploaded || collectedDataList.get(position).uploadedWithError) {
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

    public List<CoreCollectedData> getSelectedCollectedData() {
        List<CoreCollectedData> list = new ArrayList<>();

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
        CoreCollectedData coreCollectedData = getItem(position);
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

        public void setValues(CoreCollectedData cd) {

            TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
            TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
            TextView txtItem3 = rowView.findViewById(R.id.txtItem3);
            Button button = rowView.findViewById(R.id.btnItemInfo);
            CheckBox chkProcessed = rowView.findViewById(R.id.chkProcessed);

            int position = collectedDataList.indexOf(cd);
            boolean checked = selectedList[position];
            boolean checkable = (cd.uploaded && !cd.uploadedWithError);

            button.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInfoButtonClicked(cd);
                }
            });

            String createdDate = cd.createdDate==null ? "" : StringUtil.format(cd.createdDate, "yyyy-MM-dd HH:mm:ss");
            String updatedDate = cd.updatedDate==null ? "" : StringUtil.format(cd.updatedDate, "yyyy-MM-dd HH:mm:ss");
            String uploadedDate = cd.uploadedDate==null ? "" : StringUtil.format(cd.uploadedDate, "yyyy-MM-dd HH:mm:ss");
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

            chkProcessed.setVisibility(checkable ? View.INVISIBLE : View.VISIBLE);

            rowView.setBackgroundColor(checkable ? ContextCompat.getColor(mContext, R.color.zxing_transparent) : ContextCompat.getColor(mContext, R.color.nui_sync_lists_selected_item_color_1));
        }
    }
}
