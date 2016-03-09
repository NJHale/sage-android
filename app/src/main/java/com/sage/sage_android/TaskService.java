package com.sage.sage_android;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by cyberpirate on 3/5/2016.
 */
public class TaskService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
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
        private AtomicBoolean paused = new AtomicBoolean(true);
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
                //download and run a task

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
    }

}
