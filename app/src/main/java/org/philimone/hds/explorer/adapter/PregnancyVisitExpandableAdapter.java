package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.PregnancyVisit;
import org.philimone.hds.explorer.model.enums.PregnancyVisitType;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class PregnancyVisitExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<PregnancyVisitType> groupList;
    private Map<PregnancyVisitType, List<PregnancyVisit>> childMap;
    private Listener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private boolean canAddAntepartum;
    private boolean canAddPostpartum;

    public PregnancyVisitExpandableAdapter(Context context, List<PregnancyVisitType> groupList, Map<PregnancyVisitType, List<PregnancyVisit>> childMap, boolean canAddAntepartum, boolean canAddPostpartum, Listener listener) {
        this.context = context;
        this.groupList = groupList;
        this.childMap = childMap;
        this.canAddAntepartum = canAddAntepartum;
        this.canAddPostpartum = canAddPostpartum;
        this.listener = listener;
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<PregnancyVisit> children = childMap.get(groupList.get(groupPosition));
        return children != null ? children.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childMap.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        PregnancyVisitType group = (PregnancyVisitType) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pregnancy_surveillance_visit_group_item, parent, false);
        }

        TextView txtGroupName = convertView.findViewById(R.id.txtGroupName);
        Button btAddVisit = convertView.findViewById(R.id.btAddVisit);
        ImageView iconCollapsed = convertView.findViewById(R.id.iconCollapsed);
        ImageView iconExpanded = convertView.findViewById(R.id.iconExpanded);

        iconExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        iconCollapsed.setVisibility(!isExpanded ? View.VISIBLE : View.GONE);

        int count = getChildrenCount(groupPosition);
        if (group == PregnancyVisitType.ANTEPARTUM) {
            txtGroupName.setText(context.getString(R.string.pregnancy_panel_antepartum_visits, count));
            btAddVisit.setEnabled(canAddAntepartum);
            btAddVisit.setAlpha(canAddAntepartum ? 1.0f : 0.5f);
        } else {
            txtGroupName.setText(context.getString(R.string.pregnancy_panel_postpartum_visits, count));
            btAddVisit.setEnabled(canAddPostpartum);
            btAddVisit.setAlpha(canAddPostpartum ? 1.0f : 0.5f);
        }

        btAddVisit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddVisitClicked(group);
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        PregnancyVisit visit = (PregnancyVisit) getChild(groupPosition, childPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.pregnancy_surveillance_visit_child_item, parent, false);
        }

        TextView txtVisitDate = convertView.findViewById(R.id.txtVisitDate);
        TextView txtVisitDetail1 = convertView.findViewById(R.id.txtVisitDetail1);
        TextView txtVisitDetail2 = convertView.findViewById(R.id.txtVisitDetail2);
        TextView txtVisitComplications = convertView.findViewById(R.id.txtVisitComplications);
        ImageView imgVisitStatus = convertView.findViewById(R.id.imgVisitStatus);

        txtVisitDate.setText(context.getString(R.string.pregnancy_panel_outcome_date_lbl, dateFormat.format(visit.visitDate)));
        
        if (visit.visitType == PregnancyVisitType.ANTEPARTUM) {
            txtVisitDetail1.setText(context.getString(R.string.pregnancy_panel_visit_detail_gest_weeks_lbl, visit.weeksGestation));
            txtVisitDetail2.setText(context.getString(R.string.pregnancy_panel_visit_detail_provider_lbl, visit.prenatalCareProvider != null ? visit.prenatalCareProvider.name() : context.getString(R.string.not_applicable_lbl)));
            boolean complications = visit.complicationsReported != null && visit.complicationsReported;
            String compDetails = complications ? visit.complicationDetails : context.getString(R.string.pregnancy_panel_visit_detail_none_lbl);
            txtVisitComplications.setText(context.getString(R.string.pregnancy_panel_visit_detail_complications_lbl, compDetails));
            txtVisitComplications.setTextColor(complications ? context.getResources().getColor(android.R.color.holo_red_dark) : context.getResources().getColor(android.R.color.darker_gray));
            imgVisitStatus.setImageResource(complications ? R.mipmap.nui_pregnancy_visit_icon_red : R.mipmap.nui_pregnancy_visit_icon_dark);
        } else {
            txtVisitDetail1.setText(context.getString(R.string.pregnancy_panel_visit_detail_breastfeeding_lbl, visit.breastfeedingStatus != null ? visit.breastfeedingStatus.name() : context.getString(R.string.not_applicable_lbl)));
            String resumedStr = (visit.resumedDailyActivities != null && visit.resumedDailyActivities) ? context.getString(R.string.pregnancy_panel_visit_detail_resumed_lbl) : context.getString(R.string.pregnancy_panel_visit_detail_not_resumed_lbl);
            txtVisitDetail2.setText(context.getString(R.string.pregnancy_panel_visit_detail_daily_activities_lbl, resumedStr));
            boolean complications = visit.postpartumComplications != null && visit.postpartumComplications;
            String compDetails = complications ? visit.postpartumComplicationDetails : context.getString(R.string.pregnancy_panel_visit_detail_none_lbl);
            txtVisitComplications.setText(context.getString(R.string.pregnancy_panel_visit_detail_complications_lbl, compDetails));
            txtVisitComplications.setTextColor(complications ? context.getResources().getColor(android.R.color.holo_red_dark) : context.getResources().getColor(android.R.color.darker_gray));
            imgVisitStatus.setImageResource(complications ? R.mipmap.nui_pregnancy_visit_icon_red : R.mipmap.nui_pregnancy_visit_icon_dark);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface Listener {
        void onAddVisitClicked(PregnancyVisitType type);
    }
}
