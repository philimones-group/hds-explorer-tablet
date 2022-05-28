package org.philimone.hds.explorer.adapter.trackinglist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.model.Household;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.Region;
import org.philimone.hds.explorer.widget.CirclePercentageBar;

import java.util.List;

public class TrackingListExpandableViewHolder extends RecyclerView.ViewHolder {

    private ViewGroup mainView;

    public TrackingListExpandableViewHolder(@NonNull View itemView) {
        super(itemView);
        mainView = (ViewGroup) itemView;
    }

    public ViewGroup getMainView() {
        return mainView;
    }

    public void setValues(TrackingGroupItem item) {
        //get different items and set them
        if (item.isGroupItem()) {
            setGroup(item);
        } else if (item.isChildItem()) {
            setChild(item);
        }
    }

    private void setGroup(TrackingGroupItem exListItem) {
        TextView txtTitle = mainView.findViewById(R.id.txtTrackSubListTitle);
        TextView txtExtras = mainView.findViewById(R.id.txtTrackSubListExtras);
        CirclePercentageBar pBar = mainView.findViewById(R.id.pbarTrackListItem);
        ImageView iconCollapsed = mainView.findViewById(R.id.iconCollapsed);
        ImageView iconExpanded = mainView.findViewById(R.id.iconExpanded);

        TrackingSubListItem listItem = exListItem.getGroupItem();
        int n = exListItem.getChilds().size();

        iconCollapsed.setVisibility(listItem.isCollapsed() ? View.VISIBLE : View.GONE);
        iconExpanded.setVisibility(listItem.isExpanded() ? View.VISIBLE : View.GONE);
        txtTitle.setText(listItem.getTitle());
        txtExtras.setText(n + " Subjects");
        pBar.setPercentageValue(getCompletionOfList(exListItem.getChilds()));
    }

    private void setChild(TrackingGroupItem exListItem) {
        TextView txtName = mainView.findViewById(R.id.txtMemberItemName);
        TextView txtId = mainView.findViewById(R.id.txtMemberItemCode);
        TextView txtDetails = mainView.findViewById(R.id.txtMemberItemExtras);
        CirclePercentageBar pBar = mainView.findViewById(R.id.pbarTrackListItem);

        TrackingSubjectItem subjectItem = exListItem.getChildItem();

        if (subjectItem.isRegionSubject()) {
            Region region = subjectItem.getRegion();
            txtName.setText(region.getName());
            txtId.setText(region.getCode() + " -> " + region.getLevel());
            txtDetails.setText(subjectItem.getSubjectType());
        }

        if (subjectItem.isHouseholdSubject()) {
            Household household = subjectItem.getHousehold();
            txtName.setText(household.getName());
            txtId.setText(household.getCode());
            txtDetails.setText(household.getHeadName());
        }

        if (subjectItem.isMemberSubject()) {
            Member member = subjectItem.getMember();
            txtName.setText(member.getName());
            txtId.setText(member.getHouseholdName() + " -> " + member.getCode());
            txtDetails.setText(subjectItem.getSubjectType());
        }

        pBar.setMaxValue(subjectItem.getForms().size());
        pBar.setValue(subjectItem.getCollectedForms().size());

    }

    public int getCompletionOfList(List<TrackingSubjectItem> childList) {
        double to_collect = 0;
        double collected = 0;

        for (TrackingSubjectItem memberItem : childList) {
            to_collect += memberItem.getForms().size() * 1D;
            collected += memberItem.getCollectedForms().size() * 1D;
        }

        if (to_collect == 0) return 100;

        return (int) ((collected / to_collect) * 100);
    }
}
