package edu.ce.sharif.hearioun.signalProcessing;

import java.util.ArrayList;

import edu.ce.sharif.hearioun.Measure;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SignalProcess {
	int []y;
	double fps;

	// Parameters to play with
	int WINDOW_SECONDS;					//(s) Sliding window length
	double BPM_SAMPLING_PERIOD;			//(s) Time between heart rate estimations
	int BPM_L;							//(bpm) Min Valid heart rate
	int BPM_H;							//(bpm) Max Valid heart rate
	double FILTER_STABILIZATION_TIME;	//(s) Filter startup transient
	int FINE_TUNING_FREQ_INCREMENT;		//(bpm) Separation between test tones for smoothing

	public SignalProcess(int [] _y, double _fps){
		y=_y;
		//DEBUG INFO
		/*System.out.println("array ro doros geref?!");
		for(int i=0;i<y.length;i++)
			System.out.print(y[i]+" ");
		System.out.println();*/
		fps=_fps;

		WINDOW_SECONDS = Measure.SIGNAL_SECONDS;					
		BPM_SAMPLING_PERIOD = 1; //0.5		
		BPM_L = 40;							
		BPM_H = 230;						
		FILTER_STABILIZATION_TIME = 1;				
		FINE_TUNING_FREQ_INCREMENT = 1;		
	}

	public MyPoint myMax(ArrayList<MyPoint> inp){

		MyPoint maxPoint=inp.get(0);
		for(int i=0;i<inp.size(); i++)
			if(inp.get(i).greaterEqual(maxPoint))
				maxPoint=inp.get(i);
		return maxPoint;
	}
	
	public MyPoint myMaxPeak(ArrayList<MyPoint> inp){
		//The array has one extra point at the begining and one extra point in the end.
		//why? because we care about peaks. i.e. the points greater than both of their neighbours
		MyPoint maxPoint=inp.get(1);
		for(int i=1;i<inp.size()-1; i++){
			MyPoint tmp=inp.get(i);
			if(tmp.greaterEqual(inp.get(i-1)) && tmp.greaterEqual(inp.get(i+1)) && tmp.greaterEqual(maxPoint))
				maxPoint=tmp;
		}
		return maxPoint;
	}
	
	public ArrayList<MyPoint> makePointListFromArray(double [] inp){
		ArrayList<MyPoint> list=new ArrayList<MyPoint>();
		for(int i=0;i<inp.length;i++)
			list.add(new MyPoint(inp[i],i));
		return list;
	}

	public ArrayList<MyPoint> makePointListFromArray(double [] inp, int l_ind, int h_ind){
		ArrayList<MyPoint> list=new ArrayList<MyPoint>();
		for(int i=l_ind;i<=h_ind;i++){
			list.add(new MyPoint(inp[i],i));
		}
		return list;
	}

	//info: http://en.wikipedia.org/wiki/Hann_function
	public int[] hann(int [] signal_in)
	{
		for (int i = 0; i < signal_in.length; i++)
			signal_in[i] = (int) (signal_in[i] * 0.5 * (1.0 - Math.cos(2.0 * Math.PI * i / signal_in.length)));
		return signal_in;
	}
	private int averageInt(double [] inp){
		if(inp==null || inp.length==0)
			return 0;
		double sum=0;
		for(int i=0;i<inp.length;i++)
			sum+=inp[i];
		return (int)(sum/inp.length);
	}

	public int compute(){

		

		// Build and apply input filter
		//http://en.wikipedia.org/wiki/Butterworth_filter
		/*[b, a] = butter(2, [(((BPM_L)/60)/fps*2) (((BPM_H)/60)/fps*2)]);
		yf = filter(b, a, y);
		y = yf((fps * max(FILTER_STABILIZATION_TIME, CUT_START_SECONDS))+1:size(yf, 2));
		*/

		//Some initializations and precalculations

		double bpm ;
		double bpm_smooth;
		//y = hann(y);
		double [] gain=new double [y.length];
		double [] fft_y=new double[2*y.length];
		for(int i=0;i <y.length;i++){
			fft_y[2*i]=y[i];
			fft_y[2*i+1]=0;
		}
		//JTransform library for FFT
		//how to use:http://stackoverflow.com/questions/15226814/standard-fft-classes-library-for-android
		//library link: https://sites.google.com/site/piotrwendykier/software/jtransforms
		DoubleFFT_1D fft = new DoubleFFT_1D(y.length);
		fft.complexForward(fft_y);

		System.out.println("Red amount");
		for(int j=0;j<y.length; j++)
			System.out.println(y[j]);
		//FOR DEBUG
		for(int j=0;j<y.length;j++){
			gain[j]=Math.sqrt(fft_y[2*j]*fft_y[2*j]+fft_y[2*j+1]*fft_y[2*j+1]);
			System.out.println(gain[j]+" ");
		}
		System.out.println();


		//FFT indices of frequencies where the human heartbeat is
		int il = (int)(BPM_L * y.length / fps /60)+1;
		int ih = (int)Math.ceil(BPM_H * y.length / fps /60)+1;

		//Locate the highest peak
		MyPoint maxPeak = myMaxPeak(makePointListFromArray(gain,il-1,ih+1));
		bpm = (maxPeak.ind) * 60 * fps / y.length;
		//FOR DEBUG
		/*System.out.println("il: "+il+ " ih: "+ih);
		System.out.println("max freq ind? "+ (maxPeak.ind+1) +"fps: "+ fps+" y.length: "+y.length);
		System.out.println("bpm: (maxPeak.ind) * 60 * fps / y.length"+ bpm);*/


		//Smooth the highest peak frequency by finding the frequency that
		//best "correlates" in the resolution range around the peak

		double freq_resolution = 1.0 / WINDOW_SECONDS;
		double lowf = bpm / 60.0 - freq_resolution; //theoretically: 0.5*freq_resolution, but I put this for higher accuracy
		double freq_inc = FINE_TUNING_FREQ_INCREMENT / 60.0;
		int test_freqs = 2*(int)Math.ceil(freq_resolution / freq_inc);
		if (test_freqs>0){
			double []power=new double [test_freqs];
			for(int j=0;j<test_freqs;j++)
				power[j]=0;
			double [] freqs=new double [test_freqs];
//			System.out.println("freqs that have been tested: ");
			for(int j=0;j<test_freqs; j++){
				freqs[j] = j * freq_inc + lowf;
	//			System.out.println(60*freqs[j]);
			}
			for (int h = 0; h<test_freqs;h++){
				double re = 0;
				double im = 0;
				for (int j = 0; j<y.length;j++){
					double phi = 2 * Math.PI * freqs[h] * j / fps;
					re = re + y[j] * Math.cos(phi);
					im = im + y[j] * Math.sin(phi);
				}
				power[h] = re * re + im * im;
			}
			maxPeak= myMax(makePointListFromArray(power));
			bpm_smooth = 60*freqs[maxPeak.ind];
		}
		else
			bpm_smooth=bpm;
		/*int [] t=new int [i];
			for(int j=0;j<i;j++)
				t[j] = (int) (j * ((orig_y.length / fps) / (num_bpm_samples - 1)));
		 */

		return (int) bpm_smooth;
	}
}

class MyPoint{
	double val;
	int ind;
	MyPoint(){
		val=0; ind=0;
	}
	MyPoint(double _val, int _ind){
		val=_val;
		ind=_ind;
	}
	boolean greaterEqual(MyPoint inp){
		return this.val>=inp.val;
	}
}
