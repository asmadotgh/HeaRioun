package edu.ce.sharif.hearioun;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import edu.ce.sharif.hearioun.database.PrefManager;

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
			.setIndicator(getString(R.string.title_activity_measure), ressources.getDrawable(R.drawable.icon_measure_config))
			.setContent(intentMeasure);

		// History tab
		Intent intentHistory = new Intent().setClass(this, History.class);
		TabSpec tabSpecHistory = tabHost
			.newTabSpec("History")
			.setIndicator(getString(R.string.title_activity_history), ressources.getDrawable(R.drawable.icon_history_config))
			.setContent(intentHistory);
		
		// Help tab
		Intent intentHelp = new Intent().setClass(this, Help.class);
		TabSpec tabSpecHelp = tabHost
			.newTabSpec("Help")
			.setIndicator(getString(R.string.title_activity_help), ressources.getDrawable(R.drawable.icon_help_config))
			.setContent(intentHelp);
		
		// Profile tab
		Intent intentProfile = new Intent().setClass(this, Profile.class);
		TabSpec tabSpecProfile = tabHost
			.newTabSpec("Profile")
			.setIndicator(getString(R.string.title_activity_profile), ressources.getDrawable(R.drawable.icon_profile_config))
			.setContent(intentProfile);
	
		// add all tabs 
		tabHost.addTab(tabSpecProfile);
		tabHost.addTab(tabSpecHelp);
		tabHost.addTab(tabSpecHistory);
		tabHost.addTab(tabSpecMeasure);
		
		
		
		
		//set Windows tab as default (zero based)
		tabHost.setCurrentTab(3);
		

				

		
		
	}

/*
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
