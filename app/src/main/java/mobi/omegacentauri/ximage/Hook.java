package mobi.omegacentauri.ximage;

import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.newInstance;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XResources;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.inputmethodservice.InputMethodService;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class Hook implements IXposedHookLoadPackage, IXposedHookZygoteInit {
    final Hook.Data persistentData = new Hook.Data();

    @SuppressLint("NewApi")
    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        XposedBridge.log("ximage handleLoadPackage "+lpparam.packageName);
        /*        XSharedPreferences prefs = new XSharedPreferences(Options.class.getPackage().getName(), Options.PREFS); */
        hookWebKitKiller(lpparam);
    }

    private void hookWebKitKiller(LoadPackageParam lpparam) {
        final String packageName = lpparam.packageName;
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadUrl",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("ximage loadUrl "+(String)param.args[0]);
                        param.setResult(null);
                    }
                });
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "postUrl",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("ximage postUrl"+(String)param.args[0]);
                        param.setResult(null);
                    }
                });
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadDataWithBaseURL",
                new XC_MethodHook() {
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("ximage WebView.loadDataWithBaseURL");
                        param.setResult(null);
                    }
                });
        XposedBridge.log("loadData");
        XposedBridge.hookAllMethods(android.webkit.WebView.class,
                "loadData",
                new XC_MethodHook() {
                    @SuppressLint("InlinedApi")
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("ximage WebView.loadData");
                        param.setResult(null);
                    }
                });
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }


    class Data {
        long unusued = 0;
    }
}