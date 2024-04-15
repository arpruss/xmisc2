package mobi.omegacentauri.xmisc2;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XResources;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    static InputMethodService ims = null;
    final Hook.Data persistentData = new Hook.Data();
/*    static final Map<String,Object> noTabGroupOptions = new HashMap<String,Object>();
    static {
        noTabGroupOptions.put("tab_group_auto_creation", false);
        noTabGroupOptions.put("start_surface_enabled", true);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.TabGridLayoutAndroid:enable_tab_group_auto_creation", false);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:omnibox_focused_on_new_tab", true);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:show_last_active_tab_only", true);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:home_button_on_grid_tab_switcher", true);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:new_home_surface_from_home_button", "hide_tab_switcher_only");
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:hide_switch_when_no_incognito_tabs", true);
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:start_surface_variation", "single");
        noTabGroupOptions.put("Chrome.Flags.FieldTrialParamCached.StartSurfaceAndroid:tab_count_button_on_start_surface", true);
    } */

    @SuppressLint("NewApi")
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("handleLoadPackage "+lpparam.packageName);
        XSharedPreferences prefs = new XSharedPreferences(Options.class.getPackage().getName(), Options.PREFS);

        //final boolean blackStatusbar = prefs.getBoolean(Main.PREF_STAT_BAR, false);
        //final boolean blackNavbar = prefs.getBoolean(Main.PREF_NAV_BAR, true);
//        final boolean outlookEmailCompose = prefs.getBoolean(Options.PREF_OUTLOOK_COMPOSE, true);
        final boolean outlookSilence = prefs.getBoolean(Options.PREF_OUTLOOK_SILENCE, true);
        final boolean chromeMatchNavbar = prefs.getBoolean(Options.PREF_CHROME_MATCH_NAVBAR, true);
//        final boolean chromeNoTabGroup = prefs.getBoolean(Options.PREF_CHROME_KILL_TABGROUPS, false);
        final boolean longBackMenu = prefs.getBoolean(Options.PREF_LONG_BACK_MENU, false);
//        final boolean noWakeOnPlug =prefs.getBoolean(Options.PREF_NO_WAKE_ON_PLUG, false);

        XposedBridge.log("option longBackMenu "+longBackMenu);

        final int opacity = 0xFF;

        if (outlookSilence && lpparam.packageName.equals("com.microsoft.office.outlook")) {
            hookSilence(lpparam);
        }

        if (lpparam.packageName.equals("com.android.chrome")) {
            if (chromeMatchNavbar)
                hookChromeMatchNavbar(lpparam, true);
            //if (chromeNoTabGroup) hookChromeNoTabGroup(lpparam);
        }

//        if (noWakeOnPlug && lpparam.packageName.equals("android")) {
//            hookNoPluggedPower(lpparam);
//        }

//        if (lpparam.packageName.equals("android")) {
//            hookTilt(lpparam);
//        }

        if (longBackMenu)
            hookLongBack(lpparam);

//        if (lpparam.packageName.equals("com.fitness22.running"))
//            hookSocket(lpparam);

//        if (true && lpparam.packageName.equals("android")) hookPM(lpparam);
    }

    private void hookNoPluggedPower(LoadPackageParam lpparam) {
        final String packageName = lpparam.packageName;
        findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
                "getBoolean",
                int.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if ((int)param.args[0]==17891917 || ((int)param.args[0] == 17891369)) {
                            XposedBridge.log("intercepted to false");
                            param.setResult(false);
                        }
                    }
                });
    }

    private void hookTilt(LoadPackageParam lpparam) {
        XposedBridge.log("hooking tilt");
        final String packageName = lpparam.packageName;
        findAndHookMethod("android.content.res.Resources", lpparam.classLoader,
                "getIntArray",
                int.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Resources r = (Resources) param.thisObject;
                        int id = r.getIdentifier("android:array/config_autoRotationTiltTolerance",null,null);
                        int[] out = (int[]) param.getResult();
                        if (out != null) {
                            XposedBridge.log("getIntArray "+id+" "+(int)param.args[0]+" "+(int)out.length);
                        }
                    }
                });
    }

    private void hookSilence(LoadPackageParam lpparam) {
        XposedBridge.log("blocking playback");

/*
        findAndHookMethod("android.media.MediaPlayer", lpparam.classLoader,
                "setDataSource",
                AssetFileDescriptor.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("sDS asset"+((AssetFileDescriptor)param.args[0]).getFileDescriptor());
                    }
                });
        findAndHookMethod("android.media.MediaPlayer", lpparam.classLoader,
                "setDataSource",
                FileDescriptor.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("sDS file"+(FileDescriptor)param.args[0]);
                    }
                });
        findAndHookMethod("android.media.MediaPlayer", lpparam.classLoader,
                "setDataSource",
                String.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("sDS string "+(String)param.args[0]);
                    }
                }); */
        findAndHookMethod("android.media.MediaPlayer", lpparam.classLoader,
                "start",

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("blocking play");
                        param.setResult(null);
                    }
                });
    }

    private void hookSocket(LoadPackageParam lpparam) {
        XposedBridge.log("blocking sockets");

        findAndHookMethod("java.net.Socket", lpparam.classLoader,
                "connect",
                SocketAddress.class,
                int.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setThrowable(new IOException());
                    }
                });
        findAndHookMethod("java.net.Socket", lpparam.classLoader,
                "connect",
                SocketAddress.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setThrowable(new IOException());
                    }
                });
/*        XposedHelpers.findAndHookConstructor("java.net.Socket",
                lpparam.classLoader,
                InetAddress.class,
                Integer.class,
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("socket");
                        //throw(new IOException());
                    }

                }); */
    }

    private void hookPM(LoadPackageParam lpparam) {
        XposedBridge.log("hooking checkUidPermission");
        findAndHookMethod("com.android.server.pm.permission.PermissionManagerService", lpparam.classLoader,
                "checkUidPermission",
                int.class,
                String.class,
                int.class,

                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int uid = (int) param.args[0];
                        String perm = (String) param.args[1];
                        if ((uid == 10275 || uid == 10316) && perm.endsWith("INTERNET")) {
                            XposedBridge.log("check "+uid+" "+perm+" "+(uid == 10275 || uid == 10316)+" "+perm.endsWith("INTERNET"));
                            XposedBridge.log("block");
                            param.setResult(PackageManager.PERMISSION_DENIED);
                        }
                    }
                });
/*        findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader,
                "checkPermission",
                String.class,
                String.class,
                int.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("check "+(String)param.args[0]+" "+(String)param.args[1]+" "+(Integer)param.args[2]);
                    }
                }); */
    }

    private void hookLongBack(LoadPackageParam lpparam) {
        XposedBridge.log("hooking for long back menu");
        findAndHookMethod("android.app.Activity", lpparam.classLoader,
                "openOptionsMenu",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        ((Activity)param.thisObject).closeOptionsMenu();
                        ((Activity)param.thisObject).invalidateOptionsMenu();
                    }
                });
        findAndHookMethod("android.app.Activity", lpparam.classLoader,
                "onKeyLongPress",
                int.class,
                android.view.KeyEvent.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if ((Integer)param.args[0] == 4) {
                            ((Activity)param.thisObject).openOptionsMenu();
                        }
                    }
                });
    }

    /*
    private void hookChromeNoTabGroup(LoadPackageParam lpparam) {
        findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader, "getBoolean",
                String.class,
                boolean.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (noTabGroupOptions.containsKey(param.args[0])) {
                            Object value = noTabGroupOptions.get(param.args[0]);
                            if (value instanceof Boolean) {
                                param.setResult(value);
                            }
                        }
                    }
                });

        findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader, "getString",
                String.class,
                String.class,

                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (noTabGroupOptions.containsKey(param.args[0])) {
                            Object value = noTabGroupOptions.get(param.args[0]);
                            if (value instanceof String) {
                                param.setResult(value);
                            }
                        }
                    }
                });

        findAndHookMethod("android.view.ListView", lpparam.classLoader,
                        "setAdapter",
                        ListAdapter.class,

                        new XC_MethodHook() {
                            @SuppressLint("InlinedApi")
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                int id = ((View) param.thisObject).getId();
                                if (id != View.NO_ID && ((View) param.thisObject).getResources().getResourceName(id).equals("com.android.chrome:id/context_menu_list_view")) {
                                    final ListAdapter oldAdapter = (ListAdapter) param.args[0];
                                    if (oldAdapter != null) {
                                        param.args[0] = new ListAdapter() {

                                            int disabledItem = -1;

                                            @Override
                                            public void registerDataSetObserver(DataSetObserver dataSetObserver) {
                                                oldAdapter.registerDataSetObserver(dataSetObserver);
                                            }

                                            @Override
                                            public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
                                                oldAdapter.unregisterDataSetObserver(dataSetObserver);
                                            }

                                            @Override
                                            public int getCount() {
                                                return oldAdapter.getCount();
                                            }

                                            @Override
                                            public Object getItem(int i) {
                                                return oldAdapter.getItem(i);
                                            }

                                            @Override
                                            public long getItemId(int i) {
                                                return oldAdapter.getItemId(i);
                                            }

                                            @Override
                                            public boolean hasStableIds() {
                                                return oldAdapter.hasStableIds();
                                            }

                                            @Override
                                            public View getView(int i, View view, ViewGroup viewGroup) {
                                                View v = oldAdapter.getView(i, view, viewGroup);
                                                try {
                                                    if (v instanceof TextView) {
                                                        if (((TextView) v).getText().equals("Open in new tab in group")
                                                                || (i==2 && ! Locale.getDefault().getLanguage().equals("en"))) {
                                                            disabledItem = i;
                                                            ((TextView) v).setText("");
                                                        }

                                                    }
                                                } catch (Exception e) {

                                                }
                                                return v;
                                            }

                                            @Override
                                            public int getItemViewType(int i) {
                                                return oldAdapter.getItemViewType(i);
                                            }

                                            @Override
                                            public int getViewTypeCount() {
                                                return oldAdapter.getViewTypeCount();
                                            }

                                            @Override
                                            public boolean isEmpty() {
                                                return oldAdapter.isEmpty();
                                            }

                                            @Override
                                            public boolean areAllItemsEnabled() {
                                                return oldAdapter.areAllItemsEnabled();
                                            }

                                            @Override
                                            public boolean isEnabled(int i) {
                                                if (i == disabledItem) {
                                                    return false;
                                                }
                                                return oldAdapter.isEnabled(i);
                                            }
                                        };
                                    }
                                }
                            }

                        });

    }
     */

    private void hookChromeMatchNavbar(LoadPackageParam lpparam, final boolean snapToBlackAndWhite) {


        findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                "setStatusBarColor",
                int.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int color = (int)param.args[0];
                        if (snapToBlackAndWhite) {
                            final int dividerColor;

                            if (Color.luminance(color) >= 0.5f) {
                                color = 0xFFFFFFFF;
                                dividerColor = 0xFFE1E3E1;
                            }
                            else {
                                color = 0xFF000000;
                                dividerColor = 0;
                            }
                            ((Window) param.thisObject).setNavigationBarDividerColor(dividerColor);
                        }

                        ((Window) param.thisObject).setNavigationBarColor(color);
                    }
                });

        /*
        findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                "generateDecor",
                int.class,
                new XC_MethodHook() {

                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ((Window) param.thisObject).clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                        ((Window) param.thisObject).addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        ((Window) param.thisObject).setNavigationBarColor(((Window)param.thisObject).getStatusBarColor());
                        View decor = (View) param.getResult();
                        int suiv = decor.getSystemUiVisibility();
                        if ((suiv & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0) {
                            decor.setSystemUiVisibility(suiv | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                        else {
                            decor.setSystemUiVisibility(suiv & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                    }
                }); */
    }

    static public boolean isThisTheComposeButton(View v) {
        int id = v.getId();
        if (id != View.NO_ID) {
            try {
                if (v.getResources().getResourceName(id).equals("com.microsoft.office.outlook:id/compose_fab")) {
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }



    private void hookOutlookCompose(LoadPackageParam lpparam) {
/*        findAndHookMethod("android.content.res.Resources", lpparam.classLoader, "getText", int.class,
                //CharSequence.class,
                new XC_MethodHook() {
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (persistentData.composeTitle == null) {
                            String name = ((Resources) param.thisObject).getResourceName((int) param.args[0]);
                            XposedBridge.log("look up "+param.args[0]+" "+ name+ " "+param.getResult());
                            if (name.equals("com.microsoft.office.outlook:string/compose_title")) {
                                persistentData.composeTitle = (String) param.getResult();
                            }
                        }
                    }

                }); */

        findAndHookMethod("android.view.View", lpparam.classLoader,
                "setOnClickListener",
                View.OnClickListener.class,

                new XC_MethodHook() {
                    @SuppressLint({"InlinedApi", "ResourceType"})
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (//persistentData.composeTitle != null &&
                                persistentData.lastLongClick + 500 < SystemClock.uptimeMillis() && param.thisObject.getClass().toString().equals("class androidx.constraintlayout.widget.ConstraintLayout")) {
                            ViewGroup v = (ViewGroup) param.thisObject;
                            if (v.getId() == View.NO_ID && v.getChildCount() == 2) {
                                Object c0 = v.getChildAt(0);

                                if (c0.getClass().toString().equals("class androidx.appcompat.widget.AppCompatTextView") &&
                                        v.getChildAt(1).getClass().toString().equals("class com.google.android.material.floatingactionbutton.FloatingActionButton")) {

                                    CharSequence text = ((TextView) c0).getText();

                                    if (Locale.getDefault().getLanguage().equals("en")) {
                                        if (! text.equals("New message"))
                                            return;
                                    }
                                    else {
                                        if (! v.getResources().getText(2131887358).equals(text))
                                            return;
                                    }
                                    v.performClick();
                                }
                            }
                        }
                    }

                });

        findAndHookMethod("android.view.View", lpparam.classLoader,
                "setOnLongClickListener",
                View.OnLongClickListener.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (isThisTheComposeButton((View) param.thisObject)) {
                            if (param.args[0] == null) {
                                final View v = (View) param.thisObject;
                                param.args[0] = new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        persistentData.lastLongClick = SystemClock.uptimeMillis();
                                        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                                        v.performClick();
                                        return false;
                                    }
                                };
                            }
                        }
                    }
                });
        findAndHookMethod("android.view.View", lpparam.classLoader,
                "setLongClickable",
                boolean.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (isThisTheComposeButton((View) param.thisObject)) {
                            param.args[0] = true;
                        }
                    }
                });
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log("init zygote3");
        XSharedPreferences prefs = new XSharedPreferences(Options.class.getPackage().getName(), Options.PREFS);
        final boolean noWakeOnPlug =prefs.getBoolean(Options.PREF_NO_WAKE_ON_PLUG, false);
//        final boolean strictRotation = true;
        if (noWakeOnPlug)
            XResources.setSystemWideReplacement("android", "bool", "config_unplugTurnsOnScreen", false);
//        final int[] rotationTilt = { -10, 10,   -10,10,  -10,10,  -10,10 };
//        XposedBridge.log("init ");
//        if (strictRotation) {
//            XposedBridge.log("replacing rot");
//            XResources.setSystemWideReplacement("android", "array", "config_autoRotationTiltTolerance",
//                    rotationTilt); // TODO! Does not work.
//            XposedBridge.log("replaced rot");
//        }

    }

    class Data {
        long lastLongClick = 0;
    }
}