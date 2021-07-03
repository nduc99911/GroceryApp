package com.example.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.blogspot.atifsoftwares.circularimageview.CircularImageView;

public class ProfileEditUserActivity extends AppCompatActivity {

    private ImageButton btnBack,btnGps;
    private CircularImageView imageView;
    private  EditText edFullName,edPhone,edCountry,edState,edCity,edAdress;
    private Button btnUpdate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit_user);
        btnBack=findViewById(R.id.btnback);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}