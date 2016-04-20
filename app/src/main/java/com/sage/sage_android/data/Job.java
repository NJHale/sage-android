package com.sage.sage_android.data;

import android.util.Base64;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

import dalvik.system.PathClassLoader;

/**
 * Created by cyberpirate on 3/16/2016.
 */
public class Job {
    public int jobId;
    public int ordererId;
    public int nodeId;
    public Date completion;
    public JobStatus status;
    public String data;
    public long timeout;
    public BigDecimal bounty;
    public String result;
    public int javaId;

    private transient byte[] decodedData;
    public byte[] getData() {
        if(decodedData == null) decodedData = Base64.decode(data, Base64.DEFAULT);
        return decodedData;
    }

    public void setResult(byte[] r) {
        result = Base64.encodeToString(r, Base64.NO_WRAP);
    }

    public enum JobStatus {
        READY,
        RUNNING,
        DONE,
        ERROR,
        TIMED_OUT
    }

    private void downloadDex(File dexFile) throws IOException {
        URL url = new URL("http://sage-ws.ddns.net:8080/sage-bison/javas/" + javaId + "/dex");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("SageToken", Storage.getInstance().sageToken);

        conn.connect();

        InputStream is = conn.getInputStream();
        String encodedDex = IOUtils.toString(is);
        is.close();

        String[] parts = encodedDex.split("\\.");

        String name = new String(Base64.decode(parts[0], Base64.DEFAULT));
        byte[] dexData = Base64.decode(parts[1], Base64.DEFAULT);

        Storage.getInstance().jobClassNames.put(javaId, name);
        Storage.saveStorage();

        FileOutputStream fos = new FileOutputStream(dexFile);
        IOUtils.write(dexData, fos);
        fos.close();
    }

    private static transient Object syncObject = new Object();
    private static transient HashMap<Integer, Class> cachedCode = new HashMap<Integer, Class>();
    public byte[] runSageTask() throws Exception {

        Class taskClass;

        synchronized(syncObject) {
            if (!cachedCode.containsKey(javaId)) {
                //Check data on fs
                File dexFile = new File(Storage.context.getCodeCacheDir(), javaId + ".dex");

                if (!dexFile.exists()) {
                    downloadDex(dexFile);
                }

                PathClassLoader pcl = new PathClassLoader(dexFile.getPath(), ClassLoader.getSystemClassLoader());
                taskClass = pcl.loadClass(Storage.getInstance().jobClassNames.get(javaId));

                cachedCode.put(javaId, taskClass);
            }

            taskClass = cachedCode.get(javaId);
        }

        Object sageTask = taskClass.getConstructors()[0].newInstance();
        Method m = taskClass.getMethod("runTask", long.class, byte[].class);
        return (byte[]) m.invoke(sageTask, jobId, getData());
    }
}
