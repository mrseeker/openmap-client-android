package pw.openpokemap.openpokemap;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio on 20/09/2016.
 */
public class Notifications {

    private List<Integer> RarePokemons = Arrays.asList(2, 3, 4, 5, 6, 8, 9, 24, 26, 28, 31, 34, 38, 45, 51, 55, 57, 59, 67
            , 68, 75, 76, 77, 78, 83, 85, 87, 89, 94, 101, 103, 105, 107, 113, 114, 125, 126, 130, 131, 134, 135, 136, 137, 139, 141
            , 142, 143, 144, 145, 146, 147, 148, 149, 150, 151);


    public Map<Integer, String> names = new HashMap<Integer, String>();
    RequestQueue queue;
    Context context;
    NotificationManager mNotifyMgr;

    public void startNotifications(RequestQueue rq, Context ct, NotificationManager nm) {
        mNotifyMgr = nm;
        queue = rq;
        context = ct;

        String jsonString = readJson(ct);
        JsonParser parser = new JsonParser();
        JsonArray p = parser.parse(jsonString).getAsJsonArray();
        for (int i = 0; i < p.size(); i++) {
            JsonObject po = p.get(i).getAsJsonObject();
            names.put(po.get("id").getAsInt(), po.get("name").getAsString());
        }
        h.post(notificationRunnable);
    }

    Handler h = new Handler();
    LocationManager locationManager = (LocationManager)
            context.getSystemService(Context.LOCATION_SERVICE);
    public Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {
            Location l = null;
            if (ContextCompat.checkSelfPermission(MainActivity.getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                    l = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }else{
                ActivityCompat.requestPermissions((Activity)MainActivity.getContext(),
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        10);
            }
            //Create a post request
            final Location finalL = l;
            StringRequest request = new StringRequest(Request.Method.POST,
                    "http://api.openpokemap.pw/c", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Find location

                    //Handle response
                    Log.w("Json", response);
                    JsonParser parser = new JsonParser();
                    JsonArray pokemons = parser.parse(response).getAsJsonObject()
                            .getAsJsonArray("MapObjects");

                    for (int i=0; i<pokemons.size();i++){
                        JsonObject o = pokemons.get(i).getAsJsonObject();
                        int id = o.get("PokemonId").getAsInt();
                        String uuid = o.get("Id").getAsString();
                        if(RarePokemons.contains(id)){
                            Log.i("P", "Rare pokemon found!");

                            Intent notificationIntent = new Intent(context, MainActivity.class);

                            PendingIntent intent = PendingIntent.getActivity(context, 0,
                                    notificationIntent, 0);

                            String name = names.get(id);

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.map)
                                            .setContentTitle("A wild " + name + " is nearby!")
                                            .setContentIntent(intent)
                                            .setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                            .setLights(Color.MAGENTA, 3000, 3000);

                            mNotifyMgr.notify(i, mBuilder.build());
                        }
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("notifications", error.getMessage());
                }
            }){
                @Override
                protected Map<String,String> getParams() {
                    Map<String,String> params = new HashMap<String, String>();
                    String lat = Double.toString(finalL.getLatitude());
                    String lng = Double.toString(finalL.getLongitude());
                    params.put("lat", lat);
                    params.put("lng", lng);
                    params.put("p", "true");
                    return params;
                }
            };
            queue.add(request);

            //Re run this after 5 minutes
            h.postDelayed(notificationRunnable, 1*60*1000);
        }
    };

    public static String readJson (Context c) {
        try {
            InputStream is = c.getResources().openRawResource(R.raw.pokemon);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String text = new String(buffer);

            return text;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
