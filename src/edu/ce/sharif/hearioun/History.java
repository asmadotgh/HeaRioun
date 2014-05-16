package edu.ce.sharif.hearioun;


import java.text.DecimalFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import edu.ce.sharif.hearioun.database.PrefManager;

public class History extends Activity implements OnTouchListener{

	private PrefManager prefManager=null;

	private XYPlot mySimpleXYPlot;
	private Button zoomOutButton, resetButton;
	private static SimpleXYSeries[] series = null;
	private PointF minXY=new PointF(0,0);
	private PointF maxXY=new PointF(0,0);
	
	private final static float	DEFAULT_MIN_X=0, DEFAULT_MAX_X=7, DEFAULT_MIN_Y=0, DEFAULT_MAX_Y=225;


	@Override
	protected void onResume() {
		super.onResume();
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();
		prefManager.updateHistory();
	}

	public void updateView(){
		if(series[0].size()==0){
			mySimpleXYPlot.setDomainBoundaries(DEFAULT_MIN_X, DEFAULT_MAX_X, BoundaryMode.FIXED);
			mySimpleXYPlot.setRangeBoundaries(DEFAULT_MIN_Y, DEFAULT_MAX_Y, BoundaryMode.FIXED);
			mySimpleXYPlot.redraw();
			return;
		}
		minXY.x = series[0].getX(0).floatValue();
		minXY.y=DEFAULT_MIN_Y;
		maxXY.x=series[0].getX(series[0].size() - 1).floatValue();
		/*if(maxXY.x<DEFAULT_MAX_X)
			maxXY.x=DEFAULT_MAX_X;*/
		maxXY.y=DEFAULT_MAX_Y;

		mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);
		mySimpleXYPlot.setRangeBoundaries(minXY.y, maxXY.y, BoundaryMode.FIXED);
		//mySimpleXYPlot.calculateMinMaxVals();
		mySimpleXYPlot.redraw();
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_graph);


		mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
		mySimpleXYPlot.setOnTouchListener(this);
		mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(1);
		mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
		mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		mySimpleXYPlot.getGraphWidget().setRangeValueFormat(
				new DecimalFormat("#####"));
		mySimpleXYPlot.getGraphWidget().setDomainValueFormat(
				new DecimalFormat("#####"));
		mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(50);
		mySimpleXYPlot.setRangeLabel(Constants.RANGE_LABEL);
		mySimpleXYPlot.setDomainLabel("");

		mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		//mySimpleXYPlot.disableAllMarkup();
		series = new SimpleXYSeries[2];
		series[0]=new SimpleXYSeries(Constants.BR);
		series[1]=new SimpleXYSeries(Constants.HR);
		mySimpleXYPlot.addSeries(series[1],
				new LineAndPointFormatter(Color.rgb(150, 20, 20), null,
						Color.rgb(245, 50, 50), null));
		mySimpleXYPlot.addSeries(series[0],
				new LineAndPointFormatter(Color.rgb(25, 140, 25), null,
						Color.rgb(50, 230, 50), null));



		//Adding zoom out and reset button
		zoomOutButton = (Button) findViewById(R.id.zoomOutButton);
		zoomOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(series[0].size()==0)
					return;
				minXY.x = series[0].getX(0).floatValue();
				maxXY.x = series[0].getX(series[0].size() - 1).floatValue();
				mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);

				mySimpleXYPlot.redraw();
			}
		});

		resetButton =(Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				prefManager.resetHistory();
			}
		});

		//reading from DB
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();
		prefManager.updateHistory();

	}

	public void updateHistory(String name, ArrayList<Integer> val) {
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();
		if(name.equals("BR")){
			int num=series[0].size();
			for(int i=0;i<num;i++)
				series[0].removeLast();
			for(int i=0;i<val.size();i++)
				series[0].addLast(i,val.get(i));
		}
		else if(name.equals("HR")){
			int num=series[1].size();
			for(int i=0;i<num;i++)
				series[1].removeLast();
			for(int i=0;i<val.size();i++)
				series[1].addLast(i,val.get(i));
		}
		
		updateView();

	}


	// Definition of the touch states
	static final int NONE = 0;
	static final int ONE_FINGER_DRAG = 1;
	static final int TWO_FINGERS_DRAG = 2;
	int mode = NONE;

	PointF firstFinger;
	float distBetweenFingers;
	boolean stopThread = false;

	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		if(series[0].size()<=2)
			return true;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			stopThread = true;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: // second finger
			distBetweenFingers = spacing(event);
			// the distance check is done to avoid false alarms
			if (distBetweenFingers > 5f) {
				mode = TWO_FINGERS_DRAG;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == ONE_FINGER_DRAG) {
				PointF oldFirstFinger = firstFinger;
				firstFinger = new PointF(event.getX(), event.getY());
				scroll(oldFirstFinger.x - firstFinger.x);
				mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
						BoundaryMode.FIXED);
				mySimpleXYPlot.redraw();

			} else if (mode == TWO_FINGERS_DRAG) {
				float oldDist = distBetweenFingers;
				distBetweenFingers = spacing(event);
				zoom(oldDist / distBetweenFingers);
				mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x,
						BoundaryMode.FIXED);
				mySimpleXYPlot.redraw();
			}
			break;
		}
		return true;
	}

	private void zoom(float scale) {
		if(series[0].size()<=2)
			return;
		float domainSpan = maxXY.x - minXY.x;
		float domainMidPoint = maxXY.x - domainSpan / 2.0f;
		float offset = domainSpan * scale / 2.0f;

		minXY.x = domainMidPoint - offset;
		maxXY.x = domainMidPoint + offset;

		minXY.x = Math.min(minXY.x, series[1].getX(series[1].size() - 3)
				.floatValue());
		maxXY.x = Math.max(maxXY.x, series[0].getX(1).floatValue());
		clampToDomainBounds(domainSpan);
	}

	private void scroll(float pan) {
		if(series[0].size()<=2)
			return;
		float domainSpan = maxXY.x - minXY.x;
		float step = domainSpan / mySimpleXYPlot.getWidth();
		float offset = pan * step;
		minXY.x = minXY.x + offset;
		maxXY.x = maxXY.x + offset;
		clampToDomainBounds(domainSpan);
	}

	private void clampToDomainBounds(float domainSpan) {
		if(series[0].size()<=2)
			return;
		float leftBoundary = series[0].getX(0).floatValue();
		float rightBoundary = series[1].getX(series[1].size() - 1).floatValue();
		// enforce left scroll boundary:
		if (minXY.x < leftBoundary) {
			minXY.x = leftBoundary;
			maxXY.x = leftBoundary + domainSpan;
		} else if (maxXY.x > series[1].getX(series[1].size() - 1).floatValue()) {
			maxXY.x = rightBoundary;
			minXY.x = rightBoundary - domainSpan;
		}
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}
}
