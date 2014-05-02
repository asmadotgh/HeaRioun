package edu.ce.sharif.hearioun.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefManager {
	SharedPreferences pref;
	Editor editor;
	Context cont;

	String name;
	int age;
	int weight;
	int height;
	boolean gender;

/*
	public void clear(){
		editor.clear();
		editor.commit();
	}

	private void loadSavedPreferences() {
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		boolean checkBoxValue = sharedPreferences.getBoolean("CheckBox_Value", false);
		String name = sharedPreferences.getString("storedName", "YourName");
		if (checkBoxValue) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		editText.setText(name);
	}



	private void savePreferences(String key, boolean value) {
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();

	}



	private void savePreferences(String key, String value) {
		pref = PreferenceManager.getDefaultSharedPreferences(this);
		Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}


*/

}
