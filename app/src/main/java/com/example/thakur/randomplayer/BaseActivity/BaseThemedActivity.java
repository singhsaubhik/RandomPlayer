package com.example.thakur.randomplayer.BaseActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.ATEActivity;
import com.example.thakur.randomplayer.Utilities.Helper;

/**
 * Created by Thakur on 29-01-2018
 */

public class BaseThemedActivity extends ATEActivity {
    @Nullable
    @Override
    public String getATEKey() {
        return Helper.getATEKey(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
