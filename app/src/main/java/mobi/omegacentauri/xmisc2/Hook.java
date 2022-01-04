package mobi.omegacentauri.xmisc2;

import android.annotation.SuppressLint;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
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
        final boolean outlookEmailComposeOnly = true;
        final int opacity = 0xFF;

        if (outlookEmailComposeOnly && lpparam.packageName.equals("com.microsoft.office.outlook")) {

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

    }

    class Data {
        long lastLongClick = 0;
    }
}