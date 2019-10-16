package me.furtado.smsretriever;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public final class SmsBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_EVENT = "me.furtado.smsretriever:SmsEvent";

    private static final String EXTRAS_KEY = "extras";
    private static final String MESSAGE_KEY = "message";
    private static final String STATUS_KEY = "status";
    private static final String TIMEOUT_KEY = "timeout";

    private static final String EXTRAS_NULL_ERROR_MESSAGE = "Extras is null.";
    private static final String STATUS_NULL_ERROR_MESSAGE = "Status is null.";
    private static final String TIMEOUT_ERROR_MESSAGE = "Timeout error.";

    private ReactApplicationContext mContext;

    public SmsBroadcastReceiver(final ReactApplicationContext context) {
        mContext = context;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            final Bundle extras = intent.getExtras();
            if (extras == null) {
                emitJSEvent(EXTRAS_KEY, EXTRAS_NULL_ERROR_MESSAGE);
                return;
            }

            final Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            if (status == null) {
                emitJSEvent(STATUS_KEY, STATUS_NULL_ERROR_MESSAGE);
                return;
            }

            switch (status.getStatusCode()) {
                case CommonStatusCodes.SUCCESS: {
                    final String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    emitJSEvent(MESSAGE_KEY, message);
                    break;
                }

                case CommonStatusCodes.TIMEOUT: {
                    emitJSEvent(TIMEOUT_KEY, TIMEOUT_ERROR_MESSAGE);
                    break;
                }
            }
        }
    }

    //region - Privates

    private void emitJSEvent(@NonNull final String key, final String message) {
        if (mContext == null) {
            return;
        }

        if (!mContext.hasActiveCatalystInstance()) {
            return;
        }

        WritableNativeMap map = new WritableNativeMap();
        map.putString(key, message);

        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(SMS_EVENT, map);
    }

    //endregion

}
