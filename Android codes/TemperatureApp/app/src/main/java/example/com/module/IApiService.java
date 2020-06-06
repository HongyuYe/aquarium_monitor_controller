package example.com.module;


import java.util.List;

import example.com.data.Temperature;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;


public interface IApiService {

    @GET("/api/temperature/latest?location=Aquarium")
    Call<List<Temperature>> getTemperatureData();

    @GET("/trigger/sonoff_on/with/key/cAhYMuqhQvJ2rxpHOfOdok")
    Call<ResponseBody> sonoffOnFans();

    @GET("/trigger/sonoff_off/with/key/cAhYMuqhQvJ2rxpHOfOdok")
    Call<ResponseBody> sonoffOffFans();

    @GET("/trigger/led_on/with/key/cAhYMuqhQvJ2rxpHOfOdok")
    Call<ResponseBody> sonoffOnLED();

    @GET("/trigger/led_off/with/key/cAhYMuqhQvJ2rxpHOfOdok")
    Call<ResponseBody> sonoffOffLED();
}