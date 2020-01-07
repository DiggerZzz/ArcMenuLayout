package com.digger.arcmenulayout;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;

import com.digger.arcmenulayout.widget.AnimatorUtils;
import com.digger.arcmenulayout.widget.ArcMenuLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArcMenuLayout arcLayout;
    private Button btnFab;
    private boolean isChecked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arcLayout = findViewById(R.id.arcLayout);
        btnFab = findViewById(R.id.btnFab);

        btnFab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(isChecked) {
            dismissWidthParent(btnFab);
            isChecked = false;
        } else {
            showWithParent(btnFab);
            isChecked = true;
        }
    }


    private void showWithParent(View parent) {
        arcLayout.setVisibility(View.VISIBLE);
        List<Animator> animList = new ArrayList<>();

        for (int i = 0, len = arcLayout.getChildCount(); i < len; i++) {
            animList.add(createShowItemAnimator(arcLayout.getChildAt(i), parent));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(500);
        animSet.setInterpolator(new OvershootInterpolator());
        animSet.playTogether(animList);
        animSet.start();
    }

    private void dismissWidthParent(View parent) {
        List<Animator> animList = new ArrayList<>();

        for (int i = arcLayout.getChildCount() - 1; i >= 0; i--) {
            animList.add(createHideItemAnimator(arcLayout.getChildAt(i), parent));
        }

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(500);
        animSet.setInterpolator(new AnticipateInterpolator());
        animSet.playTogether(animList);
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                arcLayout.setVisibility(View.INVISIBLE);
            }
        });
        animSet.start();
    }

    private Animator createShowItemAnimator(View item, View parent) {
        float dx = parent.getX() - item.getX();
        float dy = parent.getY() - item.getY();

        item.setRotation(0f);
        item.setTranslationX(dx);
        item.setTranslationY(dy);

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(0f, 720f),
                AnimatorUtils.translationX(dx, 0f),
                AnimatorUtils.translationY(dy, 0f)
        );

        return anim;
    }

    private Animator createHideItemAnimator(final View item, View parent) {
        float dx = parent.getX() - item.getX();
        float dy = parent.getY() - item.getY();

        Animator anim = ObjectAnimator.ofPropertyValuesHolder(
                item,
                AnimatorUtils.rotation(720f, 0f),
                AnimatorUtils.translationX(0f, dx),
                AnimatorUtils.translationY(0f, dy)
        );

        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                item.setTranslationX(0f);
                item.setTranslationY(0f);
            }
        });

        return anim;
    }
}
