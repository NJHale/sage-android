package com.sage.sage_android.data;

import android.util.Log;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * Created by cyberpirate on 3/29/16.
 */
public class Utils {

    public static int getNumCores() {
        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if(Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            Log.d("DEBUG", "CPU Count: " + files.length);
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Print exception
            Log.d("DEBUG", "CPU Count: Failed.");
            e.printStackTrace();
            //Default to return 1 core
            return 1;
        }
    }
}
