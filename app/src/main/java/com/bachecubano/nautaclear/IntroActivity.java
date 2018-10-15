package com.bachecubano.nautaclear;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bachecubano.nautabackgroundlibrary.BackgroundMail;
import com.bachecubano.nautaclear.utils.AboutBox;
import com.bachecubano.nautaclear.utils.PrefManager;

public class IntroActivity extends AppCompatActivity {

    private PrefManager prefManager;
    private ProgressBar progressBar;

    private EditText etUserName;
    private EditText etPassword;

    private Button configureButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        progressBar = findViewById(R.id.progressBar);

        etUserName = findViewById(R.id.nautaUsername);
        etPassword = findViewById(R.id.nautaPassword);

        configureButton = findViewById(R.id.configureButton);

        configureButton.setOnClickListener(view -> {
            String userName = etUserName.getText().toString();
            String password = etPassword.getText().toString();

            if (userName.equals("") || password.equals("")) {
                Toast.makeText(getBaseContext(), "Debe rellenar los campos de usuario y contraseña", Toast.LENGTH_SHORT).show();
            } else {

                configureButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setIndeterminate(true);

                BackgroundMail.newBuilder(this)
                        .withUsername(userName)
                        .withPassword(password)
                        .withMailto(getResources().getString(R.string.api_email))
                        .withType()
                        .withSendingMessage("Configurando")
                        .withSubject("HIT")
                        .withBody("Ver: " + AboutBox.versionCode(getBaseContext()))
                        .withOnSuccessCallback(() -> {
                            Toast.makeText(getBaseContext(), "Mensaje enviado correctamente.", Toast.LENGTH_SHORT).show();
                            prefManager.saveNautaCredentials(userName, password);
                            launchHomeScreen();
                        })
                        .withOnFailCallback(() -> {
                            configureButton.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getBaseContext(), "No se ha podido conectar a Nauta", Toast.LENGTH_SHORT).show();
                        })
                        .send();
            }
        });

        prefManager = new PrefManager(this);
        if (!prefManager.isFirstTimeLaunch() && prefManager.isNautaConfigured()) {
            launchHomeScreen();
            finish();
        }
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunched();
        startActivity(new Intent(IntroActivity.this, SplashActivity.class));
        finish();
    }
}
