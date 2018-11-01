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

/**
 * Created by paul on 6/6/16.
 */
public class MemberArrayAdapter  extends ArrayAdapter<Member> {
    private List<Member> members;
    private List<Boolean> checkableMembers;
    private List<Boolean> supervisedMembers;
    private List<String> extras;
    private Context mContext;
    private int layoutResId;
    private int selectedIndex = -1;
    private boolean ignoreHeadOfHousehold = false;
    private boolean showHouseholdAndPermId = false;
    private MemberIcon memberIcon;

    public enum MemberIcon {  NORMAL_BLUE_ICON, NORMAL_GREEN_ICON, NORMAL_GREEN_2_ICON  }

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
        this.layoutResId = R.layout.member_item;
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

    public void setShowHouseholdAndPermId(boolean showHouseholdAndPermId) {
        this.showHouseholdAndPermId = showHouseholdAndPermId;
    }

    public void setIgnoreHeadOfHousehold(boolean ignoreHeadOfHousehold) {
        this.ignoreHeadOfHousehold = ignoreHeadOfHousehold;
    }

    public void setMemberIcon(MemberIcon memberIcon) {
        this.memberIcon = memberIcon;
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
        TextView txtPermId = (TextView) rowView.findViewById(R.id.txtMemberItemPermId);
        TextView txtExtra = (TextView) rowView.findViewById(R.id.txtMemberItemExtras);
        CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkProcessed);

        Member mb = members.get(position);

        txtName.setText(mb.getName());
        txtPermId.setText(mb.getCode());

        if (showHouseholdAndPermId){
            txtPermId.setText(mb.getHouseNumber() +" -> "+mb.getCode());
        }

        if (chkVBprocessed != null && checkableMembers != null){
            chkVBprocessed.setChecked(checkableMembers.get(position));
        }

        if (supervisedMembers != null && position < supervisedMembers.size()){
            if (supervisedMembers.get(position)==true){
                txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.member_green_chk);
            }
        }

        if (this.extras != null && position < this.extras.size()){
            txtExtra.setText(extras.get(position));
        }

        if (!ignoreHeadOfHousehold){
            if (mb.isHouseholdHead()){
                txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.member_green);
            }

            if (mb.isSubsHouseholdHead()){
                txtName.setTypeface(null, Typeface.BOLD);
                iconView.setImageResource(R.mipmap.member_green_2);
            }
        }

        if (memberIcon != null){
            switch (memberIcon){
                case NORMAL_BLUE_ICON:iconView.setImageResource(R.mipmap.member); break;
                case NORMAL_GREEN_ICON:iconView.setImageResource(R.mipmap.member_green); break;
                case NORMAL_GREEN_2_ICON:iconView.setImageResource(R.mipmap.member_green_2); break;
            }
        }

        if (selectedIndex == position){
            int colorB = Color.parseColor("#0073C6");

            rowView.setBackgroundColor(colorB);
            txtName.setTextColor(Color.WHITE);
            txtPermId.setTextColor(Color.WHITE);
            if (txtExtra!=null) txtExtra.setTextColor(Color.WHITE);
        }

        return rowView;
    }
}
