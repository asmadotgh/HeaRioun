package edu.ce.sharif.hearioun.database;

import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.RadioButton;
import android.widget.TextView;
import edu.ce.sharif.hearioun.History;
import edu.ce.sharif.hearioun.R;

public class PrefManager {
	SharedPreferences pref;
	Editor editor;
	Activity cont;
	boolean LOCK=false;

	String name;
	int age;
	int weight;
	int height;
	boolean gender;
	public ArrayList <Integer> HR=new ArrayList<Integer>();
	public ArrayList<Integer> BR=new ArrayList<Integer>();
	
	public PrefManager(Activity _cont) {
		if(LOCK)
			return;
		cont=_cont;
		pref = PreferenceManager.getDefaultSharedPreferences(cont.getApplicationContext());
		editor=pref.edit();
		LOCK=true;
	}

	public void clear(){
		editor.clear();
		editor.commit();
	}

	public void updateProfile(){
		
	    TextView name_tv=(TextView) cont.findViewById(R.id.editTextName);
	    name_tv.setText(name);
	    
	    TextView age_tv=(TextView) cont.findViewById(R.id.editTextAge);
	    age_tv.setText(age+"");
	    
	    TextView weight_tv=(TextView) cont.findViewById(R.id.editTextWeight);
	    weight_tv.setText(weight+"");
	    
	    TextView height_tv=(TextView) cont.findViewById(R.id.editTextHeight);
	    height_tv.setText(height+"");
	    
	    
	    RadioButton male_rb=(RadioButton) cont.findViewById(R.id.radioMale);
	    RadioButton female_rb=(RadioButton) cont.findViewById(R.id.radioFemale);
	    
	    if(gender){
	    	male_rb.setChecked(true);
	    	female_rb.setChecked(false);
	    }else{
	    	male_rb.setChecked(false);
	    	female_rb.setChecked(true);
	    }
	}
	
	public void updateHistory(){
		History hist=(History)cont;
		hist.updateHistory("HR", HR);
		hist.updateHistory("BR", BR);
	}
	
	public void resetHistory(){
		History hist=(History)cont;
		for(int i=0;i<HR.size();i++)
			editor.remove("HRStatus_" + i);
		for(int i=0;i<BR.size();i++)
			editor.remove("BRStatus_"+i);
		
		HR.clear();
		BR.clear();
		
		editor.remove("HR");
		editor.putInt("HR", 0);
		editor.remove("BR");
		editor.putInt("BR", 0);
		
		editor.commit();
		
		hist.updateHistory("HR", HR);
		hist.updateHistory("BR", BR);
		
	}
	
	public void loadSavedPreferences() {
		
		name = pref.getString("name", "");
		age = pref.getInt("age", -1);
		weight = pref.getInt("weight", 0);
		height = pref.getInt("height", 0);
		gender = pref.getBoolean("gender", false);
		
		
		HR=new ArrayList<Integer>();
	    int HR_size = pref.getInt("HR", 0);  
	    for(int i=0;i<HR_size;i++)
	        HR.add(pref.getInt("HRStatus_" + i, 0));
	    
	    BR=new ArrayList<Integer>();
	    int BR_size = pref.getInt("BR", 0);  
	    for(int i=0;i<BR_size;i++)
	        BR.add(pref.getInt("BRStatus_" + i, 0));
	    
	    
	}
	



	public void savePreferencesInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}



	public void savePreferencesString(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	
	public void savePreferencesBool(String key, boolean value){
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public void savePreferencesIntArray(String key, int value){
		int array_size = pref.getInt(key, 0);
		editor.putInt(key+"Status_" + array_size,value);
		array_size++;
		editor.remove(key);
		editor.putInt(key, array_size);
		editor.commit();
	}


}
