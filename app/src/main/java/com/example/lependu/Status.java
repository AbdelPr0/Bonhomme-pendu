package com.example.lependu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Status extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        TextView icon     = findViewById(R.id.result_icon);
        TextView title    = findViewById(R.id.result_title);
        TextView subtitle = findViewById(R.id.result_subtitle);
        TextView motRevele   = findViewById(R.id.mot_revele);
        TextView scoreFinal  = findViewById(R.id.score_final);
        TextView erreursFinal = findViewById(R.id.erreurs_final);
        LinearLayout root = findViewById(R.id.status_root);

        Bundle extras = getIntent().getExtras();
        if (extras == null) return;

        String mot    = extras.getString("MOT", "—");
        boolean gagne = extras.getBoolean("GAGNE", false);
        int score     = extras.getInt("SCORE", 0);
        int erreurs   = extras.getInt("ERREURS", 0);

        if (gagne) {
            icon.setText("🏆");
            title.setText(getString(R.string.victoire));
            title.setTextColor(ContextCompat.getColor(this, R.color.gold_primary));
            subtitle.setText(getString(R.string.victoire_msg));
            root.setBackgroundColor(ContextCompat.getColor(this, R.color.bg_dark));
        } else {
            icon.setText("💀");
            title.setText(getString(R.string.defaite));
            title.setTextColor(ContextCompat.getColor(this, R.color.wrong_red));
            subtitle.setText(getString(R.string.defaite_msg));
        }

        motRevele.setText(mot.toUpperCase());
        scoreFinal.setText(String.valueOf(score));
        erreursFinal.setText(erreurs + " / 6");

        // ── Entrance animations ──
        root.setAlpha(0f);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f);
        fadeIn.setDuration(400);
        fadeIn.start();

        // Icon bounces in
        icon.setScaleX(0f);
        icon.setScaleY(0f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            icon.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_in));
            icon.setScaleX(1f);
            icon.setScaleY(1f);
        }, 200);

        // Title slides up
        title.setTranslationY(60f);
        title.setAlpha(0f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator ty = ObjectAnimator.ofFloat(title, "translationY", 60f, 0f);
            ObjectAnimator ta = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);
            AnimatorSet ts = new AnimatorSet();
            ts.playTogether(ty, ta);
            ts.setDuration(400);
            ts.start();
        }, 400);

        // Score card pulses after appearing
        scoreFinal.setScaleX(0f);
        scoreFinal.setScaleY(0f);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            scoreFinal.setScaleX(1f);
            scoreFinal.setScaleY(1f);
            scoreFinal.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce_in));
        }, 700);
    }

    public void rejouer(View view) {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
