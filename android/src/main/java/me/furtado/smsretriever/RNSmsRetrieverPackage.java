package me.furtado.smsretriever;

import androidx.annotation.NonNull;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RNSmsRetrieverPackage implements ReactPackage {
  
  @Override
  @NonNull
  public List<ViewManager> createViewManagers(@NonNull final ReactApplicationContext context) {
    return Collections.emptyList();
  }
  
  @Override
  @NonNull
  public List<NativeModule> createNativeModules(@NonNull final ReactApplicationContext context) {
    List<NativeModule> modules = new ArrayList<>();
    modules.add(new RNSmsRetrieverModule(context));
    return modules;
  }
  
}
