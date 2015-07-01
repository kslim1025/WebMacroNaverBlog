package net.asamaru.webmacronaverblog;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.squareup.otto.Bus;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EBean(scope = EBean.Scope.Singleton)
public class Advisor implements Application.ActivityLifecycleCallbacks {
	static public String deviceUuid;

	static private Bus mBus;
	static private WMApplication mApp;
	static private Dialog mLoading;
	static private Activity mActiveActivity;
	static private boolean mDebugable;
	static private float density;

	static private Toast toast;
//	static CookieManager cookieManager;

//	@Bean
//	EventBus eventBus;

	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

	public Advisor() {
	}

	@AfterInject
	void afterInject() {
		mBus = new Bus();
//		mBus.register(eventBus);
	}

	static public void registerEventListener(Object listener) {
		mBus.register(listener);
	}

	static public void unregisterEventListener(Object listener) {
		mBus.unregister(listener);
	}

	static public void postEvent(final Object event) {
		Handler handler_ = new Handler(Looper.getMainLooper());
		handler_.post(new Runnable() {
			@Override
			public void run() {
				mBus.post(event);
			}
		});

	}

	@SuppressWarnings("deprecation")
	public void setApp(WMApplication app) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			CookieSyncManager.createInstance(app.getApplicationContext());
		}

		Advisor.deviceUuid = Settings.Secure.getString(app.getContentResolver(), Settings.Secure.ANDROID_ID);
		mApp = app;
		mApp.registerActivityLifecycleCallbacks(this);

		mDebugable = BuildConfig.DEBUG;
		density = app.getResources().getDisplayMetrics().density;
	}

	static public String getDeviceUUID() {
		return Advisor.deviceUuid;
	}

	static public WMApplication getApp() {
		return mApp;
	}

	static public String getAppVersion() {
		Context context = getAppContext();
		String appVersion;
		try {
			PackageInfo i = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			appVersion = i.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			appVersion = "undefined";
		}
		return appVersion;
	}

	private static int _generateViewId() {
		for (;;) {
			final int result = sNextGeneratedId.get();
			// aapt-generated IDs have the high byte nonzero; clamp to the range under that.
			int newValue = result + 1;
			if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
			if (sNextGeneratedId.compareAndSet(result, newValue)) {
				return result;
			}
		}
	}

	static public int generateViewId() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return  _generateViewId();
		} else {
			return View.generateViewId();
		}
	}

	static public int dpToPixel(int dp) {
		//return TypedValue.complexToDimensionPixelSize(dp, mApp.getResources().getDisplayMetrics());
		return (int) (dp * density);
	}

	static public boolean isDebugable() {
		return mDebugable;
	}

	static public Context getAppContext() {
		return mApp.getApplicationContext();
	}

	static public Resources getResources() {
		return mApp.getResources();
	}

	@SuppressWarnings("deprecation")
	static public Drawable getDrawable(int id) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {    // stock browser
			return mApp.getResources().getDrawable(id);
		} else {
			return mApp.getResources().getDrawable(id, mApp.getTheme());
		}
	}

	static public SharedPreferences getSharedPreferences() {
		return mApp.getSharedPreferences("PrefB9", Context.MODE_PRIVATE);
	}

	// ---------------------------------------------------------------

	/**
	 * 로딩중 표시
	 */
	static public void showLoading() {
		showCancelableLoading(false);
	}

	static public void showCancelableLoading() {
		showCancelableLoading(true);
	}

	static protected void showCancelableLoading(final boolean cancelable) {
		Handler handler_ = new Handler(Looper.getMainLooper());
		handler_.post(new Runnable() {
			@Override
			public void run() {
				if (mLoading == null) {
					mLoading = new Dialog(mActiveActivity, R.style.NewDialog);
					mLoading.setCancelable(cancelable);
					mLoading.addContentView(
							new ProgressBar(mActiveActivity),
							new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
					mLoading.show();
				}
			}
		});
	}

	/**
	 * 로딩중 숨김
	 */
	static public void hideLoading() {
		Handler handler_ = new Handler(Looper.getMainLooper());
		handler_.post(new Runnable() {
			@Override
			public void run() {
				if (mLoading != null) {
					mLoading.dismiss();
					mLoading = null;
				}
			}
		});
	}

	static public void showToast(final String message) {
		Handler handler_ = new Handler(Looper.getMainLooper());
		handler_.post(new Runnable() {
			@Override
			public void run() {
				if (toast != null) {
					toast.cancel();
				}
				toast = Toast.makeText(mActiveActivity, message, Toast.LENGTH_SHORT);
				toast.show();
//				Toast.makeText(mApp.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	static public Activity getActiveActivity() {
		return mActiveActivity;
	}

	static public void startActivity(Intent intent) {
		mActiveActivity.startActivity(intent);
	}

	static public void runOnUiThread(Runnable runnable) {
		mActiveActivity.runOnUiThread(runnable);
	}

	static public String getQDN(String url) {
		Pattern p = Pattern.compile("([A-Za-z0-9][A-Za-z0-9\\-]{1,63}\\.[A-Za-z\\.]{2,6})$");
		Matcher m = p.matcher(url);
		if (m.find()) {
			String qdn = m.group(0);
			if (isDebugable()) {
				p = Pattern.compile("([^\\.]+\\.corez\\.kr)$");
				m = p.matcher(url);
				if (m.find()) {
					return m.group(0);
				}
			}
			return qdn;
		}
		int pos = url.indexOf("://");
		return url.substring((pos < 0) ? 0 : pos + 3);
	}

//	static public void setCookie(String cookie) {
//		cookieManager.setCookie(Advisor.getB9Url(), cookie);
//	}

	// ---------------------------------------------------------------
	// interface Application.ActivityLifecycleCallbacks

	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		// Logger.d("activity created " + activity.toString());
	}

	public void onActivityStarted(Activity activity) {
		// Logger.d("activity started " + activity.toString());
	}

	public void onActivityResumed(Activity activity) {
		// Logger.d("activity resumed " + activity.toString());
		mActiveActivity = activity;
	}

	public void onActivityPaused(Activity activity) {
		// Logger.d("activity paused " + activity.toString());
	}

	public void onActivityStopped(Activity activity) {
		// Logger.d("activity stopped " + activity.toString());
	}

	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
		// Logger.d("activity save instance " + activity.toString());
	}

	public void onActivityDestroyed(Activity activity) {
		// Logger.d("activity destroyed " + activity.toString());
	}
}