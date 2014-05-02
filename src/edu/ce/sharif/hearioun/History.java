package edu.ce.sharif.hearioun;


import java.text.DecimalFormat;
import java.util.Random;

import com.androidplot.Plot;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class History extends Activity implements OnTouchListener{

	 private static final int SERIES_SIZE = 100;
	    private XYPlot mySimpleXYPlot;
	    private Button resetButton;
	    private SimpleXYSeries[] series = null;
	    private PointF minXY;
	    private PointF maxXY;

	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.touch_zoom_example);
	        resetButton = (Button) findViewById(R.id.resetButton);
	        resetButton.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	                minXY.x = series[0].getX(0).floatValue();
	                maxXY.x = series[1].getX(series[1].size() - 1).floatValue();
	                mySimpleXYPlot.setDomainBoundaries(minXY.x, maxXY.x, BoundaryMode.FIXED);

	                // pre 0.5.1 users should use postRedraw() instead.
	                mySimpleXYPlot.redraw();
	            }
	        });
	        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
	        mySimpleXYPlot.setOnTouchListener(this);
	        mySimpleXYPlot.getGraphWidget().setTicksPerRangeLabel(2);
	        mySimpleXYPlot.getGraphWidget().setTicksPerDomainLabel(2);
	        mySimpleXYPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
	        mySimpleXYPlot.getGraphWidget().setRangeValueFormat(
	                new DecimalFormat("#####"));
	        mySimpleXYPlot.getGraphWidget().setDomainValueFormat(
	                new DecimalFormat("#####"));
	        mySimpleXYPlot.getGraphWidget().setRangeLabelWidth(20);
	        mySimpleXYPlot.setRangeLabel("BPM");
	        mySimpleXYPlot.setDomainLabel("");

	        mySimpleXYPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
	        //mySimpleXYPlot.disableAllMarkup();
	        series = new SimpleXYSeries[2];
	        int scale = 10;
	        series[0]=new SimpleXYSeries("HRV");
	        series[1]=new SimpleXYSeries("HR");
	        for (int i = 0; i < 2; i++, scale *= 5) {
	            populateSeries(series[i], scale);
	        }
	        /*mySimpleXYPlot.addSeries(series[3],
	                new LineAndPointFormatter(Color.rgb(50, 0, 0), null,
	                        Color.rgb(100, 0, 0), null));
	        mySimpleXYPlot.addSeries(series[2],
	                new LineAndPointFormatter(Color.rgb(50, 50, 0), null,
	                        Color.rgb(100, 100, 0), null));*/
	        mySimpleXYPlot.addSeries(series[1],
	                new LineAndPointFormatter(Color.rgb(150, 20, 20), null,
	                        Color.rgb(245, 50, 50), null));
	        mySimpleXYPlot.addSeries(series[0],
	                new LineAndPointFormatter(Color.rgb(25, 140, 25), null,
	                        Color.rgb(50, 230, 50), null));
	        mySimpleXYPlot.redraw();
	        mySimpleXYPlot.calculateMinMaxVals();
	        minXY = new PointF(mySimpleXYPlot.getCalculatedMinX().floatValue(),
	                mySimpleXYPlot.getCalculatedMinY().floatValue());
	        maxXY = new PointF(mySimpleXYPlot.getCalculatedMaxX().floatValue(),
	                mySimpleXYPlot.getCalculatedMaxY().floatValue());
	    }

	    private void populateSeries(SimpleXYSeries series, int max) {
	        Random r = new Random();
	        for(int i = 0; i < SERIES_SIZE; i++) {
	            series.addLast(i, r.nextInt(max)+max);
	        }
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
	        float domainSpan = maxXY.x - minXY.x;
	        float step = domainSpan / mySimpleXYPlot.getWidth();
	        float offset = pan * step;
	        minXY.x = minXY.x + offset;
	        maxXY.x = maxXY.x + offset;
	        clampToDomainBounds(domainSpan);
	    }

	    private void clampToDomainBounds(float domainSpan) {
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
