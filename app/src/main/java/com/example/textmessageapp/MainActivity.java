package com.example.textmessageapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    String text;
    String sender;
    SmsMessage[] smsMessage;
    SmsManager smsManager;
    Handler handler;
    JSONObject jsonObject;
    Boolean registered = false;
    String last;
    int number = 0;
    //Boolean permission = false;
    BroadcastReceiver broadcastReceiver;


    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TEXTTAG", "onStrart");
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(broadcastReceiver, intentFilter);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TEXTTAG", "here");
                Bundle b = intent.getExtras();
                Object[] objects = (Object[]) b.get("pdus");
                smsMessage = new SmsMessage[objects.length];
                for (int i = 0; i < objects.length; i++) {
                    byte[] bytes = (byte[]) objects[i];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        smsMessage[i] = SmsMessage.createFromPdu(bytes, b.getString("format"));
                        sender = smsMessage[i].getOriginatingAddress();
                        Log.d("TEXTTAG", smsMessage[i].getMessageBody());
                    }
                }

                AsyncThread task = new AsyncThread();
                task.execute(smsMessage[smsMessage.length - 1].getMessageBody());
                Log.d("TEXTTAG", "sending this: "+ smsMessage[smsMessage.length-1].getMessageBody());
                Log.d("TEXTTAG", "length: "+ smsMessage.length);

                for (int i = 0; i < smsMessage.length; i++)
                    Log.d("TEXTTAG", "messageArray: "+ smsMessage[i].getMessageBody());

                /*if(smsMessage.length == 1){
                    if(!(smsMessage[smsMessage.length - 1].getMessageBody().equals(last))) {
                        Log.d("TEXTTAG", "actually sent");
                        task.execute(smsMessage[smsMessage.length - 1].getMessageBody()); //used in async task
                        last = smsMessage[smsMessage.length - 1].getMessageBody();
                    }
                }

                 */
                /*else if((!((smsMessage[smsMessage.length-1].getMessageBody()).equals(smsMessage[smsMessage.length-2].getMessageBody())))) {
                    Log.d("TEXTTAG", "actually sent");
                    task.execute(smsMessage[smsMessage.length - 1].getMessageBody()); //used in async task
                }

                 */
                if(smsMessage.length != 1)
                    Log.d("TEXTTAG", "Difference: " + (smsMessage[smsMessage.length-1].getMessageBody()) + "        " + (smsMessage[smsMessage.length-2].getMessageBody()));



            }
        };
        Log.d("TEXTTAG", "here1");

        /*IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        if(!registered) {
            Log.d("TEXTTAG", "REGISTERING");
            //unregisterReceiver(broadcastReceiver);
            if(number==1)
                unregisterReceiver(broadcastReceiver);
            registerReceiver(broadcastReceiver, intentFilter);
            registered = true;
            number++;
        }

         */


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TEXTTAG", "preasled");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, 1);
            Log.d("TEXTTAG", "asled");
        }













    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED&& grantResults[1] == PackageManager.PERMISSION_GRANTED)
            permission = true;
    }

     */



    public Runnable sendingMethod(String string){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d("TEXTTAG", "inmethod");
                smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(sender,null,string,null,null);
            }
        };
        return runnable;
    }




    public class AsyncThread extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {

            try {

                URL url1 = new URL("http://api.brainshop.ai/get?bid=175244&key=dANxuRmsEQKsLmms&uid=[uid]&msg=" + strings[0]);
                Log.d("TAG", url1.toString());
                URLConnection urlConnection1 = url1.openConnection();
                Log.d("TAG", "1");
                InputStream inputStream1 = urlConnection1.getInputStream();
                Log.d("TAG", "2");
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(inputStream1));
                Log.d("TAG", "3");

                String linetemp = "";
                String response = "";

                while ((linetemp = bufferedReader1.readLine()) != null) {
                    //Log.d("Poop","2");
                    response += linetemp;
                }


                jsonObject = new JSONObject(response);



            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                text = jsonObject.getString("cnt");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("TEXTTAG", "return" + text);
            handler.postDelayed(sendingMethod(text), 1000);
        }
    }


}