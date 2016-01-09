package com.coinomi.wallet.tasks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.coinomi.wallet.util.PowerGenerationInfo;
import com.coinomi.wallet.util.XMLRequest;


/**
 * Created by andy on 16/1/9.
 */
public abstract class PowerGenerationInfoTask extends TimerTask {
    //private static final String POWER_GENERATION_URL = "http://www3.city.sabae.fukui.jp/xml/population/population.xml";
    // confidential information, not showed in hackathon
    private static final String POWER_GENERATION_URL = "*************";
    private RequestQueue mQueue;
    private XMLRequest xmlRequest;
    private static PowerGenerationInfo powerGenerationInfo = new PowerGenerationInfo();

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PowerGenerationInfoTask.class);

    public PowerGenerationInfoTask(Context context) {
         mQueue = Volley.newRequestQueue(context);
         xmlRequest = new XMLRequest(
                POWER_GENERATION_URL,
                new com.android.volley.Response.Listener<XmlPullParser>() {
                    @Override
                    public void onResponse(XmlPullParser response) {
                        try {
                            int eventType = response.getEventType();
                            while (eventType != XmlPullParser.END_DOCUMENT) {
                                switch (eventType) {
                                    case XmlPullParser.START_TAG:
                                        String nodeName = response.getName();
                                        if ("name".equals(nodeName)) {
                                            String pName = response.nextText();
                                            powerGenerationInfo.powerStation = pName;
                                            Log.d("TAG", "powerStation is " + pName);
                                        }

                                        if ("date".equals(nodeName)) {
                                            String pName = response.nextText();
                                            powerGenerationInfo.updateTime = pName;
                                            Log.d("TAG", "date is " + pName);
                                        }

                                        if ("acEnergy".equals(nodeName)) {
                                            String pName = response.nextText();
                                            powerGenerationInfo.acEnergy = pName;
                                            Log.d("TAG", "acEnergy is " + pName);
                                        }
                                        if ("insolation".equals(nodeName)) {
                                            String pName = response.nextText();
                                            powerGenerationInfo.insolation = pName;
                                            Log.d("TAG", "insolation is " + pName);
                                        }
                                        if ("temperature".equals(nodeName)) {
                                            String pName = response.nextText();
                                            powerGenerationInfo.temperature = pName;
                                            Log.d("TAG", "temperature is " + pName);
                                        }

                                        break;
                                }
                                eventType = response.next();
                            }
                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });

    }

    abstract public void onHandlePowerGenerationInfo(PowerGenerationInfo powerGenerationInfo);

    @Override
    public void run() {
        PowerGenerationInfo powerGenerationInfo = getPowerGenerationInfoSync();
        if (powerGenerationInfo != null) {
            onHandlePowerGenerationInfo(powerGenerationInfo);
        }
    }

    /**
     * Makes a call to ShapeShift about the market info of a pair. If case of a problem, it will
     * retry 3 times and return null if there was an error.
     *
     * Note: do not call this from the main thread!
     */
    @Nullable
    public PowerGenerationInfo getPowerGenerationInfoSync() {
        // Try 3 times
        String pName = "";
        for (int tries = 1; tries <= 1; tries++) {
            try {
                log.info("Polling PowerGeneration info for bitproperty");
                //mQueue.add(xmlRequest);
                return powerGenerationInfo;
            } catch (Exception e) {
                log.info("Will retry: {}", e.getMessage());
                    /* ignore and retry, with linear backoff */

                try {
                    Thread.sleep(100 * tries);
                } catch (InterruptedException ie) { /*ignored*/ }
            }
         }
        return null;
    }
}

