package example.com.utils;

import com.shidian.mail.SendMailUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    public static final String DATE_FORMAT = "yyyy-MM-dd-hh:mm:ss";

    public static String getCurrentTime() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }


    public static void sendMail(String mail, String title, String content) {
        SendMailUtil.send(mail, title, content);
    }
}
