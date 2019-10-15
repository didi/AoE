package com.didi.aoe.examples.demo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.didi.aoe.extensions.service.Aoe;
import com.didi.aoe.extensions.service.AoeDataProvider;

import java.util.ArrayList;
import java.util.List;

public class ShowcaseActivity extends AppCompatActivity implements NavController.OnDestinationChangedListener {
    private static final String TAG = "ShowcaseActivity";

    private static final int PERMISSION_REQUESTS = 1;

    private boolean mAllPermissionsGranted;

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return false;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showcase);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Navigation.findNavController(this, R.id.nav_host_fragment).addOnDestinationChangedListener(this);

        if (allPermissionsGranted()) {
            // Nothing to do
            mAllPermissionsGranted = true;
        } else {
            getRuntimePermissions();
        }

        Aoe.getInstance().setDataProvider(new AoeDataProvider() {
            @Override
            public long appId() {
                return 164;
            }

            @Override
            public double latitude() {
                return 39.92;
            }

            @Override
            public double longitude() {
                return 116.46;
            }
        });
    }

    @Override
    protected void onDestroy() {
        Navigation.findNavController(this, R.id.nav_host_fragment).removeOnDestinationChangedListener(this);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        if (getSupportActionBar() != null) {
            if (R.id.featuresFragment != destination.getId()) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } else {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            getSupportActionBar().setTitle(destination.getLabel());
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "Permission granted!");
        if (allPermissionsGranted()) {
            // Nothing to do
            mAllPermissionsGranted = true;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
