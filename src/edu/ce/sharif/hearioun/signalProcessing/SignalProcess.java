package edu.ce.sharif.hearioun.signalProcessing;

import java.util.ArrayList;

import biz.source_code.dsp.filter.IirFilterCoefficients;
import edu.ce.sharif.hearioun.Measure;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class SignalProcess {
	//FOR HR
	int []y;

	//FOR BR
	int[]y_BR;
	final static int AVERAGING_LENGTH=10;

	double fps;

	// Parameters to play with
	double WINDOW_SECONDS;					//(s) Sliding window length
	double BPM_SAMPLING_PERIOD;				//(s) Time between heart rate estimations
	int BPM_L;								//(bpm) Min Valid heart rate
	int BPM_H;								//(bpm) Max Valid heart rate
	int BR_L;								//(BR) Min Valid breathing rate
	int BR_H;								//(BR) Max Valid breathing rate
	//double fcl;							// Min Valid frequency
	//double fch;							// Max Valid frequency
	double FILTER_STABILIZATION_TIME;	//(s) Filter startup transient
	int FINE_TUNING_FREQ_INCREMENT;		//(bpm) Separation between test tones for smoothing
	
	public static int [] makeBreathingSignal(int [] inp){
		int [] res=new int [inp.length];
		myAverageFilter(inp, res, AVERAGING_LENGTH);
		myAverageFilter(res, res, AVERAGING_LENGTH);
		return res;
	}
	private static void myAverageFilter(int [] inp, int [] out, int length){
		int sum=0;
		for(int i=0;i<inp.length;i++){
			sum+=inp[i];
			if(i>=length)
				sum-=inp[i-length];

			if(i<length)
				out[i]=sum/(i+1);
			else
				out[i]=sum/length;
		}
	}
	public SignalProcess(int [] _y, double _fps){
		y=_y;
		y_BR=new int[y.length];
		fps=_fps;

		WINDOW_SECONDS = Measure.SIGNAL_SECONDS;					
		BPM_SAMPLING_PERIOD = 1; //0.5		
		BPM_L = 40;							
		BPM_H = 230;	
		BR_L=8;
		BR_H=20;
		//fcl=BPM_L/60.0/fps*2;
		//fch=BPM_H/60.0/fps*2;
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
		//The array has one extra point at the beginning and one extra point in the end.
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

	private void applyFilter(IirFilterCoefficients filter, double [] inp){
		double [] res=new double [inp.length];
		for(int i=0;i<filter.b.length;i++){
			res[i]=0.0;
			for(int j=0;j<i;j++){
				res[i]+=inp[i-j]*filter.b[j];
				if(j>0)
					res[i]-=res[i-j]*filter.a[j];
			}
		}

		for(int i=0;i<inp.length;i++)
			inp[i]=res[i];
	}

	public int computeWithFFT(){


		// Build and apply input filter
		//src: http://www.source-code.biz/dsp/java/apidocs/biz/source_code/dsp/filter/IirFilterDesignFisher.html
		//design(FilterPassType filterPassType, FilterCharacteristicsType filterCharacteristicsType, int filterOrder, double ripple, double fcf1, double fcf2);
		//ripple is ignored for filters rather than chebychev
		/*
		IirFilterCoefficients butterworth= IirFilterDesignFisher.design(FilterPassType.bandpass, FilterCharacteristicsType.butterworth, 2, -1, fcl, fch);
		applyFilter(butterworth,y);*/


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

		//FOR DEBUG
		/*System.out.println("Red amount");
		for(int j=0;j<y.length; j++)
			System.out.println(y[j]);

		System.out.println();
		System.out.println("FFT: ");
		for(int j=0;j<y.length;j++){
			gain[j]=Math.sqrt(fft_y[2*j]*fft_y[2*j]+fft_y[2*j+1]*fft_y[2*j+1]);
			System.out.println(gain[j]+" ");
		}
		System.out.println();*/



		//FFT indices of frequencies where the human heart rate is
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

	boolean isValidPeak(MyIntPoint p1, MyIntPoint p2){
		int MIN_P2P=(int)(fps*60.0/BPM_H);
		int MAX_P2P=(int)(fps*60.0/BPM_L);
		return ( (p2.ind-p1.ind)>=MIN_P2P && (p2.ind-p1.ind)<=MAX_P2P);

	}
	private int countPeaks(int [] inp){
		//version 2

		//FOR DEBUG
		int [] debug=new int[inp.length];

		for(int i=0;i<debug.length;i++)
			debug[i]=0;
		ArrayList<MyIntPoint> tmp=new ArrayList<MyIntPoint>();
		for(int i=1;i<inp.length-1;i++){
			if((inp[i]>=inp[i-1] && inp[i]>inp[i+1]) || (inp[i]>inp[i-1] && inp[i]>=inp[i+1])){
				MyIntPoint newPoint=new MyIntPoint(inp[i], i);
				if(tmp.size()==0 || isValidPeak(tmp.get(tmp.size()-1), newPoint)){
					tmp.add(newPoint);
					debug[i]=1;
				}
			}
		}

		//FOR DEBIG
		/*System.out.println("peaks: ");
		for(int i=0;i<debug.length;i++)
			System.out.println(debug[i]);*/

		return tmp.size();
	}

	private int countPeaks2(int [] inp){
		//version 3
		int num=0;
		ArrayList<Integer> tmp=new ArrayList<Integer>();
		for(int i=1;i<inp.length;i++){
			if(inp[i]>inp[i-1])
				tmp.add(1);
			else if (inp[i]<inp[i-1])
				tmp.add(-1);
		}

		for(int i=0;i<tmp.size()-1;i++)
			if(tmp.get(i)==1 && tmp.get(i+1)==-1)
				num++;
		return num;
	}
	
	private int countPeaksTime(int [] inp){
		//version 4
		ArrayList<Integer> res=new ArrayList<Integer>();
		ArrayList<Integer> tmp=new ArrayList<Integer>();
		for(int i=1;i<inp.length;i++){
			if(inp[i]>inp[i-1])
				tmp.add(i);
			else if (inp[i]<inp[i-1])
				tmp.add(-1*i);
		}

		for(int i=0;i<tmp.size()-1;i++)
			if(tmp.get(i)>0 && tmp.get(i+1)<0)
				res.add(tmp.get(i));
		if(tmp.get(tmp.size()-1)>0)
			res.add(tmp.get(tmp.size()-1));
		
		int avgTime;
		if(res.size()==0)
			avgTime=inp.length;
		else if(res.size()==1)
			avgTime=inp.length-res.get(0);
		else
			avgTime=res.get(1)-res.get(0);
		
		return avgTime;
	}


	public int computeWithPeakMeasurement(){
		//version 1: only counting local maximas
		//version 2: counting peaks based on local maximas and minimas
		//version 3: counting peaks based on slope
		//version 4: version 3+ smoothing BPM with calculating power

		// Build and apply input filter

		double bpm ;
		//y = hann(y);

		//FOR DEBUG
		/*System.out.println("Red amount");
		for(int j=0;j<y.length; j++)
			System.out.println(y[j]);
		System.out.println();*/

		int noPeaks=countPeaks2(y);
		//System.out.println("noPeaks: "+noPeaks+" window seconds: "+WINDOW_SECONDS);
		bpm=noPeaks*60.0/WINDOW_SECONDS;
		return smoothBPMwithPower(BPM_L, BPM_H, bpm,y);
	}

	public int computeBRWithPeakMeasurement(){
		//double average filter for finding out breathing pattern

		double br;

		myAverageFilter(y, y_BR, AVERAGING_LENGTH);
		myAverageFilter(y_BR, y_BR, AVERAGING_LENGTH);

		//FOR DEBUG
		/*System.out.println("Smoothed for BR measurement:");
		for(int j=0;j<y_BR.length; j++)
			System.out.println(y_BR[j]);
		System.out.println();*/

		int noPeaks=countPeaks2(y_BR);
		if(noPeaks==0)
			noPeaks=1;

		br=noPeaks*60.0/WINDOW_SECONDS;
		if(br<BR_L)
			br=BR_L;
		if(br>BR_H)
			br=BR_H;
		return smoothBPMwithPower(BR_L, BR_H, br,y_BR);
	}
	
	public int computeBRWithPeakTimeMeasurement(){
		//double average filter for finding out breathing pattern

		double br;

		myAverageFilter(y, y_BR, AVERAGING_LENGTH);
		myAverageFilter(y_BR, y_BR, AVERAGING_LENGTH);

		//FOR DEBUG
		/*System.out.println("Smoothed for BR measurement:");
		for(int j=0;j<y_BR.length; j++)
			System.out.println(y_BR[j]);
		System.out.println();*/

		int tPeaks=countPeaksTime(y_BR);

		br=60.0/(tPeaks/fps);
		if(br<BR_L)
			br=BR_L;
		if(br>BR_H)
			br=BR_H;
		return (int)br;
		//return smoothBPMwithPower(BR_L, BR_H, br,y_BR);
	}

	private int smoothBPMwithPower(int lowVal, int highVal, double bpm, int [] array){
		int bpm_smooth=(int) bpm;


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
			for(int j=0;j<test_freqs; j++)
				freqs[j] = j * freq_inc + lowf;
			for (int h = 0; h<test_freqs;h++){
				double re = 0;
				double im = 0;
				for (int j = 0; j<array.length;j++){
					double phi = 2 * Math.PI * freqs[h] * j / fps;
					re = re + array[j] * Math.cos(phi);
					im = im + array[j] * Math.sin(phi);
				}
				power[h] = re * re + im * im;
			}
			MyPoint maxPeak= myMax(makePointListFromArray(power));
			bpm_smooth = (int) (60*freqs[maxPeak.ind]);
		}
		else
			bpm_smooth= (int) bpm;

		if(bpm_smooth<lowVal)
			bpm_smooth=lowVal;
		if(bpm_smooth>highVal)
			bpm_smooth=highVal;
		return bpm_smooth;
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

class MyIntPoint{
	int val;
	int ind;
	MyIntPoint(){
		val=0; ind=0;
	}
	MyIntPoint(int _val, int _ind){
		val=_val;
		ind=_ind;
	}
	boolean greaterEqual(MyPoint inp){
		return this.val>=inp.val;
	}
}