package org.philimone.hds.explorer.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.fragment.MaritalRelationshipDialog;
import org.philimone.hds.explorer.model.MaritalRelationship;
import org.philimone.hds.explorer.model.Member;
import org.philimone.hds.explorer.model.enums.Gender;
import org.philimone.hds.explorer.model.enums.temporal.ResidencyEndType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import mz.betainteractive.utilities.StringUtil;

public class MaritalRelationshipAdapter extends RecyclerView.Adapter<MaritalRelationshipAdapter.MaritalRelationshipViewHolder> {

    private List<MaritalRelationshipItem> relationships;
    private Context mContext;
    private int selectedIndex = -1;
    private Listener listener;

    /**
     * Adapter of a List View Item for members (name and code are displayed)
     * @param context
     * @param objects
     */
    public MaritalRelationshipAdapter(Context context, List<MaritalRelationshipItem> objects, Listener listener){
        this.relationships = new ArrayList<>();
        this.relationships.addAll(objects);
        this.mContext = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MaritalRelationshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.marital_relationship_spouse_item, parent, false);

        return new MaritalRelationshipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MaritalRelationshipViewHolder holder, int position) {
        MaritalRelationshipItem member = this.relationships.get(position);
        holder.setValues(member);
    }

    @Override
    public int getItemCount() {
        return relationships.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public MaritalRelationshipItem getItem(int position) {
        return this.relationships.get(position);
    }

    private int getPosition(MaritalRelationshipItem relationship) {
        return relationships.indexOf(relationship);
    }

    public List<MaritalRelationshipItem> getRelationships(){
        return this.relationships;
    }

    public void setSelectedIndex(int index){
        this.selectedIndex = index;
        notifyDataSetChanged();
    }

    public int getSelectedIndex(){
        return selectedIndex;
    }

    public MaritalRelationshipItem getSelectedMaritalRelationship(){
        return (selectedIndex < 0 || selectedIndex >= relationships.size()) ? null : relationships.get(selectedIndex);
    }

    public boolean isEmpty() {
        return this.relationships.isEmpty();
    }

    class MaritalRelationshipViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mainView;

        public MaritalRelationshipViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = (ViewGroup) itemView;
        }

        public void setValues(MaritalRelationshipItem mr) {
            ImageView iconView = mainView.findViewById(R.id.iconView);
            TextView txtName = mainView.findViewById(R.id.txtMemberItemName);
            TextView txtCode = mainView.findViewById(R.id.txtMemberItemCode);
            TextView txtExtra = mainView.findViewById(R.id.txtMemberItemExtras);
            Button buttonEndRel = mainView.findViewById(R.id.btnVisitMaritalRelationship);

            buttonEndRel.setOnClickListener(v -> onEndRelationship(mr));

            int position = MaritalRelationshipAdapter.this.getPosition(mr);
            Member spouse = mr.spouseA!=null && mr.spouseA.gender==Gender.FEMALE ? mr.spouseA : mr.spouseB;

            String nameText = spouse.getName();
            String codeText = "";
            String extrasText = "";
            String endTypeText = "";
            String memberDetailsText = "";

            if (spouse.endType != ResidencyEndType.NOT_APPLICABLE) {
                switch (spouse.endType) {
                    case DEATH:
                        endTypeText = " - DTH";
                        break;
                    case EXTERNAL_OUTMIGRATION:
                        endTypeText = " - EXT";
                        break;
                }
            }

            codeText = spouse.getHouseholdName() +" -> "+spouse.getCode()+endTypeText;
            String maritalText = mContext.getString(R.string.relationship_type_title_abbrv_lbl) + ": " + mContext.getString(spouse.maritalStatus.name);
            memberDetailsText = mContext.getString(R.string.member_details_gender_lbl)+" "+spouse.gender.code + ", " + mContext.getString(R.string.member_details_age_lbl)+" "+spouse.age + ", " +maritalText;
            extrasText = memberDetailsText + " - " + StringUtil.formatYMD(mr.maritalRelationship.startDate);

            txtName.setText(nameText);
            txtCode.setText(codeText);
            txtExtra.setText(extrasText);

            if (spouse.isRecentlyCreated()){
                //iconView.setImageResource(R.mipmap.nui_member_red_new_icon);
            }

            buttonEndRel.setEnabled(mr.buttonEnabled);

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

        }
    }

    private void onEndRelationship(MaritalRelationshipItem mr) {
        this.listener.onEndingRelationship(mr.maritalRelationship);
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

        return StringUtil.formatYMD(date);
    }

    public static class MaritalRelationshipItem {
        public MaritalRelationship maritalRelationship;
        public Member spouseA;
        public Member spouseB;

        public boolean buttonEnabled;

        public MaritalRelationshipItem(MaritalRelationship maritalRelationship, Member spouseA, Member spouseB, boolean buttonEnabled) {
            this.maritalRelationship = maritalRelationship;
            this.spouseA = spouseA;
            this.spouseB = spouseB;
            this.buttonEnabled = buttonEnabled;
        }
    }

    public interface Listener {
        void onEndingRelationship(MaritalRelationship maritalRelationship);
    }
}
