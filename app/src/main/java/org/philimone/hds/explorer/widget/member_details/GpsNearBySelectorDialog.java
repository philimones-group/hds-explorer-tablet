package org.philimone.hds.explorer.widget.member_details;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.adapter.DistanceArrayAdapter;

public class GpsNearBySelectorDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private ListView lvDistancesList;
    private Button btDialogBack;

    private DistanceArrayAdapter adapter;

    private OnClickListener listener;

    public GpsNearBySelectorDialog() {
        super();
    }

    public static GpsNearBySelectorDialog createDialog(FragmentManager fm, OnClickListener listener){
        GpsNearBySelectorDialog dialog = new GpsNearBySelectorDialog();

        dialog.fragmentManager = fm;
        dialog.listener = listener;

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.gps_near_by_distance_dialog, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        initialize(view);
    }

    private void initialize(View view){
        this.lvDistancesList = (ListView) view.findViewById(R.id.lvDistancesList);
        this.btDialogBack = (Button) view.findViewById(R.id.btDialogBack);

        if (this.btDialogBack != null)
            this.btDialogBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackCicked();
                }
            });

        if (this.lvDistancesList != null)
            this.lvDistancesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onSelectedItem(position);
                }
            });

        //initialize spinner
        initializeLists();
    }

    private void onSelectedItem(int position) {
        if (position > -1){
            Distance distance = this.adapter.getItem(position);

            if (listener != null) listener.onSelectedDistance(distance);
        }
    }

    private void initializeLists(){

        Distance[] distances = new Distance[]{
                new Distance("50m", 0.05),
                new Distance("100m", 0.1),
                new Distance("200m", 0.2),
                new Distance("500m", 0.5),
                new Distance("1 Km", 1.0),
                new Distance("2 Km", 2.0),
                new Distance("5 Km", 5.0)};


        this.adapter = new DistanceArrayAdapter(this.getActivity(), distances);
        lvDistancesList.setAdapter(adapter);

    }

    private void onBackCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnClickListener {
        void onSelectedDistance(Distance distance);

        void onCancelClicked();
    }
}
