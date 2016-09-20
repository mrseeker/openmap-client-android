package pw.openpokemap.openpokemap;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.util.ArrayMap;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Antonio on 20/09/2016.
 */
public class Background extends Service{
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startid) {
        Log.w("Service", "Starting service");
        Gson gson = new Gson();
        RequestQueue queue = Volley.newRequestQueue(this);

        //Start notifications runnable
        Notifications n = new Notifications();
        n.startNotifications(queue, getApplicationContext(), (NotificationManager) getSystemService(NOTIFICATION_SERVICE));

        AsyncHttpClient.getDefaultInstance().websocket("ws://ws.openpokemap.pw:8080/websocket", "ws",
                new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(Exception ex, final WebSocket webSocket) {
                        if (ex != null){
                            Log.w("Service", ex.getMessage());
                            return;
                        }

                        Log.w("R", "Connected");

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            @Override
                            public void onStringAvailable(String s) {
                                // Parse JSON
                                final Gson gson = new Gson();
                                Map<String, String> data = new HashMap<String, String>();
                                data = gson.fromJson(s, Map.class);

                                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                                final Map<String, String> finalData = data;
                                Log.w("R", "Recived query");

                                final Map<String, String> mHeaders = new ArrayMap<String, String>();
                                mHeaders.put("User-Agent", data.get("user"));

                                StringRequest r = new StringRequest(finalData.get("meth") == "GET"
                                        ? Request.Method.GET : Request.Method.POST, finalData.get("host"),
                                        new Response.Listener<String>(){

                                            @Override
                                            public void onResponse(String response) {
                                                Log.w("R", response);
                                                Map<String, Object> f = new HashMap<String, Object>();
                                                String to_send = Base64.encodeToString(response.getBytes(), Base64.NO_WRAP);
                                                f.put("response", to_send);
                                                f.put("status", 200);
                                                Log.w("R", gson.toJson(f).toString())
;                                                webSocket.send(gson.toJson(f).toString());
                                            }
                                        }, new Response.ErrorListener(){
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Map<String, String> f = new HashMap<String, String>();
                                        f.put("response", error.getMessage());
                                        f.put("status", "400");
                                        webSocket.send(gson.toJson(f));
                                    }
                                }){
                                    @Override
                                    public byte[] getBody() throws com.android.volley.AuthFailureError {
                                        String to_send = finalData.get("data");
                                        byte[] data = Base64.decode(to_send, Base64.DEFAULT);
                                        return data;
                                    }

                                    public Map<String, String> getHeaders() {
                                        return mHeaders;
                                    }
                                };

                                queue.add(r);
                            }
                        });

                    }
                });

        return START_STICKY;
    }
}
