package example.com.temperatureapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import example.com.config.ConstantsConfig;

public class Settings extends AppCompatActivity{
    private static final String TAG = "Settings";
    private Toast mToast;
    private Button setEmailButton;
    private Button setFanButton;
    private Button setLEDButton;
    public String userEmail;
    public int fanOnTemperature;
    public int fanOffTemperature;
    public int LEDOpenHour;
    public int LEDCloseHour;
    public int LEDOpenMin;
    public int LEDCloseMin;

    private SharedPreferences sharedPreferences;

    //system creating the activity instance.
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPreferences = getSharedPreferences(ConstantsConfig.SP_CONFIG_NAME, Context.MODE_PRIVATE);

        final ClearEditText email = (ClearEditText) findViewById(R.id.email);
        final ClearEditText fanOnTem = (ClearEditText) findViewById(R.id.fanOnTem);
        final ClearEditText fanOffTem = (ClearEditText) findViewById(R.id.fanOffTem);
        final ClearEditText LEDOnHour = (ClearEditText) findViewById(R.id.LEDOnHour);
        final ClearEditText LEDOnMin = (ClearEditText) findViewById(R.id.LEDOnMin);
        final ClearEditText LEDOffHour = (ClearEditText) findViewById(R.id.LEDOffHour);
        final ClearEditText LEDOffMin = (ClearEditText) findViewById(R.id.LEDOffMin);

        //init the value of default
        email.setText(sharedPreferences.getString(ConstantsConfig.SP_EMAIL, ConstantsConfig.SP_EMAIL_DEFAULT));
        fanOnTem.setText(String.valueOf(sharedPreferences.getInt(ConstantsConfig.SP_FAN_ON_TEMPERATURE, ConstantsConfig.SP_FAN_ON_DEFAULT)));
        fanOffTem.setText(String.valueOf(sharedPreferences.getInt(ConstantsConfig.SP_FAN_OFF_TEMPERATURE, ConstantsConfig.SP_FAN_OFF_DEFAULT)));
        int openHour = sharedPreferences.getInt(ConstantsConfig.SP_LED_OPEN_HOUR, ConstantsConfig.SP_OPEN_HOUR_DEFAULT);
        int openMin = sharedPreferences.getInt(ConstantsConfig.SP_LED_OPEN_MIN, ConstantsConfig.SP_OPEN_MIN_DEFAULT);
        int closeHour = sharedPreferences.getInt(ConstantsConfig.SP_LED_CLOSE_HOUR, ConstantsConfig.SP_CLOSE_HOUR_DEFAULT);
        int closeMin = sharedPreferences.getInt(ConstantsConfig.SP_LED_CLOSE_MIN, ConstantsConfig.SP_CLOSE_MIN_DEFAULT);
        LEDOnHour.setText(String.valueOf(openHour));
        LEDOnMin.setText(String.valueOf(openMin));
        LEDOffHour.setText(String.valueOf(closeHour));
        LEDOffMin.setText(String.valueOf(closeMin));

        setFanButton = (Button) findViewById(R.id.setFan);
        setEmailButton = (Button) findViewById(R.id.setEmail);
        setLEDButton = (Button) findViewById(R.id.setLED);

        setEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email.getText())){
                    email.setShakeAnimation();
                    showToast("Email cannot be empty!");
                    return;
                }

                userEmail = email.getText().toString();
                sharedPreferences.edit().putString(ConstantsConfig.SP_EMAIL, userEmail).commit();
                showToast("Your email set.");
            }
        });


        setLEDButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(LEDOnHour.getText())){
                    LEDOnHour.setShakeAnimation();
                    showToast("LED open hour cannot be empty!");
                    return;
                }
                if (TextUtils.isEmpty(LEDOnMin.getText())){
                    LEDOnMin.setShakeAnimation();
                    showToast("LED open minute cannot be empty!");
                    return;
                }

                if (TextUtils.isEmpty(LEDOffHour.getText())){
                    LEDOffHour.setShakeAnimation();
                    showToast("LED close hour cannot be empty!");
                    return;
                }
                if (TextUtils.isEmpty(LEDOffMin.getText())){
                    LEDOffMin.setShakeAnimation();
                    showToast("LED close minute cannot be empty!");
                    return;
                }

                LEDOpenHour = Integer.parseInt(LEDOnHour.getText().toString());
                LEDOpenMin = Integer.parseInt(LEDOnMin.getText().toString());
                LEDCloseHour = Integer.parseInt(LEDOffHour.getText().toString());
                LEDCloseMin = Integer.parseInt(LEDOffMin.getText().toString());

                sharedPreferences.edit().putInt(ConstantsConfig.SP_LED_OPEN_HOUR, LEDOpenHour).commit();
                sharedPreferences.edit().putInt(ConstantsConfig.SP_LED_OPEN_MIN, LEDOpenMin).commit();
                sharedPreferences.edit().putInt(ConstantsConfig.SP_LED_CLOSE_HOUR, LEDCloseHour).commit();
                sharedPreferences.edit().putInt(ConstantsConfig.SP_LED_CLOSE_MIN, LEDCloseMin).commit();
                showToast("Auto LED Set.");

            }

        });

        setFanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(fanOnTem.getText())){
                    fanOnTem.setShakeAnimation();
                    showToast("Fan on temperature cannot be empty!");
                    return;
                }
                if (TextUtils.isEmpty(fanOffTem.getText())){
                    fanOffTem.setShakeAnimation();
                    showToast("Fan off temperature cannot be empty!");
                    return;
                }

                fanOnTemperature = Integer.parseInt(fanOnTem.getText().toString());
                fanOffTemperature = Integer.parseInt(fanOffTem.getText().toString());
                sharedPreferences.edit().putInt(ConstantsConfig.SP_FAN_ON_TEMPERATURE, fanOnTemperature).commit();
                sharedPreferences.edit().putInt(ConstantsConfig.SP_FAN_OFF_TEMPERATURE, fanOffTemperature).commit();
                showToast("Auto Fan Set.");

            }

        });

        // displaying the go back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setTitle("Settings");

    }

    @Override
    // click the go back button will go back to home page
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:   //return the id of the key
                this.finish();
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//Display Toast message
    private void showToast(String msg) {
        if (mToast == null){
            mToast = Toast.makeText(this,msg,Toast.LENGTH_SHORT);
        }else{
            mToast.setText(msg);
        }
        mToast.show();
    }
}
