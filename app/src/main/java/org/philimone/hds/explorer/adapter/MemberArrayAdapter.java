package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;

/**
 * Created by paul on 6/6/16.
 */
public class MemberArrayAdapter  extends ArrayAdapter<Member> {
    private List<Member> members;
    private List<Boolean> checkableMembers;
    private List<Boolean> supervisedMembers;
    private List<String> extras;
    private Context mContext;
    private @LayoutRes int layoutResId;
    private int selectedIndex = -1;
    private boolean ignoreHeadOfHousehold = false;
    private boolean showHouseholdAndCode = false;
    private boolean showHouseholdHead = true;
    private MemberIcon memberIcon;

    public enum MemberIcon {NORMAL_MEMBER_ICON, NORMAL_HEAD_ICON, NORMAL_SECHEAD_ICON, NORMAL_MEMBER_NEW_ICON, NORMAL_HEAD_NEW_ICON}

    /**
     * Adapter of a List View Item for members (name and perm-id are displayed)
     * @param context
     * @param objects
     */
    public MemberArrayAdapter(Context context, List<Member> objects){
        super(context, R.layout.member_item, objects);

        this.members = new ArrayList<>();
        this.members.addAll(objects);
        this.mContext = context;
        this.layoutResId = R.layout.member_item_xtra;
    }

    public MemberArrayAdapter(Context context, @LayoutRes int layoutResId, List<Member> objects){
        super(context, R.layout.member_item, objects);

        this.members = new ArrayList<>();
        this.members.addAll(objects);
        this.mContext = context;
        this.layoutResId = layoutResId;
    }


    /**
     * Adapter of a List View Item for members (name, perm-id, and a check box are displayed)
     * @param context
     * @param objects
     * @param checks
     */
    public MemberArrayAdapter(Context context, List<Member> objects, List<Boolean> checks){
        super(context, R.layout.member_item_chk, objects);

        this.members = new ArrayList<>();
        this.members.addAll(objects);

        if (checks != null){
            this.checkableMembers = new ArrayList<>();
            this.checkableMembers.addAll(checks);
        }

        this.mContext = context;
        this.layoutResId = R.layout.member_item_chk;
    }

    /**
     * Adapter of a List View Item for members (name, perm-id, checkbox and different icon for supervised member are displayed)
     * @param context
     * @param objects
     * @param checks
     * @param supervisionList
     */
    public MemberArrayAdapter(Context context, List<Member> objects, List<Boolean> checks, List<Boolean> supervisionList){
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
    public MemberArrayAdapter(Context context, List<Member> objects, ArrayList<String> extras){
        super(context, R.layout.member_item_xtra, objects);

        this.members = new ArrayList<>();
        this.members.addAll(objects);

        if (extras != null){
            this.extras = new ArrayList<>();
            this.extras.addAll(extras);
        }

        this.ignoreHeadOfHousehold = true;

        this.mContext = context;
        this.layoutResId = R.layout.member_item_xtra;
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

    public void setIgnoreHeadOfHousehold(boolean ignoreHeadOfHousehold) {
        this.ignoreHeadOfHousehold = ignoreHeadOfHousehold;
    }

    public void setShowHouseholdHead(boolean showHouseholdHead) {
        this.showHouseholdHead = showHouseholdHead;
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

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(layoutResId, parent, false);

        ImageView iconView = (ImageView) rowView.findViewById(R.id.iconView);
        TextView txtName = (TextView) rowView.findViewById(R.id.txtMemberItemName);
        TextView txtCode = (TextView) rowView.findViewById(R.id.txtMemberItemCode);
        TextView txtExtra = (TextView) rowView.findViewById(R.id.txtMemberItemExtras);
        CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        Member mb = members.get(position);

        String endType = "";
        switch (mb.endType){
            case DEATH: endType = "- DTH"; break;
            case EXTERNAL_OUTMIGRATION: endType = "- EXT"; break;
        }

        txtName.setText(mb.getName());
        txtCode.setText(mb.getCode()+endType);


        if (showHouseholdAndCode){
            txtCode.setText(mb.getHouseholdName() +" -> "+mb.getCode());
        }

        if (chkVBprocessed != null && checkableMembers != null){
            chkVBprocessed.setChecked(checkableMembers.get(position));
        }

        if (supervisedMembers != null && position < supervisedMembers.size()){
            if (supervisedMembers.get(position)==true){
                //txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.nui_member_red_chk_icon);
            }
        }

        if (txtExtra != null) {
            txtExtra.setText(mb.getHouseholdCode() +" -> "+mb.getHouseholdName());
        }

        if (this.extras != null && position < this.extras.size()){
            txtExtra.setText(extras.get(position));
        }

        if (!ignoreHeadOfHousehold){
            if (mb.isHouseholdHead()){
                //txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.nui_member_red_filled_icon);
            }

            if (mb.isSecHouseholdHead()){
                //txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon);
            }
        }

        if (mb.isRecentlyCreated()){
            iconView.setImageResource(R.mipmap.nui_member_red_new_icon);

            if (mb.isHouseholdHead() || mb.isSecHouseholdHead()){
                iconView.setImageResource(R.mipmap.nui_member_red_filled_new_icon);
            }
        }

        if (memberIcon != null){
            switch (memberIcon){
                case NORMAL_MEMBER_ICON:iconView.setImageResource(R.mipmap.nui_member_red_icon); break;
                case NORMAL_HEAD_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_icon); break;
                case NORMAL_SECHEAD_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_two_icon); break;
                case NORMAL_MEMBER_NEW_ICON:iconView.setImageResource(R.mipmap.nui_member_red_new_icon); break;
                case NORMAL_HEAD_NEW_ICON:iconView.setImageResource(R.mipmap.nui_member_red_filled_new_icon); break;
            }
        }

        if (endType != null && !endType.isEmpty()){
            //paint with a color
            txtCode.setTypeface(null, Typeface.BOLD);
            rowView.setBackgroundColor(mContext.getResources().getColor(R.color.nui_lists_item_special_textcolor));
        }

        if (selectedIndex == position){
            int colorA = mContext.getResources().getColor(R.color.nui_lists_selected_item_textcolor);
            int colorB = mContext.getResources().getColor(R.color.nui_lists_selected_item_color_2);

            rowView.setBackgroundColor(colorB);
            txtName.setTextColor(colorA);
            txtCode.setTextColor(colorA);
            if (txtExtra!=null) txtExtra.setTextColor(colorA);
        }

        txtExtra.setVisibility(showHouseholdHead ? View.VISIBLE : View.GONE);

        return rowView;
    }
}
