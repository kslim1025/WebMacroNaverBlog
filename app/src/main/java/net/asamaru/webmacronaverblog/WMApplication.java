package net.asamaru.webmacronaverblog;

import android.app.Application;

import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

@ReportsCrashes(
		formKey = "", // This is required for backward compatibility but not used
		formUri = "http://dev.coregisul.kr/rpc/tracker4app.php?trackerId=AppCrashB9"
)
@EApplication
public class WMApplication extends Application {
	@Bean
	Advisor advisor;

	@Override
	public void onCreate() {
		super.onCreate();

		//if (2 != (this.getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
		if (!BuildConfig.DEBUG) {
			ACRA.init(this); // The following line triggers the initialization of ACRA
			Logger.init().setLogLevel(LogLevel.NONE);  // default : LogLevel.FULL
		} else {
			Logger.init("WMLOG")               // default tag : PRETTYLOGGER or use just init()
					.setMethodCount(3)            // default 2
					.hideThreadInfo()             // default it is shown
					.setLogLevel(LogLevel.FULL);  // default : LogLevel.FULL
		}

		advisor.setApp(this);    // advisor에 app 세팅

		//Iconics.registerFont(new FontAwesome());
		//Iconics.registerFont(new GoogleMaterial());

		//finalTest f = new finalTest();
		//f.someMethod(1);
		//f = null;
	}
}
