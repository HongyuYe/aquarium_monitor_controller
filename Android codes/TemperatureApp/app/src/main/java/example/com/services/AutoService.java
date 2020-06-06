package example.com.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import example.com.config.ConstantsConfig;
import example.com.data.Temperature;
import example.com.module.NetworkRequestModule;
import example.com.notification.NotificationMgr;
import example.com.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AutoService extends Service {
    private static final String TAG = "AutoService";

    private static final String CHANNEL_ID_STRING = "123";

    private Timer myTimer;

    private Context mContext;
    private Boolean emailSent = false;
    private Toast mToast;
    Calendar cal;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        sharedPreferences = getSharedPreferences(ConstantsConfig.SP_CONFIG_NAME, Context.MODE_PRIVATE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID_STRING, "switch",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            Notification notification = new Notification.Builder(getApplicationContext(), CHANNEL_ID_STRING).build();
            startForeground(1, notification);
        }

        myTimer = new Timer();
        //update temperature data every 5 seconds.
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTemperature();
            }
        }, 5000, 5000);

    }

    private void updateTemperature() {

        NetworkRequestModule.updateTemperature(new Callback<List<Temperature>>() {
            @Override
            public void onResponse(Call<List<Temperature>> call, Response<List<Temperature>> response) {
                Log.i(TAG, "Temperature updated");
                Temperature temp = response.body().get(0);
                int fanOnTemperature = sharedPreferences.getInt(
                        ConstantsConfig.SP_FAN_ON_TEMPERATURE, ConstantsConfig.SP_FAN_ON_DEFAULT);
                int fanOffTemperature = sharedPreferences.getInt(
                        ConstantsConfig.SP_FAN_OFF_TEMPERATURE, ConstantsConfig.SP_FAN_OFF_DEFAULT);
                if (temp.temperature >= fanOnTemperature) { //turn on fan
                    NetworkRequestModule.sonoffOnFans(null);
                    Log.i(TAG, "Switch on fan " + fanOnTemperature);
                    showToast("Cooling system is on");
                    NotificationMgr.notify(Utils.getCurrentTime(), "Cooling system is on. Temperature is " + temp.temperature);
                    if (!emailSent) {
                        Utils.sendMail(sharedPreferences.getString(
                                ConstantsConfig.SP_EMAIL, ConstantsConfig.SP_EMAIL_DEFAULT),
                                "Alert From Your Aquarium",
                                "Cooling system is on now, the temperature is " + temp.temperature);
                        emailSent = true;
                    }
                } else if ((temp.temperature <= fanOffTemperature)
                        && !sharedPreferences.getBoolean(ConstantsConfig.SP_FAN_MANUAL_OPEN, ConstantsConfig.SP_FAN_MANUAL_DEFAULT)) {
                    NetworkRequestModule.sonoffOffFans(null);
                    Log.i(TAG, "Switch off fan " + fanOffTemperature);
                    emailSent = false;
                }
            }

            @Override
            public void onFailure(Call<List<Temperature>> call, Throwable t) {

            }
        });
        int openHour = sharedPreferences.getInt(ConstantsConfig.SP_LED_OPEN_HOUR, ConstantsConfig.SP_OPEN_HOUR_DEFAULT);
        int openMin = sharedPreferences.getInt(ConstantsConfig.SP_LED_OPEN_MIN, ConstantsConfig.SP_OPEN_MIN_DEFAULT);
        if (openHour == getCurrentHour() && openMin == getCurrentMin()) {
            NetworkRequestModule.sonoffOnLED(null);
            Log.i(TAG, "Switch on LED");
        }

        int closeHour = sharedPreferences.getInt(ConstantsConfig.SP_LED_CLOSE_HOUR, ConstantsConfig.SP_CLOSE_HOUR_DEFAULT);
        int closeMin = sharedPreferences.getInt(ConstantsConfig.SP_LED_CLOSE_MIN, ConstantsConfig.SP_CLOSE_MIN_DEFAULT);
        if (closeHour == getCurrentHour() && closeMin == getCurrentMin() &&
                !sharedPreferences.getBoolean(ConstantsConfig.SP_LED_MANUAL_OPEN, ConstantsConfig.SP_LED_MANUAL_DEFAULT)) {
            NetworkRequestModule.sonoffOffLED(null);
            Log.i(TAG, "Switch off LED");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (myTimer != null) {
            myTimer.cancel();
        }
        if(!sharedPreferences.getBoolean(ConstantsConfig.SP_FAN_MANUAL_OPEN, ConstantsConfig.SP_FAN_MANUAL_DEFAULT)){
            NetworkRequestModule.sonoffOffFans(null);
        }
        if(!sharedPreferences.getBoolean(ConstantsConfig.SP_LED_MANUAL_OPEN, ConstantsConfig.SP_LED_MANUAL_DEFAULT)){
            NetworkRequestModule.sonoffOffLED(null);
        }
        showToast("Auto control stopped.");
        emailSent = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService(Context context) {
        Intent intent = newIntent(context);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopService(Context context) {
        Intent intent = newIntent(context);
        context.stopService(intent);
    }

    private static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AutoService.class);
        return intent;
    }

//Display Toast message
    private void showToast(String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

    private int getCurrentHour() {
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));

        if (cal.get(Calendar.AM_PM) == 0)
            return cal.get(Calendar.HOUR);
        else
            return cal.get(Calendar.HOUR) + 12;
    }

    private int getCurrentMin() {
        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));

        return cal.get(Calendar.MINUTE);
    }
}
