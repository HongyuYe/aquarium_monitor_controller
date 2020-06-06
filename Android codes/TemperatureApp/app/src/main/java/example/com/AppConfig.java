package example.com;

import android.content.Context;

public class AppConfig {

    private Context context;
    private static final AppConfig ourInstance = new AppConfig();

    public static AppConfig getInstance() {
        return ourInstance;
    }

    private AppConfig() {
    }

    public void init(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }
}
