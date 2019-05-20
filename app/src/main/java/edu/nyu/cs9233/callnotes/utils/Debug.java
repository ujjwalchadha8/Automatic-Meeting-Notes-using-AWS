package edu.nyu.cs9233.callnotes.utils;

import android.util.Log;

/**
 * Created by ujjwalchadha8 on 12/4/2017.
 */

public class Debug {
    public static Object[] log(Object... objects){
        StringBuilder stringToPrint = new StringBuilder();
        for (Object object : objects) {
            stringToPrint.append(String.valueOf(object)).append(", ");
        }
        stringToPrint = new StringBuilder(stringToPrint.substring(0, stringToPrint.lastIndexOf(",")));
        Log.d("DEBUG:: ", stringToPrint.toString());
        return objects;
    }

    public static void throwMethodNotImplementedException(){
        throw new MethodNotImplementedException();
    }

    public static void throwMethodNotImplementedException(String message){
        throw new MethodNotImplementedException(message);
    }

    public static void crash(String message){
        throw new Error("DEBUG CRASH: " + message);
    }

    public static class MethodNotImplementedException extends RuntimeException {
        public MethodNotImplementedException() {}

        public MethodNotImplementedException(String message) {
            super(message);
        }

        public MethodNotImplementedException(String message, Throwable cause) {
            super(message, cause);
        }

        public MethodNotImplementedException(Throwable cause) {
            super(cause);
        }
    }
}
