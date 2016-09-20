package pw.openpokemap.openpokemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Antonio on 20/09/2016.
 */
public class Reciver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent background = new Intent(context, Background.class);
        context.startService(background);
    }
}
