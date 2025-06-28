package com.example.tailorz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tailorz.register.Login;

import org.opencv.android.OpenCVLoader;

public class SplashScreen extends AppCompatActivity {

    private LottieAnimationView animationView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (!OpenCVLoader.initDebug())
            Log.e("OpenCV", "Unable to load OpenCV!");
        else
            Log.d("OpenCV", "OpenCV loaded Successfully!");
        animationView = findViewById(R.id.animationView);
        // Optionally, you can set listeners for animation events
        animationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
                finish(); //
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
            // Implement required methods
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        animationView.playAnimation(); // Start the animation
    }

    @Override
    protected void onPause() {
        super.onPause();
        animationView.pauseAnimation(); // Pause the animation
    }
}