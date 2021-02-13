package io.github.marcocipriani01.telescopetouch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import static io.github.marcocipriani01.telescopetouch.ApplicationConstants.EXIT_ACTION_PREF;
import static io.github.marcocipriani01.telescopetouch.ApplicationConstants.NSD_PREF;
import static io.github.marcocipriani01.telescopetouch.ApplicationConstants.POLARIS_HEMISPHERE_PREF;
import static io.github.marcocipriani01.telescopetouch.ApplicationConstants.POLARIS_RETICLE_PREF;

/**
 * Pro.
 */
public class ProUtils {

    public static final String PRO_ID = "io.github.marcocipriani01.telescopetouchpro";
    public static final String PRO_MESSAGE_PREF = "pro_message_0";
    public static final String[] PRO_PREFERENCES = {
            NSD_PREF, EXIT_ACTION_PREF, POLARIS_HEMISPHERE_PREF, POLARIS_RETICLE_PREF
    };
    private static final boolean ENABLE_PRO_CHECK = true;
    private static final boolean DUMMY_PRO_VERSION = false;
    public static boolean isPro = false;

    public static void maybeProVersionDialog(SharedPreferences preferences, Context context) {
        if (preferences.getBoolean(PRO_MESSAGE_PREF, true)) {
            new AlertDialog.Builder(context).setTitle(R.string.app_name)
                    .setMessage("Telescope.Touch Pro is now available! Added features include: the Aladin Sky Atlas, galaxies and nebulae previews, altitude graphs and advanced settings!")
                    .setIcon(R.drawable.new_icon).setCancelable(false)
                    .setPositiveButton("Get it!", (a, b) -> ProUtils.playStore(context))
                    .setNegativeButton(android.R.string.cancel, null).show();
            preferences.edit().putBoolean(PRO_MESSAGE_PREF, false).apply();
        }
    }

    public static void update(Context context) {
        if (ENABLE_PRO_CHECK) {
            try {
                context.getPackageManager().getApplicationInfo(PRO_ID, 0);
                isPro = true;
            } catch (PackageManager.NameNotFoundException e) {
                isPro = false;
            }
        } else {
            isPro = DUMMY_PRO_VERSION;
        }
    }

    public static void toast(Context context) {
        Toast.makeText(context, "Pro feature!", Toast.LENGTH_LONG).show();
    }

    public static void playStore(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PRO_ID)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + PRO_ID)));
        }
    }
}