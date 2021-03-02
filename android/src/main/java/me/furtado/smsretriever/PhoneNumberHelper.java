package me.furtado.smsretriever;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

final class PhoneNumberHelper {

    private static final int REQUEST_PHONE_NUMBER_REQUEST_CODE = 1;

    private static final String ACTIVITY_NULL_ERROR_TYPE = "ACTIVITY_NULL_ERROR_TYPE";
    private static final String ACTIVITY_RESULT_NOOK_ERROR_TYPE = "ACTIVITY_RESULT_NOOK_ERROR_TYPE";
    private static final String CONNECTION_SUSPENDED_ERROR_TYPE = "CONNECTION_SUSPENDED_ERROR_TYPE";
    private static final String CONNECTION_FAILED_ERROR_TYPE = "CONNECTION_FAILED_ERROR_TYPE";
    private static final String SEND_INTENT_ERROR_TYPE = "SEND_INTENT_ERROR_TYPE";

    private static final String ACTIVITY_NULL_ERROR_MESSAGE = "Activity is null.";
    private static final String ACTIVITY_RESULT_NOOK_ERROR_MESSAGE = "There was an error trying to get the phone number.";
    private static final String CONNECTION_SUSPENENDED_ERROR_MESSAGE = "Client is temporarily in a disconnected state.";
    private static final String CONNECTION_FAILED_ERROR_MESSAGE = "There was an error connecting the client to the service.";
    private static final String SEND_INTENT_ERROR_MESSAGE = "There was an error trying to send intent.";

    private GoogleApiClient mGoogleApiClient;
    private Promise mPromise;
    private Listener mListener;


    PhoneNumberHelper() { }

    //region - Package Access

    ActivityEventListener getActivityEventListener() {
        return mActivityEventListener;
    }

    void setListener(@NonNull final Listener listener) {
        mListener = listener;
    }

    boolean isAvailable(@NonNull final Context context) {
        if (!GooglePlayServicesHelper.isAvailable(context)) {
            promiseReject(GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE, GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE);
            callAndResetListener();
            return false;
        }

        if (!GooglePlayServicesHelper.hasSupportedVersion(context)) {
            promiseReject(GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE, GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE);
            callAndResetListener();
            return false;
        }

        return true;
    }

    void requestPhoneNumber(@NonNull final Context context, final Activity activity, final Promise promise) {
        if (promise == null) {
            callAndResetListener();
            return;
        }

        mPromise = promise;

        if (!isAvailable(context)) {
            return;
        }

        if (activity == null) {
            promiseReject(ACTIVITY_NULL_ERROR_TYPE, ACTIVITY_NULL_ERROR_MESSAGE);
            callAndResetListener();
            return;
        }

        final HintRequest request = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        final GoogleApiClient googleApiClient = getGoogleApiClient(context);

        final PendingIntent intent = Auth.CredentialsApi
                .getHintPickerIntent(googleApiClient, request);

        try {
            activity.startIntentSenderForResult(intent.getIntentSender(),
                    REQUEST_PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            promiseReject(SEND_INTENT_ERROR_TYPE, SEND_INTENT_ERROR_MESSAGE);
            callAndResetListener();
        } finally {
            if (googleApiClient.isConnected()) {
                googleApiClient.disconnect();
            }
        }
    }

    //endregion

    //region - Privates

    @NonNull
    private GoogleApiClient getGoogleApiClient(@NonNull final Context context) {
        if (mGoogleApiClient == null) {
            GoogleApiClient.Builder builder = new GoogleApiClient.Builder(context);
            builder = builder.addConnectionCallbacks(mApiClientConnectionCallbacks);
            builder = builder.addApi(Auth.CREDENTIALS_API);
/*
            if (activity instanceof FragmentActivity) {
                final FragmentActivity fragmentActivity = (FragmentActivity) activity;
                builder = builder.enableAutoManage(fragmentActivity, mApiClientOnConnectionFailedListener);
            }
*/
            mGoogleApiClient = builder.build();
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        return mGoogleApiClient;
    }

    private void callAndResetListener() {
        if (mListener != null) {
            mListener.phoneNumberResultReceived();
            mListener = null;
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

    //region - Callbacks and Listeners

    private final GoogleApiClient.ConnectionCallbacks mApiClientConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) { }

        @Override
        public void onConnectionSuspended(int i) {
            promiseReject(CONNECTION_SUSPENDED_ERROR_TYPE, CONNECTION_SUSPENENDED_ERROR_MESSAGE);
            callAndResetListener();
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mApiClientOnConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            promiseReject(CONNECTION_FAILED_ERROR_TYPE, CONNECTION_FAILED_ERROR_MESSAGE);
            callAndResetListener();
        }
    };

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            super.onActivityResult(activity, requestCode, resultCode, data);

            if (requestCode == REQUEST_PHONE_NUMBER_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    final Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    final String phoneNumber = credential.getId();
                    promiseResolve(phoneNumber);
                    callAndResetListener();
                    return;
                }
            }

            promiseReject(ACTIVITY_RESULT_NOOK_ERROR_TYPE, ACTIVITY_RESULT_NOOK_ERROR_MESSAGE);
            callAndResetListener();
        }
    };

    //endregion

    //region - Classes

    public interface Listener {

        void phoneNumberResultReceived();

    }

    //endregion

}
