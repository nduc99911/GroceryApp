package com.example.groceryapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class MainUserActivity extends AppCompatActivity {
private TextView tvName;
private ImageButton btnLogout;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);
        tvName=findViewById(R.id.tvName);
        btnLogout=findViewById(R.id.btnlogout);

        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> hashMap=new HashMap<>();
                hashMap.put("online","false");

                //update value to db
                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
                reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                //update successfully

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure( Exception e) {
                                //fail updateing
                                progressDialog.dismiss();
                                Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }



    private void loginInfo() {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange( DataSnapshot snapshot) {
                        for(DataSnapshot s: snapshot.getChildren()){
                            String name=""+s.child("name").getValue();
                            String accountType=""+s.child("accountType").getValue();

                            tvName.setText(name+"("+accountType+")");


                        }
                    }

                    @Override
                    public void onCancelled( DatabaseError error) {

                    }
                });


    }
    private void makeOfflie() {
        progressDialog.setMessage("Loging out...");

        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("online","false");

        //update value to db
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //update successfully

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure( Exception e) {
                        //fail updateing
                        progressDialog.dismiss();
                        Toast.makeText(MainUserActivity.this,""+e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
    }
    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user == null){
            Intent intent=new Intent(MainUserActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            loginInfo();
        }
    }
}