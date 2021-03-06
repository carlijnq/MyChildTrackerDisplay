package com.example.carli.mychildtrackerdisplay;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carli.mychildtrackerdisplay.ViewModel.DisplayViewModel;
import com.example.carli.mychildtrackerdisplay.ViewModel.TrackerViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.widget.Toast.LENGTH_SHORT;

public class TrackerActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 1;

    private TrackerViewModel TrackerViewModel;
    ImageView buttonSOS;
    Button logOutButton;
    TextView tvSOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);
        TrackerViewModel = ViewModelProviders.of(this).get(TrackerViewModel.class);

        initializeUI();

        // Check GPS is enabled
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
            //finish();
        }

        // Check location permission is granted - if it is, start
        // the service, otherwise request the permission
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            startTrackerService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }

    }

    public void initializeUI(){

        // textView
        tvSOS = findViewById(R.id.tvSOS);
        tvSOS.setVisibility(View.INVISIBLE);

        //SOS button
        buttonSOS = findViewById(R.id.bSOS);
        buttonSOS.setVisibility(View.VISIBLE);
        buttonSOS.setOnClickListener(v -> {
            TrackerViewModel.setSOS(true);
            buttonSOS.setVisibility(View.INVISIBLE);
            tvSOS.setVisibility(View.VISIBLE);
        });

        // log out button
        logOutButton = findViewById(R.id.childLogOut);
        logOutButton.setOnClickListener(v -> {
            TrackerViewModel.signOut();
            goToLogInActivity();
        });

    }

    public void goToLogInActivity(){
        Toast.makeText(this, R.string.signed_out, LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginXActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public void onBackPressed() {
        moveTaskToBack(true);


    }

    private void startTrackerService() {
        startService(new Intent(this, TrackerService.class));
        Toast.makeText(this, "Tracker service started", Toast.LENGTH_SHORT).show();
        //finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Start the service when the permission is granted
            startTrackerService();
            //Log.d(TAG, "Tracking started...");
        } else {
            Toast.makeText(this, "No permission to start service", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}