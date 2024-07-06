package com.example.necessity.LoginSignup;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.necessity.MainActivityNgo;
import com.example.necessity.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupNgo extends AppCompatActivity {
    public static final String TAG = "TAG";
    private Button register;
    private EditText Iname, Ingoid, Iemail, Iphone, Ipassword;
    private TextView login;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String ngoID;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_ngo);
        Iname = findViewById(R.id.name);
        Ingoid = findViewById(R.id.ngoid);
        Iemail = findViewById(R.id.email);
        Iphone = findViewById(R.id.phone);
        Ipassword = findViewById(R.id.password);

        register = findViewById(R.id.register);
        login = findViewById(R.id.login);

        fAuth= FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = Iname.getText().toString();
                String idngo = Ingoid.getText().toString();
                String email = Iemail.getText().toString();
                String phone = Iphone.getText().toString();
                String password = Ipassword.getText().toString();

                if (TextUtils.isEmpty(name))
                {
                    Iname.setError("Name is required");
                    return;
                }
                else if (TextUtils.isEmpty(idngo))
                {
                    Ingoid.setError("NGO Id is required");
                    return;
                }
                else if (TextUtils.isEmpty(email))
                {
                    Iemail.setError("Email is required");
                    return;
                }
                else if (TextUtils.isEmpty(phone))
                {
                    Iphone.setError("Phone is required");
                    return;
                }
                else if (TextUtils.isEmpty(password))
                {
                    Ipassword.setError("Password is required");
                    return;
                }

                fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SignupNgo.this, "User Created.", Toast.LENGTH_SHORT) .show();
                            ngoID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("Ngos").document(ngoID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("name",name);
                            user.put("email",email);
                            user.put("phone",phone);
                            user.put("idngo", idngo);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG,"onSuccess: user Profile is created for "+ ngoID);
                                    Toast.makeText(SignupNgo.this, "Registered Successfully.", Toast.LENGTH_SHORT) .show();
                                    ValidateData(name, idngo, email, phone, password);
                                }
                            });
                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));

                        }
                        else{
                            Toast.makeText(SignupNgo.this, "Error!" + task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LogupNgo.class));
            }
        });
    }

    private void ValidateData(final String name, final String idngo, final String email,final String phone, final String password) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("Ngos").child(phone).exists()) ){
                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("email", email);
                    userdataMap.put("phone", phone);
                    userdataMap.put("password", password);
                    userdataMap.put("name", name);
                    userdataMap.put("idngo", idngo);
                    RootRef.child("Ngos").child(phone).updateChildren(userdataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                Toast.makeText(SignupNgo.this, "Congratulations, your account has been created.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignupNgo.this, MainActivityNgo.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignupNgo.this, "Network Error: Please try again after some time...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(SignupNgo.this, "This " + phone + " already exists.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(SignupNgo.this, "Please try again using another phone number.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupNgo.this, landingpage.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

