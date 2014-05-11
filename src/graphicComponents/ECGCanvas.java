package graphicComponents;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;

//src for help: http://stackoverflow.com/questions/18616035/how-to-animate-a-path-on-canvas-android
public class ECGCanvas{

	static int BEAT[];
	final static int SPEED=7;
	
	final static int OFFSET=30;
	int lastX=0;
	boolean moveCanvas;
	int noOfTicks;

	final static int PATH_SIZE=13;
	int path_ind=0;

	public boolean isDrawing=false;

	Matrix matrix; // transformation matrix
	Path path;       // your path
	Paint paint;    // your paint
	Canvas canvas;
	Bitmap bg;



	long startTime;
	long moveTime = 100; //in ms

	public ECGCanvas() {

		BEAT=new int [PATH_SIZE];
		//only Y coordinate
		BEAT[0]=OFFSET;
		BEAT[1]=OFFSET-2;
		BEAT[2]=OFFSET-2;
		BEAT[3]=OFFSET;
		BEAT[4]=OFFSET;
		BEAT[5]=OFFSET+5;
		BEAT[6]=OFFSET-25;
		BEAT[7]=OFFSET+4;
		BEAT[8]=OFFSET;
		BEAT[9]=OFFSET;
		BEAT[10]=OFFSET-5;
		BEAT[11]=OFFSET-5;
		BEAT[12]=OFFSET;

		
		moveCanvas=false;
		noOfTicks=0;
		
		matrix = new Matrix();
		matrix.setTranslate(-1*SPEED*PATH_SIZE, 0);
		
		path = new Path();    
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(Color.BLACK);

		bg = Bitmap.createBitmap(300, 50, Bitmap.Config.ARGB_8888);
		canvas = new Canvas(bg);


		moveTime = 500; // 0.5 seconds
		path_ind=0;
		isDrawing=false;

		startTime = System.currentTimeMillis();
	}

	public BitmapDrawable flat(){
		path.moveTo(lastX, OFFSET);
		lastX+=SPEED;
		path.lineTo(lastX, OFFSET);
		return new BitmapDrawable(bg);
	}
	public BitmapDrawable tick() {

		
		if(isDrawing){
			for(int i=0;i<PATH_SIZE;i++){
				/*double currentTime=System.currentTimeMillis();
				while (currentTime-startTime<moveTime)
					currentTime=System.currentTimeMillis();*/
				path.moveTo(lastX, BEAT[(path_ind+PATH_SIZE-1)%PATH_SIZE]);
				lastX+=SPEED;
				path.lineTo(lastX, BEAT[path_ind]);
				
				canvas.drawPath(path, paint);

				path_ind=(path_ind+1)%PATH_SIZE;
			}
			noOfTicks++;
		}
		/*
		if(noOfTicks>=5){
			moveCanvas=true;
			noOfTicks=5;
		}
		if(moveCanvas){
			canvas.drawBitmap(bg, SPEED*PATH_SIZE, 0, paint);
			canvas.concat(matrix);
		}*/
		return new BitmapDrawable(bg);
	}

}
