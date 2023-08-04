package com.example.geolocationtest;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsResultContract extends ActivityResultContract<Void, Void> {

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, Void unused) {
        return new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    }

    @Override
    public Void parseResult(int i, @Nullable Intent intent) {
        return null;
    }
}
