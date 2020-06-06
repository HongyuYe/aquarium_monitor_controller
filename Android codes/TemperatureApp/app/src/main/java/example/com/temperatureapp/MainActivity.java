package example.com.temperatureapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import example.com.config.ConstantsConfig;
import example.com.data.Temperature;
import example.com.module.NetworkRequestModule;
import example.com.services.AutoService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private Context mContext;

    private TextView location;
    private TextView temperature;
    private TextView dateT;
    private TextView timeT;
    private Switch switchFan;
    public Boolean fanManualOpen = false;
    private Switch switchLED;
    public Boolean LEDManualOpen = false;
    private Timer myTimer;
    private Toast mToast;

    TextView txtTime1;
    TextView txtTime2;
    Calendar cal;
    String year;
    String month;
    String day;
    String hour;
    String minute;
    String second;
    String my_time_1;
    String my_time_2;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(ConstantsConfig.SP_CONFIG_NAME, Context.MODE_PRIVATE);
        this.mContext = this;

        location = (TextView) findViewById(R.id.location);
        temperature = (TextView) findViewById(R.id.temperature);
        dateT = (TextView) findViewById(R.id.dateText);
        timeT = (TextView) findViewById(R.id.timeText);

        switchFan = (Switch) findViewById(R.id.fanSwitch);
        switchFan.setChecked(sharedPreferences.getBoolean(ConstantsConfig.SP_FAN_MANUAL_OPEN, ConstantsConfig.SP_FAN_MANUAL_DEFAULT));
        switchFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //turn on  fan
                    NetworkRequestModule.sonoffOnFans(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.i(TAG, "Switch on fan");
                            showToast("Cooling system is on");
                            fanManualOpen = true;
                            sharedPreferences.edit().putBoolean(ConstantsConfig.SP_FAN_MANUAL_OPEN, true).commit();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                } else { //turn off fan.
                    NetworkRequestModule.sonoffOffFans(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.i(TAG, "Switch off fan");
                            showToast("Cooling system is off");
                            fanManualOpen = false;
                            sharedPreferences.edit().putBoolean(ConstantsConfig.SP_FAN_MANUAL_OPEN, false).commit();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                }
            }
        });

        switchLED = (Switch) findViewById(R.id.LEDSwitch);
        switchLED.setChecked(sharedPreferences.getBoolean(ConstantsConfig.SP_LED_MANUAL_OPEN, ConstantsConfig.SP_LED_MANUAL_DEFAULT));
        switchLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { //turn on  LED
                    NetworkRequestModule.sonoffOnLED(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.i(TAG, "Switch on LED");
                            showToast("LED is on");
                            LEDManualOpen = true;
                            sharedPreferences.edit().putBoolean(ConstantsConfig.SP_LED_MANUAL_OPEN, true).commit();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                } else { //turn off LED.
                    NetworkRequestModule.sonoffOffLED(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            Log.i(TAG, "Switch off LED");
                            showToast("LED is off");
                            LEDManualOpen = false;
                            sharedPreferences.edit().putBoolean(ConstantsConfig.SP_LED_MANUAL_OPEN, false).commit();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });
                }
            }
        });

        Button mSettingsButton = (Button) findViewById(R.id.settingsButton);
        mSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Settings.class);
                view.getContext().startActivity(intent);
            }
        });

        Button mGetTempButton = (Button) findViewById(R.id.getTempButton);
        mGetTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTemperature();
            }
        });

        Button mClearButton = (Button) findViewById(R.id.clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location.setText("Location");
                updateTime();
                temperature.setText("None");
                dateT.setText("None");
                timeT.setText("None");
            }
        });

        findViewById(R.id.buttonStartAuto).setOnClickListener(this);
        findViewById(R.id.buttonStopAuto).setOnClickListener(this);

        updateTemperature();
    }

    private void updateTemperature() {
        NetworkRequestModule.updateTemperature(new Callback<List<Temperature>>() {
            @Override
            public void onResponse(Call<List<Temperature>> call, Response<List<Temperature>> response) {
                Log.i(TAG, "Completed");

                Temperature temp = response.body().get(0);

                location.setText(temp.partitionKey);
                updateTime();
                temperature.setText(Double.toString(temp.temperature));

                Log.i(TAG, response.body().toString());
            }

            @Override
            public void onFailure(Call<List<Temperature>> call, Throwable t) {
                Log.e(TAG, "failed " + t);
            }
        });
    }

    public void onClick(View src) {
        switch (src.getId()) {
            case R.id.buttonStartAuto:
                Log.i(TAG, "onClick: starting service");
                showToast("Auto control started.");
                AutoService.startService(mContext);
                //refresh the data every 5 seconds
                myTimer = new Timer();
                myTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        updateTemperature();
                    }
                }, 5000, 5000);
                break;
            case R.id.buttonStopAuto:
                Log.i(TAG, "onClick: stopping service");
                showToast("Auto control stopped.");
                AutoService.stopService(mContext);
                //stop refreshing
                if (myTimer != null) {
                    myTimer.cancel();
                }
                break;
        }
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

    private void updateTime() {
        txtTime1 = (TextView) findViewById(R.id.dateText);
        txtTime2 = (TextView) findViewById(R.id.timeText);

        cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+10:00"));

        year = String.valueOf(cal.get(Calendar.YEAR));
        month = String.valueOf(cal.get(Calendar.MONTH) + 1);
        day = String.valueOf(cal.get(Calendar.DATE));
        if (cal.get(Calendar.AM_PM) == 0)
            hour = String.valueOf(cal.get(Calendar.HOUR));
        else
            hour = String.valueOf(cal.get(Calendar.HOUR) + 12);
        minute = String.valueOf(cal.get(Calendar.MINUTE));
        second = String.valueOf(cal.get(Calendar.SECOND));

        my_time_1 = year + "-" + month + "-" + day;
        my_time_2 = hour + ":" + minute + ":" + second;

        txtTime1.setText(my_time_1);
        txtTime2.setText(my_time_2);

    }
}
