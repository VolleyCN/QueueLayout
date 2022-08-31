package com.volley.library.anim;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.AnimRes;


/**
 * Created by MENG
 */

public class AnimUtils {

    public static Animation getAnimation(Context context, @AnimRes int res) {
        return AnimationUtils.loadAnimation(context, res);
    }

    public static AnimatorSet getInAnim(View view, float from, float to) {
        ObjectAnimator tran = ObjectAnimator.ofFloat(view, "translationX", from, to);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(tran, alpha);
        return animatorSet;
    }

    public static AnimatorSet getOutAnim(View view, float from, float to) {
        ObjectAnimator tran = ObjectAnimator.ofFloat(view, "translationX", from, to);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(500);
        animatorSet.playTogether(tran, alpha);
        return animatorSet;
    }

    public static AnimatorSet getNumAnim(View view) {
        ObjectAnimator animX = ObjectAnimator.ofFloat(view, "scaleX", 1.6f, 1.0f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(view, "scaleY", 1.6f, 1.0f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(400);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animX, animY);
        return animSet;
    }

}
