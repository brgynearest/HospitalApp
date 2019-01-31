package com.thesis.sad.hospitalapp;

import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.thesis.sad.hospitalapp.Interaction.Common;
import com.thesis.sad.hospitalapp.Model.FCMResponse;
import com.thesis.sad.hospitalapp.Model.Notification;
import com.thesis.sad.hospitalapp.Model.Sender;
import com.thesis.sad.hospitalapp.Model.Token;
import com.thesis.sad.hospitalapp.Remote.IFCMService;
import com.thesis.sad.hospitalapp.Remote.IGoogleAPI;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalCall extends AppCompatActivity {

    private static final String TAG = "HospitalCall";
    TextView text_service;
    Button btnrespond,btndecline;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    String ambulanceid;
    IFCMService mFCMService;
    double lat,lng;
    private Location mLastLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_call);
        btnrespond = findViewById(R.id.btn_respond);
        btndecline = findViewById(R.id.btn_decline);
        btndecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!TextUtils.isEmpty(ambulanceid))
                    declinerequest(ambulanceid);
            }
        });
        btnrespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptrequest(ambulanceid);
                startActivity(new Intent(HospitalCall.this, Welcome.class));
            }
        });

        mService = Common.getIGoogleAPI();
        mFCMService = Common.getFCMService();
        text_service = findViewById(R.id.textService);
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() !=null)
        {

            ambulanceid = getIntent().getStringExtra("ambulance");
            showServiceText();
        }

    }

    private void showServiceText() {
        text_service.setText("The Victim has " + Common.services);
    }

    private void acceptrequest(String ambulanceid){
                                Token token = new Token(ambulanceid);
                                String location = Welcome.getLocation();
                                String victimToken = FirebaseInstanceId.getInstance().getToken();
                                Notification data = new Notification(victimToken, location);
                                Sender content = new Sender(token.getToken(), data);
                                Log.d(TAG, "onDataChange: " + location);

                                mFCMService.sendMessage(content)
                                        .enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(@NonNull Call<FCMResponse> call, @NonNull Response<FCMResponse> response) {
                                                assert response.body() != null;
                                                if (response.body().success == 1)
                                                    Toast.makeText(HospitalCall.this, "You Accept Ambulance Request", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(HospitalCall.this, "Failed!", Toast.LENGTH_SHORT).show();

                                            }
                                            @Override
                                            public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                Log.e(TAG, "Error" + t.getMessage());

                                            }
                                        });

                            }






    private void declinerequest(String ambulanceid) {
        try {
            Token token = new Token(ambulanceid);
            Notification notification = new Notification("Declined", "The Hospital has declined your request");
            Sender sender = new Sender(token.getToken(), notification);
            mFCMService.sendMessage(sender)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(HospitalCall.this, "You Declined the Ambulance Request", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

}
