package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
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
import org.philimone.hds.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<Member> members;
    private List<Boolean> checkableMembers;
    private List<Boolean> supervisedMembers;
    private List<String> extras;
    private Context mContext;
    private @LayoutRes int layoutResourceId;
    private int selectedIndex = -1;
    private boolean showHouseholdHeadIcon = true;
    private boolean showHouseholdAndCode = false;
    private boolean showExtraDetails = false;
    private boolean showGender = false;
    private boolean showAge = false;
    private MemberIcon memberIcon;

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
     * Adapter of a List View Item for members (name, code, checkbox and different icon for supervised member are displayed)
     * @param context
     * @param objects
     * @param checks
     * @param supervisionList
     */
    public MemberAdapter(Context context, List<Member> objects, List<Boolean> checks, List<Boolean> supervisionList){
        this(context, objects, checks);

        if (supervisionList != null){
            this.supervisedMembers = new ArrayList<>();
            this.supervisedMembers.addAll(supervisionList);
        }
    }

    /**
     * Adapter of a List View Item for members (name, perm-id, extra text view and a large sized icon are displayed)
     * @param context
     * @param objects
     * @param extras
     */
    public MemberAdapter(Context context, List<Member> objects, ArrayList<String> extras){
        this.members = new ArrayList<>();
        this.members.addAll(objects);

        if (extras != null){
            this.extras = new ArrayList<>();
            this.extras.addAll(extras);
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

    public void setShowExtraDetails(boolean showExtraDetails) {
        this.showExtraDetails = showExtraDetails;
    }

    public boolean isShowGender() {
        return showGender;
    }

    public void setShowGender(boolean showGender) {
        this.showGender = showGender;
    }

    public boolean isShowAge() {
        return showAge;
    }

    public void setShowAge(boolean showAge) {
        this.showAge = showAge;
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
            TextView txtExtra = mainView.findViewById(R.id.txtMemberItemExtras);
            CheckBox chkVBprocessed = mainView.findViewById(R.id.chkProcessed);
            int position = MemberAdapter.this.getPosition(mb);

            String endType = "";
            String extraCode = "";
            switch (mb.endType){
                case DEATH: endType = " - DTH"; break;
                case EXTERNAL_OUTMIGRATION: endType = " - EXT"; break;
            }

            extraCode += endType;

            if (showGender) {
                extraCode += (extraCode.isEmpty() ? "  " : " - ") + mContext.getString(R.string.member_details_gender_lbl)+" "+mb.gender.code;
            }

            if (showAge) {
                extraCode += ", " + mContext.getString(R.string.member_details_age_lbl)+" "+mb.age;
            }

            txtName.setText(mb.getName());
            txtCode.setText(mb.getCode()+extraCode);

            memberIcon = MemberIcon.NORMAL_MEMBER_ICON;

            if (showHouseholdAndCode){
                txtCode.setText(mb.getHouseholdName() +" -> "+mb.getCode()+endType);
            }

            if (chkVBprocessed != null && checkableMembers != null){
                chkVBprocessed.setChecked(checkableMembers.get(position));
            }

            if (supervisedMembers != null && position < supervisedMembers.size()){
                if (supervisedMembers.get(position)==true){
                    //txtName.setTypeface(null, Typeface.BOLD);
                    //iconView.setImageResource(R.mipmap.nui_member_red_chk_icon);
                    memberIcon = MemberIcon.NORMAL_MEMBER_CHECKED_ICON;
                }
            }

            if (txtExtra != null) {
                txtExtra.setText(mb.getHouseholdCode() +" -> "+mb.getHouseholdName());
            }

            if (MemberAdapter.this.extras != null && position < MemberAdapter.this.extras.size()){
                txtExtra.setText(extras.get(position));
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

            if (endType != null && !endType.isEmpty()){
                //paint with a color
                txtCode.setTypeface(null, Typeface.BOLD);
                mainView.setBackgroundColor(mContext.getResources().getColor(R.color.nui_lists_item_special_textcolor));
            }

            if (selectedIndex == position){
                int colorA = mContext.getResources().getColor(R.color.nui_lists_selected_item_textcolor);
                int colorB = mContext.getResources().getColor(R.color.nui_lists_selected_item_color_2);

                mainView.setBackgroundColor(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtExtra!=null) txtExtra.setTextColor(colorA);
            } else {
                TypedValue colorBvalue = new TypedValue();
                mContext.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, colorBvalue, true);

                int colorA = mContext.getResources().getColor(R.color.nui_member_item_textcolor);
                int colorB = colorBvalue.resourceId;

                mainView.setBackgroundResource(colorB);
                txtName.setTextColor(colorA);
                txtCode.setTextColor(colorA);
                if (txtExtra!=null) txtExtra.setTextColor(colorA);
            }

            txtExtra.setVisibility(showExtraDetails ? View.VISIBLE : View.GONE);
        }
    }
}
