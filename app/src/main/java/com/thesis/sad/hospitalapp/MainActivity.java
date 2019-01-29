package com.thesis.sad.hospitalapp;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.thesis.sad.hospitalapp.Interaction.Common;
import com.thesis.sad.hospitalapp.Model.Barangay;

import dmax.dialog.SpotsDialog;

import org.w3c.dom.Text;

import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
public class MainActivity extends AppCompatActivity {

    Button signinbtn,registerbtn;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference ambulance;
    RelativeLayout rootLayout;
    TextView txtforgotpwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);
        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        ambulance = db.getReference(Common.hospitals);
        signinbtn= findViewById(R.id.btn_sign_in);
        registerbtn = findViewById(R.id.btn_register);
        rootLayout = findViewById(R.id.rootLayout);

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });

        signinbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginDialog();
            }
        });

        txtforgotpwd = findViewById(R.id.textforgotpassword);
        txtforgotpwd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showDialogForgotPwd();
                return false;
            }
        });

        if(user!=null && pwd !=null){

            if(!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd) ){

                autologin(user,pwd);
            }
        }
    }

    private void showDialogForgotPwd() {
        android.support.v7.app.AlertDialog.Builder alertDialog = new android.support.v7.app.AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("FORGOT PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View forgot_pwd_layout = inflater.inflate(R.layout.layout_forgot_pass,null);

        final EditText edtEmail = forgot_pwd_layout.findViewById(R.id.edittext_email);
        alertDialog.setView(forgot_pwd_layout);
        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {
                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                Snackbar.make(rootLayout,"Reset password link has been sent",Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showLoginDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to Sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final EditText edittext_email = login_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = login_layout.findViewById(R.id.edittext_password);

        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                signinbtn.setEnabled(false);
                if (TextUtils.isEmpty(edittext_email.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if (TextUtils.isEmpty(edittext_password.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter your password", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (edittext_password.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                final android.app.AlertDialog alertDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
                alertDialog.setMessage("Logging in...");
                alertDialog.show();

                auth.signInWithEmailAndPassword(edittext_email.getText().toString(),edittext_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                alertDialog.dismiss();
                                Paper.book().write(Common.user_field,edittext_email.getText().toString());
                                Paper.book().write(Common.pwd_field,edittext_password.getText().toString());

                                FirebaseDatabase.getInstance().getReference(Common.hospitals)
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                Common.currentAmbulance = dataSnapshot.getValue(Barangay.class);

                                            }


                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                startActivity(new Intent(MainActivity.this,Welcome.class));
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                signinbtn.setEnabled(true);
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final EditText edittext_email = register_layout.findViewById(R.id.edittext_email);
        final EditText edittext_password = register_layout.findViewById(R.id.edittext_password);
        final EditText edittext_name = register_layout.findViewById(R.id.edittext_name);
        final EditText edittext_platenumber = register_layout.findViewById(R.id.edittext_platenumber);

        dialog.setView(register_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(TextUtils.isEmpty(edittext_email.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your email address", Snackbar.LENGTH_SHORT).show();
                    return;

                }
                if(TextUtils.isEmpty(edittext_password.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your password", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_name.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your Name", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edittext_platenumber.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter your phone", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edittext_password.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout,"Password too short!", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(edittext_email.getText().toString()
                        ,edittext_password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                Barangay brgy = new Barangay();
                                brgy.setEmail(edittext_email.getText().toString());
                                brgy.setPassword(edittext_password.getText().toString());
                                brgy.setName(edittext_name.getText().toString());
                                brgy.setPhone(edittext_platenumber.getText().toString());

                                ambulance.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(brgy)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Registered!", Snackbar.LENGTH_SHORT).show();

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed in Registration" +e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed", Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();

    }
    private void autologin(String user, String pwd) {
        final android.app.AlertDialog alertDialog = new SpotsDialog.Builder().setContext(MainActivity.this).build();
        alertDialog.setMessage("Logging in...");
        alertDialog.show();


        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        alertDialog.dismiss();
                        startActivity(new Intent(MainActivity.this,Welcome.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        signinbtn.setEnabled(true);
                        Snackbar.make(rootLayout,""+e.getMessage(),Snackbar.LENGTH_SHORT).show();

                    }
                });

    }
}
