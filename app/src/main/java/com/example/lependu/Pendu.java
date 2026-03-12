package com.example.lependu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Pendu extends AppCompatActivity {

    // ── UI views ──────────────────────────────────────────────────────────
    private ImageView hangmanImage;
    private TextView scoreView;
    private TextView erreursLabel;
    private LinearLayout wordContainer;
    private LinearLayout keyboardContainer;
    private LinearLayout livesContainer;

    // ── Game state ────────────────────────────────────────────────────────
    private static final String[] MOTS = {
            "chapeau", "revolver", "cheval", "bottes", "cowboy",
            "vache", "lasso", "indien", "desert", "cactus",
            "pistolet", "bandana", "saloon", "poudre", "fleche",
            "totem", "prairie", "mustang", "bison", "canyon"
    };

    private static final int[] HANGMAN_IMAGES = {
            R.drawable.acceuil,
            R.drawable.err01,
            R.drawable.err02,
            R.drawable.err03,
            R.drawable.err04,
            R.drawable.err05,
            R.drawable.err06
    };

    private static final String[] KEYBOARD_ROWS = {"ABCDEFGHI", "JKLMNOPQR", "STUVWXYZ"};

    private Jeu jeu;
    private char[] motAffiche;
    private TextView[] letterTiles;
    private Map<Character, Button> letterButtons;
    private boolean partieTerminee = false;

    // ── Lifecycle ─────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hangmanImage    = findViewById(R.id.img);
        scoreView       = findViewById(R.id.score);
        erreursLabel    = findViewById(R.id.erreurs_label);
        wordContainer   = findViewById(R.id.word_container);
        keyboardContainer = findViewById(R.id.keyboard_container);
        livesContainer  = findViewById(R.id.lives_container);

        demarrerNouvellePartie();
    }

    // ── Game init ─────────────────────────────────────────────────────────
    private void demarrerNouvellePartie() {
        jeu = new Jeu(MOTS);
        partieTerminee = false;

        String mot = jeu.getMotADeviner();
        motAffiche = new char[mot.length()];
        Arrays.fill(motAffiche, '_');

        hangmanImage.setImageResource(R.drawable.acceuil);
        scoreView.setText("0");
        erreursLabel.setText("0 / 6 erreurs");

        construireTuilesMot(mot);
        construireClavier();
        construireVies();
    }

    // ── Word tiles ────────────────────────────────────────────────────────
    private void construireTuilesMot(String mot) {
        wordContainer.removeAllViews();
        letterTiles = new TextView[mot.length()];

        int tileSize = dpToPx(42);
        int tileMargin = dpToPx(4);

        for (int idx = 0; idx < mot.length(); idx++) {
            TextView tile = new TextView(this);
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(tileSize, tileSize);
            params.setMargins(tileMargin, 0, tileMargin, 0);
            tile.setLayoutParams(params);
            tile.setGravity(Gravity.CENTER);
            tile.setBackgroundResource(R.drawable.word_tile_hidden);
            tile.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tile.setTextSize(18);
            tile.setTypeface(null, Typeface.BOLD);
            tile.setText("");
            letterTiles[idx] = tile;
            wordContainer.addView(tile);
        }
    }

    // ── Keyboard ──────────────────────────────────────────────────────────
    private void construireClavier() {
        keyboardContainer.removeAllViews();
        letterButtons = new HashMap<>();

        int btnSize = dpToPx(38);
        int btnMargin = dpToPx(3);

        for (String row : KEYBOARD_ROWS) {
            LinearLayout rowLayout = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rowParams.setMargins(0, 0, 0, dpToPx(6));
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);

            for (char c : row.toCharArray()) {
                final char lettre = c;
                Button btn = new Button(this);
                LinearLayout.LayoutParams btnParams =
                        new LinearLayout.LayoutParams(btnSize, btnSize);
                btnParams.setMargins(btnMargin, 0, btnMargin, 0);
                btn.setLayoutParams(btnParams);
                btn.setText(String.valueOf(c));
                btn.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                btn.setTextSize(13);
                btn.setTypeface(null, Typeface.BOLD);
                btn.setBackgroundResource(R.drawable.btn_letter_normal);
                btn.setPadding(0, 0, 0, 0);
                btn.setStateListAnimator(null);
                btn.setOnClickListener(v -> onLettreClic(lettre, btn));
                letterButtons.put(c, btn);
                rowLayout.addView(btn);
            }
            keyboardContainer.addView(rowLayout);
        }
    }

    // ── Lives display ─────────────────────────────────────────────────────
    private void construireVies() {
        livesContainer.removeAllViews();
        for (int i = 0; i < 6; i++) {
            TextView heart = new TextView(this);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(dpToPx(3), 0, dpToPx(3), 0);
            heart.setLayoutParams(p);
            heart.setText("♥");
            heart.setTextSize(18);
            heart.setTextColor(ContextCompat.getColor(this, R.color.life_active));
            heart.setTag("heart_" + i);
            livesContainer.addView(heart);
        }
    }

    private void mettreAJourVies(int nbErreurs) {
        for (int i = 0; i < livesContainer.getChildCount(); i++) {
            View child = livesContainer.getChildAt(i);
            if (child instanceof TextView) {
                TextView heart = (TextView) child;
                int erreurIndex = 5 - i; // hearts from right represent remaining lives
                if (i >= (6 - nbErreurs)) {
                    heart.setTextColor(ContextCompat.getColor(this, R.color.life_lost));
                    heart.setText("♡");
                } else {
                    heart.setTextColor(ContextCompat.getColor(this, R.color.life_active));
                    heart.setText("♥");
                }
            }
        }
    }

    // ── Letter click handler ──────────────────────────────────────────────
    private void onLettreClic(char lettre, Button btn) {
        if (partieTerminee) return;

        // ONE call — fixes the double-scoring / double-error bug
        int[] positions = jeu.essayerUneLettre(lettre);
        btn.setEnabled(false);

        if (positions.length > 0) {
            // ── Correct guess ──
            btn.setBackgroundResource(R.drawable.btn_letter_correct);
            btn.setTextColor(ContextCompat.getColor(this, R.color.correct_green));

            for (int pos : positions) {
                motAffiche[pos] = lettre;
                revelerTuile(pos, lettre);
            }

            animerPulsationScore();
            scoreView.setText(String.valueOf(jeu.getPointage()));

            if (jeu.estReussi()) {
                partieTerminee = true;
                new Handler(Looper.getMainLooper()).postDelayed(this::lancerEcranResultat, 800);
            }

        } else {
            // ── Wrong guess ──
            btn.setBackgroundResource(R.drawable.btn_letter_wrong);
            btn.setTextColor(ContextCompat.getColor(this, R.color.wrong_red));

            int nbErreurs = jeu.getNbErreurs();
            mettreAJourHangman(nbErreurs);
            mettreAJourVies(nbErreurs);
            erreursLabel.setText(nbErreurs + " / 6 erreurs");

            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            hangmanImage.startAnimation(shake);

            Animation flash = AnimationUtils.loadAnimation(this, R.anim.wrong_flash);
            btn.startAnimation(flash);

            if (nbErreurs >= 6) {
                partieTerminee = true;
                new Handler(Looper.getMainLooper()).postDelayed(this::lancerEcranResultat, 900);
            }
        }
    }

    // ── Reveal tile with animation ────────────────────────────────────────
    private void revelerTuile(int pos, char lettre) {
        TextView tile = letterTiles[pos];

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tile, "scaleX", 0f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tile, "scaleY", 0f, 1.15f, 1f);
        ObjectAnimator alpha  = ObjectAnimator.ofFloat(tile, "alpha", 0f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY, alpha);
        set.setDuration(350);
        set.setStartDelay(50L * pos % 200); // slight cascade delay

        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(android.animation.Animator animation) {
                tile.setBackgroundResource(R.drawable.word_tile_revealed);
                tile.setTextColor(ContextCompat.getColor(Pendu.this, R.color.correct_green));
                tile.setText(String.valueOf(Character.toUpperCase(lettre)));
            }
        });
        set.start();
    }

    // ── Hangman image update with crossfade ───────────────────────────────
    private void mettreAJourHangman(int nbErreurs) {
        if (nbErreurs < 0 || nbErreurs >= HANGMAN_IMAGES.length) return;

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(hangmanImage, "alpha", 1f, 0f);
        fadeOut.setDuration(150);
        fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                hangmanImage.setImageResource(HANGMAN_IMAGES[nbErreurs]);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(hangmanImage, "alpha", 0f, 1f);
                fadeIn.setDuration(250);
                fadeIn.start();
            }
        });
        fadeOut.start();
    }

    // ── Score pulse ───────────────────────────────────────────────────────
    private void animerPulsationScore() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(scoreView, "scaleX", 1f, 1.4f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(scoreView, "scaleY", 1f, 1.4f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(300);
        set.start();
    }

    // ── Launch result screen ──────────────────────────────────────────────
    private void lancerEcranResultat() {
        boolean gagne = jeu.estReussi();
        Bundle extras = new Bundle();
        extras.putString("MOT", jeu.getMotADeviner());
        extras.putBoolean("GAGNE", gagne);
        extras.putInt("SCORE", jeu.getPointage());
        extras.putInt("ERREURS", jeu.getNbErreurs());

        Intent intent = new Intent(this, Status.class);
        intent.putExtras(extras);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, android.R.anim.fade_out);
    }

    // ── Reset / new game ──────────────────────────────────────────────────
    public void reset(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                com.google.android.material.R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);
        builder.setTitle(getString(R.string.confirmer_reset));
        builder.setMessage(getString(R.string.confirmer_msg));
        builder.setPositiveButton(getString(R.string.oui), (dialog, which) -> {
            // Fade everything out, then restart
            View root = getWindow().getDecorView().getRootView();
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(root, "alpha", 1f, 0f);
            fadeOut.setDuration(250);
            fadeOut.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    demarrerNouvellePartie();
                    ObjectAnimator fadeIn = ObjectAnimator.ofFloat(root, "alpha", 0f, 1f);
                    fadeIn.setDuration(350);
                    fadeIn.start();
                }
            });
            fadeOut.start();
        });
        builder.setNegativeButton(getString(R.string.non), null);
        builder.show();
    }

    // ── dp helper ─────────────────────────────────────────────────────────
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
