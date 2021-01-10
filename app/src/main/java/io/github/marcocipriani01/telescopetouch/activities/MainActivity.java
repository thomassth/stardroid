package io.github.marcocipriani01.telescopetouch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.github.marcocipriani01.telescopetouch.R;
import io.github.marcocipriani01.telescopetouch.TelescopeTouchApp;

/**
 * The main activity of the application, that manages all the fragments.
 *
 * @author marcocipriani01
 */
public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    /**
     * Last open page.
     */
    private Pages currentPage = Pages.CONNECTION;
    /**
     * The activity's toolbar.
     */
    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new ConnectionFragment()).commit();
        final BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setSelectedItemId(R.id.menu_connection);
        navigation.setOnNavigationItemSelectedListener(this);
        TelescopeTouchApp.setGoToConnectionTab(() -> runOnUiThread(() -> {
            currentPage = Pages.CONNECTION;
            toolbar.setElevation(8);
            navigation.setOnNavigationItemSelectedListener(null);
            navigation.setSelectedItemId(currentPage.itemId);
            navigation.setOnNavigationItemSelectedListener(this);
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.CONNECTION.instance).commit();
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (itemId == R.id.menu_compass) {
            startActivity(new Intent(this, CompassActivity.class));
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Pages newPage = Pages.fromId(item.getItemId());
        if (newPage == Pages.SKY_MAP) {
            startActivity(new Intent(this, DynamicStarMapActivity.class));
        } else if ((newPage != null) && (newPage != currentPage)) {
            if (newPage == Pages.GENERIC) {
                toolbar.setElevation(0);
            } else {
                toolbar.setElevation(8);
            }
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.animator.fade_in, R.animator.fade_out, R.animator.fade_in, R.animator.fade_out)
                    .replace(R.id.content_frame, Pages.values()[newPage.ordinal()].instance).commit();
            currentPage = newPage;
            return true;
        }
        return false;
    }

    /**
     * @author marcocipriani01
     */
    private enum Pages {
        SKY_MAP(R.id.menu_skymap, null),
        CONNECTION(R.id.menu_connection, new ConnectionFragment()),
        MOTION(R.id.menu_move, new MountControlFragment()),
        GENERIC(R.id.menu_generic, new ControlPanelFragment()),
        FOCUSER(R.id.menu_focuser, new FocuserFragment());

        private final int itemId;
        private final Fragment instance;

        Pages(int itemId, Fragment instance) {
            this.itemId = itemId;
            this.instance = instance;
        }

        private static Pages fromId(int id) {
            for (Pages p : Pages.values()) {
                if (p.itemId == id) return p;
            }
            return null;
        }
    }
}