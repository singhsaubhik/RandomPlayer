package com.example.thakur.randomplayer.BaseActivity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thakur.randomplayer.R;
import com.example.thakur.randomplayer.ServiceRemote.ServiceRemote;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by Thakur on 24-01-2018
 */

public class BaseServiceActivity extends AppCompatActivity implements SlidingUpPanelLayout.PanelSlideListener {

    private ServiceRemote.ServiceToken serviceToken;
    private boolean receiverRegistered;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceToken = ServiceRemote.bindToService(this, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(BaseServiceActivity.this, "Getting token", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceRemote.unbindFromService(serviceToken);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {

    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                collapsePanel(); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    private void onPanelCollapsed(View panel) {
    }

    private void onPanelExpanded(View panel) {
    }

    private void collapsePanel() {
    }


}
