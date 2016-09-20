package pw.openpokemap.openpokemap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Antonio on 20/09/2016.
 */
public class Notifications {

    private List<Integer> RarePokemons = Arrays.asList(2,3,4,5,6,8,9,24,26,28,31,34,38,45,51,55,57,59,67
            ,68,75,76,77,78,83,85,87,89,94,101,103,105,107,113,114,125,126,130,131,134,135,136,137,139,141
            ,142,143,144,145,146,147,148,149,150,151);

    RequestQueue queue;
    Context context;
    NotificationManager mNotifyMgr;

    public void startNotifications(RequestQueue rq, Context ct, NotificationManager nm){
        mNotifyMgr = nm;
        queue = rq;
        context = ct;
        //h.post(notificationRunnable);
    }

    Handler h = new Handler();

    public Runnable notificationRunnable = new Runnable() {
        @Override
        public void run() {

            //Create a post request
            StringRequest request = new StringRequest(Request.Method.POST,
                    "https://4aa7d206.ngrok.io/c", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Handle response
                    JsonParser parser = new JsonParser();
                    JsonArray pokemons = parser.parse(response).getAsJsonObject()
                            .getAsJsonArray("MapObjects");

                    for (int i=0; i<pokemons.size();i++){
                        JsonObject o = pokemons.get(i).getAsJsonObject();
                        int id = o.get("PokemonId").getAsInt();
                        if(RarePokemons.contains(id)){
                            Log.i("P", "Rare pokemon found!");

                            Intent notificationIntent = new Intent(context, MainActivity.class);

                            PendingIntent intent = PendingIntent.getActivity(context, 0,
                                    notificationIntent, 0);

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.mipmap.ic_launcher)
                                            .setContentTitle("A rare pokemon is nearby!")
                                            .setContentIntent(intent);

                            mNotifyMgr.notify(1, mBuilder.build());
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
                    params.put("lat", "34.0095897345215");
                    params.put("lng", "-118.49791288375856");
                    params.put("p", "true");
                    return params;
                }
            };
            queue.add(request);

            //Re run this after 5 minutes
            h.postDelayed(notificationRunnable, 5*60*1000);
        }
    };
}
