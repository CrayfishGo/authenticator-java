package com.evan1120.test;

import com.evan1120.authenticator.PinCodeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class Test {
    public static void main(String[] args) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    //单纯输出时间
                    Date now = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");//可以方便地修改日期格式
                    String hehe = dateFormat.format(now);

                    //测试算法
                    String secret = "dgf3j5csiu2jn6wehechiuuclyhcnyaw";
                    String result = PinCodeUtil.getCurrentCode(secret);
                    System.out.println(hehe + " -- " + result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
    }
}