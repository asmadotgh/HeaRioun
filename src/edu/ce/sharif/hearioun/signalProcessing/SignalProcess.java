package edu.ce.sharif.hearioun.signalProcessing;

import java.util.ArrayList;

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
	int CUT_START_SECONDS;				//(s) Initial signal period to cut off
	int FINE_TUNING_FREQ_INCREMENT;		//(bpm) Separation between test tones for smoothing

	public SignalProcess(int [] _y, double _fps){
		y=_y;
		//DEBUG INFO
		/*System.out.println("array ro doros geref?!");
		for(int i=0;i<y.length;i++)
			System.out.print(y[i]+" ");
		System.out.println();*/
		fps=_fps;

		WINDOW_SECONDS = 6;					
		BPM_SAMPLING_PERIOD = 1; //0.5		
		BPM_L = 40;							
		BPM_H = 230;						
		FILTER_STABILIZATION_TIME = 1;	
		CUT_START_SECONDS = 0;				
		FINE_TUNING_FREQ_INCREMENT = 1;		
	}

	public MyPoint myMax(ArrayList<MyPoint> inp){

		MyPoint maxPoint=inp.get(0);
		for(int i=0;i<inp.size(); i++)
			if(inp.get(i).greaterEqual(maxPoint))
				maxPoint=inp.get(i);
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

		//Some initializations and precalculations

		double bpm ;
		double bpm_smooth;
		y = hann(y);
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
		fft.realForwardFull(fft_y);

		System.out.println();
		for(int j=0;j<y.length;j++){
			gain[j]=Math.sqrt(fft_y[2*j]*fft_y[2*j]+fft_y[2*j+1]*fft_y[2*j+1]);
			System.out.println(gain[j]+" ");
		}
		System.out.println();

		//DEBUG INFO
		/*
				for(int i=0;i<gain.length;i++)
					System.out.println(i + " "+ gain[i]);
				System.out.println();*/

		//FFT indices of frequencies where the human heartbeat is
		int il = (int)(BPM_L * y.length / fps /60)+1;
		int ih = (int)Math.ceil(BPM_H * y.length / fps /60)+1;

		//Locate the highest peak
		MyPoint maxPeak = myMax(makePointListFromArray(gain,il,ih));
		System.out.println("il: "+il+ " ih: "+ih);
		System.out.println("max freq ind? "+ (maxPeak.ind+1) +"fps: "+ fps+" y.length: "+y.length);
		bpm = (maxPeak.ind+1) * 60 * fps / y.length;
		System.out.println("bpm: (maxPeak.ind+1) * 60 * fps / y.length"+ bpm);


		//Smooth the highest peak frequency by finding the frequency that
		//best "correlates" in the resolution range around the peak

		System.out.println("bpm avalie: "+bpm);
		double freq_resolution = 1.0 / WINDOW_SECONDS;
		double lowf = bpm / 60 - 0.5 * freq_resolution;
		double freq_inc = FINE_TUNING_FREQ_INCREMENT / 60.0;
		int test_freqs = (int)Math.ceil(freq_resolution / freq_inc);
		System.out.println(test_freqs);
		if (test_freqs>0){
			double []power=new double [test_freqs];
			for(int j=0;j<test_freqs;j++)
				power[j]=0;
			double [] freqs=new double [test_freqs];
			for(int j=0;j<test_freqs; j++)
				freqs[j] = j * freq_inc + lowf;
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
