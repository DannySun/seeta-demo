package com.sdf.seetademo.jni;

public class SeetaHandle {
    private static SeetaHandle instance = null;
    private SeetaHandle(){

    }
    public static SeetaHandle getInstance() {
        if (instance == null) {
            synchronized (SeetaHandle.class) {
                if (instance == null) {
                    instance = new SeetaHandle();
                }
            }
        }
        return instance;
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int initModels(String faceDetector, String landMarker, String agePredictor);
    public native int predictAge(String imageUrl);
}
