package org.philimone.hds.explorer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.data.FormDataLoader;
import org.philimone.hds.explorer.model.Module;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class ModulesSelectorDialog extends DialogFragment {

    private FragmentManager fragmentManager;

    private ListView lvMainList;
    private Button btDialogBack;


    private ModulesAdapter adapter;
    private List<Module> modules;

    private OnModuleSelectedListener listener;

    public ModulesSelectorDialog() {
        super();
    }

    public static ModulesSelectorDialog createDialog(FragmentManager fm, List<Module> moduleList, OnModuleSelectedListener listener){
        ModulesSelectorDialog dialog = new ModulesSelectorDialog();

        dialog.fragmentManager = fm;
        dialog.listener = listener;
        dialog.modules = new ArrayList<>(moduleList);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.modules_selector, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initialize(view);
    }

    private void initialize(View view){
        this.lvMainList = view.findViewById(R.id.lvMainList);
        this.btDialogBack = view.findViewById(R.id.btDialogBack);


        if (this.btDialogBack != null)
            this.btDialogBack.setOnClickListener(v -> onBackCicked());

        if (this.lvMainList != null){
            this.lvMainList.setOnItemClickListener((parent, view1, position, id) -> {
                Module module = adapter.getItem(position);
                onSelectedModule(module);
            });
        }

        this.adapter = new ModulesAdapter(getActivity(), this.modules);
        this.lvMainList.setAdapter(this.adapter);

    }

    private void onBackCicked(){
        dismiss();
        if (listener != null) listener.onCancelClicked();
    }

    private void onSelectedModule(Module module) {
        dismiss();
        List<Module> modules = new ArrayList<>();
        modules.add(module);

        if (listener != null) listener.onModulesSelected(modules);
    }

    public void show(){
        this.show(fragmentManager, "relatype");
    }

    public interface OnModuleSelectedListener {
        void onModulesSelected(List<Module> modules);

        void onCancelClicked();
    }

    class ModulesAdapter extends ArrayAdapter {
        private List<Module> moduleList;
        private Context mContext;

        public ModulesAdapter(Context context, List<Module> objects){
            super(context, R.layout.modules_selector_item, objects);

            this.moduleList = new ArrayList<>();
            this.moduleList.addAll(objects);
            this.mContext = context;
        }

        public List<Module> getModuleList(){
            return this.moduleList;
        }

        @Override
        public Module getItem(int position) {
            return moduleList.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View rowView = inflater.inflate(R.layout.modules_selector_item, parent, false);

            TextView txtName = rowView.findViewById(R.id.txtItem1);
            TextView txtCode = rowView.findViewById(R.id.txtItem2);
            TextView txtDesc = rowView.findViewById(R.id.txtItem3);

            Module m = moduleList.get(position);

            txtName.setText(m.name);
            txtCode.setText(m.code);
            txtDesc.setText(m.description);

            return rowView;
        }
    }
}
