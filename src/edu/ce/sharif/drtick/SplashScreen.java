package edu.ce.sharif.drtick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashScreen extends Activity {
	     
	    /** 
	     * The thread to process splash screen events 
	     */
	 
	    /** Called when the activity is first created. */ 
	    @Override 
	    public void onCreate(Bundle savedInstanceState) { 
	        super.onCreate(savedInstanceState); 
	 
	        // Splash screen view 
	        setContentView(R.layout.splash); 
	        
	        new Handler().postDelayed(new Runnable() {
	              
	            // Using handler with postDelayed called runnable run method
	  
	            @Override
	            public void run() {
	                Intent i = new Intent(SplashScreen.this, MainActivity.class);
	                startActivity(i);
	  
	                // close this activity
	                finish();
	            }
	        }, 3*1000);
	        
	        /*
	         
	        final SplashScreen sPlashScreen = this;    
	         
	        // The thread to wait for splash screen events 
	        mSplashThread =  new Thread(){ 
	            @Override 
	            public void run(){ 
	                try { 
	                    synchronized(this){ 
	                        // Wait given period of time or exit on touch 
	                        wait(5000); 
	                    } 
	                } 
	                catch(InterruptedException ex){                     
	                } 
	 
	                finish(); 
	                 
	                // Run next activity 
	                Intent intent = new Intent(); 
	                intent.setClass(sPlashScreen, MainActivity.class); 
	                startActivity(intent); 
	                stop();                     
	            } 
	        }; 
	         
	        mSplashThread.start();   */      
	    } 
   
}
