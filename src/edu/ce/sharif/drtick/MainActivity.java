package edu.ce.sharif.drtick;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends TabActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);		
		
		Resources ressources = getResources(); 
		TabHost tabHost = getTabHost(); 
		
		//setting font
		/*Typeface font_fa = Typeface.createFromAsset(getAssets(), "fonts/bnazanin.ttf");
		TextView txtFont = new TextView(this);
		txtFont.setTypeface(font_fa);*/
		
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

	private View createTabView(final Context context, final String text, final Drawable icon) {
	    View view = LayoutInflater.from(context).inflate(R.layout.drtick_tab_indicator,
	            null);
	    ImageView iv= (ImageView) view.findViewById(R.id.tabIcon);
	    iv.setBackground(icon);
	    TextView tv = (TextView) view.findViewById(R.id.tabTitle);
	    tv.setText(text);
	    return view;
	}
}
