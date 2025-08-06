package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mz.betainteractive.utilities.DateUtil;
import mz.betainteractive.utilities.StringUtil;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> members;
    private List<Boolean> checkableMembers;
    private List<String> externalDataList;
    private Context mContext;
    private @LayoutRes int layoutResourceId;
    private int selectedIndex = -1;
    private boolean showHouseholdHeadIcon = true;
    private boolean showHouseholdAndCode = false;
    private boolean showMemberCodeWithHousehold = false;
    private boolean showExternalData = false;
    private boolean showExtraDetails = false;
    private boolean showMemberDetails = false;
    boolean showEndTypeCode = false; //put as general variable
    boolean showResidencyStatus = false; //put as general variable
    private MemberIcon memberIcon;

    private DateUtil dateUtil = Bootstrap.getDateUtil();

    public enum MemberIcon {NORMAL_MEMBER_ICON, NORMAL_MEMBER_CHECKED_ICON, NORMAL_HEAD_ICON, NORMAL_SECHEAD_ICON, NORMAL_MEMBER_NEW_ICON, NORMAL_HEAD_NEW_ICON}

    /**
     * Adapter of a List View Item for members (name and code are displayed)
     * @param context
     * @param objects
     */
    public MemberAdapter(Context context, List<Member> objects){
        this.members = new ArrayList<>();
        this.members.addAll(objects);
        this.mContext = context;
        this.layoutResourceId = R.layout.member_item_xtra;
    }

    public MemberAdapter(Context context, @LayoutRes int layoutResId, List<Member> objects){
        this.members = new ArrayList<>();
        this.members.addAll(objects);
        this.mContext = context;
        this.layoutResourceId = layoutResId;
    }


    /**
     * Adapter of a List View Item for members (name, code, and a check box are displayed)
     * @param context
     * @param objects
     * @param checks
     */
    public MemberAdapter(Context context, List<Member> objects, List<Boolean> checks){
        this.members = new ArrayList<>();
        this.members.addAll(objects);

        if (checks != null){
            this.checkableMembers = new ArrayList<>();
            this.checkableMembers.addAll(checks);
        }

        this.mContext = context;
        this.layoutResourceId = R.layout.member_item_chk;
    }

    /**
     * Adapter of a List View Item for members (name, perm-id, extra text view and a large sized icon are displayed)
     * @param context
     * @param objects
     * @param externalDataList
     */
    public MemberAdapter(Context context, List<Member> objects, ArrayList<String> externalDataList){
        this.members = new ArrayList<>();
        this.members.addAll(objects);

        if (externalDataList != null){
            this.externalDataList = new ArrayList<>();
            this.externalDataList.addAll(externalDataList);
            this.showExternalData = true;
        }

        this.showHouseholdHeadIcon = true;

        this.mContext = context;
        this.layoutResourceId = R.layout.member_item_xtra;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layoutResourceId, parent, false);

        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member member = this.members.get(position);
        holder.setValues(member);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public Member getItem(int position) {
        return this.members.get(position);
    }

    private int getPosition(Member member) {
        return members.indexOf(member);
    }

    public List<Member> getMembers(){
        return this.members;
    }

    public void setSelectedIndex(int index){
        this.selectedIndex = index;
        notifyDataSetChanged();
    }

    public int getSelectedIndex(){
        return selectedIndex;
    }

    public Member getSelectedMember(){
        return (selectedIndex < 0 || selectedIndex >= members.size()) ? null : members.get(selectedIndex);
    }

    public void setShowHouseholdAndCode(boolean showHouseholdAndCode) {
        this.showHouseholdAndCode = showHouseholdAndCode;
    }

    public void setShowHouseholdHeadIcon(boolean showHouseholdHeadIcon) {
        this.showHouseholdHeadIcon = showHouseholdHeadIcon;
    }

    public boolean isShowMemberCodeWithHousehold() {
        return showMemberCodeWithHousehold;
    }

    public void setShowMemberCodeWithHousehold(boolean showMemberCodeWithHousehold) {
        this.showMemberCodeWithHousehold = showMemberCodeWithHousehold;
    }

    public void setShowExtraDetails(boolean showExtraDetails) {
        this.showExtraDetails = showExtraDetails;
    }

    public boolean isShowMemberDetails() {
        return showMemberDetails;
    }

    public void setShowMemberDetails(boolean showGender) {
        this.showMemberDetails = showGender;
    }

    public boolean isShowEndTypeCode() {
        return showEndTypeCode;
    }

    public void setShowEndTypeCode(boolean showEndTypeCode) {
        this.showEndTypeCode = showEndTypeCode;
    }

    public boolean isShowResidencyStatus() {
        return showResidencyStatus;
    }

    public void setShowResidencyStatus(boolean showResidencyStatus) {
        this.showResidencyStatus = showResidencyStatus;
    }

    public boolean isEmpty() {
        return this.members.isEmpty();
    }

    public void setMemberIcon(MemberIcon memberIcon) {
        this.memberIcon = memberIcon;
    }

    public int indexOf(Member member) {
        for (int i = 0; i < this.members.size(); i++) {
            Member m = this.members.get(i);
            if (m != null && m.code.equals(member.code)) {
                return i;
            }
        }

        return -1;
    }

    class MemberViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mainView;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = (ViewGroup) itemView;
        }

        public void setValues(Member mb) {
            ImageView iconView = mainView.findViewById(R.id.iconView);
            TextView txtName = mainView.findViewById(R.id.txtMemberItemName);
            TextView txtCode = mainView.findViewById(R.id.txtMemberItemCode);
            TextView txtMemberItemHousehold = mainView.findViewById(R.id.txtMemberItemHousehold);
            TextView txtExtra = mainView.findViewById(R.id.txtMemberItemExtras);
            CheckBox chkVBprocessed = mainView.findViewById(R.id.chkProcessed);
            int position = MemberAdapter.this.getPosition(mb);
            memberIcon = MemberIcon.NORMAL_MEMBER_ICON;

            String nameText = mb.getName();
            String codeText = mb.getCode();
            String extrasText = "";
            String endTypeText = "";
            String memberDetailsText = "";

            if (showEndTypeCode) {
                switch (mb.endType) {
                    case DEATH:
                        endTypeText = " - DTH";
                        break;
                    case EXTERNAL_OUTMIGRATION:
                        endTypeText = " - EXT";
                        break;
                }
                codeText += endTypeText;
            }

            if (showHouseholdAndCode){
                codeText = mb.getCode() + endTypeText;

                //mb.getHouseholdName()
                if (txtMemberItemHousehold != null) {
                    txtMemberItemHousehold.setVisibility(View.VISIBLE);
                    txtMemberItemHousehold.setText(mb.getHouseholdCode() + " - " + mb.getHouseholdName());
                }

            }

            if (showMemberDetails) {
                String maritalText = mContext.getString(R.string.relationship_type_title_abbrv_lbl) + ": " + mContext.getString(mb.maritalStatus.name);
                memberDetailsText = mContext.getString(R.string.member_details_gender_lbl)+" "+mb.gender.code + ", " + mContext.getString(R.string.member_details_age_lbl)+" "+mb.age + ", " +maritalText;
                extrasText = memberDetailsText;
            }

            if (showExternalData){
                extrasText = externalDataList.get(position);
            }

            if (showResidencyStatus) {
                nameText = mb.getName();
                codeText = mb.getCode() + "  [" + memberDetailsText + "]";
                extrasText = mContext.getString(R.string.household_details_members_item_status_lbl) + " "+ getEndTypeMsg(mb) + ", ";
                extrasText += mContext.getString(R.string.household_details_members_item_since_lbl) + " " + getEndDateMsg(mb);
            }

            if (showMemberCodeWithHousehold) {
                txtMemberItemHousehold.setVisibility(View.GONE);

                codeText += " -> " + (mb.getHouseholdCode() + " - " + mb.getHouseholdName());
            }

            txtName.setText(nameText);
            txtCode.setText(codeText);
            txtExtra.setText(extrasText);

            if (chkVBprocessed != null && checkableMembers != null){
                chkVBprocessed.setChecked(checkableMembers.get(position));
            }

            if (showHouseholdHeadIcon){
                if (mb.isHouseholdHead()){
                    //txtName.setTypeface(null, Typeface.BOLD);
                    //iconView.setImageResource(R.mipmap.nui_member_red_filled_icon);
                    memberIcon = MemberIcon.NORMAL_HEAD_ICON;
                } else if (mb.isSecHouseholdHead()){
                    //txtName.setTypeface(null, Typeface.BOLD);
                    //iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon);
                    memberIcon = MemberIcon.NORMAL_SECHEAD_ICON;
                } else {
                    //iconView.setImageResource(R.mipmap.nui_member_red_icon);
                    memberIcon = MemberIcon.NORMAL_MEMBER_ICON;
                }

            }

            if (mb.isRecentlyCreated()){
                //iconView.setImageResource(R.mipmap.nui_member_red_new_icon);
                memberIcon = MemberIcon.NORMAL_MEMBER_NEW_ICON;

                if (mb.isHouseholdHead() || mb.isSecHouseholdHead()){
                    //iconView.setImageResource(R.mipmap.nui_member_red_filled_new_icon);
                    memberIcon = MemberIcon.NORMAL_HEAD_NEW_ICON;
                }
            }

            if (memberIcon != null){
                switch (memberIcon){
                    case NORMAL_MEMBER_ICON:iconView.setImageResource(R.mipmap.nui_member_red_icon); break;
                    case NORMAL_MEMBER_CHECKED_ICON:iconView.setImageResource(R.mipmap.nui_member_red_chk_icon); break;
                    case NORMAL_HEAD_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_icon); break;
                    case NORMAL_SECHEAD_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon); break;
                    case NORMAL_MEMBER_NEW_ICON:iconView.setImageResource(R.mipmap.nui_member_red_new_icon); break;
                    case NORMAL_HEAD_NEW_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_new_icon); break;
                }
            }

            if (showEndTypeCode){
                //paint with a color
                //txtCode.setTypeface(null, Typeface.BOLD);
                //mainView.setBackgroundColor(mContext.getResources().getColor(R.color.nui_lists_item_special_textcolor));
            }

            if (selectedIndex == position){
                int colorA = mContext.getResources().getColor(R.color.nui_lists_selected_item_textcolor);
                int colorB = mContext.getResources().getColor(R.color.nui_lists_selected_item_color_2);

                mainView.setBackgroundColor(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtExtra != null) txtExtra.setTextColor(colorA);
                if (txtMemberItemHousehold != null) txtMemberItemHousehold.setTextColor(colorA);
            } else {
                TypedValue colorBvalue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, colorBvalue, true);

                int colorA = mContext.getResources().getColor(R.color.nui_member_item_textcolor);
                int colorB = colorBvalue.resourceId;

                mainView.setBackgroundResource(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtExtra != null) txtExtra.setTextColor(colorA);
                if (txtMemberItemHousehold != null) txtMemberItemHousehold.setTextColor(colorA);
            }

            txtExtra.setVisibility(showExtraDetails ? View.VISIBLE : View.GONE);
        }
    }

    private String getEndTypeMsg(Member member){
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) return mContext.getString(R.string.member_details_endtype_na_lbl);
        if (member.getEndType() == ResidencyEndType.EXTERNAL_OUTMIGRATION) return mContext.getString(R.string.member_details_endtype_ext_lbl);
        if (member.getEndType() == ResidencyEndType.DEATH) return mContext.getString(R.string.member_details_endtype_dth_lbl);

        return mContext.getString(ResidencyEndType.INVALID_ENUM.name);
    }

    private String getEndDateMsg(Member member){
        Date date = member.getEndDate();
        if (member.getEndType() == ResidencyEndType.NOT_APPLICABLE) {
            date = member.getStartDate();
        }

        return dateUtil.formatYMD(date);
    }
}
