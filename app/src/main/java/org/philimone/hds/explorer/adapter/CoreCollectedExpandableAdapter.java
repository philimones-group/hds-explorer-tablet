package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.CoreCollectedData;
import org.philimone.hds.explorer.model.CoreFormExtension;
import org.philimone.hds.explorer.model.enums.CoreFormEntity;
import org.philimone.hds.explorer.widget.CirclePercentageBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import mz.betainteractive.utilities.StringUtil;

public class CoreCollectedExpandableAdapter extends BaseExpandableListAdapter implements Serializable {

    private Context mContext;
    private List<CoreFormEntity> groupItems;
    private List<List<CoreCollectedData>> childItems;

    public CoreCollectedExpandableAdapter(Context mContext, LinkedHashMap<CoreFormEntity, List<CoreCollectedData>> collectionMap) {
        this.mContext = mContext;
        this.groupItems = new ArrayList<>();
        this.childItems = new ArrayList<>();

        collectionMap.forEach((coreFormEntity, coreCollectedDataList) -> {
            groupItems.add(coreFormEntity);
            childItems.add(new ArrayList<>(coreCollectedDataList));
        });
    }

    @Override
    public int getGroupCount() {
        return groupItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childItems.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.groupItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.childItems.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return (groupPosition*1000)+childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.core_form_group_item, parent, false);
        }

        ImageView iconView = convertView.findViewById(R.id.groupItemIcon);
        TextView txtGroupTitle = convertView.findViewById(R.id.txtGroupTitle);
        TextView txtGroupSubtitle = convertView.findViewById(R.id.txtGroupSubtitle);
        CirclePercentageBar pBar = convertView.findViewById(R.id.pbarChildsItem);

        pBar.setVisibility(View.GONE);

        CoreFormEntity formEntity = groupItems.get(groupPosition);
        int n = childItems.get(groupPosition).size();

        //update icon
        iconView.setImageResource(getIcon(formEntity));

        txtGroupTitle.setText(mContext.getString(formEntity.name));
        txtGroupSubtitle.setText(n + " " + mContext.getString(R.string.core_form_group_item_subtitle));
        pBar.setMaxValue(n);
        pBar.setValue(n);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View rowView, ViewGroup parent) {

        if (rowView==null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.core_form_group_child_item, parent, false);
        }

        CoreCollectedData cd = (CoreCollectedData) getChild(groupPosition, childPosition);
        CoreFormExtension formExtension = cd.extension.getTarget();

        TextView txtItem1 = rowView.findViewById(R.id.txtItem1);
        TextView txtItem2 = rowView.findViewById(R.id.txtItem2);
        TextView txtItem3 = rowView.findViewById(R.id.txtItem3);
        TextView txtItem4 = rowView.findViewById(R.id.txtItem4);
        Button button = rowView.findViewById(R.id.btnItemInfo);
        CheckBox chkProcessed = rowView.findViewById(R.id.chkProcessed);

        button.setVisibility(View.GONE);
        chkProcessed.setVisibility(View.GONE);
        txtItem3.setVisibility(formExtension==null ? View.GONE : View.VISIBLE);

        String createdDate = cd.createdDate==null ? "" : StringUtil.format(cd.createdDate, "yyyy-MM-dd HH:mm:ss");
        String updatedDate = cd.updatedDate==null ? "" : StringUtil.format(cd.updatedDate, "yyyy-MM-dd HH:mm:ss");
        String uploadedDate = cd.uploadedDate==null ? "" : StringUtil.format(cd.uploadedDate, "yyyy-MM-dd HH:mm:ss");
        String code = StringUtil.isBlank(cd.formEntityCode) ? "" : cd.formEntityCode + " - ";

        txtItem1.setText(cd.formEntityName);
        //txtItem2.setText(code + this.mContext.getString(cd.formEntity.name));
        txtItem2.setText(createdDate);

        if (formExtension != null) {
            String message = "";

            if (cd.extensionCollected) {
                message = mContext.getString(R.string.core_form_group_item_extension_collected_lbl);
            } else if(formExtension.required){
                message = mContext.getString(R.string.core_form_group_item_extension_not_collected_lbl);
            } else {
                message = mContext.getString(R.string.core_form_group_item_extension_not_required_lbl);
            }

            txtItem3.setText(mContext.getString(R.string.core_form_group_item_extension_lbl));
            txtItem4.setText(message);
        }


        return rowView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private @DrawableRes int getIcon(CoreFormEntity formEntity){
        switch (formEntity) {
            case HOUSEHOLD:
                return R.mipmap.nui_household_filled_icon_dark;
            case VISIT:
                return R.mipmap.nui_household_filled_icon_dark;
            case MEMBER_ENU:
                return R.mipmap.nui_member_filled_icon_dark;
            case HEAD_RELATIONSHIP:
                return R.mipmap.nui_member_filled_icon_dark;
            case MARITAL_RELATIONSHIP:
                return R.mipmap.nui_marital_relationship_icon_dark;
            case INMIGRATION:
                return R.mipmap.nui_int_inmigration_icon_dark;
            case EXTERNAL_INMIGRATION:
                return R.mipmap.nui_ext_inmigration_icon_dark;
            case OUTMIGRATION:
                return R.mipmap.nui_outmigration_icon_dark;
            case PREGNANCY_REGISTRATION:
                return R.mipmap.nui_pregnancy_icon_dark;
            case PREGNANCY_OUTCOME:
                return R.mipmap.nui_baby_icon_dark;
            case DEATH:
                return R.mipmap.nui_death_icon_dark;
            case CHANGE_HOUSEHOLD_HEAD:
                return R.mipmap.nui_changehoh_icon_dark;
            case INCOMPLETE_VISIT:
                return R.mipmap.nui_member_incomplete_icon_dark;

        }

        return R.mipmap.nui_lists_members_white_icon;
    }
}
