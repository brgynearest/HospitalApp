package com.thesis.sad.hospitalapp.Service;


import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override

    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getData()!=null) {
            LatLng victim_location = new Gson().fromJson(remoteMessage.getNotification().getBody(), LatLng.class);
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
    }

}
