package me.furtado.smsretriever;

import android.app.Activity;
import androidx.annotation.NonNull;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

final class RNSmsRetrieverModule extends ReactContextBaseJavaModule {

  private final PhoneNumberHelper mPhoneNumberHelper = new PhoneNumberHelper();
  private final SmsHelper mSmsHelper;

  RNSmsRetrieverModule(@NonNull final ReactApplicationContext context) {
    super(context);
    mSmsHelper = new SmsHelper(context);
  }

  //region - ReactContextBaseJavaModule
  
  @Override
  @NonNull
  public String getName() {
    return "RNSmsRetrieverModule";
  }

  //endregion

  //region - ReactMethod

  @SuppressWarnings("unused")
  @ReactMethod
  public void requestPhoneNumber(final Promise promise) {
    final ReactApplicationContext context = getReactApplicationContext();
    final Activity activity = getCurrentActivity();
    final ActivityEventListener eventListener = mPhoneNumberHelper.getActivityEventListener();

    context.addActivityEventListener(eventListener);

    mPhoneNumberHelper.setListener(new PhoneNumberHelper.Listener() {
      @Override
      public void phoneNumberResultReceived() {
        context.removeActivityEventListener(eventListener);
      }
    });

    mPhoneNumberHelper.requestPhoneNumber(context, activity, promise);
  }

  @SuppressWarnings("unused")
  @ReactMethod
  public void startSmsRetriever(final Promise promise) {
    mSmsHelper.startRetriever(promise);
  }

  //endregion


}
