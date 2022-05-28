package org.philimone.hds.explorer.adapter.trackinglist;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.model.TrackingSubListItem;
import org.philimone.hds.explorer.adapter.model.TrackingSubjectItem;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.model.User;
import org.philimone.hds.explorer.model.followup.TrackingList;
import org.philimone.hds.explorer.widget.CirclePercentageBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mz.betainteractive.utilities.StringUtil;

public class TrackingListAdapter extends RecyclerView.Adapter<TrackingListAdapter.TrackingListViewHolder> {

    private List<TrackingList> trackingLists;
    private String filterText;
    private User currentUser;
    private Context mContext;

    public TrackingListAdapter(Context context, List<TrackingList> collection) {
        this.trackingLists = new ArrayList<>();
        this.trackingLists.addAll(collection);

        this.currentUser = Bootstrap.getCurrentUser();
        this.mContext = context;
    }

    @NonNull
    @Override
    public TrackingListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.tracking_list_item, parent, false);

        Log.d("create-viewholder", ""+view+", viewtype="+viewType);
        return new TrackingListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackingListViewHolder holder, int position) {
        TrackingList item = getItem(position);
        holder.setValues(item);
    }

    @Override
    public int getItemCount() {
        return filterList(trackingLists).size();
    }

    public TrackingList getItem(int position) {
        return filterList(trackingLists).get(position);
    }

    public int getCompletionOfTrackingList(){
        int n = trackingLists.size();
        int c = 0;

        for (TrackingList item : trackingLists){
            //c += getCompletionOfList(item);
        }

        if (n==0) return 0;

        return (c / n);
    }

    public int getCompletionOfList(List<TrackingSubjectItem> childList){
        double to_collect = 0;
        double collected = 0;

        for (TrackingSubjectItem memberItem : childList){
            to_collect += memberItem.getForms().size()*1D;
            collected += memberItem.getCollectedForms().size()*1D;
        }

        if (to_collect==0) return 100;

        return (int)((collected / to_collect) *100);
    }

    public void filterSubjects(String code){
        Log.d("filtering", ""+code);

        if (StringUtil.isBlank(code)){
            this.filterText = null;
        } else {
            this.filterText = code;
        }

        notifyDataSetChanged();
    }

    public boolean codeMatches(TrackingList trackingList, String code){
        String codeRegex = ".*" + code.toLowerCase() + ".*";

        String title = trackingList.getTitle().toLowerCase();
        String name = trackingList.getName().toLowerCase();
        String details = trackingList.getDetails().toLowerCase();

        return title.matches(codeRegex) || name.matches(codeRegex) || details.matches(codeRegex);

    }

    private List<TrackingList> filterList(List<TrackingList> itemList){
        if (filterText==null) return itemList;

        List<TrackingList> filtered = new ArrayList<>();

        for (TrackingList item : itemList) {
            if (codeMatches(item, filterText)){
                filtered.add(item);
            }
        }

        return filtered;
    }

    class TrackingListViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mainView;

        public TrackingListViewHolder(@NonNull View itemView) {
            super(itemView);
            mainView = (ViewGroup) itemView;
        }

        public ViewGroup getMainView() {
            return mainView;
        }

        public void setValues(TrackingList trackingList) {
            TextView txtTitle = mainView.findViewById(R.id.txtTrackListItemTitle);
            TextView txtModule = mainView.findViewById(R.id.txtTrackListItemModule);
            TextView txtDetails = mainView.findViewById(R.id.txtTrackListItemDetails);
            CirclePercentageBar pBar = mainView.findViewById(R.id.pbarTrackListItem);

            DecimalFormat df = new DecimalFormat("#0.0");
            //Log.d("test", ""+trackingLists.size());

            int completion = (int) (trackingList.getCompletionRate()*100);  // df.format(trackingList.getCompletionRate()*100D) + "%";

            txtTitle.setText(trackingList.getName() + ":  " + trackingList.getTitle());
            txtDetails.setText(trackingList.getDetails());
            txtModule.setText(mContext.getString(R.string.trackinglist_module_lbl) + ": " + currentUser.getModulesNamesAsText(trackingList.modules));

            pBar.setPercentageValue(completion);
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

}


