package example.com.module;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private ApiClient() {}

    private static Retrofit temperatureRetrofit;
    private static final String TEMPERATURE_BASE_URL = "http://aquarium-monitor.azurewebsites.net/";

    private static Retrofit fanSwitchRetrofit;
    private static Retrofit LEDSwitchRetrofit;
    private static final String FAN_SWITCH_BASE_URL = "https://maker.ifttt.com/";

    public static IApiService getTemperatureAPIService() {
        if (temperatureRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // set connect time out.
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    // set read time out
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)
                    .build();
            temperatureRetrofit = new Retrofit.Builder()
                    .baseUrl(TEMPERATURE_BASE_URL)
                    .client(okHttpClient)
                    // set the factory to create bean
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return temperatureRetrofit.create(IApiService.class);
    }

    public static IApiService getFanSwitchAPIService() {
        if (fanSwitchRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // set connect time out
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    // set read time out
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)
                    .build();
            fanSwitchRetrofit = new Retrofit.Builder()
                    .baseUrl(FAN_SWITCH_BASE_URL)
                    .client(okHttpClient)
                    // set the factory to create bean
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return fanSwitchRetrofit.create(IApiService.class);
    }

    public static IApiService getLEDSwitchAPIService() {
        if (LEDSwitchRetrofit == null) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    // set connect time out
                    .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                    // set read time out
                    .readTimeout(10000L, TimeUnit.MILLISECONDS)
                    .build();
            LEDSwitchRetrofit = new Retrofit.Builder()
                    .baseUrl(FAN_SWITCH_BASE_URL)
                    .client(okHttpClient)
                    // set the factory to create bean
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return LEDSwitchRetrofit.create(IApiService.class);
    }
}