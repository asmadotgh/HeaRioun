package edu.ce.sharif.hearioun.signalProcessing;

import java.io.ByteArrayOutputStream;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;


public abstract class ImageProcessing {
	
	
	public static int decodeYUV420SPtoRedSum(byte[] yuv420sp, int width, int height) {
	    final int frameSize = width * height;
 
	    final int ii = 0;
	    final int ij = 0;
	    final int di = +1;
	    final int dj = +1;

	    int sum=0;
	    //int sumG=0, sumB=0;
	    
	    for (int i = 0, ci = ii; i < height; ++i, ci += di) {
	        for (int j = 0, cj = ij; j < width; ++j, cj += dj) {
	            int y = (0xff & ((int) yuv420sp[ci * width + cj]));
	            int v = (0xff & ((int) yuv420sp[frameSize + (ci >> 1) * width + (cj & ~1) + 0]));
	            int u = (0xff & ((int) yuv420sp[frameSize + (ci >> 1) * width + (cj & ~1) + 1]));
	            y = y < 16 ? 16 : y;

	            int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
	            int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	            int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

	            r = r < 0 ? 0 : (r > 255 ? 255 : r);
	            g = g < 0 ? 0 : (g > 255 ? 255 : g);
	            b = b < 0 ? 0 : (b > 255 ? 255 : b);
	            
	            sum+=r;
	            //sumG+=g; sumB+=b;

	            //argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
	        }
	    }
	    //System.out.println("R: "+sum/(width*height)+" G: "+sumG/(width*height)+" B: "+sumB/(width*height));
	    return sum;
	}

	private static int decodeYUV420SPtoRedSum3(byte[] yuv420sp, int width, int height){
		
		int numPixels = width*height;
		// the buffer we fill up which we then fill the bitmap with
		//IntBuffer intBuffer = IntBuffer.allocate(width*height);
		// If you're reusing a buffer, next line imperative to refill from the start,
		// if not good practice
		//intBuffer.position(0);

		// Set the alpha for the image: 0 is transparent, 255 fully opaque
		//final byte alpha = (byte) 255;

		int sum=0;
		int sumG=0, sumB=0;
		// Get each pixel, one at a time
		for (int y = 0; y < height; y++) {
		    for (int x = 0; x < width; x++) {
		        // Get the Y value, stored in the first block of data
		        // The logical "AND 0xff" is needed to deal with the signed issue
		        int Y = yuv420sp[y*width + x] & 0xff;

		        // Get U and V values, stored after Y values, one per 2x2 block
		        // of pixels, interleaved. Prepare them as floats with correct range
		        // ready for calculation later.
		        int xby2 = x/2;
		        int yby2 = y/2;
		        float U = (float)(yuv420sp[numPixels + 2*xby2 + yby2*width] & 0xff) - 128.0f;
		        float V = (float)(yuv420sp[numPixels + 2*xby2 + 1 + yby2*width] & 0xff) - 128.0f;
		        // Do the YUV -> RGB conversion
		        float Yf = 1.164f*((float)Y) - 16.0f;
		        int R = (int)(Yf + 1.596f*V);
		        int G = (int)(Yf - 0.813f*V - 0.391f*U);
		        int B = (int)(Yf            + 2.018f*U);

		        // Clip rgb values to 0-255
		        R = R < 0 ? 0 : R > 255 ? 255 : R;
		        G = G < 0 ? 0 : G > 255 ? 255 : G;
		        B = B < 0 ? 0 : B > 255 ? 255 : B;

		        //System.out.println("R: "+R+" G: "+G+" B: "+B);
		        sum+=R;
		        sumG+=G;
		        sumB+=B;

		        // Put that pixel in the buffer
		        //intBuffer.put(alpha*16777216 + R*65536 + G*256 + B);
		    }
		}

		// Get buffer ready to be read
		//intBuffer.flip();
		System.out.println("R: "+sum/(width*height)+" G: "+sumG/(width*height)+" B: "+sumB/(width*height));
		return sum;

		
	}
	//src1: http://stackoverflow.com/questions/9325861/converting-yuv-rgbimage-processing-yuv-during-onpreviewframe-in-android
	//src2: http://stackoverflow.com/questions/7807360/how-to-get-pixel-colour-in-android
	private static int decodeYUV420SPtoRedSum2(byte[] yuv420sp, int width, int height){
		// pWidth and pHeight define the size of the previ
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuv = new YuvImage(yuv420sp, ImageFormat.NV21, width, height, null);

		// bWidth and bHeight define the size of the bitmap you wish the fill with the preview image
		yuv.compressToJpeg(new Rect(0, 0, width, height), 50, out);
		byte[] bytes = out.toByteArray();
		Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		
		int sum=0;
		for(int i=0;i<width;i++)
			for(int j=0;j<height;j++)
				sum+=Color.red(bitmap.getPixel(i, j));
		return sum;
	}
    private static int decodeYUV420SPtoRedSum1(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) return 0;

        final int frameSize = width * height;

        int sum = 0;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                int pixel = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
                int red = (pixel >> 16) & 0xff;
                sum += red;
            }
        }
        return sum;
    }

    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     * 
     * @param yuv420sp
     *            Byte array representing a yuv420sp image
     * @param width
     *            Width of the image.
     * @param height
     *            Height of the image.
     * @return int representing the average amount of red in the image.
     */
    public static int decodeYUV420SPtoRedAvg(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) return 0;

        final int frameSize = width * height;

        int sum = decodeYUV420SPtoRedSum(yuv420sp, width, height);
        //System.out.println("avg: "+ sum/frameSize);
        return (sum / frameSize);
    }
}
