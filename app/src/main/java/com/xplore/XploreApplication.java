import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Nika on 7/17/2017.
 * TODO write description of this class - what it does and why.
 */

public class XploreApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
