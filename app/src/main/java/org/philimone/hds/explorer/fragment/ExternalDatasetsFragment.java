package org.philimone.hds.explorer.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.ObjectBoxDatabase;
import org.philimone.hds.explorer.model.Dataset;
import org.philimone.hds.explorer.model.Dataset_;
import org.philimone.hds.explorer.model.FormSubject;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.User;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.objectbox.Box;
import mz.betainteractive.io.readers.CSVReader;
import mz.betainteractive.utilities.StringUtil;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExternalDatasetsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExternalDatasetsFragment extends Fragment {

    private Spinner datasetsSpinner;
    private RecyclerView columnsDataList;
    private TextView mainListTextView;
    private ProgressBar mainListProgressBar;

    private FormSubject subject;
    private Dataset selectedDataset;

    private Box<Dataset> boxDatasets;

    private User currentUser = Bootstrap.getCurrentUser();

    public ExternalDatasetsFragment() {
        // Required empty public constructor
        initBoxes();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HouseholdVisitFragment.
     */
    public static ExternalDatasetsFragment newInstance(FormSubject subject) {
        ExternalDatasetsFragment fragment = new ExternalDatasetsFragment();
        fragment.subject = subject;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.external_datasets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initialize(view);
    }

    private void initBoxes(){
        this.boxDatasets = ObjectBoxDatabase.get().boxFor(Dataset.class);
    }

    private void initialize(View view) {
        this.datasetsSpinner = view.findViewById(R.id.datasetsSpinner);
        this.columnsDataList = view.findViewById(R.id.columnsDataList);
        this.mainListTextView = view.findViewById(R.id.mainListTextView);
        this.mainListProgressBar = view.findViewById(R.id.mainListProgressBar);

        this.datasetsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Dataset dataset = (Dataset) parent.getAdapter().getItem(position);
                    loadDatasetToList(dataset);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                loadDatasetToList(null);
            }
        });

        this.mainListProgressBar.setVisibility(View.GONE);
        this.mainListTextView.setVisibility(View.GONE);
        this.columnsDataList.setVisibility(View.VISIBLE);

        this.columnsDataList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        loadDatasetsListToSpinner();
    }

    private void showLoading(){
        this.mainListProgressBar.setVisibility(View.VISIBLE);
        this.mainListTextView.setVisibility(View.GONE);
        this.columnsDataList.setVisibility(View.GONE);
    }

    private void showNotFound(){
        this.mainListProgressBar.setVisibility(View.GONE);
        this.mainListTextView.setVisibility(View.VISIBLE);
        this.columnsDataList.setVisibility(View.GONE);
    }

    private void showDatasets(){
        this.mainListProgressBar.setVisibility(View.GONE);
        this.mainListTextView.setVisibility(View.GONE);
        this.columnsDataList.setVisibility(View.VISIBLE);
    }

    private void loadDatasetsListToSpinner(){
        this.selectedDataset = null;

        List<String> selectedModules = new ArrayList<>(currentUser.getSelectedModules());

        List<Dataset> datasets = this.boxDatasets.query().equal(Dataset_.tableName, this.subject.getTableName().code)
                                                         .filter((d) -> StringUtil.containsAny(d.modules, selectedModules))  //filter by module
                                                         .build().find();

        if (datasets != null && datasets.size() > 0) {
            Log.d("datasets", ""+datasets.size());
            ArrayAdapter<Dataset> adapter = new ArrayAdapter<>(this.getContext(), R.layout.external_datasets_spinner_item, R.id.txtDatasetItem, datasets);
            datasetsSpinner.setAdapter(adapter);
            //datasetsSpinner.setSelection(0);
        }
    }

    private void loadDatasetToList(Dataset dataset) {
        this.selectedDataset = dataset;

        if (selectedDataset != null) {
            showLoading();
            List<DatasetValue> values = getDatasetValues(selectedDataset);

            if (values == null) {
                showNotFound();
                return;
            }

            //load data to an adapter
            DatasetValuesAdapter adapter = new DatasetValuesAdapter(values);
            columnsDataList.setAdapter(adapter);
            showDatasets();

        } else {
            columnsDataList.setAdapter(null);
        }
    }

    /* Load Datasets Values */
    private List<DatasetValue> getDatasetValues(Dataset dataset) {

        String tableName = dataset.getTableName();
        String tableColumnName = dataset.getTableColumn();
        String linkValue = subject.getValueByName(tableColumnName);
        CSVReader.CSVRow valueRow = getRowFromCSVFile(dataset, linkValue);

        Log.d("found row", ""+valueRow+", linkValue: "+linkValue);

        if (valueRow == null){
            return null;
        }

        //put data in recyclerview
        List<DatasetValue> list = new ArrayList<>();
        List<String> fields = valueRow.getFieldNames();
        for (int i = 0; i < fields.size() ; i++) {
            DatasetValue dt = new DatasetValue();
            dt.columnName = fields.get(i);
            dt.columnLabel = dataset.getLabels().get(i);
            dt.columnValue = valueRow.getField(dt.columnName);
            list.add(dt);

            Log.d("datasetvalue"+(i+1), ""+dt.columnLabel);
        }

        return list;
    }

    private CSVReader.CSVRow getRowFromCSVFile(Dataset dataset, String linkValue) {
        //Log.d("zip", "processing zip file, linkValue="+linkValue);

        if (linkValue == null || linkValue == "") return null;  //dont need to read the csv without need

        try {
            InputStream inputStream = new FileInputStream(dataset.getFilename());

            ZipInputStream zin = new ZipInputStream(inputStream);
            ZipEntry entry = zin.getNextEntry();

            if (entry != null){ //has a file inside (supposed to be a csv file)
                //processXMLDocument(zin);

                CSVReader csvReader = new CSVReader(zin, true, ",");
                //Log.d("fields", csvReader.getFieldNames()+", "+csvReader.getMapFields()+", "+dataSet.getKeyColumn());
                for (CSVReader.CSVRow row : csvReader.getRows()){
                    String csvKeyCol = row.getField(dataset.getKeyColumn());
                    //Log.d("keyColValue", ""+csvKeyCol+" == "+linkValue);
                    if (csvKeyCol!=null && csvKeyCol.equals(linkValue)){
                        return row; //break the loop
                    }
                }


                zin.closeEntry();
            }

            zin.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    class DatasetValue {
        public String columnName;
        public String columnLabel;
        public String columnValue;
    }

    class DatasetValuesAdapter extends RecyclerView.Adapter<DatasetValueViewHolder>{

        private List<DatasetValue> datasetValueList = new ArrayList<>();

        public DatasetValuesAdapter(List<DatasetValue> list) {
            this.datasetValueList.addAll(list);
        }

        @NonNull
        @Override
        public DatasetValueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.external_datasets_item1, parent, false);
            Log.d("create-viewholder", ""+view);
            return new DatasetValueViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DatasetValueViewHolder holder, int position) {
            DatasetValue datasetValue = datasetValueList.get(position);
            holder.updateValues(datasetValue);

        }

        @Override
        public int getItemCount() {
            return datasetValueList.size();
        }
    }

    class DatasetValueViewHolder extends RecyclerView.ViewHolder {
        private ViewGroup mainView;

        public DatasetValueViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mainView = (ViewGroup) itemView;
        }

        public void updateValues(DatasetValue datasetValue) {
            TextView txtLabel = mainView.findViewById(R.id.txtItem1);
            TextView txtValue = mainView.findViewById(R.id.txtItem2);

            Log.d("updating-viewholder", ""+datasetValue.columnLabel);

            txtLabel.setText(datasetValue.columnLabel);
            txtValue.setText(datasetValue.columnValue);
        }
    }



}