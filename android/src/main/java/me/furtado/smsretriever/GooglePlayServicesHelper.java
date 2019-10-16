package me.furtado.smsretriever;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

final class GooglePlayServicesHelper {

    private static final int MINIMAL_VERSION = 10200000;

    static final String UNAVAILABLE_ERROR_TYPE = "UNAVAILABLE_ERROR_TYPE";
    static final String UNSUPORTED_VERSION_ERROR_TYPE = "UNSUPORTED_VERSION_ERROR_TYPE";

    static final String UNAVAILABLE_ERROR_MESSAGE = "Google Play Services is not available.";
    static final String UNSUPORTED_VERSION_ERROR_MESSAGE = "The device version of Google Play Services is not supported.";

    private GooglePlayServicesHelper() { }

    static boolean isAvailable(@NonNull final Context context) {
        final int result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);

        return (result != ConnectionResult.SERVICE_MISSING
                && result != ConnectionResult.SERVICE_DISABLED
                && result != ConnectionResult.SERVICE_INVALID);
    }

    static boolean hasSupportedVersion(@NonNull final Context context) {
        final PackageManager manager = context.getPackageManager();

        try {
            int version = manager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0).versionCode;
            return (version >= MINIMAL_VERSION);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

}
