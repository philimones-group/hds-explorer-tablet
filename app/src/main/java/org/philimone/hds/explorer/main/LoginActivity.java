package org.philimone.hds.explorer.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.philimone.hds.explorer.BuildConfig;
import org.philimone.hds.explorer.R;
import org.philimone.hds.explorer.database.Bootstrap;
import org.philimone.hds.explorer.database.Converter;
import org.philimone.hds.explorer.database.Database;
import org.philimone.hds.explorer.database.DatabaseHelper;
import org.philimone.hds.explorer.database.Queries;
import org.philimone.hds.explorer.io.SyncDatabaseListener;
import org.philimone.hds.explorer.model.ApplicationParam;
import org.philimone.hds.explorer.model.Module;
import org.philimone.hds.explorer.model.User;

import org.mindrot.jbcrypt.BCrypt;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements SyncDatabaseListener{

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    private SyncLoginTask mSyncTask = null;

    // UI references.
    private AutoCompleteTextView txtUsername;
    private EditText txtPassword;
    private TextView txtCopyrightAppName;
    private TextView txtCopyrightCompany;
    private TextView txtCopyrightDevs;
    private View loginIconView;
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private User loggedUser;
    private String adminUser;
    private String adminPassword;
    private String serverUrl;

    private boolean useLocalServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        // Set up the login form.
        loginIconView = (View) findViewById(R.id.imageView);
        txtUsername = (AutoCompleteTextView) findViewById(R.id.login_username_txt);
        txtPassword = (EditText) findViewById(R.id.login_password_txt);
        txtCopyrightAppName = (TextView) findViewById(R.id.txtCopyrightAppName);
        txtCopyrightCompany = (TextView) findViewById(R.id.txtCopyrightAppName);
        txtCopyrightDevs = (TextView) findViewById(R.id.txtCopyrightDevs);

        txtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    userLogin();
                    return true;
                }
                return false;
            }
        });

        Button btLogin = (Button) findViewById(R.id.login_button);
        btLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
                //launchModulesSelector();
            }
        });

        Button btSync = (Button) findViewById(R.id.synchronize_button);
        btSync.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                supervisorLogin();
            }
        });

        Button btSettings = (Button) findViewById(R.id.settings_button);
        btSettings.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        this.progressDialog = new ProgressDialog(this);

        //Extra Settings
        this.loginIconView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(LoginActivity.this, "Xtra settings mode activated! Shira Tensei Jutsu!!!", Toast.LENGTH_LONG);
                Log.d("justu", "Xtra settings mode activated! Shira Tensei Jutsu!!! - "+!useLocalServer) ;
                useLocalServer = !useLocalServer;
                return true;
            }
        });

        //txtUsername.setText("FWPF1"); //txtUsername.setText("supervisor");
        //txtPassword.setText("test"); //txtPassword.setText("dssmanhica");

        updateView();

        initdb();
/*

        Database db = new Database(this);
        db.open();

        /*
        List<CollectedData> list = Queries.getAllCollectedDataBy(db, null, null);

        for (CollectedData cd : list){
            //16
            Log.d("colldata", cd.getId()+ ", fid:"+cd.getFormId()+", fur:"+cd.getFormUri()+", fxl:"+cd.getFormXmlPath()+", rid:"+cd.getRecordId()+", tbn:"+cd.getTableName()+", supervised: "+cd.isSupervised());
            //ContentValues cv = new ContentValues();
            //cv.put(DatabaseHelper.CollectedData.COLUMN_SUPERVISED, 0);
            //db.update(CollectedData.class, cv, DatabaseHelper.CollectedData._ID+"=?", new String[]{ cd.getId()+"" });
        }*/

       /*
        loggedUser = user;

        List<Form> list = Queries.getAllFormBy(db, null, null);
        for (Form f : list){
            Log.d("form", ""+f.getFormId()+", bind->"+f.getFormMap());
        }

        db.close();
        */

    }

    private void updateView() {
        txtCopyrightAppName.setText(getString(R.string.app_name)+" v"+ BuildConfig.VERSION_NAME);
    }

    private void initdb(){
        Bootstrap bootstrap = new Bootstrap(this);
        //bootstrap.dropTables();
        bootstrap.init();
    }

    @Override
    public void collectionComplete(String result) {

    }

    private void openSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void userLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        txtUsername.setError(null);
        txtPassword.setError(null);

        // Store values at the time of the login attempt.
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        /*
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        }
*/
        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError(getString(R.string.error_field_required));
            focusView = txtUsername;
            cancel = true;
        }
        cancel = false; //bad
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
    }

    private void supervisorLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        txtUsername.setError(null);
        txtPassword.setError(null);

        // Store values at the time of the login attempt.
        String username = txtUsername.getText().toString();
        String password = txtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            txtPassword.setError(getString(R.string.error_invalid_password));
            focusView = txtPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError(getString(R.string.error_field_required));
            focusView = txtUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mSyncTask = new SyncLoginTask(username, password);
            mSyncTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, User> {

        private final String mUsername;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mUsername = email;
            mPassword = password;
        }

        @Override
        protected User doInBackground(Void... params) {

            User user = null;

            Database db = new Database(LoginActivity.this);
            db.open();

            try {
                // Simulate network access.
                Cursor cursor = db.query(User.class, DatabaseHelper.User.COLUMN_USERNAME + " = ?", new String[] { this.mUsername }, null, null, null);

                if (cursor != null) {
                    boolean found = cursor.moveToFirst();

                    Log.d("user-"+this.mUsername, "found "+found);

                    if (found){
                        user = Converter.cursorToUser(cursor);
                    }

                    cursor.close();

                    return user;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return loggedUser;
            }

            db.close();

            return user;
        }

        @Override
        protected void onPostExecute(final User user) {
            mAuthTask = null;
            showProgress(false);

            if (user != null) {
                //finish();
                if(BCrypt.checkpw(mPassword, user.getPassword())){
                    loggedUser = user;
                    launchModulesSelector();
                }else{
                    txtPassword.setError(getString(R.string.error_invalid_password));
                    txtPassword.requestFocus();
                }
            } else {
                txtPassword.setError(getString(R.string.error_incorrect_password));
                txtPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous login task used to authenticate
     * the user.
     */
    public class SyncLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mUsername;
        private final String mPassword;
        private final String mServerUrl;

        SyncLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
            mServerUrl = Queries.getApplicationParamValue(ApplicationParam.APP_URL, LoginActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            if (useLocalServer){ //this is working perfectly - if is a release version wont use my personal computer
                serverUrl = "http://172.16.234.123:8080/hds-explorer-server";
            }else{
                serverUrl = mServerUrl;
            }

            //http request
            try {
                HttpURLConnection connection = null;
                String basicAuth = "Basic " + new String(Base64.encode((this.mUsername+":"+this.mPassword).getBytes(),Base64.NO_WRAP ));

                URL url = new URL(serverUrl+"/api/explorer/login");

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setDoInput(true);
                connection.setRequestProperty("Authorization", basicAuth);

                Log.d("login", ""+url);

                connection.connect();

                if (connection.getResponseCode()==200){

                    Scanner scan = new Scanner(connection.getInputStream());
                    String result = scan.next();
                    scan.close();

                    Log.d("result", ""+result);

                    return result != null && result.equals("OK");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                adminUser = mUsername;
                adminPassword = mPassword;
                launchServerSync();
            } else {
                txtPassword.setError(getString(R.string.error_incorrect_password));
                txtPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void launchServerSync() {
        Intent intent = new Intent(this, ServerSyncActivity.class);
        intent.putExtra("username", adminUser);
        intent.putExtra("password", adminPassword);
        intent.putExtra("server-url", serverUrl);

        //usernameEditText.setText("");
        //passwordEditText.setText("");

        startActivity(intent);
    }

    private void launchModulesSelector(){
        //Toast.makeText(this, "Sucessfull Login", Toast.LENGTH_LONG);
        //Log.d("logged","logged"+loggedUser.getModules());

        Intent intent = null;

        String[] modules = loggedUser.getModules().split(",");

        modules = filterSupervisor(modules);

        if (loggedUser.getModules()==null && loggedUser.getModules().isEmpty()){
            createAlertDialog(getString(R.string.error_lbl), getString(R.string.error_no_modules_permission));
            return;
        }

        /*
        if (modules.length > 1){
            intent = new Intent(this, ModuleSelectorActivity.class); //open Modules Selector
        }

        if (modules.length==1 && modules[0].equals(Module.DSS_SURVEY_MODULE)){
            intent = new Intent(this, SurveyActivity.class); //
        }

        if (modules.length==1 && modules[0].equals(Module.DSS_OTHERS)){
            createAlertDialog("Exception", "Not yet Implemented");
            return;
        }*/

        intent = new Intent(this, SurveyActivity.class);
        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }

    private String[] filterSupervisor(String[] modules){
        String[] newstr = null;

        if (isSupervisor(modules) && modules.length>1){
            newstr = new String[modules.length-1];
            int i=0;
            for (String str : modules) {
                if (str.equals(Module.DSS_SUPERVISOR)) continue;
                newstr[i++] = str;
            }
        }else{
            return modules;
        }

        return newstr;
    }

    private boolean isSupervisor(String[] modules) {
        for(String s : modules ){
            if (s.equals(Module.DSS_SUPERVISOR)){
                return true;
            }
        }

        return false;
    }

    private void createAlertDialog(String title, String message) {

        alertDialog = null;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(true);

        alertDialogBuilder.setNeutralButton(getString(R.string.bt_ok_lbl), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}

