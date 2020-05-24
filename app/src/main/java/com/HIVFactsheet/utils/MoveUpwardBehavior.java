package com.HIVFactsheet.utils;

import android.content.Context;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Ajay on 07-03-2017.
 */

//Use this class only if you want toslide up the FAB whenever the seekbar appear, if u don't want this funcionality delete this file
public class MoveUpwardBehavior extends CoordinatorLayout.Behavior<View> {
    public MoveUpwardBehavior() {
        super();
    }

    public MoveUpwardBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        return dependency instanceof Snackbar.SnackbarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        float translationY = Math.min(0, ViewCompat.getTranslationY(dependency) - dependency.getHeight());
        ViewCompat.setTranslationY(child, translationY);
        return true;
    }

    //you need this when you swipe the snackbar(thanx to ubuntudroid's comment)
    public void onDependentViewRemoved(CoordinatorLayout parent, View child, View dependency) {
        ViewCompat.animate(child).translationY(0).start();
    }
}
