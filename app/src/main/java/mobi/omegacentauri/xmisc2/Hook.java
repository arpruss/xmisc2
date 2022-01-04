package mobi.omegacentauri.xmisc2;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class Hook implements IXposedHookLoadPackage {
    static InputMethodService ims = null;
    final Hook.Data persistentData = new Hook.Data();

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

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XSharedPreferences prefs = new XSharedPreferences(Main.class.getPackage().getName(), Main.PREFS);

        //final boolean blackStatusbar = prefs.getBoolean(Main.PREF_STAT_BAR, false);
        //final boolean blackNavbar = prefs.getBoolean(Main.PREF_NAV_BAR, true);
        final boolean outlookEmailCompose = prefs.getBoolean(Main.PREF_OUTLOOK_COMPOSE, true);
        final boolean chromeMatchNavbar = prefs.getBoolean(Main.PREF_CHROME_MATCH_NAVBAR, true);
        final int opacity = 0xFF;

        if (outlookEmailCompose && lpparam.packageName.equals("com.microsoft.office.outlook")) {
            hookOutlookCompose(lpparam);
        }

        if (chromeMatchNavbar && lpparam.packageName.equals("com.android.chrome")) {
            hookChromeMatchNavbar(lpparam, true);
        }

    }

    private void hookChromeMatchNavbar(LoadPackageParam lpparam, final boolean snapToBlackAndWhite) {
        findAndHookMethod("com.android.internal.policy.PhoneWindow", lpparam.classLoader,
                "setStatusBarColor",
                int.class,
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        int color = (int)param.args[0];
                        if (snapToBlackAndWhite) {
                            if (Color.luminance(color) >= 0.5f) {
                                color = 0xFFFFFFFF;
                            }
                            else {
                                color = 0xFF000000;
                            }
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

    private void hookOutlookCompose(LoadPackageParam lpparam) {
        findAndHookMethod("android.view.View", lpparam.classLoader,
                "setOnClickListener",
                View.OnClickListener.class,

                new XC_MethodHook() {
/*                        @SuppressLint("InlinedApi")
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (isThisTheComposeButton((View)param.thisObject)) {
                                final View.OnClickListener oldListener = (View.OnClickListener) param.args[0];
                                param.args[0] = new View.OnClickListener(){
                                    @Override
                                    public void onClick(View view) {
                                        XposedBridge.log("short click");
                                        persistentData.lastComposeClickWasShort = true;
                                        oldListener.onClick(view);
                                    }
                                };
                            }
                        } */

                    @SuppressLint("InlinedApi")
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (persistentData.lastLongClick + 500 < SystemClock.uptimeMillis() && param.thisObject.getClass().toString().equals("class androidx.constraintlayout.widget.ConstraintLayout")) {
                            ViewGroup v = (ViewGroup) param.thisObject;
                            if (v.getId() == View.NO_ID && v.getChildCount() == 2) {
                                Object c0 = v.getChildAt(0);
                                if (c0.getClass().toString().equals("class androidx.appcompat.widget.AppCompatTextView") &&
                                        v.getChildAt(1).getClass().toString().equals("class com.google.android.material.floatingactionbutton.FloatingActionButton")
                                        && ((TextView) c0).getText().equals("New message")) {
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
                                XposedBridge.log("null was requested");
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

    class Data {
        long lastLongClick = 0;
    }
}