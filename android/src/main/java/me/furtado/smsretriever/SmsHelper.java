package me.furtado.smsretriever;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

final class SmsHelper {

    private static final String TASK_FAILURE_ERROR_TYPE = "TASK_FAILURE_ERROR_TYPE";
    private static final String TASK_FAILURE_ERROR_MESSAGE = "Task failed.";

    private final ReactApplicationContext mContext;

    private BroadcastReceiver mReceiver;
    private Promise mPromise;

    SmsHelper(@NonNull final ReactApplicationContext context) {
        mContext = context;
    }

    //region - Package Access

    void startRetriever(final Promise promise) {
        mPromise = promise;

        if (!GooglePlayServicesHelper.isAvailable(mContext)) {
            promiseReject(GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE, GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE);
            return;
        }

        if (!GooglePlayServicesHelper.hasSupportedVersion(mContext)) {
            promiseReject(GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE, GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE);
            return;
        }

        final SmsRetrieverClient client = SmsRetriever.getClient(mContext);
        final Task<Void> task = client.startSmsRetriever();
        task.addOnSuccessListener(mOnSuccessListener);
        task.addOnFailureListener(mOnFailureListener);
    }

    //endregion

    //region - Privates

    private boolean tryToRegisterReceiver() {
        mReceiver = new SmsBroadcastReceiver(mContext);

        final IntentFilter intentFilter = new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION);

        try {
            mContext.registerReceiver(mReceiver, intentFilter);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void unregisterReceiverIfNeeded() {
        if (mReceiver == null) {
            return;
        }

        try {
            mContext.unregisterReceiver(mReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //endregion

    //region - Promises

    private void promiseResolve(@NonNull final Object value) {
        if (mPromise != null) {
            mPromise.resolve(value);
            mPromise = null;
        }
    }

    private void promiseReject(@NonNull final String type, @NonNull final String message) {
        if (mPromise != null) {
            mPromise.reject(type, message);
            mPromise = null;
        }
    }

    //endregion

    //region - Listeners

    private final OnSuccessListener<Void> mOnSuccessListener = new OnSuccessListener<Void>() {
        @Override
        public void onSuccess(Void aVoid) {
            final boolean registered = tryToRegisterReceiver();
            promiseResolve(registered);
        }
    };

    private final OnFailureListener mOnFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            unregisterReceiverIfNeeded();
            promiseReject(TASK_FAILURE_ERROR_TYPE, TASK_FAILURE_ERROR_MESSAGE);
        }
    };

    //endregion

}
