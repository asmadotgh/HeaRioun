package edu.ce.sharif.hearioun;


import android.os.Bundle;
import android.app.Activity;
import android.graphics.Typeface;
import android.view.Menu;
import android.widget.TextView;

public class Help extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);

		//setting the font

		Typeface font_fa = Typeface.createFromAsset(getAssets(), "fonts/bnazanin.ttf");

		//all text views
		TextView tmp=(TextView)findViewById(R.id.textViewHelp_h1);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp1);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp_h2);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp2);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp_h3);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp3);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp_h4);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp4);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp_h5);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp5_1);
		tmp.setTypeface(font_fa);
		//5.2: English
		tmp=(TextView)findViewById(R.id.textViewHelp5_3);
		tmp.setTypeface(font_fa);
		//5.4: English
		tmp=(TextView)findViewById(R.id.textViewHelp5_5);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp_h6);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp6_1);
		tmp.setTypeface(font_fa);
		//6.2: link in English
		tmp=(TextView)findViewById(R.id.textViewHelp6_3);
		tmp.setTypeface(font_fa);
		//6.4, 6.5: links in English

		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

}
