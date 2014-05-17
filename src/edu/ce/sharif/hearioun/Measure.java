package edu.ce.sharif.hearioun;


import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import edu.ce.sharif.hearioun.database.PrefManager;
import edu.ce.sharif.hearioun.signalProcessing.ImageProcessing;
import edu.ce.sharif.hearioun.signalProcessing.SignalProcess;
import graphicComponents.BreathingCanvas;
import graphicComponents.ECGCanvas;

public class Measure extends Activity {
	
	private PrefManager prefManager=null;

	//for beating sound
	private MediaPlayer beatingSound;
	private float MAX_VOLUME=7;
	private float volume_progress=4;
	private float volume = (float) (1 - (Math.log(MAX_VOLUME - volume_progress) / Math.log(MAX_VOLUME)));
	
	
	SignalProcess signalProcess=null;

	static int HR=0, BR=0;
	private static Camera camera=null;

	//private View image = null;
	//private TextView text = null;

	private static final AtomicBoolean processing = new AtomicBoolean(true);

	private int averageIndex = 0;
	private static final int averageArraySize = 4;
	private static final int[] averageArray = new int[averageArraySize];
	
	private boolean autoStop=true;
	private CheckBox autoStop_cb;
	
	

	/**********			START drawing beating graph			***********/
	ECGCanvas canvas;
    LinearLayout drawingGraph;
	
	/**********			END drawing beating graph			***********/

    /**********			START drawing breathing graph			***********/
	BreathingCanvas breathingCanvas;
    LinearLayout breathingGraph;
	
	/**********			END drawing breathing graph			***********/

	/**********			START acquiring the signal			***********/
	private boolean STARTING_NOISE=true;
	private int CUT_START_SECONDS=2;//(s) Initial signal period to cut off
	
	boolean SIGNAL_ACQUIRED=false;
	int SIGNAL_IND=0;
	int SIGNAL[];
	public static double SIGNAL_SECONDS=10.0;//changed from 6
	//as long as 10 seconds in the beginning
	static double SIGNAL_FPS=8.5; //9 // 11 previously //23.5 old phone
	static int SIGNAL_SIZE=(int) (SIGNAL_FPS*SIGNAL_SECONDS); //instead of 66
	
	//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
	private long start_time, end_time;

	public void resetSignal(){
		SIGNAL=new int[SIGNAL_SIZE];
		SIGNAL_ACQUIRED=false;
		SIGNAL_IND=0;
		progress=0;
		//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
		start_time=System.currentTimeMillis();
		
		//reset graphical components
		beating=false;
		canvas=new ECGCanvas();
		breathingCanvas=new BreathingCanvas(signalProcess.makeBreathingSignal(SIGNAL));
		
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
			//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
			end_time=System.currentTimeMillis();
			SIGNAL_SECONDS=(end_time-start_time)/1000.0;
		}else if (SIGNAL_IND==1)
			start_time=System.currentTimeMillis();
	}
	
	private double myMin(double d1, double d2){
		if(d1<d2)
			return d1;
		return d2;
	}
	
	public MyPoint processSignal(){
		MyPoint res= new MyPoint();
		TextView tv=(TextView) findViewById(R.id.progressText);
		autoStop=autoStop_cb.isChecked();
		if (progress>=100){
			tv.setVisibility(View.INVISIBLE);
			tv.setText("Done!");
			//if still measuring, toggles as if the stop button is clicked
			if(autoStop && progress==100){
				myStop();
				progress=101;
			}
		}
		else{
			if(STARTING_NOISE){
				tv.setText(getString(R.string.initializing));
				tv.setVisibility(View.VISIBLE);
			}
			else
				tv.setText((int) (myMin(((100-progress)*myMin(SIGNAL_SECONDS,10)/100)+1,10))+" "+getString(R.string.seconds_left));
		}
		
		if(SIGNAL_ACQUIRED){
			int [] input_singal=new int[SIGNAL_SIZE];
			for(int i=SIGNAL_IND;i<SIGNAL_SIZE;i++)
				input_singal[i-SIGNAL_IND]=SIGNAL[i];
			for(int i=0;i<SIGNAL_IND;i++)
				input_singal[i+SIGNAL_SIZE-SIGNAL_IND]=SIGNAL[i];
			signalProcess=new SignalProcess(input_singal, SIGNAL_FPS);
			res.val1= signalProcess.computeWithPeakMeasurement();
			res.val2= signalProcess.computeBRWithPeakMeasurement();
		}
		return res;
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
            	ImageButton startButton=(ImageButton) Measure.this.findViewById(R.id.startButton);
            	ImageButton stopButton=(ImageButton) Measure.this.findViewById(R.id.stopButton);
        		if(progress<100){
        			startButton.setEnabled(false);
        			startButton.setBackgroundResource(R.drawable.startd);
        			stopButton.setEnabled(false);
        			stopButton.setBackgroundResource(R.drawable.stopd);
        		}else{
        			if(processing.get()){
        				startButton.setEnabled(true);
        				startButton.setBackgroundResource(R.drawable.start);
        				stopButton.setEnabled(false);
        				stopButton.setBackgroundResource(R.drawable.stopd);
        			}
        			else{
        				stopButton.setEnabled(true);
        				stopButton.setBackgroundResource(R.drawable.stop);
        				startButton.setEnabled(false);
        				startButton.setBackgroundResource(R.drawable.startd);
        			}
        		}
                
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

	private final static int NEEDLE_ARRAY_SIZE=13;
	private static int needleDX[]=new int[NEEDLE_ARRAY_SIZE];
	private Matrix needleMatrix;
	private int needleIndex=0;
	private int paperIndex=0;
	Path ECGLine;


	private void initializeBeat(){
		int index=0;
		needleDX[index++]=0;
		needleDX[index++]=-2;
		needleDX[index++]=-2;
		needleDX[index++]=-0;
		needleDX[index++]=-0;
		needleDX[index++]=5;
		needleDX[index++]=-25;
		needleDX[index++]=4;
		needleDX[index++]=0;
		needleDX[index++]=0;
		needleDX[index++]=-5;
		needleDX[index++]=-5;
		needleDX[index++]=0;
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_measure);
		
		//setting the font
		
		Typeface font_fa = Typeface.createFromAsset(getAssets(), "fonts/bnazanin.ttf");
		
		//all text views
		TextView tmp=(TextView)findViewById(R.id.TextViewHR);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.TextViewHRAmount);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.TextViewBR);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.TextViewBRAmount);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.progressText);
		tmp.setTypeface(font_fa);
		//checkboxes
		CheckBox tmp2=(CheckBox)findViewById(R.id.autoStopCheckBox);
		tmp2.setTypeface(font_fa);


		//reading from DB
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();

		//initializing beating volume to half
		beatingSound = MediaPlayer.create(this, R.raw.beat); 
		beatingSound.setVolume(volume, volume);
		
		ImageButton stop=(ImageButton) findViewById(R.id.stopButton);
		stop.setEnabled(false);
		stop.setBackgroundResource(R.drawable.stopd);
		
	

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

		BitmapDrawable bitmapDrawable  = new BitmapDrawable(getResources(),BitmapFactory.decodeResource(getResources(), R.drawable.paper_all0));
		ECGPainter.setImageDrawable(bitmapDrawable);
		paperIndex++;

		ECGPainter.invalidate();

		
		//initializing graphical components
		canvas=new ECGCanvas();
	    drawingGraph = (LinearLayout) findViewById(R.id.ECGView); 
	    drawingGraph.setBackgroundDrawable(canvas.drawECG());
	    
	    
	    breathingCanvas=new BreathingCanvas(signalProcess.makeBreathingSignal(SIGNAL));
	    breathingGraph = (LinearLayout) findViewById(R.id.breathingView);
	    breathingGraph.setBackgroundDrawable(breathingCanvas.drawBreathing());
	    

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



			if(!STARTING_NOISE){
				canvas.isBeating=beating;
				BitmapDrawable tmp=canvas.drawECG();
				drawingGraph.setBackground(tmp);
				
				breathingCanvas.signal=signalProcess.makeBreathingSignal(SIGNAL);
				breathingCanvas.isDrawing=!processing.get();
				BitmapDrawable tmp2=breathingCanvas.drawBreathing();
				breathingGraph.setBackground(tmp2);
			}
			
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

			/************			START processing signal 					**********/
			int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
			//int imgAvg = ImageProcessing.getRedAvg(data.clone(), height, width);
			addToSignal(imgAvg);
			MyPoint res=processSignal();
			HR=res.val1;
			TextView hr=(TextView)findViewById(R.id.TextViewHRAmount);
			hr.setText(HR+"");
			
			BR=res.val2;
			TextView br=(TextView)findViewById(R.id.TextViewBRAmount);
			br.setText(BR+"");
			/************			END processing signal 						**********/



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

			processing.set(false);

			/************			END animating heart beat					**********/

		}
	};

	private void beat_on(){
		//if in the initialization mode, no need to draw
		if(STARTING_NOISE)
			return;
		if(needleIndex!=0)
			return;
		direction=-1;
		beat();
		beatingSound = MediaPlayer.create(this, R.raw.beat);
		beatingSound.setVolume(volume, volume);
		beatingSound.start(); 
		//lastBeatTime=System.currentTimeMillis();
		beating=true;

	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			if(volume_progress<MAX_VOLUME){
				volume_progress++;
				volume = (float) (1 - (Math.log(MAX_VOLUME - volume_progress) / Math.log(MAX_VOLUME)));
				return true;
			}
		}
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			if(volume_progress>0){
				volume_progress--;
				volume = (float) (1 - (Math.log(MAX_VOLUME - volume_progress) / Math.log(MAX_VOLUME)));
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
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

		if(beating && needleIndex>=2){
				direction=0;
				beat();
		}
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
		
		ImageButton startButton=(ImageButton) Measure.this.findViewById(R.id.startButton);
		ImageButton stopButton=(ImageButton) Measure.this.findViewById(R.id.stopButton);
		startButton.setEnabled(true);
		startButton.setBackgroundResource(R.drawable.start);
    	stopButton.setEnabled(false);
    	stopButton.setBackgroundResource(R.drawable.stopd);
    	
		ImageView still = (ImageView) Measure.this.findViewById(R.id.progressBarStill);
		
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
		builder.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dlg,
					int sumthin) {
				ImageButton start = (ImageButton) Measure.this.findViewById(R.id.startButton);
				ImageButton stop = (ImageButton) Measure.this.findViewById(R.id.stopButton);
				start.setEnabled(true);
				start.setBackgroundResource(R.drawable.start);
				stop.setEnabled(false);
				stop.setBackgroundResource(R.drawable.stopd);
				
				
				prefManager=new PrefManager(Measure.this);
				prefManager.loadSavedPreferences();
				
				prefManager.savePreferencesIntArray("HR", HR);
				prefManager.savePreferencesIntArray("BR", BR);
			}
		});

		final AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void myStart(){
		
		ImageButton stopButton=(ImageButton) Measure.this.findViewById(R.id.stopButton);
    	stopButton.setEnabled(false);
    	stopButton.setBackgroundResource(R.drawable.stopd);
    	
    	
		ImageView still = (ImageView) Measure.this.findViewById(R.id.progressBarStill);
		
		still.setVisibility(View.INVISIBLE);
		progressBar.setVisibility(View.VISIBLE);
		
		new Thread(progressRunnable).start();
		
		resetSignal();
		STARTING_NOISE=true;
		final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		camera.setParameters(p);
		processing.set(false);
	}
	
	private void myReset(){
		ImageButton startButton=(ImageButton) Measure.this.findViewById(R.id.startButton);
		ImageButton stopButton=(ImageButton) Measure.this.findViewById(R.id.stopButton);
		startButton.setEnabled(true);
		startButton.setBackgroundResource(R.drawable.start);
    	stopButton.setEnabled(false);
    	stopButton.setBackgroundResource(R.drawable.stopd);
    	
		ImageView still = (ImageView) Measure.this.findViewById(R.id.progressBarStill);
		
		still.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.INVISIBLE);
		
		final Parameters p = camera.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_OFF);
		camera.setParameters(p);
		processing.set(true);
		
		resetSignal();
		
		STARTING_NOISE=true;
		
		TextView tv=(TextView) findViewById(R.id.progressText);
		tv.setText(getString(R.string.press_start));
		
		canvas=new ECGCanvas();
	    drawingGraph = (LinearLayout) findViewById(R.id.ECGView); 
	    drawingGraph.setBackgroundDrawable(canvas.drawECG());
	    
	    breathingCanvas=new BreathingCanvas(signalProcess.makeBreathingSignal(SIGNAL));
	    breathingGraph = (LinearLayout) findViewById(R.id.breathingView); 
	    breathingGraph.setBackgroundDrawable(breathingCanvas.drawBreathing());  
	}

	public void startClicked(View v){
		myStart();
	}
	
	public void stopClicked(View v){
		myStop();

	}
	
	public void resetClicked(View v){
		myReset(); 
	}
	
	

	class MyPoint{
		int val1, val2;
		MyPoint(){
			val1=0;
			val2=0;
		}
		MyPoint(int _val1, int _val2){
			val1=_val1;
			val2=_val2;
		}
	}
}
