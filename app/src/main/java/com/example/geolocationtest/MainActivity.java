package com.example.geolocationtest;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private static final int C_REQUEST_FINE_LOCATION_PERMISSION_CODE = 0;
    private static final int C_REQUEST_COARSE_LOCATION_PERMISSION_CODE = 1;

    private FusedLocationProviderClient m_fusedLocationProviderClient = null;
    private CancellationTokenSource m_cancellationTokenSource = null;

    private TextView m_textView = null;

    private ActivityResultLauncher<Void> m_activityResultLauncher = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_cancellationTokenSource = new CancellationTokenSource();

        setContentView(R.layout.activity_main);

        m_textView = findViewById(R.id.location);

        if (!processAccessibility()) return;
    }

    @Override
    protected void onStop() {
        m_cancellationTokenSource.cancel();

        if (m_activityResultLauncher != null) {
            m_activityResultLauncher.unregister();
        }

        super.onStop();
    }

    protected boolean processAccessibility() {
        m_fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(this);

        processPermissions();

        return true;
    }

    private void processPermissions() {
        processLocationPermissions();
    }

    private void processLocationPermissions() {
        if (!checkLocationPermissions()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    C_REQUEST_FINE_LOCATION_PERMISSION_CODE);

            return;
        }

        gripLocation();
    }

    private boolean checkLocationPermissions() {
        int fineResult =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarseResult =
                ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION);

        return (fineResult == PackageManager.PERMISSION_GRANTED ||
                coarseResult == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode,
            @NonNull final String[] permissions,
            @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length <= 0 || grantResults.length <= 0) return;

        switch (requestCode) {
            case C_REQUEST_FINE_LOCATION_PERMISSION_CODE: {
                onFineLocationRequestPermissionsResult(permissions, grantResults);

                break;
            }
            case C_REQUEST_COARSE_LOCATION_PERMISSION_CODE: {
                onCoarseLocationRequestPermissionsResult(permissions, grantResults);

                break;
            }
        }
    }

    private void onFineLocationRequestPermissionsResult(
            final String[] permissions,
            final int[] grantResults)
    {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gripLocation();

            return;
        }

        Log.d(getClass().getName(), "Fine geolocation permission hasn't been granted!");

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                C_REQUEST_COARSE_LOCATION_PERMISSION_CODE);
    }

    private void onCoarseLocationRequestPermissionsResult(
            final String[] permissions,
            final int[] grantResults)
    {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            gripLocation();

            return;
        }

        Log.d(getClass().getName(), "Coarse geolocation permission hasn't been granted!");

        m_textView.setText("No geolocation info has been provided!");
    }

    private void gripLocation() {
        @SuppressLint("MissingPermission") Task<Location> curLocationTask =
                m_fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                        m_cancellationTokenSource.getToken());

        curLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull final Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();

                    if (location == null) {
                        Log.d(getClass().getName(), "Location was null!");

                        m_textView.setText("Location was null!");

                        return;
                    }

                    StringBuilder locationStringBuilder =
                            new StringBuilder("Latitude: ");

                    locationStringBuilder.append(String.valueOf(location.getLatitude()));
                    locationStringBuilder.append("; Longitude: ");
                    locationStringBuilder.append(String.valueOf(location.getLongitude()));
                    locationStringBuilder.append(";");

                    m_textView.setText(locationStringBuilder.toString());
                } else {
                    Log.d(getClass().getName(), "Exception: " + task.getException().getMessage());

                    m_textView.setText("An exception has been occurred!");
                }
            }
        });
    }
}