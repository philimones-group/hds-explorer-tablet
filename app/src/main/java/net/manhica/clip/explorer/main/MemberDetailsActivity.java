package net.manhica.clip.explorer.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.adapter.FormLoaderAdapter;
import net.manhica.clip.explorer.data.FormDataLoader;
import net.manhica.clip.explorer.data.FormDataLoaderList;
import net.manhica.clip.explorer.listeners.ActionListener;
import net.manhica.clip.explorer.listeners.MemberActionListener;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Member;

import java.util.ArrayList;
import java.util.List;

import mz.betainteractive.odk.FormUtilities;
import mz.betainteractive.odk.model.FilledForm;

public class MemberDetailsActivity extends AppCompatActivity {

    private TextView mbDetailsName;
    private TextView mbDetailsPermId;
    private TextView mbDetailsGender;
    private TextView mbDetailsAge;
    private TextView mbDetailsHouseNo;
    private TextView mbDetailsEndType;
    private TextView mbDetailsFather;
    private TextView mbDetailsMother;
    private CheckBox chkMemDetailIsPreg;
    private CheckBox chkMemDetailsHasDelv;
    private Button btMemDetailsCollectData;
    private Button btMemDetailsBack;
            
    private Member member;
    private List<FormDataLoader> formDataLoaders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_details);

        this.member = (Member) getIntent().getExtras().get("member");
        readFormDataLoader();

        initialize();
    }

    private void readFormDataLoader(){

        Object[] objs = (Object[]) getIntent().getExtras().get("dataloaders");

        for (int i=0; i < objs.length; i++){
            FormDataLoader formDataLoader = (FormDataLoader) objs[i];
            if (isVisualizableForm(formDataLoader.getForm())){
                this.formDataLoaders.add(formDataLoader);
            }
        }
    }

    private boolean isVisualizableForm(Form form) {
        return  (member.getAge() >= form.getMinAge() && member.getAge() <= form.getMaxAge()) && (member.getGender().equals(form.getGender()) || form.getGender().equals("ALL"));
    }

    public void setMember(Member member){
        this.member = member;
    }

    private void initialize() {
        mbDetailsName = (TextView) findViewById(R.id.mbDetailsName);
        mbDetailsPermId = (TextView) findViewById(R.id.mbDetailsPermId);
        mbDetailsGender = (TextView) findViewById(R.id.mbDetailsGender);
        mbDetailsAge = (TextView) findViewById(R.id.mbDetailsAge);
        mbDetailsHouseNo = (TextView) findViewById(R.id.mbDetailsHouseNo);
        mbDetailsEndType = (TextView) findViewById(R.id.mbDetailsEndType);
        mbDetailsFather = (TextView) findViewById(R.id.mbDetailsFather);
        mbDetailsMother = (TextView) findViewById(R.id.mbDetailsMother);
        chkMemDetailIsPreg = (CheckBox) findViewById(R.id.chkMemDetailIsPreg);
        chkMemDetailsHasDelv = (CheckBox) findViewById(R.id.chkMemDetailsHasDelv);
        btMemDetailsCollectData = (Button) findViewById(R.id.btMemDetailsCollectData);
        btMemDetailsBack = (Button) findViewById(R.id.btMemDetailsBack);

        btMemDetailsBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MemberDetailsActivity.this.onBackPressed();
            }
        });

        btMemDetailsCollectData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCollectDataClicked();
            }
        });

        setMemberData();
    }

    private void setMemberData(){
        mbDetailsName.setText(member.getName());
        mbDetailsPermId.setText(member.getPermId());
        mbDetailsGender.setText(member.getGender());
        mbDetailsAge.setText(member.getAge()+"");
        mbDetailsHouseNo.setText(member.getHhNumber());
        mbDetailsEndType.setText(getEndTypeMsg(member));
        mbDetailsFather.setText(getParentName(member.getFatherName()));
        mbDetailsMother.setText(getParentName(member.getMotherName()));
        chkMemDetailIsPreg.setChecked(member.isPregnant());
        chkMemDetailsHasDelv.setChecked(member.isHasDelivered());
    }

    private String getEndTypeMsg(Member member){
        if (member.getHhEndType().equals("NA")) return getString(R.string.member_details_endtype_na_lbl);
        if (member.getHhEndType().equals("EXT")) return getString(R.string.member_details_endtype_ext_lbl);
        if (member.getHhEndType().equals("DTH")) return getString(R.string.member_details_endtype_dth_lbl);

        return member.getHhEndType();
    }

    private String getParentName(String name){
        if (name.equals("Unknown")){
            return getString(R.string.member_details_unknown_lbl);
        }else {
            return name;
        }
    }

    private void onCollectDataClicked(){

        if (formDataLoaders != null && formDataLoaders.size() > 0){

            if (formDataLoaders.size()==1){
                //open directly the form
                openOdkForm(formDataLoaders.get(0));
            }else {
                //load list dialog and choice the form
                buildFormSelectortDialog(formDataLoaders);
            }
        }
    }

    private void openOdkForm(FormDataLoader formDataLoader) {
        FormUtilities formUtilities = new FormUtilities(this);

        Form form = formDataLoader.getForm();

        FilledForm filledForm = new FilledForm(form.getFormId());
        filledForm.putAll(formDataLoader.getValues());

        formUtilities.loadForm(filledForm);
    }

    private void buildFormSelectortDialog(List<FormDataLoader> loaders) {

        final FormLoaderAdapter adapter = new FormLoaderAdapter(this, loaders);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.member_details_forms_selector_lbl));
        builder.setCancelable(true);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FormDataLoader formDataLoader = adapter.getItem(which);
                openOdkForm(formDataLoader);
            }
        });

        builder.show();
    }


}
