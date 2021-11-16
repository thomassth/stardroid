package com.google.android.stardroid.activities.dialogs

import android.app.DialogFragment
import android.app.FragmentManager

/**
 * A dialog fragment that only shows itself if it's not already shown.  This prevents
 * a java.lang.IllegalStateException when the activity gets backgrounded.
 * Created by johntaylor on 4/11/16.
 */
abstract class DialogFragment : DialogFragment() {
    override fun show(fragmentManager: FragmentManager, tag: String) {
        if (this.isAdded) return
        super.show(fragmentManager, tag)
    }
}