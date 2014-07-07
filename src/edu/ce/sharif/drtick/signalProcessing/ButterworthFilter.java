package edu.ce.sharif.drtick.signalProcessing;

public class ButterworthFilter {
	/*
    // creates a Butterworth lowpass filter
    public ByteProcessor Butterworth(int n)
    {
            ByteProcessor ip = new ByteProcessor(M,N);
            double value = 0;
            double distance = 0;
            int xcenter = (M/2)+1;
            int ycenter = (N/2)+1;

            for (int y = 0; y < N; y++)
            {
                    for (int x = 0; x < M; x++)
                    {
                            distance = Math.abs(x-xcenter)*Math.abs(x-xcenter)+Math.abs(y-ycenter)*Math.abs(y-ycenter);
                            distance = Math.sqrt(distance);
                            double parz = Math.pow(distance/threshold,2*n);
                            value = 255*(1/(1+parz));
                            ip.putPixelValue(x,y,value);
	      	}
            }

            ByteProcessor ip2 = new ByteProcessor(size,size);
            ip2.fill();
            ip2.insert(ip, w, h);
            if (displayFilter) new ImagePlus("Butterworth filter", ip2).show();
            return ip2;
    }
*/
}
