package com.thesis.sad.hospitalapp.Service;


import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.thesis.sad.hospitalapp.Interaction.Common;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override

    public void onMessageReceived(final RemoteMessage remoteMessage) {

        if(remoteMessage.getData()!=null) {
            Common.services = remoteMessage.getNotification().getTitle();
            /*LatLng victim_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);*/
            /*Map<String,String> data = remoteMessage.getData();
            String victimId  = data.get("victimId");
            String lat = data.get("lat");
            String lng = data.get("lng");*/

            /*Intent intent = new Intent(getBaseContext(), VictimCall.class);*/
            /*intent.putExtra("lat", victim_location.latitude);
            intent.putExtra("lng", victim_location.longitude);
            intent.putExtra("victim",remoteMessage.getNotification().getTitle());
            startActivity(intent);*/

       }

       else if(remoteMessage.getNotification().getTitle().equals("Arrived")){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MyFirebaseMessaging.this," "+remoteMessage.getNotification()
                            .getBody(),Toast.LENGTH_SHORT).show();

                }
            });

        }
    }

}
