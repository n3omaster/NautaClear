package com.bachecubano.nautaclear;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.bachecubano.nautabackgroundlibrary.RetrieveImap;
import com.bachecubano.nautaclear.utils.PrefManager;
import com.dd.processbutton.iml.ActionProcessButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RetrieveImap retrieveImap;
    private String[] inboxData;
    private RoundCornerProgressBar mProgressBar;
    private TextView txtStorageUsage;
    private TextView txtEmailsUsage;
    private ActionProcessButton btnClearInbox;
    private SwipeRefreshLayout swipeRefreshLayout;

    private PrefManager prefManager;
    private final int PERMISSION_CALL_PHONE = 101;
    private final String transfer_code = "*234*1*55149082*1234*1" + Uri.encode("#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] randomDonate = new String[]{"Donación Voluntaria", "Regálame una cerveza", "Regálame un café", "Obsequia $1", "Apoya al creador", "Dona al creador"};
        List<String> strList = Arrays.asList(randomDonate);
        Collections.shuffle(strList);

        mProgressBar = findViewById(R.id.progressBarSPace);

        txtStorageUsage = findViewById(R.id.txtStorageUsage);
        txtEmailsUsage = findViewById(R.id.txtEmailsUsage);

        btnClearInbox = findViewById(R.id.btnClearInbox);
        btnClearInbox.setMode(ActionProcessButton.Mode.ENDLESS);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        prefManager = new PrefManager(getBaseContext());

        Button donateButton = findViewById(R.id.donateButton);
        donateButton.setText(strList.toArray(new String[strList.size()])[0]);

        checkInboxData();

        btnClearInbox.setOnClickListener(view -> {
            CleanInboxTask cleanInboxTask = new CleanInboxTask();
            cleanInboxTask.execute();
        });
    }

    private void checkInboxData() {
        if (prefManager.isNautaConfigured()) {
            retrieveImap = new RetrieveImap(getBaseContext(), prefManager.getNautaUser(), prefManager.getNautaPassword());
            RetrieveMailTask retrieveMailTask = new RetrieveMailTask();
            retrieveMailTask.execute();
        } else {
            Toast.makeText(getBaseContext(), "Debe configurar su correo", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_config)
            startActivity(new Intent(getBaseContext(), SettingsActivity.class));
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        checkInboxData();
    }

    /**
     * Donate about 0.50 cents to the owner of the project
     *
     * @param view
     */
    public void donate(View view) {
        Intent intent = new Intent("android.intent.action.CALL");
        intent.setData(Uri.parse("tel:" + transfer_code));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CALL_PHONE);
            } else {
                startActivity(intent);
            }
        } else {
            startActivity(intent);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_CALL_PHONE && resultCode == RESULT_OK) {
            Intent intent = new Intent("android.intent.action.CALL");
            intent.setData(Uri.parse("tel:" + transfer_code));
            startActivity(intent);
        }
    }

    /**
     * AsyncTask for retrieve Data
     */
    private class RetrieveMailTask extends AsyncTask<String, Void, Boolean> {

        RetrieveMailTask() {
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Log.d("NBL", "do this AsyncClass");
            inboxData = retrieveImap.retrieveData();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            updateUI();
        }
    }

    private class CleanInboxTask extends AsyncTask<String, Void, Boolean> {

        CleanInboxTask() {
        }

        @Override
        protected void onPreExecute() {
            btnClearInbox.setProgress(1);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return retrieveImap.clearInbox();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                swipeRefreshLayout.setRefreshing(true);
                doneButton();
                checkInboxData();
            } else {
                failButton();
            }
            updateUI();
        }
    }

    private void updateUI() {

        mProgressBar.setMax(Integer.valueOf(inboxData[0]));
        mProgressBar.setProgress(Integer.valueOf(inboxData[1]));
        mProgressBar.setPadding(5);
        mProgressBar.setRadius(12);
        mProgressBar.setProgressColor(Color.parseColor("#2196f3"));
        mProgressBar.setProgressBackgroundColor(Color.parseColor("#dfdfdf"));


        txtStorageUsage.setText(getString(R.string.buzonUsage, Float.valueOf(inboxData[1]) / 1024));
        txtEmailsUsage.setText(getString(R.string.emailsUsage, Integer.valueOf(inboxData[3])));
        swipeRefreshLayout.setRefreshing(false);
    }

    private void doneButton() {
        btnClearInbox.setProgress(100);
    }

    private void failButton() {
        btnClearInbox.setProgress(-1);
    }
}
