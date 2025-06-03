package com.lucascodedevs.nuevacalculadora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText valorUno, valorDos;
    Button btnSumar, btnRestar, btnMultiplicar, btnDividir;
    TextView resultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        valorUno = findViewById(R.id.valorUno);
        valorDos = findViewById(R.id.valorDos);
        resultado = findViewById(R.id.resultado);

        btnSumar = findViewById(R.id.btnSumar);
        btnRestar = findViewById(R.id.btnRestar);
        btnMultiplicar = findViewById(R.id.btnMultiplicar);
        btnDividir = findViewById(R.id.btnDividir);


        btnSumar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String valor1 = valorUno.getText().toString();
                String valor2 = valorDos.getText().toString();
                double num1 = Double.parseDouble(valor1);
                double num2 = Double.parseDouble(valor2);
                double suma = num1 + num2;
                resultado.setText(String.valueOf(suma));
            }
        });


        btnRestar.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick (View view){
                    String valor1 = valorUno.getText().toString();
                    String valor2 = valorDos.getText().toString();
                    double num1 = Double.parseDouble(valor1);
                    double num2 = Double.parseDouble(valor2);
                    double resta = num1 - num2;
                    resultado.setText(String.valueOf(resta));
                }
        });

        btnMultiplicar = findViewById(R.id.btnMultiplicar);
        btnMultiplicar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double num1 = Double.parseDouble(valorUno.getText().toString());
                double num2 = Double.parseDouble(valorDos.getText().toString());
                double producto = num1 * num2;
                resultado.setText(String.valueOf(producto));
            }
        });

        btnDividir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double num1 = Double.parseDouble(valorUno.getText().toString());
                double num2 = Double.parseDouble(valorDos.getText().toString());
                if (num2 == 0) {
                    resultado.setText("Error: División por cero");
                } else {
                    double division = num1 / num2;
                    resultado.setText(String.valueOf(division));
                }
            }
        });
    }
}