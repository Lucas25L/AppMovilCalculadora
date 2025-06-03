package com.lucascodedevs.nuevacalculadora;
// Importación de las clases necesarias para la actividad
import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
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

        // Agregamos un listener para detectar los eventos de la animación
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {

            // Estos métodos se implementan para manejar los eventos de la animación
            @Override
            public void onAnimationStart(Animator animation) {}

            // Este método se ejecuta cuando la animación termina
            @Override
            public void onAnimationEnd(Animator animation) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish(); // Cierra la SplashActivity para no volver a ella con el botón Atrás
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // Este método se ejecuta si la animación se cancela (opcional)
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
                // Este método se ejecuta si la animación se repite (opcional)
            }
        });
    }
}