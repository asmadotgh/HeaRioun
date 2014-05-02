package edu.ce.sharif.hearioun;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class Profile extends Activity {
	
	/*
	public Profile(){
		genderSw=(Switch) findViewById(R.id.SwitchGender);
		genderSw.setTextOn("female");
		genderSw.setTextOff("male");
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}
	
	public void onConfirm(View v){
		
		Toast.makeText(this,"Profile updated.",Toast.LENGTH_SHORT).show();
		/*SharedPreferences prefs = this.getSharedPreferences("edu.ce.sharif.hearioun", Context.MODE_PRIVATE);
		
		String dateTimeKey = "com.example.app.datetime";

		// use a default value using new Date()
		long l = prefs.getLong(dateTimeKey, new Date().getTime()); 
		
		Date dt = getSomeDate();
		prefs.edit().putLong(dateTimeKey, dt.getTime()).commit();
		
		
		Button confirmBtn = (Button) findViewById(R.id.buttonProfileConfirm);
		Intent measure = new Intent(this, ECGDrawer.class);
		measure.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(measure);*/
		
	}

}
