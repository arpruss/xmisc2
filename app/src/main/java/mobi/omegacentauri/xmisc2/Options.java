package mobi.omegacentauri.xmisc2;

import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;

public class Options extends PreferenceActivity {
    static final String PREFS = "preferences";
    static final String PREF_OUTLOOK_COMPOSE = "outlook_compose";
    static final String PREF_CHROME_MATCH_NAVBAR = "chrome_match_navbar";
    static final String PREF_CHROME_KILL_TABGROUPS = "chrome_kill_tabgroups";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(PREFS);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.options);

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setNavigationBarColor(Color.BLACK);
    }
}
