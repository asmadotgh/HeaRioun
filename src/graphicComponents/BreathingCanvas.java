package graphicComponents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;

//src for help: http://stackoverflow.com/questions/18616035/how-to-animate-a-path-on-canvas-android
public class BreathingCanvas{

	public int signal[];
	private int PATH_SIZE;
	
	final static int SPEED=-4;
	
	final static int OFFSET=45;
	final static int SCALE=10;
	int lastX;
	boolean moveCanvas;

	
	int path_ind=0;

	public boolean isDrawing=false;

	Matrix matrix; // transformation matrix
	Path path;    
	Paint paint;
	Canvas canvas;
	Bitmap bg;


	public BreathingCanvas(int [] _signal) {
		signal=_signal;
		if(signal!=null)
			PATH_SIZE=signal.length;
		else
			PATH_SIZE=0;

		
		moveCanvas=false;
		
		matrix = new Matrix();
		matrix.postTranslate((-1)*SPEED, 0);
		
		path = new Path();    
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.BLACK);

		bg = Bitmap.createBitmap(300, 50, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bg);


		path_ind=0;
		isDrawing=false;
		
		lastX=0;

	}
	
	public BitmapDrawable drawBreathing(){
		if(signal==null || isDrawing==false)
			return new BitmapDrawable(bg);

		/*System.out.println(signal.length);
		for(int i=0;i<signal.length;i++)
			System.out.println(signal[i]);
		System.out.println("");
		System.out.println("");*/
		path=new Path();
		//System.out.println((lastX+SPEED)+" "+ (OFFSET-signal[0]/SCALE));
		path.moveTo(lastX+SPEED, OFFSET-signal[(path_ind+PATH_SIZE-1)%PATH_SIZE]/SCALE);
		//path.moveTo(path_ind, 10);
		//lastX+=SPEED;
		//System.out.println(lastX+" "+ (OFFSET-signal[1]/SCALE));
		path.lineTo(lastX, OFFSET-signal[path_ind]/SCALE);
		//path.lineTo(path_ind+1, 20);
		canvas.drawPath(path, paint);
		path_ind=(path_ind+1)%PATH_SIZE;

		canvas.concat(matrix);
		return new BitmapDrawable(bg);
	}

}
