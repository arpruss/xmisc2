package mobi.omegacentauri.xmisc2;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class Main extends Activity {
	Resources res;
	SharedPreferences prefs;
	static final String PREFS = "preferences";
	static final String PREF_OUTLOOK_COMPOSE = "outlook_compose";
	static final String PREF_CHROME_MATCH_NAVBAR = "chrome_match_navbar";
	static final String PREF_CHROME_KILL_TABGROUPS = "chrome_kill_tabgroups";

	private void message(String title, String msg) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		alertDialog.setTitle(title);
		alertDialog.setMessage(msg);
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", 
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {} });
		alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {} });
		alertDialog.show();

	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		prefs = getSharedPreferences(Main.PREFS, Context.MODE_WORLD_READABLE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.apps);
        
        CheckBox cb = (CheckBox) findViewById(R.id.outlook_compose);
		cb.setChecked(prefs.getBoolean(PREF_OUTLOOK_COMPOSE, true));

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_OUTLOOK_COMPOSE, isChecked).apply();
			}
		});

        cb = (CheckBox)findViewById(R.id.chrome_match_navbar);
        cb.setChecked(prefs.getBoolean(PREF_CHROME_MATCH_NAVBAR, true));

        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean(PREF_CHROME_MATCH_NAVBAR, isChecked).apply();
            }
        });

		cb = (CheckBox)findViewById(R.id.chrome_kill_tabgroups);
		cb.setChecked(prefs.getBoolean(PREF_CHROME_KILL_TABGROUPS, false));

		cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				prefs.edit().putBoolean(PREF_CHROME_KILL_TABGROUPS, isChecked).apply();
			}
		});

		Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		w.setNavigationBarColor(Color.BLACK);
	}

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//    	switch(item.getItemId()) {
//    	case R.id.clear:
//    		clear();
//    		return true;
//    	case R.id.options:
//    		startActivity(new Intent(this, Options.class));
//    		return true;
//    	default:
//    		return false;
//    	}
//    }
//
//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		getMenuInflater().inflate(R.menu.main, menu);
//	    return true;
//	}

	@Override
    public void onResume() {
    	super.onResume();
    }

}

