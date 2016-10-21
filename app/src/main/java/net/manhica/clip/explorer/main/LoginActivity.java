package net.manhica.clip.explorer.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
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

import net.manhica.clip.explorer.R;
import net.manhica.clip.explorer.database.Bootstrap;
import net.manhica.clip.explorer.database.Converter;
import net.manhica.clip.explorer.database.Database;
import net.manhica.clip.explorer.database.DatabaseHelper;
import net.manhica.clip.explorer.database.Queries;
import net.manhica.clip.explorer.io.SyncDatabaseListener;
import net.manhica.clip.explorer.model.CollectedData;
import net.manhica.clip.explorer.model.Form;
import net.manhica.clip.explorer.model.Module;
import net.manhica.clip.explorer.model.User;

import org.mindrot.jbcrypt.BCrypt;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
    private View mProgressView;
    private View mLoginFormView;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    private User loggedUser;
    private String adminUser;
    private String adminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        // Set up the login form.
        txtUsername = (AutoCompleteTextView) findViewById(R.id.login_username_txt);
        txtPassword = (EditText) findViewById(R.id.login_password_txt);

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
            }
        });

        Button btSync = (Button) findViewById(R.id.synchronize_button);
        btSync.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                supervisorLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        this.progressDialog = new ProgressDialog(this);

        txtUsername.setText(""); //txtUsername.setText("smonjane");
        txtPassword.setText(""); //txtPassword.setText("35");

        initdb();


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

        List<Form> list = Queries.getAllFormBy(db, null, null);
        for (Form f : list){
            Log.d("form", ""+f.getFormId()+", bind->"+f.getBindMap());
        }

        db.close();

    }

    private void initdb(){
        Bootstrap bootstrap = new Bootstrap(this);
        //bootstrap.dropTables();
        bootstrap.init();
    }

    @Override
    public void collectionComplete(String result) {

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
                return null;
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

        SyncLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            //http request
            try {
                HttpURLConnection connection = null;
                String basicAuth = "Basic " + new String(Base64.encode((this.mUsername+":"+this.mPassword).getBytes(),Base64.NO_WRAP ));

                URL url = new URL(LoginActivity.this.getString(R.string.server_url_not_secure)+"/api/clip-explorer/login");

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

        //usernameEditText.setText("");
        //passwordEditText.setText("");

        startActivity(intent);
    }

    private void launchModulesSelector(){
        //Toast.makeText(this, "Sucessfull Login", Toast.LENGTH_LONG);
        Log.d("logged","logged"+loggedUser.getModules());

        Intent intent = null;

        String[] modules = loggedUser.getModules().split(",");

        modules = filterSupervisor(modules);

        if (loggedUser.getModules()==null && loggedUser.getModules().isEmpty()){
            createAlertDialog(getString(R.string.error_lbl), getString(R.string.error_no_modules_permission));
            return;
        }

        if (modules.length > 1){
            intent = new Intent(this, ModuleSelectorActivity.class); //open Modules Selector
        }

        if (modules.length==1 && modules[0].equals(Module.CLIP_FACILITY_MODULE)){
            intent = new Intent(this, FacilityActivity.class); //
        }

        if (modules.length==1 && modules[0].equals(Module.CLIP_POM_MODULE)){
            //intent = new Intent(this, POMActivity.class); //
            intent = new Intent(this, SurveyMembersActivity.class);
        }

        if (modules.length==1 && modules[0].equals(Module.CLIP_SURVEY_MODULE)){
            intent = new Intent(this, SurveyActivity.class); //
        }

        if (modules.length==1 && modules[0].equals(Module.CLIP_OTHERS)){
            createAlertDialog("Exception", "Not yet Implemented");
            return;
        }

        intent.putExtra("user", loggedUser);
        startActivity(intent);
    }

    private String[] filterSupervisor(String[] modules){
        String[] newstr = null;

        if (isSupervisor(modules) && modules.length>1){
            newstr = new String[modules.length-1];
            int i=0;
            for (String str : modules) {
                if (str.equals(Module.CLIP_SUPERVISOR)) continue;
                newstr[i++] = str;
            }
        }else{
            return modules;
        }

        return newstr;
    }

    private boolean isSupervisor(String[] modules) {
        for(String s : modules ){
            if (s.equals(Module.CLIP_SUPERVISOR)){
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

