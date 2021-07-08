package com.example.groceryapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.groceryapp.Constants;
import com.example.groceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class SettingsActivity extends AppCompatActivity {
private ImageButton btnBack;
private android.widget.Switch Switch;
    private android.widget.TextView TvNotificationStatus;

    private static final String enabledMesssage="Notifications are enabled";
    private static final String disabledMesssage="Notifications are disabled";

    private boolean isChecked=false;

    private FirebaseAuth firebaseAuth;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btnBack=findViewById(R.id.btnBack);
        Switch=findViewById(R.id.SwitchFcm);
        TvNotificationStatus=findViewById(R.id.TvNotificationStatus);

        firebaseAuth=FirebaseAuth.getInstance();

        sharedPreferences=getSharedPreferences("SETTINGS",MODE_PRIVATE);

        isChecked=sharedPreferences.getBoolean("FCM_ENABLED",false);
        Switch.setChecked(isChecked);

        if(isChecked){
            TvNotificationStatus.setText(""+enabledMesssage);
        }
        else {
            TvNotificationStatus.setText(""+disabledMesssage);
        }


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    subscribeTopic();
                }
                else {
                    UnsubscribeTopic();
                }
            }
        });
    }

    private void subscribeTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        editor=sharedPreferences.edit();
                        editor.putBoolean("FCM_ENABLED",true);
                        editor.apply();

                        Toast.makeText(SettingsActivity.this,""+enabledMesssage,Toast.LENGTH_SHORT).show();
                        TvNotificationStatus.setText(""+enabledMesssage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(SettingsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void UnsubscribeTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants.FCM_TOPIC)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        editor=sharedPreferences.edit();
                        editor.putBoolean("FCM_ENABLED",false);
                        editor.apply();
                        Toast.makeText(SettingsActivity.this,""+disabledMesssage,Toast.LENGTH_SHORT).show();
                        TvNotificationStatus.setText(""+disabledMesssage);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        Toast.makeText(SettingsActivity.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }
}