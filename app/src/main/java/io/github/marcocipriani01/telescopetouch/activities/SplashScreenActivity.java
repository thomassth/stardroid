package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import androidx.fragment.app.FragmentManager;

import javax.inject.Inject;

import io.github.marcocipriani01.telescopetouch.ApplicationConstants;
import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.StardroidApplication;
import io.github.marcocipriani01.telescopetouch.activities.dialogs.EulaDialogFragment;
import io.github.marcocipriani01.telescopetouch.activities.util.ConstraintsChecker;
import io.github.marcocipriani01.telescopetouch.inject.HasComponent;
import io.github.marcocipriani01.telescopetouch.util.MiscUtil;

/**
 * Shows a splash screen, then launch the next activity.
 */
public class SplashScreenActivity extends InjectableActivity
        implements EulaDialogFragment.EulaAcceptanceListener, HasComponent<SplashScreenComponent> {

    private final static String TAG = MiscUtil.getTag(SplashScreenActivity.class);
    // Update this with new versions of the EULA
    private static final int EULA_VERSION_CODE = 1;
    @Inject
    StardroidApplication app;
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    Animation fadeAnimation;
    @Inject
    EulaDialogFragment eulaDialogFragmentWithButtons;
    @Inject
    FragmentManager fragmentManager;
    @Inject
    ConstraintsChecker cc;
    private View graphic;
    private SplashScreenComponent daggerComponent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        daggerComponent = DaggerSplashScreenComponent.builder()
                .applicationComponent(getApplicationComponent())
                .splashScreenModule(new SplashScreenModule(this)).build();
        daggerComponent.inject(this);

        graphic = findViewById(R.id.splash);

        fadeAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(Animation arg0) {
                Log.d(TAG, "onAnimationEnd");
                graphic.setVisibility(View.INVISIBLE);
                launchSkyMap();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {

            }

            @Override
            public void onAnimationStart(Animation arg0) {
                Log.d(TAG, "SplashScreen.Animcation onAnimationStart");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean eulaShown = maybeShowEula();
        if (!eulaShown) {
            // User has previously accepted - let's get on with it!
            graphic.startAnimation(fadeAnimation);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private boolean maybeShowEula() {
        boolean eulaAlreadyConfirmed = (sharedPreferences.getInt(
                ApplicationConstants.READ_TOS_PREF_VERSION, -1) == EULA_VERSION_CODE);
        if (!eulaAlreadyConfirmed) {
            eulaDialogFragmentWithButtons.show(fragmentManager, "Eula Dialog");
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void eulaAccepted() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ApplicationConstants.READ_TOS_PREF_VERSION, EULA_VERSION_CODE);
        editor.apply();
        View graphic = findViewById(R.id.splash);
        graphic.startAnimation(fadeAnimation);
    }

    @Override
    public void eulaRejected() {
        Log.d(TAG, "Sorry chum, no accept, no app.");
        finish();
    }

    private void launchSkyMap() {
        Intent intent = new Intent(SplashScreenActivity.this, DynamicStarMapActivity.class);
        cc.check();
        startActivity(intent);
        finish();
    }

    @Override
    public SplashScreenComponent getComponent() {
        return daggerComponent;
    }
}