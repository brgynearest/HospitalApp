package com.thesis.sad.hospitalapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    TextView textTime,textDistance,textAddress;
    Button btnrespond,btndecline;
    MediaPlayer mediaPlayer;
    IGoogleAPI mService;
    String ambulanceid;
    IFCMService mFCMService;
    double lat,lng;
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
                Intent intent = new Intent(HospitalCall.this, HospitalTracking.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("ambulanceid",ambulanceid);
                startActivity(intent);
                finish();
            }
        });

        mService = Common.getIGoogleAPI();
        mFCMService = Common.getFCMService();
        textTime = findViewById(R.id.textTime);
        textAddress = findViewById(R.id.textAddress);
        textDistance = findViewById(R.id.textDistance);
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() !=null)
        {
            lat = getIntent().getDoubleExtra("lat",-1.0);
            lng = getIntent().getDoubleExtra("lng",-1.0);
            ambulanceid = getIntent().getStringExtra("ambulance");
            getDirection(lat,lng);
        }
    }

    private void declinerequest(String victimId) {

        try {
            Token token = new Token(victimId);
            Notification notification = new Notification("Declined", "Brgy Ambulance has declined your request");
            Sender sender = new Sender(token.getToken(), notification);
            mFCMService.sendMessage(sender)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body().success == 1) {
                                Toast.makeText(HospitalCall.this, "You Declined the Victims Request", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                        }
                    });
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage()                                                                                                                                        , Toast.LENGTH_SHORT).show();
        }
    }


    private void getDirection(double lat,double lng) {
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preferences=less_driving&"+
                    "origin="+ Common.mLastlocation.getLatitude()+","+Common.mLastlocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_directions_api);
            Log.d("POWER",requestApi);
            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsobject = legs.getJSONObject(0);

                                JSONObject distance = legsobject.getJSONObject("distance");
                                textDistance.setText(distance.getString("text"));

                                JSONObject time = legsobject.getJSONObject("duration");
                                textTime.setText(time.getString("text"));

                                String address = legsobject.getString("end_address");
                                textAddress.setText(address);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Toast.makeText(HospitalCall.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception e){

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
