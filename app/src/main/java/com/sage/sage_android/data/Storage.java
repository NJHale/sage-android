package com.sage.sage_android.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sage.sage_android.TaskService;


/**
 * Created by cyberpirate on 3/15/2016.
 */
public class Storage {


    private transient static final String PREF_NAME = "storage";
    private transient static final String KEY = "settings";
    private transient static Storage instance;
    public transient static Context context;

    public String nodeId = "-1";
    public String googleToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6ImNlZjUwNTEzNjVjMjBiNDkwODg2N2UyZjg1ZGUxZTU0MWM2Y2NkM2MifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6ImRJeUJhNGlid2tSOUdPeU4yZEUxTWciLCJhdWQiOiI2NjU1NTEyNzQ0NjYtazllNW91bjIxY2hlN3FhbW0yY3Q5Ym42MDNkc3M2NW4uYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTQzODkyMTYwODI4ODY4Njc0NjAiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiNjY1NTUxMjc0NDY2LWs5ZTVvdW4yMWNoZTdxYW1tMmN0OWJuNjAzZHNzNjVuLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJuam9obmhhbGVAZ21haWwuY29tIiwiaWF0IjoxNDU2NTkxMzI2LCJleHAiOjE0NTY1OTQ5MjZ9.VX5oqY3OrnqLFaGaifu6JV_PWlgHmfBgE1c1o5cO9aNVoLxFFdjH523UvMwX1d7VGkbvAety7KgWDNIftMrwV9OpyR0vGdwuxcjkb7ICOqAoQuSFFj5P-jd1r7KhCFo40e7NUHDNDBZoqjpsT0KGxui8PxfADVuhWNKjSK0Fb7IjlDWEuPl8qJe58nqwCHFjhfQaOC4xTBazC_VdteDSsjnVLy3MFHK-uVQjl0pINt3mYco5sNvTpheWjKic9cwv8J_HDjy0eUv0-aFGqJO_ADqGplVdpgzt_DrHHhlCyGVPfDwHsuMiGaK7MjSXnaCox5NBvy3kEcXBDDkYQihgEQ";
    public String sageToken = "";
    public boolean running = true;
    public long earnedBounty = 0;

    private Storage() {

    }

    public static Storage getInstance() {
        if(instance == null) loadStorage();
        return instance;
    }

    public static void loadStorage() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        instance = gson.fromJson(pref.getString(KEY, "{}"), Storage.class);
    }

    public static void saveStorage() {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(instance);
        pref.edit().putString(KEY, json).apply();
    }

    public boolean getRunning() {
        synchronized(this) {
            return running;
        }
    }

    public void setRunning(boolean b) {
        synchronized (this) {
            running = b;
        }
        TaskService.taskRunner.setPaused(!b);
    }

    public void addToBounty(int bounty) {
        synchronized(this) {
            earnedBounty += bounty;
            saveStorage();
        }
    }
}
