package ca.uwo.csd.Caffe;

import org.opencv.core.Mat;

import android.util.Log;
public class CaffeMobile {
    public native void enableLog(boolean enabled);
    public native int loadModel(String modelPath, String weightsPath);
    public native int predictImage(String imgPath);
    public native float[] predictProb(long img_addr);
    static {
		Log.i("LB","Loading");
        System.loadLibrary("caffe");
        System.loadLibrary("my_caffe");
        Log.i("LB","Load OK");
    }
}