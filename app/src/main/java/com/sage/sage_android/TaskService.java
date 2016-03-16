package com.sage.sage_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sage.SageTask;
import com.sage.sage_android.data.AndroidNode;
import com.sage.sage_android.data.AppInit;
import com.sage.sage_android.data.Storage;
import com.sage.sage_android.data.Task;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cyberpirate on 3/5/2016.
 */
public class TaskService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        AppInit.appInit(getApplicationContext());
        taskRunner = new TaskRunner();
        taskRunner.start();
    }

    @Override
    public void onDestroy() {
        taskRunner.stopRunning();
        taskRunner = null;
    }

    TaskRunner taskRunner;

    private class TaskRunner extends Thread {

        private AtomicBoolean running = new AtomicBoolean(true);
        private AtomicBoolean paused = new AtomicBoolean(false);
        private Object syncObject = new Object();

        public void stopRunning() {
            running.set(false);
        }

        public void setPaused(boolean p) {
            paused.set(p);
            syncObject.notify();
        }

        public void run() {

            while(running.get()) {
                downloadAndRunTask();

                while(paused.get()) {
                    synchronized (syncObject) {
                        try {
                            syncObject.wait();
                        } catch(Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void downloadAndRunTask() {
            try {
                sendAndroidNode();
//                Task taskData = getNextReady();
//                Task.DecodedDex dex = taskData.getDecodedDex();
//
//                SageTask task = dex.getSageTask();
//                byte[] result = task.runTask(taskData.jobId, taskData.getData());
            } catch(Exception e) {
                e.printStackTrace();
            }

        }

        private Task getNextReady() throws IOException {
//            URL url = new URL("http://sage-ws.ddns.net:8080/sage/alpaca/jobs/nextReady/" + Storage.getInstance().androidId);
//
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
//            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);
//
//            conn.connect();

//            InputStream is = conn.getInputStream();

            Gson gson = new GsonBuilder().create();
//            return gson.fromJson(new InputStreamReader(is), Task.class);
            String json = "{\"jobId\":27,\"ordererId\":1,\"nodeId\":32,\"status\":\"RUNNING\",\"encodedDex\":\"Q2xhc3NXaXRoU2FnZVRhc2s=.ZGV4CjAzNQDvv71Z77+9fxgQY++/ve+/ve+/vU4D77+977+9cO+/vTfqsYXvv73vv70CAABwAAAAeFY0EgAAAAAAAAAANAIAAA4AAABwAAAABwAAAO+/vQAAAAMAAADvv70AAAAAAAAAAAAAAAQAAADvv70AAAABAAAACAEAAO+/vQEAACgBAABwAQAAeAEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAO+/vQEAAAICAAAGAgAAEAIAAAMAAAAFAAAABwAAAAgAAAAJAAAACgAAAAsAAAAKAAAABQAAAAAAAAAEAAAABgAAAAAAAAAGAAAABgAAAGgBAAABAAAAAAAAAAEAAgANAAAAAwAAAAAAAAAEAAEADAAAAAEAAAABAAAAAwAAAGABAAABAAAAAAAAACUCAAAAAAAAAQABAAEAAAAZAgAABAAAAHAQAgAAAA4ABQAEAAEAAAAeAgAABwAAABoAAgBuEAMAAAAMABEAAAABAAAAAgAAAAIAAAAAAAYABjxpbml0PgAWQ2xhc3NXaXRoU2FnZVRhc2suamF2YQALR2FyYmFnZURhdGEAAUoAAUwAE0xDbGFzc1dpdGhTYWdlVGFzazsAA0xKTAAYTGNvbS9zYWdlL3Rhc2svU2FnZVRhc2s7ABJMamF2YS9sYW5nL09iamVjdDsAEkxqYXZhL2xhbmcvU3RyaW5nOwABVgACW0IACGdldEJ5dGVzAAdydW5UYXNrAAYABw4ACAIAAAcOAAAAAQEA77+977+9BO+/vQIBAe+/vQIADAAAAAAAAAABAAAAAAAAAAEAAAAOAAAAcAAAAAIAAAAHAAAA77+9AAAAAwAAAAMAAADvv70AAAAFAAAABAAAAO+/vQAAAAYAAAABAAAACAEAAAEgAAACAAAAKAEAAAEQAAACAAAAYAEAAAIgAAAOAAAAcAEAAAMgAAACAAAAGQIAAAAgAAABAAAAJQIAAAAQAAABAAAANAIAAA==\",\"data\":\"U2FnZVRva2VuR2FyYmFnZQ==\",\"timeout\":1000000}";
            return gson.fromJson(json, Task.class);
        }

        private void sendAndroidNode() throws IOException {
            URL url = new URL("http://sage-ws.ddns.net:8080/sage/alpaca/jobs/nextReady/" + Storage.getInstance().androidId);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("GoogleToken", Storage.getInstance().googleToken);
            conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

            conn.connect();

            Gson gson = new GsonBuilder().create();
            OutputStream os = conn.getOutputStream();
            IOUtils.write(gson.toJson(new AndroidNode()), os);
            os.close();

            InputStream is = conn.getInputStream();
            String id = IOUtils.toString(is);

            Storage.getInstance().androidId = id;
            Storage.saveStorage();
        }
    }

}
