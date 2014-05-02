package edu.ce.sharif.hearioun;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Resources ressources = getResources(); 
		TabHost tabHost = getTabHost(); 
		
		// Measure tab
		Intent intentMeasure = new Intent().setClass(this, Measure.class);
		TabSpec tabSpecMeasure = tabHost
			.newTabSpec("Measure")
			.setIndicator("Measure", ressources.getDrawable(R.drawable.icon_measure_config))
			.setContent(intentMeasure);

		// History tab
		Intent intentHistory = new Intent().setClass(this, History.class);
		TabSpec tabSpecHistory = tabHost
			.newTabSpec("History")
			.setIndicator("History", ressources.getDrawable(R.drawable.icon_history_config))
			.setContent(intentHistory);
		
		// Help tab
		Intent intentHelp = new Intent().setClass(this, Help.class);
		TabSpec tabSpecHelp = tabHost
			.newTabSpec("Help")
			.setIndicator("Help", ressources.getDrawable(R.drawable.icon_help_config))
			.setContent(intentHelp);
		
		// Profile tab
		Intent intentProfile = new Intent().setClass(this, Profile.class);
		TabSpec tabSpecProfile = tabHost
			.newTabSpec("Profile")
			.setIndicator("Profile", ressources.getDrawable(R.drawable.icon_profile_config))
			.setContent(intentProfile);
	
		// add all tabs 
		tabHost.addTab(tabSpecMeasure);
		tabHost.addTab(tabSpecHistory);
		tabHost.addTab(tabSpecHelp);
		tabHost.addTab(tabSpecProfile);
		
		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(0);
	}

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
