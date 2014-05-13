package edu.ce.sharif.hearioun;

import edu.ce.sharif.hearioun.database.PrefManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class Profile extends Activity {
	
	private PrefManager prefManager=null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		//reading from DB
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();
		prefManager.updateProfile();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		return true;
	}
	
	public void onConfirm(View v){
		
		//Read DB first
		prefManager=new PrefManager(this);
		prefManager.loadSavedPreferences();
		
		
		Toast.makeText(this,"Profile updated.",Toast.LENGTH_SHORT).show();
		
		TextView name_tv=(TextView) findViewById(R.id.editTextName);
		TextView age_tv=(TextView) findViewById(R.id.editTextAge);
		TextView weight_tv=(TextView) findViewById(R.id.editTextWeight);
		TextView height_tv=(TextView) findViewById(R.id.editTextHeight);
		boolean gender=((RadioButton) findViewById(R.id.radioMale)).isChecked();
		
		prefManager.savePreferencesString("name", name_tv.getText().toString());
		try{
			int age=Integer.parseInt(age_tv.getText().toString());
			prefManager.savePreferencesInt("age", age);
		}catch(Exception e){
		}
		
		try{
			int weight=Integer.parseInt(weight_tv.getText().toString());
			prefManager.savePreferencesInt("weight", weight);
		}catch(Exception e){
		}
		
		try{
			int height=Integer.parseInt(height_tv.getText().toString());
			prefManager.savePreferencesInt("height", height);
		}catch(Exception e){
		}
		
		prefManager.savePreferencesBool("gender", gender);
		
	}

}
