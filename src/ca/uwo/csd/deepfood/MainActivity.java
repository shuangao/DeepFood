package ca.uwo.csd.deepfood;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import ca.uwo.csd.Caffe.CaffeMobile;
import ca.uwo.csd.deepfood.CameraDrawView;

import com.example.deepfood.R;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.R.string;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends Activity implements CvCameraViewListener2,
OnTouchListener {

	private static String path = Environment.getExternalStorageDirectory().getPath()
			+ "/foodrecg/model/";
	private CameraDrawView mOpenCvCameraView;
	private static final String TAG = "MainActive";
	
	private boolean backclick = false;
	private long startBack;
	
	private ArrayAdapter<String> adapter;
	private List<String> data;
	private ListView lv;
	
	public static int onExtract=1;
	public static int ExtractDone=2;
	
	private static boolean food_mode = false;
	private static boolean mode_change = false;
	
	private ProgressDialog dialog;
	private Boolean on_extractBoolean = false;
	private static CaffeMobile caffeMobile;
	
	private Handler Eventhandler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg){
			if (msg.what == onExtract) {
				on_extractBoolean = true;
			} else if(msg.what == ExtractDone){
				ArrayList<String> data2 = msg.getData().getStringArrayList(
						"CLASSIFICATION");
				lv = (ListView) findViewById(R.id.MyListView);
				adapter = new ArrayAdapter<String>(MainActivity.this,
						R.layout.simple_text, data2);
				lv.setAdapter(adapter);
				if(food_mode & mode_change)//load food model
				{
					String weightsPath = path+"g101.caffemodel";
					String modelPath = path+"g101.prototxt";
					caffeMobile = new CaffeMobile();
					Log.i("Native", "LOADING Genreal MODEL");
					caffeMobile.loadModel(modelPath, weightsPath);
					data = MainActivity.readFromFile(path+"food.txt");
					Log.i(TAG, Integer.toString(data.size()));
					adapter = new ArrayAdapter<String>(MainActivity.this, 
							R.layout.simple_text, data);
					lv.setAdapter(adapter);
					mode_change = false;
				}
				else if (!food_mode & mode_change) {//load general model
					String weightsPath = path+"g.caffemodel";
					String modelPath = path+"g.prototxt";
					caffeMobile = new CaffeMobile();
					Log.i("Native", "LOADING Genreal MODEL");
					caffeMobile.loadModel(modelPath, weightsPath);
					data = MainActivity.readFromFile(path+"synset_words.txt");
					Log.i(TAG, Integer.toString(data.size()));
					adapter = new ArrayAdapter<String>(MainActivity.this, 
							R.layout.simple_text, data);
					lv.setAdapter(adapter);
					mode_change = false;
				}
				on_extractBoolean = false;
			}
		}
	};
	
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				Log.i(TAG, "OpenCV loaded successfully");
				
				String weightsPath = path+"g.caffemodel";
				String modelPath = path+"g.prototxt";
				caffeMobile = new CaffeMobile();
				Log.i("Native", "LOADING MODEL");
				caffeMobile.loadModel(modelPath, weightsPath);
				
				mOpenCvCameraView.enableView();
				mOpenCvCameraView.setOnTouchListener(MainActivity.this);
				Log.i(TAG, "Loading model Done");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(TAG, "called onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Initialize();
		mOpenCvCameraView = (CameraDrawView) findViewById(R.id.camera_surface_view);

		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

		mOpenCvCameraView.setCvCameraViewListener(this);

		mOpenCvCameraView.assign(this);
		
//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		else if (id == R.id.general) {
			item.setChecked(true);	
			if (food_mode) {
				mode_change = true;
				food_mode = false;
			}
			
			return true;
		}else if (id == R.id.food) {	
			item.setChecked(true);
			if (!food_mode) {
				mode_change = true;
				food_mode = true;
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	
	private void Initialize()
	{
		lv = (ListView) findViewById(R.id.MyListView);
		data = new ArrayList<String>();
		data = this.readFromFile(path+"synset_words.txt");
		Log.i(TAG, Integer.toString(data.size()));
		adapter = new ArrayAdapter<String>(this, R.layout.simple_text, data);
		lv.setAdapter(adapter);
	}
	public static ArrayList<String> readFromFile(String FILENAME) {
        
		ArrayList<String> ret = new ArrayList<String>();
         
        try {
        	FileInputStream inputStream = new FileInputStream (new File(FILENAME));
             
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                 
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                	ret.add(receiveString);
                }
                 
                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
 
        return ret;
    }

@Override
public boolean onTouch(View arg0, MotionEvent arg1) {
	Log.i(TAG, "onTouch event");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	String currentDateandTime = sdf.format(new Date());
	String fileName = Environment.getExternalStorageDirectory().getPath()
			+ "/DeepFood/img/food_rec_" + currentDateandTime + ".jpg";
	mOpenCvCameraView.takePicture(fileName);
	Toast.makeText(this, fileName + " saved", Toast.LENGTH_SHORT).show();
	return false;
}

@Override
public void onCameraViewStarted(int width, int height) {
	// TODO Auto-generated method stub
	
}

@Override
public void onCameraViewStopped() {
	// TODO Auto-generated method stub
	
}
@Override
public void onResume() {
	super.onResume();
	OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this,
			mLoaderCallback);
}

@Override
public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	// TODO Auto-generated method stub
	Mat rgba = inputFrame.rgba();
	if(!this.on_extractBoolean)
	{
		Mat rgbMat = new Mat(rgba.rows(),rgba.cols(),rgba.type());
		Imgproc.cvtColor(rgba, rgbMat, Imgproc.COLOR_RGBA2RGB);
		pred_thread thread = new pred_thread(caffeMobile, rgbMat);
		new Thread(thread).start();
	}
	return rgba;
}
private class pred_thread implements Runnable{
	private CaffeMobile model;
	private Mat imageMat;
	private float[] probs;
	
	public pred_thread(CaffeMobile caffeMobile, Mat imageMat) {
		super();
		this.model = caffeMobile;
		this.imageMat = imageMat;
		Message msg = Message.obtain();
		msg.what = onExtract;
		Eventhandler.sendMessage(msg);
//		on_extractBoolean = true;
		Log.i(TAG, "Prediction Start");
	}
	private ArrayList<String> sortByValue(HashMap<String, Float> map) {
		List list = new ArrayList<>(map.entrySet());
		// Defined Custom Comparator here
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o2)).getValue())
						.compareTo(((Map.Entry) (o1)).getValue());
			}
		});
		ArrayList<String> resultArrayList = new ArrayList<String>();
		for (Iterator it = list.iterator(); it.hasNext();) {
			DecimalFormat df = new DecimalFormat("#.##"); 
			Map.Entry entry = (Map.Entry) it.next();
//			resultArrayList.add((String) entry.getKey()+
//					"_"+df.format((float)entry.getValue()));
			resultArrayList.add((String) entry.getKey());
			// Log.i("result rank: ", entry.getKey()+" "+entry.getValue());
		}
		return resultArrayList;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Imgproc.resize( imageMat, imageMat, new Size(256, 256));
		Log.i(TAG, "Image size: "+ this.imageMat.size());
		probs = caffeMobile.predictProb(imageMat.getNativeObjAddr());
		Log.i(TAG, "Prediction OK");
		
		HashMap<String, Float> Name2Prob = new HashMap<String, Float>();
		for (int i = 0; i < probs.length; i++) {
			Name2Prob.put(MainActivity.this.data.get(i), probs[i]);
		}
		ArrayList<String> resultArrayList = sortByValue(Name2Prob);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("CLASSIFICATION", resultArrayList);
		Message msg = Message.obtain();
		msg.what = ExtractDone;
		msg.setData(bundle);
		Eventhandler.sendMessage(msg);
//		on_extractBoolean = false;
	}
	
}
public void onBackPressed() {
	if (!backclick) {
		backclick = true;
		startBack = System.currentTimeMillis();
		Toast.makeText(getApplicationContext(), "Click again to exit",
				Toast.LENGTH_LONG).show();
	} else {
		Long now_timeLong = System.currentTimeMillis();
		if (now_timeLong - startBack <= 5000) {
			finish();
			moveTaskToBack(true);
			System.exit(0);
		}
		backclick = false;
		startBack = System.currentTimeMillis();
		Toast.makeText(getApplicationContext(), "Click again to exit",
				Toast.LENGTH_LONG).show();
	}

}
}
