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
		TextView tmp=(TextView)findViewById(R.id.textViewHelp);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp2);
		tmp.setTypeface(font_fa);
		tmp=(TextView)findViewById(R.id.textViewHelp3);
		tmp.setTypeface(font_fa);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

}
