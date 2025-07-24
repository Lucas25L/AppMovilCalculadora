package com.lucascodedevs.nuevacalculadora;
// Importación de las clases necesarias para la actividad
import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

public class SplashActivity extends AppCompatActivity {

    // Declaración del LottieAnimationView
    private LottieAnimationView lottieAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Establece el layout de la actividad splash
        setContentView(R.layout.activity_splash);

        // Inicializa el LottieAnimationView buscando su ID en el layout
        lottieAnimationView = findViewById(R.id.lottieAnimationView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3 segundos de espera
    }
}