package com.sage.sage_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sage.sage_android.data.AndroidNode;
import com.sage.sage_android.data.AppInit;
import com.sage.sage_android.data.GoogleToken;
import com.sage.sage_android.data.SageToken;
import com.sage.sage_android.data.Storage;
import com.sage.sage_android.data.Job;
import com.sage.sage_android.data.Utils;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cyberpirate on 3/5/2016.
 */
public class TaskService extends Service {

    public static AtomicBoolean serviceStarted = new AtomicBoolean(false);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        serviceStarted.set(true);
        AppInit.appInit(getApplicationContext());
        startRunners();
    }

    @Override
    public void onDestroy() {
        stopRunners();
        taskRunners = new TaskRunner[0];
        serviceStarted.set(false);
    }

    private void startRunners() {
        taskRunners = new TaskRunner[Utils.getNumCores()];
//        taskRunners = new TaskRunner[1];

        for(int i = 0; i < taskRunners.length; i++) {
            taskRunners[i] = new TaskRunner();
            taskRunners[i].start();
        }
    }

    private void stopRunners() {
        for(TaskRunner tr : taskRunners) {
            tr.stopRunning();
        }
    }

    public static void setRunnersPaused(boolean b) {
        for(TaskRunner tr : taskRunners) {
            tr.setPaused(b);
        }
    }

    public static void runnersCheckPaused() {
        for(TaskRunner tr : taskRunners) {
            tr.checkPause();
        }
    }

    public static TaskRunner[] taskRunners = new TaskRunner[0];

    public class TaskRunner extends Thread {

        private AtomicBoolean running = new AtomicBoolean(true);
        private AtomicBoolean paused = new AtomicBoolean(false);
        private Object syncObject = new Object();

        public void stopRunning() {
            running.set(false);
            setPaused(false);
        }

        public void setPaused(boolean p) {
            paused.set(p);
            synchronized(syncObject) {
                syncObject.notify();
            }
        }

        public boolean shouldPause() {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean pluggedIn =  plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;

            return !(/*mWifi.isConnected() && pluggedIn && */Storage.getInstance().getRunning());
        }

        public void checkPause() {
            setPaused(shouldPause());
        }

        public void run() {

            while(running.get()) {
                checkPause();

                while(paused.get()) {
                    synchronized (syncObject) {
                        try {
                            syncObject.wait();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                if(running.get()) {
                    downloadAndRunTask();
                }
            }
        }

        private void downloadAndRunTask() {
            Job taskData = null;
            try {

                if(Storage.getInstance().sageToken == null || Storage.getInstance().sageToken.isEmpty()) {
                    refreshToken();
                }

                if("-1".equals(Storage.getInstance().nodeId)) {
                    checkAndroidNode();
                }

                taskData = getNextReady();
                if(taskData == null) {
                    Storage.getInstance().exceptionString = "";
                    checkLater(5);
                    return;
                }

                byte[] result = taskData.runSageTask();
                taskData.setResult(result);
                taskData.status = Job.JobStatus.DONE;
                taskData.data = null;
                submitJob(taskData);
                Storage.getInstance().addToBounty(taskData.bounty);
                Storage.getInstance().exceptionString = "";
            } catch(Exception e) {
                e.printStackTrace();
                Storage.getInstance().exceptionString = e.toString();
                Storage.getInstance().sageToken = "";
                Storage.saveStorage();
                checkLater(10);
            }
        }

        private Timer wakeTimer = new Timer(true);
        private void checkLater(int seconds) {
            wakeTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    checkPause();
                }
            }, seconds*1000);

            synchronized (syncObject) {
                try {
                    syncObject.wait();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void refreshToken() throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/sageTokens");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            conn.connect();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            OutputStream os = conn.getOutputStream();
            IOUtils.write(gson.toJson(new GoogleToken()), os);
            os.close();

            InputStream is = conn.getInputStream();

            SageToken st = gson.fromJson(new InputStreamReader(is), SageToken.class);
            Storage.getInstance().sageToken = st.sageTokenStr;
            Storage.saveStorage();
        }

        private Job getNextReady() throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/jobs/nextReady/" + Storage.getInstance().nodeId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

            conn.connect();

            InputStream is = conn.getInputStream();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.fromJson(new InputStreamReader(is), Job.class);
        }

        private void sendAndroidNode() throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/androidNodes");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

            conn.connect();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            OutputStream os = conn.getOutputStream();
            IOUtils.write(gson.toJson(new AndroidNode()), os);
            os.close();

            InputStream is = conn.getInputStream();
            String id = IOUtils.toString(is);

            Storage.getInstance().nodeId = id;
            Storage.saveStorage();
        }

        private void checkAndroidNode() throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/androidNodes");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

            conn.connect();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            InputStream is = conn.getInputStream();

            AndroidNode[] nodes = gson.fromJson(new InputStreamReader(is), AndroidNode[].class);

            for(AndroidNode node : nodes) {
                if(AndroidNode.getUUID().equals(node.androidId)) {
                    Storage.getInstance().nodeId = node.nodeId;
                    Storage.saveStorage();
                    return;
                }
            }

            sendAndroidNode();
        }

        private void submitJob(Job job) throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/jobs");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

            conn.connect();

            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            OutputStream os = conn.getOutputStream();
            IOUtils.write(gson.toJson(job), os);
            os.close();

            InputStream is = conn.getInputStream();
            String resp = IOUtils.toString(is);

            Log.d("DEBUG", resp);
        }
    }

}
