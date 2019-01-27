package com.facebook.react.uimanager;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.views.textinput.ReactEditText;
import com.facebook.react.ReactInstanceManager;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RNAllmaxKeyboardModule extends ReactContextBaseJavaModule {
  private static final String TAG = "RNAllmaxKeyboardModule";
  private static final int DEFAULT_TIMEOUT = 300;
  private final ReactApplicationContext reactContext;

  private Method setShowSoftInputOnFocusMethod;
  private Method setSoftInputShownOnFocusMethod;

  private ConcurrentHashMap<String, ReactEditText> edits =  new ConcurrentHashMap<String, ReactEditText>();
  private ConcurrentHashMap<String, RelativeLayout> keyboardLayouts =  new ConcurrentHashMap<String, RelativeLayout>();
  private Handler mHandler = new Handler(Looper.getMainLooper());

  public RNAllmaxKeyboardModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    initReflectMethod();
  }

  private void initReflectMethod () {
    Class<ReactEditText> cls = ReactEditText.class;
    try {
      setShowSoftInputOnFocusMethod = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
      setShowSoftInputOnFocusMethod.setAccessible(true);
    } catch (Exception e) {
      Log.i(TAG, "initReflectMethod 1  err=" + e.getMessage());
    }
    try {
      setSoftInputShownOnFocusMethod = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
      setSoftInputShownOnFocusMethod.setAccessible(true);
    } catch (Exception e) {
      Log.i(TAG, "initReflectMethod 2  err=" + e.getMessage());
    }
  }

  private ReactEditText getEditById(int id) throws IllegalViewOperationException {
    UIViewOperationQueue uii = this.getReactApplicationContext().getNativeModule(UIManagerModule.class).getUIImplementation().getUIViewOperationQueue();
    return (ReactEditText) uii.getNativeViewHierarchyManager().resolveView(id);
  }

  private void showKeyboard (final Activity activity, final int tag) {
    final ResultReceiver receiver = new ResultReceiver(mHandler) {
      @Override
      protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == InputMethodManager.RESULT_UNCHANGED_HIDDEN || resultCode == InputMethodManager.RESULT_HIDDEN) {
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              final ReactEditText edit = edits.get(String.valueOf(tag));
              final RelativeLayout keyboardLayout = keyboardLayouts.get(String.valueOf(tag));

              if (keyboardLayout != null && edit != null  && edit.isFocused()) {
                if (keyboardLayout.getParent() != null) {
                  ((ViewGroup)keyboardLayout.getParent()).removeView(keyboardLayout);
                }
                activity.addContentView(keyboardLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
              }
            }
          }, 5);
        }
      }
    };

    mHandler.post(new Runnable() {
      @Override
      public void run() {
        InputMethodManager im = ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE));
        im.hideSoftInputFromWindow(activity.getWindow().getDecorView().getWindowToken(), 0, receiver);
      }
    });
  }

  public void disableShowSoftInput(ReactEditText editText) {
    try {
      setShowSoftInputOnFocusMethod.invoke(editText, false);
    } catch (Exception e) {
      Log.i(TAG, "disableShowSoftInput 1  err=" + e.getMessage());
    }

    try {
      setSoftInputShownOnFocusMethod.invoke(editText, false);
    } catch (Exception e) {
      Log.i(TAG, "disableShowSoftInput 2  err=" + e.getMessage());
    }
  }

  private void setEditTextTagAndListener (final ReactEditText edit, final int tag, final String type, final int keyboardHeight) {
    final Activity activity = getCurrentActivity();
    if (edit == null || activity == null) {
      Log.e(TAG, "setEditTextListener error null, edit=" + edit);
      return;
    }
    disableShowSoftInput(edit);
    final RelativeLayout keyboardLayout = createCustomKeyboard(activity, tag, type, keyboardHeight);

    edits.put(String.valueOf(tag), edit);
    keyboardLayouts.put(String.valueOf(tag), keyboardLayout);

    final View.OnFocusChangeListener oldOnFocusChangeListener = edit.getOnFocusChangeListener();
    edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override
      public void onFocusChange(final View v, boolean hasFocus) {
        Log.i(TAG, "onFocusChange hasFocus=" + hasFocus );
        if (hasFocus) {
          showKeyboard(activity, tag);
        } else {
          if (keyboardLayout.getParent() != null) {
            ((ViewGroup) keyboardLayout.getParent()).removeView(keyboardLayout);
          }
        }
        oldOnFocusChangeListener.onFocusChange(v, hasFocus);
      }
    });

    edit.setOnClickListener(new View.OnClickListener(){
      @Override
      public void onClick(final View v) {
        WritableMap params = Arguments.createMap();
        params.putInt("reactTag", tag);
        sendEvent(reactContext, "TouchDownTextField", params);
      }
    });
  }

  @ReactMethod
  public void installKeyboard(final String type, final int tag, final int keyboardHeight, final @Nullable Callback successCallback, final @Nullable Callback errorCallback) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        try {
          ReactEditText edit = getEditById(tag);
          setEditTextTagAndListener(edit, tag, type, keyboardHeight);
          if (successCallback != null) {
            successCallback.invoke();
          }
        } catch (IllegalViewOperationException e) {
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              try {
                final ReactEditText edit = getEditById(tag);
                setEditTextTagAndListener(edit, tag, type, keyboardHeight);
                if (successCallback != null) {
                  successCallback.invoke();
                }
              } catch (IllegalViewOperationException err) {
                Log.e(TAG, err.toString());
                if (errorCallback != null) {
                  errorCallback.invoke(err.getMessage());
                }
              }
            }
          }, DEFAULT_TIMEOUT);
        }
      }
    });
  }

  private RelativeLayout createCustomKeyboard(Activity activity, int tag, String type, int keyboardHeight) {
    RelativeLayout keyboardLayout = new RelativeLayout(activity);
    ReactRootView rootView = new ReactRootView(this.getReactApplicationContext());

    Bundle bundle = new Bundle();
    bundle.putInt("tag", tag);
    bundle.putString("keyboardType", type);
    rootView.startReactApplication(
            ((ReactApplication) activity.getApplication()).getReactNativeHost().getReactInstanceManager(),
            "AllMaxKeyboard",
            bundle);

    final float scale = activity.getResources().getDisplayMetrics().density;
    RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(keyboardHeight * scale));
    lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    if (rootView.getParent() != null) {
      ((ViewGroup)rootView.getParent()).removeView(rootView);
    }
    keyboardLayout.addView(rootView, lParams);

    WritableMap params = Arguments.createMap();
    params.putInt("reactTag", tag);
    params.putInt("height", keyboardHeight);
    sendEvent(reactContext, "onChangeKeyboardHeight", params);

    return keyboardLayout;
  }

  @ReactMethod
  public void uninstall(final int tag) {
    Log.v(TAG, String.format("uninstall: %d", tag));
//        mHandler.removeCallbacksAndMessages(null);
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();

        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        ReactEditText edit = edits.get(String.valueOf(tag));
        RelativeLayout keyboardLayout = keyboardLayouts.get(String.valueOf(tag));
        ReactRootView rootView = (ReactRootView)keyboardLayout.getChildAt(0);
        edit.setOnFocusChangeListener(null);
        edit.setOnClickListener(null);

        if (keyboardLayout != null && keyboardLayout.getParent() != null) {
          ((ViewGroup) keyboardLayout.getParent()).removeView(keyboardLayout);
        }

        if (rootView != null/* && activity != null*/) {
          rootView.unmountReactApplication();
//          ((ReactApplication) activity.getApplication()).getReactNativeHost().getReactInstanceManager().detachRootView(rootView);
        }
        edits.remove(String.valueOf(tag));
        keyboardLayouts.remove(String.valueOf(tag));
      }
    });
  }

  @ReactMethod
  public void clear(final Callback successCallback, final Callback errorCallback) {
    mHandler.removeCallbacksAndMessages(null);
    mHandler.post(new Runnable() {
      @Override
      public void run() {
          try {
            for(Map.Entry<String, RelativeLayout> entry : keyboardLayouts.entrySet()) {
              RelativeLayout keyboardLayout = entry.getValue();
              ReactRootView rootView = (ReactRootView)keyboardLayout.getChildAt(0);
              if (keyboardLayout != null && keyboardLayout.getParent() != null) {
                ((ViewGroup) keyboardLayout.getParent()).removeView(keyboardLayout);
              }
              if (rootView != null) {
                rootView.unmountReactApplication();
              }
            }
            for(Map.Entry<String, ReactEditText> entry : edits.entrySet()) {
              ReactEditText edit = entry.getValue();
              if (edit != null) {
                edit.setOnFocusChangeListener(null);
                edit.setOnClickListener(null);
              }
            }
            keyboardLayouts.clear();
            edits.clear();
            successCallback.invoke();
          } catch ( Exception e) {
            errorCallback.invoke(e.getMessage());
          }
      }
    });
  }

  @ReactMethod
  public void setHeight(final int tag, final int keyboardHeight) {
    Log.v(TAG, "setHeight");
    mHandler.post(new Runnable() {
      @Override
      public void run() {

        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }
        if (!keyboardLayouts.containsKey(String.valueOf(tag))) {
          return;
        }
        final Activity activity = getCurrentActivity();
        if (activity == null) {
          return;
        }
        final float scale = activity.getResources().getDisplayMetrics().density;
        RelativeLayout keyboardLayout = keyboardLayouts.get(String.valueOf(tag));


        RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(keyboardHeight*scale));
        lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

        if (keyboardLayout != null && keyboardLayout.getChildAt(0) != null) {
          keyboardLayout.updateViewLayout(keyboardLayout.getChildAt(0), lParams);

          WritableMap params = Arguments.createMap();
          params.putInt("reactTag", tag);
          params.putInt("height", keyboardHeight);
          sendEvent(reactContext, "onChangeKeyboardHeight", params);
        }
      }
    });
  }

  @ReactMethod
  public void insertText(final int tag, final String text) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        edit.getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());
      }
    });
  }

  @ReactMethod
  public void backSpace(final int tag) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.getText().delete(start, end);
        } else if (start > 0) {
          edit.getText().delete(start - 1, end);
        }
      }
    });
  }

  @ReactMethod
  public void doDelete(final int tag) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.getText().delete(start, end);
        } else if (start > 0) {
          edit.getText().delete(start, end + 1);
        }
      }
    });
  }

  @ReactMethod
  public void moveLeft(final int tag) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.setSelection(start, start);
        } else {
          edit.setSelection(start - 1, start - 1);
        }
      }
    });
  }

  @ReactMethod
  public void moveRight(final int tag) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.setSelection(end, end);
        } else if (start > 0) {
          edit.setSelection(end + 1, end + 1);
        }
      }
    });
  }

  @ReactMethod
  public void switchSystemKeyboard(final int tag) {
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }
        if (!keyboardLayouts.containsKey(String.valueOf(tag))) {
          return;
        }
        final ReactEditText edit = edits.get(String.valueOf(tag));
        final RelativeLayout keyboardLayout = keyboardLayouts.get(String.valueOf(tag));

        if (keyboardLayout.getParent() != null) {
          ((ViewGroup) keyboardLayout.getParent()).removeView(keyboardLayout);
        }
        mHandler.post(new Runnable() {

          @Override
          public void run() {
            ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
          }
        });
      }
    });
  }

  @ReactMethod
  public void done(final int tag) {
    Log.v(TAG, "done");
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        if (!edits.containsKey(String.valueOf(tag))) {
          return;
        }

        final ReactEditText edit = edits.get(String.valueOf(tag));

        edit.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_ENTER, 0));

      }
    });
  }

  private void sendEvent(ReactContext reactContext,
                         String eventName,
                         @Nullable WritableMap params) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }

  @Override
  public String getName() {
    return "RNAllmaxKeyboard";
  }
}