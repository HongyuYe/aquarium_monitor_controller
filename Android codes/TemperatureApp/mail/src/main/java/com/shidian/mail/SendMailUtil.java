package com.shidian.mail;

import android.support.annotation.NonNull;

import java.io.File;


public class SendMailUtil {

    //qq email
    private static final String HOST = "smtp.qq.com";
    private static final String PORT = "587";
    private static final String FROM_ADD = "1013170312@qq.com";
    private static final String FROM_PSW = "dqsbeqehethubbcg";
//    //163
//    private static final String HOST = "smtp.163.com";
//    private static final String PORT = "465"; //or 465  994
//    private static final String FROM_ADD = "teprinciple@163.com";
//    private static final String FROM_PSW = "teprinciple163";
////    private static final String TO_ADD = "2584770373@qq.com";


    public static void send(final File file, String toAdd, String title, String content) {
        final MailInfo mailInfo = creatMail(toAdd, title, content);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendFileMail(mailInfo, file);
            }
        }).start();
    }


    public static void send(String toAdd, String title, String content) {
        final MailInfo mailInfo = creatMail(toAdd, title, content);
        final MailSender sms = new MailSender();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sms.sendTextMail(mailInfo);
            }
        }).start();
    }

    @NonNull
    private static MailInfo creatMail(String toAdd, String title, String content) {
        final MailInfo mailInfo = new MailInfo();
        mailInfo.setMailServerHost(HOST);
        mailInfo.setMailServerPort(PORT);
        mailInfo.setValidate(true);
        mailInfo.setUserName(FROM_ADD); // email address of sender
        mailInfo.setPassword(FROM_PSW);// email password of sender
        mailInfo.setFromAddress(FROM_ADD); // email address of sender
        mailInfo.setToAddress(toAdd); // email address of receiver
        mailInfo.setSubject(title); // title of the email
        mailInfo.setContent(content); // content of the email
        return mailInfo;
    }

}
