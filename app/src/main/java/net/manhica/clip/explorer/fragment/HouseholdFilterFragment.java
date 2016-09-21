package net.manhica.clip.explorer.fragment;


import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mapswithme.maps.api.MWMPoint;
import com.mapswithme.maps.api.MapsWithMeApi;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.HouseholdArrayAdapter;
import net.manhica.clip.explorer.adapter.MemberArrayAdapter;
import net.manhica.clip.explorer.database.Converter;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.model.Household;
import net.manhica.clip.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class HouseholdFilterFragment extends Fragment {

    private Context mContext;
    private EditText txtHouseFilterNr;
    private ListView hfHousesList;
    private Button btHouseFilterClear;
    private Button btHouseFilterSearch;
    private Button btHouseFilterGpsMap;
    private View hfViewProgressBar;

    private Listener listener;

    private Database database;

    public HouseholdFilterFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.household_filter, container, false);
        initialize(view);

        return view;
    }

    private void initialize(View view) {
        this.database = new Database(getActivity());

        this.mContext = getActivity();

        if (getActivity() instanceof Listener){
            this.listener = (Listener) getActivity();
        }

        this.txtHouseFilterNr = (EditText) view.findViewById(R.id.txtHouseFilterNr);
        this.hfHousesList = (ListView) view.findViewById(R.id.hfHousesList);
        this.btHouseFilterClear = (Button) view.findViewById(R.id.btHouseFilterClear);
        this.btHouseFilterSearch = (Button) view.findViewById(R.id.btHouseFilterSearch);
        this.btHouseFilterGpsMap = (Button) view.findViewById(R.id.btHouseFilterGpsMap);
        this.hfViewProgressBar = (View) view.findViewById(R.id.hfViewProgressBar);

        this.txtHouseFilterNr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>2){
                    searchHouses(s.toString());
                }
            }
        });

        if (btHouseFilterClear != null)
            this.btHouseFilterClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        if (btHouseFilterSearch != null)
            this.btHouseFilterSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = txtHouseFilterNr.getText().toString();
                    if (text.length()>1){
                        searchHouses(text);
                    }
                }
            });

        if (btHouseFilterGpsMap != null)
            this.btHouseFilterGpsMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showHouseholdsMap();
                }
            });

        this.hfHousesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HouseholdArrayAdapter adapter = (HouseholdArrayAdapter) hfHousesList.getAdapter();
                Household household = adapter.getItem(position);
                adapter.setSelectedIndex(position);

                if (listener != null && household != null){
                    onHouseholdClicked(household);
                }
            }
        });
    }

    private void showHouseholdsMap() {
        HouseholdArrayAdapter adapter = (HouseholdArrayAdapter) this.hfHousesList.getAdapter();

        final MWMPoint[] points = new MWMPoint[adapter.getHouseholds().size()];


        for (int i=0; i < points.length; i++){
            Household h = adapter.getHouseholds().get(i);
            String name = h.getHouseNumber();
            double lat = Double.parseDouble(h.getLatitude());
            double lon = Double.parseDouble(h.getLongitude());
            points[i] = new MWMPoint(lat, lon, name);
        }

        MapsWithMeApi.showPointsOnMap(this.getActivity(), getString(R.string.map_women_coordinates), points);
    }

    private void searchHouses(String houseNumber){
        HouseholdSearchTask task = new HouseholdSearchTask(null, houseNumber);
        task.execute();
    }

    private void onHouseholdClicked(Household household){
        //paint item as selected
        listener.onHouseholdClick(household);
    }

    public HouseholdArrayAdapter loadHouseholdsByFilters(String houseNumber) {
        //open loader

        //search on database
        List<Household> households = new ArrayList<>();
        List<String> whereValues = new ArrayList<>();
        String[] arrayWhereValues;

        String whereClause = DatabaseHelper.Household.COLUMN_HOUSE_NUMBER + " like ?";
        whereValues.add(houseNumber+"%");

        arrayWhereValues = new String[whereValues.size()];

        //search
        database.open();

        Cursor cursor = database.query(Household.class, DatabaseHelper.Household.ALL_COLUMNS, whereClause, whereValues.toArray(arrayWhereValues), null, null, DatabaseHelper.Household.COLUMN_HOUSE_NUMBER);

        while (cursor.moveToNext()){
            households.add(Converter.cursorToHousehold(cursor));
        }

        database.close();

        HouseholdArrayAdapter currentAdapter = new HouseholdArrayAdapter(this.getActivity(), households);

        return currentAdapter;
    }

    public void showProgress(final boolean show) {
        hfViewProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        hfHousesList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    class HouseholdSearchTask extends AsyncTask<Void, Void, HouseholdArrayAdapter> {
        private String extId;
        private String houseNumber;

        public HouseholdSearchTask(String extId, String houseNumber) {
            this.extId = extId;
            this.houseNumber = houseNumber;
        }

        @Override
        protected HouseholdArrayAdapter doInBackground(Void... params) {
            return loadHouseholdsByFilters(houseNumber);
        }

        @Override
        protected void onPostExecute(HouseholdArrayAdapter adapter) {
            hfHousesList.setAdapter(adapter);
            //showProgress(false);
        }
    }

    public interface Listener {
        void onHouseholdClick(Household household);
    }
}
