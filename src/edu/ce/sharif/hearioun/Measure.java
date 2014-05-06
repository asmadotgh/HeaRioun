package edu.ce.sharif.hearioun;


import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.ce.sharif.hearioun.signalProcessing.ImageProcessing;
import edu.ce.sharif.hearioun.signalProcessing.SignalProcess;

public class Measure extends Activity {

	//FOR DEBUG
	/*long alaki_time=0;
	int alaki_frame;*/
	
	SignalProcess signalProcess=null;

	static int HR=0, HRV=0;
	private static Camera camera=null;

	//private View image = null;
	//private TextView text = null;

	private static final AtomicBoolean processing = new AtomicBoolean(true);

	private int averageIndex = 0;
	private static final int averageArraySize = 4;
	private static final int[] averageArray = new int[averageArraySize];
	
	private boolean autoStop=true;
	private CheckBox autoStop_cb;
	
	



	/**********			START acquiring the signal			***********/
	private boolean STARTING_NOISE=true;
	private int CUT_START_SECONDS=2;//(s) Initial signal period to cut off
	
	boolean SIGNAL_ACQUIRED=false;
	int SIGNAL_IND=0;
	int SIGNAL[];
	public static int SIGNAL_SECONDS=10;//changed from 6
	//as long as 10 seconds in the beginning
	static int SIGNAL_SIZE=90; //instead of 66
	static double SIGNAL_FPS=8.5; //9 // 11 previously //23.5 old phone

	public void resetSignal(){
		SIGNAL=new int[SIGNAL_SIZE];
		SIGNAL_ACQUIRED=false;
		SIGNAL_IND=0;
		progress=0;
	}
	public void addToSignal(int inp){
		SIGNAL_IND=(SIGNAL_IND+1)%SIGNAL_SIZE;
		if(STARTING_NOISE && SIGNAL_IND==((int)SIGNAL_FPS*CUT_START_SECONDS)){
			STARTING_NOISE=false;
			resetSignal();
			return;
		}
		SIGNAL[SIGNAL_IND]=inp;
		if(SIGNAL_IND==0){
			SIGNAL_ACQUIRED=true;
		}
	}
	public int processSignal(){
		TextView tv=(TextView) findViewById(R.id.progressText);
		autoStop=autoStop_cb.isChecked();
		if (progress>=100){
			tv.setText("Done!");
			//if still measuring, toggles as if the finish button is clicked
			if(autoStop && progress==100){
				myStop();
				progress=101;
			}
		}
		else{
			if(STARTING_NOISE)
				tv.setText("Initializing...");
			else
				tv.setText((100-progress)*SIGNAL_SECONDS/100+1+" seconds left...");
		}
		
		if(SIGNAL_ACQUIRED){
			int [] input_singal=new int[SIGNAL_SIZE];
			for(int i=SIGNAL_IND;i<SIGNAL_SIZE;i++)
				input_singal[i-SIGNAL_IND]=SIGNAL[i];
			for(int i=0;i<SIGNAL_IND;i++)
				input_singal[i+SIGNAL_SIZE-SIGNAL_IND]=SIGNAL[i];
			signalProcess=new SignalProcess(input_singal, SIGNAL_FPS);
			return signalProcess.computeWithPeakMeasurement();
		}
		return 0;
	}

	/**********			END acquiring the signal			***********/


	/**********			START progress bar					**********/
	
	ProgressBar progressBar;
	int progress=0;
	
	private Runnable progressRunnable = new Runnable()
    { 
        @Override
        public void run() 
        {
        	hnd.sendMessage(hnd.obtainMessage());
            while (progress < 100)
            {
            	progressBar.setEnabled(true);
            	if(SIGNAL_ACQUIRED)
            		progress=100;
            	else
            		progress=100*SIGNAL_IND/SIGNAL_SIZE;
             }
            hnd.sendMessage(hnd.obtainMessage());

        }
        Handler hnd = new Handler()
        {    
            @Override
            public void handleMessage(Message msg) 
            {
            	Button confirmButton=(Button) Measure.this.findViewById(R.id.ButtonStart);
        		if(progress<100)
        			confirmButton.setEnabled(false);
        		else
        			confirmButton.setEnabled(true);
                
            }
        };
    };
    /**********			END progress bar					**********/
    
	public static enum TYPE {
		GREEN, RED
	};

	private static TYPE currentType = TYPE.GREEN;

	public static TYPE getCurrent() {
		return currentType;
	}

	private static int beatsIndex = 0;
	private static final int beatsArraySize = 3;
	private static final int[] beatsArray = new int[beatsArraySize];
	private static double beats = 0;
	private static long startTime = 0;

	private Matrix beatMatrix;
	private int direction=-1;
	//private long lastBeatTime=0;
	private boolean beating=false;
	//private final static long BEAT_DELAY=15000;

	private final static int NEEDLE_ARRAY_SIZE=9;
	private static int needleDX[]=new int[NEEDLE_ARRAY_SIZE];
	private Matrix needleMatrix;
	private int needleIndex=0;
	private int paperIndex=0;
	Path ECGLine;


	private void initializeBeat(){
		int index=0;
		needleDX[index++]=0;
		needleDX[index++]=8;
		needleDX[index++]=0;
		needleDX[index++]=-16;
		needleDX[index++]=-26;
		needleDX[index++]=-16;
		needleDX[index++]=0;
		needleDX[index++]=4;
		needleDX[index++]=0;
		//System.out.println("index: "+index);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);

		//initializing progress bar
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		
		autoStop_cb= (CheckBox) findViewById(R.id.autoStopCheckBox);
		autoStop_cb.setChecked(autoStop);
		
		resetSignal();


		needleMatrix=new Matrix();
		//lastBeatTime=System.currentTimeMillis();
		beating=false;
		ECGLine=new Path();

		ImageView ECGPainter=(ImageView) findViewById(R.id.ECGLine);

		beatMatrix=new Matrix();
		//beatMatrix.setTranslate(0, 0);
		/*ECGPainter.setImageMatrix(needleMatrix);
		ECGPainter.invalidate();*/


		//for(int i=0;i<100;i++){
		//BitmapDrawable bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_graph));

		//}
		BitmapDrawable bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all0));
		ECGPainter.setImageDrawable(bitmapDrawable);
		paperIndex++;

		ECGPainter.invalidate();

		//HRPainter ECGPainter=(HRPainter) findViewById(R.id.ECGLine);



		//beatMatrix.set(getResources().getDrawable(R.drawable.beat));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.measure, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		camera=Camera.open();
		/*final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(p);*/
		camera.setPreviewCallback(previewCallback);
		camera.startPreview();
		initializeBeat();
	}

	public void onPause() {
		super.onPause();
		final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(p);
		camera.setPreviewCallback(null);
		camera.stopPreview();
		camera.release();
		camera = null;
	}



	private PreviewCallback previewCallback = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera cam) {
			
			//FOR DEBUG
			/*alaki_frame++;*/

			if (data == null) throw new NullPointerException();
			Camera.Size size = cam.getParameters().getPreviewSize();
			if (size == null) throw new NullPointerException();
			
			if (!processing.compareAndSet(false, true)) return;
			if(progress>100)
				return;

			drawNeelde();
			movePaper();
			int width = size.width;
			int height = size.height;

			/************			START processing signal with FFT			**********/
			int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
			//int imgAvg = ImageProcessing.getRedAvg(data.clone(), height, width);
			addToSignal(imgAvg);
			HR=processSignal();
			TextView tmp=(TextView)findViewById(R.id.TextViewHRAmount);
			tmp.setText(HR+"");
			/************			END processing signal with FFT				**********/



			/************			START animating heart beat					**********/
			imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
			if (imgAvg == 0 || imgAvg == 255) {
				processing.set(false);
				return;
			}

			int averageArrayAvg = 0;
			int averageArrayCnt = 0;
			for (int i = 0; i < averageArray.length; i++) {
				if (averageArray[i] > 0) {
					averageArrayAvg += averageArray[i];
					averageArrayCnt++;
				}
			}

			int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;
			TYPE newType = currentType;
			if (imgAvg < rollingAverage) {
				newType = TYPE.RED;
				if (newType != currentType) {
					/*beats++;
                    HR++;
                    TextView tmp=(TextView)findViewById(R.id.TextViewHRAmount);
                    tmp.setText(HR+"");*/
					beat_on();
				}
			} else if (imgAvg > rollingAverage) {
				newType = TYPE.GREEN;
			}
			if (averageIndex == averageArraySize) averageIndex = 0;
			averageArray[averageIndex] = imgAvg;

			averageIndex++;

			// Transitioned from one state to another to the same
			if (newType != currentType) {
				currentType = newType;
				//image.postInvalidate();
			}

			/*long endTime = System.currentTimeMillis();
			double totalTimeInSecs = (endTime - startTime) / 1000d;
			if (totalTimeInSecs >= 10) {
				double bps = (beats / totalTimeInSecs);
				int dpm = (int) (bps * 60d);
				if (dpm < 30 || dpm > 180) {
					startTime = System.currentTimeMillis();
					beats = 0;
					processing.set(false);
					return;
				}


				if (beatsIndex == beatsArraySize) beatsIndex = 0;
				beatsArray[beatsIndex] = dpm;
				beatsIndex++;

				
				startTime = System.currentTimeMillis();
				beats = 0;
			}*/
			processing.set(false);

			/************			END animating heart beat					**********/

		}
	};

	private void beat_on(){
		direction=-1;
		beat();
		MediaPlayer sound = MediaPlayer.create(this, R.raw.beat); 
		sound.start();  
		//lastBeatTime=System.currentTimeMillis();
		beating=true;

	}
	private void beat_off(){
		direction=0;
		beat();
		beating=false;
	}
	private void beat(){
		//animating the beating heart
		ImageView tmp=(ImageView)findViewById(R.id.imageViewBeat);
		beatMatrix.setTranslate(0,59*direction);
		tmp.setImageMatrix(beatMatrix);
		tmp.invalidate();
	}
	private void drawNeelde(){
		ECGLine.lineTo(0, needleDX[needleIndex]);
		ImageView needle=(ImageView) findViewById(R.id.imageViewNeedle);
		needleMatrix.setTranslate(0, needleDX[needleIndex]);
		needle.setImageMatrix(needleMatrix);
		needle.invalidate();
		if(beating && needleIndex<NEEDLE_ARRAY_SIZE-1){
			needleIndex++;
		}
		else {
			needleIndex=0;
			if(beating)
				beat_off();
		}
	}

	public void movePaper(){
		ImageView ECGPainter=(ImageView) findViewById(R.id.ECGLine);
		BitmapDrawable bitmapDrawable=null;
		switch(paperIndex){
		case 1:
			bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all1));
			break;
		case 2:
			bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all2));
			break;
		case 3:
			bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all3));
			break;
		case 4:
			bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all4));
			break;
		}
		paperIndex++;
		if(paperIndex==5)
			paperIndex=3;

		ECGPainter.setImageDrawable(bitmapDrawable);

	}
	
	private void myStop(){
		ImageView still = (ImageView) Measure.this.findViewById(R.id.progressBarStill);
		//FOR DEBUG
		/*long alaki_time2=System.currentTimeMillis();
		long zaman=(alaki_time2-alaki_time)/1000;
		System.out.println("zaman: "+zaman);
		System.out.println("tedade frame ha: "+alaki_frame);
		System.out.println("fps: "+alaki_frame/zaman);*/
		
		still.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
		
		final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(p);
		processing.set(true);
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		TextView myMsg = new TextView(this);
		myMsg.setText("TODO:\n\nDecision tree here. is the person:\n\n*Healthy?\n*Needs exercise?\n*At risk?\n*etc.");
		myMsg.setTextSize(20);
		myMsg.setPadding(10, 0, 0, 5);
		myMsg.setGravity(Gravity.LEFT);
		builder.setView(myMsg);
		builder.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg,
					int sumthin) {
				Button confirmButton = (Button) Measure.this.findViewById(R.id.ButtonStart);
				confirmButton.setText("Start");
				//processing.set(false);
			}
		});

		final AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void myStart(){
		ImageView still = (ImageView) Measure.this.findViewById(R.id.progressBarStill);
		//FOR DEBUG
		/*alaki_time=System.currentTimeMillis();*/
		
		still.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		
		new Thread(progressRunnable).start();
		Button confirmButton = (Button) Measure.this.findViewById(R.id.ButtonStart);
		confirmButton.setText("Finish");
		
		resetSignal();
		STARTING_NOISE=true;
		final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(p);
		processing.set(false);
		
	}

	public void interpretMessage(View v){
		///////		DONE
		if(!processing.get()){
			myStop();
			//////		START
		}else{
			myStart();
		}
	}

}
