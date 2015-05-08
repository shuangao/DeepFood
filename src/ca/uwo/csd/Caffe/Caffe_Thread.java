package ca.uwo.csd.Caffe;

import org.opencv.core.Mat;

import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ListView;

public class Caffe_Thread implements Runnable{


	private CaffeMobile caffeMobile;
	private Mat imageMat;
	private ListView lv;
	private Boolean doneBoolean;
	public CaffeMobile getCaffeMobile() {
		return caffeMobile;
	}
	public void setCaffeMobile(CaffeMobile caffeMobile) {
		this.caffeMobile = caffeMobile;
	}
	public Mat getImageMat() {
		return imageMat;
	}
	public void setImageMat(Mat imageMat) {
		this.imageMat = imageMat;
	}
	public ListView getLv() {
		return lv;
	}
	public void setLv(ListView lv) {
		this.lv = lv;
	}
	private static String path = Environment.getExternalStorageDirectory().getPath()
			+ "/foodrecg/model/";
	private int label_index;

	public void run() {
		
	}
	

}
