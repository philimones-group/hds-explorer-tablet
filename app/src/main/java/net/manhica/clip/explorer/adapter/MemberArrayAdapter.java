package net.manhica.clip.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 6/6/16.
 */
public class MemberArrayAdapter  extends ArrayAdapter<Member> {
    private List<Member> members;
    private Context mContext;

    public MemberArrayAdapter(Context context, List<Member> objects){
        super(context, R.layout.member_item, objects);

        this.members = new ArrayList<>();
        this.members.addAll(objects);
        this.mContext = context;
    }

    public List<Member> getMembers(){
        return this.members;
    }

    @Override
    public Member getItem(int position) {
        return members.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.member_item, parent, false);

        TextView txtName = (TextView) rowView.findViewById(R.id.txtMemberItemName);
        TextView txtPermId = (TextView) rowView.findViewById(R.id.txtMemberItemPermId);
        CheckBox chkVBprocessed = (CheckBox) rowView.findViewById(R.id.chkVaProcessed);

        Member mb = members.get(position);

        String processed = "0";

        txtName.setText(mb.getName());
        txtPermId.setText(mb.getPermId());

        if (chkVBprocessed != null){
            chkVBprocessed.setChecked(processed.equalsIgnoreCase("1"));
        }

        return rowView;
    }
}
