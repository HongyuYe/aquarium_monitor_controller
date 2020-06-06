package example.com;

import android.app.Application;

public class HTApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppConfig.getInstance().init(this);
    }
}
