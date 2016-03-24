package com.sage.sage_android.data;

import android.util.Base64;

import com.sage.task.SageTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;

/**
 * Created by cyberpirate on 3/16/2016.
 */
public class Task {
    public int jobId;
    public int ordererId;
    public JobStatus status;
    public String encodedDex;
    public String data;
    public int timeout;

    private DecodedDex decodedDex;
    public DecodedDex getDecodedDex() {
        if(decodedDex == null) decodedDex = new DecodedDex(encodedDex);
        return decodedDex;
    }

    public class DecodedDex {

        public String name;
        public byte[] dexData;

        private DecodedDex(String encodedDex) {
            String[] parts = encodedDex.split("\\.");

            name = new String(Base64.decode(parts[0], Base64.DEFAULT));
            dexData = Base64.decode(parts[1], Base64.DEFAULT);
        }

        public byte[] runSageTask() throws Exception {

            File dexFile = new File(Storage.context.getCodeCacheDir(), name + ".dex");

            OutputStream os = new FileOutputStream(dexFile);
            os.write(dexData);
            os.close();

            PathClassLoader pcl = new PathClassLoader(dexFile.getPath(), ClassLoader.getSystemClassLoader());
            Class taskClass = pcl.loadClass(name);
            Object sageTask = taskClass.getConstructors()[0].newInstance();
            Method m = taskClass.getMethod("runTask", long.class, byte[].class);
            return (byte[]) m.invoke(sageTask, jobId, getData());

        }
    }

    private byte[] decodedData;
    public byte[] getData() {
        if(decodedData == null) decodedData = Base64.decode(data, Base64.DEFAULT);
        return decodedData;
    }

    public enum JobStatus {
        READY,
        RUNNING,
        DONE,
        ERROR,
        TIMED_OUT
    }
}
