package edu.ce.sharif.drtick.signalProcessing;


public class SignalInfo{
	
	private boolean starting_noise=true;
	private int cut_start_seconds=2;//(s) Initial signal period to cut off
	
	boolean signal_acquired=false;
	int signal_ind=0;
	int signal[];
	double signal_seconds;
	double signal_FPS=8.5;
	int signal_size;
	
	int progress=0;
	
	//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
	private long start_time, end_time;
	
	SignalInfo(double _signal_seconds){
		starting_noise=true;
		cut_start_seconds=2;
		signal_acquired=false;
		signal_ind=0;
		signal_FPS=8.5;
		progress=0;
		
		
		signal_seconds=_signal_seconds;
		signal_size=(int) (signal_FPS*signal_seconds);
	}
	
	void resetSignal(){
		signal=new int[signal_size];
		signal_acquired=false;
		signal_ind=0;
		progress=0;
		//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
		start_time=System.currentTimeMillis();
	}
	public void addToSignal(int inp){
		signal_ind=(signal_ind+1)%signal_size;
		if(starting_noise && signal_ind==((int)signal_FPS*cut_start_seconds)){
			starting_noise=false;
			resetSignal();
			return;
		}
		signal[signal_ind]=inp;
		if(signal_ind==0){
			signal_acquired=true;
			//FOR DYNAMIC MEASURMENT OF FPS RELATED STUFF
			end_time=System.currentTimeMillis();
			signal_seconds=(end_time-start_time)/1000.0;
		}else if (signal_ind==1)
			start_time=System.currentTimeMillis();
	}
	
}