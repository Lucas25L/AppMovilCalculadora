package com.lucascodedevs.nuevacalculadora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvOperation, tvResult;
    private StringBuilder currentInput = new StringBuilder(); // Para construir la entrada actual
    private String lastOperator = ""; // Último operador presionado (+, -, *, /)
    private double firstOperand = 0; // Primer operando de la operación
    private boolean newOperation = true; // Indica si estamos empezando una nueva operación
    private boolean isDecimalAdded = false; // Controla si ya se añadió un punto decimal

    // Historial
    private ArrayList<String> historyList = new ArrayList<>();
    private int historyIndex = -1; // -1 significa que no estamos viendo el historial

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOperation = findViewById(R.id.tvOperation);
        tvResult = findViewById(R.id.tvResult);

        // Configurar listeners para botones numéricos y el punto decimal
        setNumericButtonListeners();
        // Configurar listeners para botones de operadores
        setOperatorButtonListeners();
        // Configurar listeners para botones especiales (C, =, Historial, Avanzadas)
        setSpecialButtonListeners();
    }

    // --- Métodos de Configuración de Listeners ---

    private void setNumericButtonListeners() {
        int[] numericButtonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        View.OnClickListener numericListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (newOperation) {
                    currentInput.setLength(0); // Limpiar para nueva operación
                    tvOperation.setText("");
                    newOperation = false;
                    isDecimalAdded = false;
                }
                currentInput.append(b.getText().toString());
                tvResult.setText(currentInput.toString());
            }
        };

        for (int id : numericButtonIds) {
            findViewById(id).setOnClickListener(numericListener);
        }

        // Listener para el punto decimal
        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDecimalAdded) {
                    if (currentInput.length() == 0) { // Si no hay nada, empieza con "0."
                        currentInput.append("0.");
                    } else {
                        currentInput.append(".");
                    }
                    isDecimalAdded = true;
                    tvResult.setText(currentInput.toString());
                    newOperation = false; // No es una nueva operación si se añade un decimal
                }
            }
        });
    }

    private void setOperatorButtonListeners() {
        int[] operatorButtonIds = {
                R.id.btnSum, R.id.btnSubtract, R.id.btnMultiply, R.id.btnDivide
        };

        View.OnClickListener operatorListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                String operator = b.getText().toString();

                if (currentInput.length() == 0 && firstOperand == 0 && !newOperation) {
                    // Permite cambiar el operador si no hay nueva entrada
                    lastOperator = operator;
                    String opText = tvOperation.getText().toString();
                    if (!opText.isEmpty()) {
                        opText = opText.substring(0, opText.length() - 1) + operator;
                        tvOperation.setText(opText);
                    }
                    return;
                }

                if (currentInput.length() > 0) {
                    try {
                        double secondOperand = Double.parseDouble(currentInput.toString());
                        if (!lastOperator.isEmpty() && !newOperation) {
                            // Realizar la operación anterior antes de establecer el nuevo operador
                            double result = calculate(firstOperand, secondOperand, lastOperator);
                            firstOperand = result;
                            tvResult.setText(formatResult(result));
                            tvOperation.setText(formatResult(firstOperand) + operator);
                        } else {
                            firstOperand = secondOperand;
                            tvOperation.setText(formatResult(firstOperand) + operator);
                        }
                        lastOperator = operator;
                        currentInput.setLength(0); // Limpiar para la siguiente entrada
                        isDecimalAdded = false;
                        newOperation = false; // No es una nueva operación, estamos encadenando
                    } catch (NumberFormatException e) {
                        showError("Entrada numérica inválida.");
                        clearAll(); // Limpiar para empezar de nuevo
                    }
                } else if (tvResult.getText().toString().equals("0") && tvOperation.getText().toString().isEmpty()) {
                    // Si solo hay un 0 y no hay operación, permite empezar con un 0 y el operador
                    firstOperand = 0;
                    lastOperator = operator;
                    tvOperation.setText("0" + operator);
                    newOperation = false;
                }
            }
        };

        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(operatorListener);
        }
    }

    private void setSpecialButtonListeners() {
        // Botón C (Clear All)
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        // Botón Igual (=)
        findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0 && !lastOperator.isEmpty()) {
                    try {
                        double secondOperand = Double.parseDouble(currentInput.toString());
                        double finalResult = calculate(firstOperand, secondOperand, lastOperator);
                        String historyEntry = formatResult(firstOperand) + " " + lastOperator + " " + formatResult(secondOperand) + " = " + formatResult(finalResult);
                        addHistoryEntry(historyEntry);

                        tvOperation.setText(historyEntry); // Muestra la operación completa
                        tvResult.setText(formatResult(finalResult));
                        currentInput.setLength(0); // Limpiar para la siguiente operación
                        firstOperand = finalResult; // El resultado es el primer operando para futuras operaciones
                        lastOperator = ""; // Reiniciar el operador
                        newOperation = true; // Siguiente entrada comenzará una nueva operación
                        isDecimalAdded = false;
                    } catch (NumberFormatException e) {
                        showError("Entrada numérica inválida.");
                        clearAll();
                    }
                } else if (currentInput.length() > 0 && lastOperator.isEmpty()) {
                    // Si solo hay un número ingresado y se presiona "=", mostrarlo como resultado
                    tvResult.setText(currentInput.toString());
                    firstOperand = Double.parseDouble(currentInput.toString());
                    tvOperation.setText(currentInput.toString());
                    currentInput.setLength(0);
                    newOperation = true;
                } else if (!tvOperation.getText().toString().isEmpty() && !tvResult.getText().toString().isEmpty() && lastOperator.isEmpty()) {
                    // Si ya se ha calculado un resultado y se presiona '=', solo muestra el resultado
                    // No hacer nada si ya está en estado final y no hay nueva entrada
                } else {
                    showError("Operación incompleta.");
                }
            }
        });

        // Botón Porcentaje (%)
        findViewById(R.id.btnPercent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    try {
                        double value = Double.parseDouble(currentInput.toString());
                        double percentResult = value / 100.0;
                        tvResult.setText(formatResult(percentResult));
                        currentInput.setLength(0);
                        currentInput.append(formatResult(percentResult)); // Para que el resultado sea la nueva entrada
                        newOperation = true; // Permite que el siguiente número borre esto
                    } catch (NumberFormatException e) {
                        showError("Valor inválido para porcentaje.");
                    }
                } else if (firstOperand != 0 && !lastOperator.isEmpty()) {
                    // Permite calcular un porcentaje del primer operando si ya hay uno
                    try {
                        double percentOfFirst = firstOperand / 100.0;
                        tvResult.setText(formatResult(percentOfFirst));
                        currentInput.setLength(0);
                        currentInput.append(formatResult(percentOfFirst));
                        newOperation = true;
                    } catch (Exception e) {
                        showError("Error calculando porcentaje.");
                    }
                }
            }
        });

        // Botón Potencia (x²) - lo usaremos como x^2 simplificado para ahora
        findViewById(R.id.btnPower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0) {
                    try {
                        double value = Double.parseDouble(currentInput.toString());
                        double powerResult = Math.pow(value, 2); // Calcula x al cuadrado
                        String historyEntry = formatResult(value) + "^2 = " + formatResult(powerResult);
                        addHistoryEntry(historyEntry);

                        tvOperation.setText(historyEntry);
                        tvResult.setText(formatResult(powerResult));
                        currentInput.setLength(0);
                        firstOperand = powerResult; // El resultado es el primer operando para encadenar
                        lastOperator = "";
                        newOperation = true;
                        isDecimalAdded = false;
                    } catch (NumberFormatException e) {
                        showError("Valor inválido para potencia.");
                    }
                } else if (!tvResult.getText().toString().equals("0") && !tvResult.getText().toString().isEmpty()) {
                    // Si no hay entrada actual, usa el resultado mostrado
                    try {
                        double value = Double.parseDouble(tvResult.getText().toString());
                        double powerResult = Math.pow(value, 2);
                        String historyEntry = formatResult(value) + "^2 = " + formatResult(powerResult);
                        addHistoryEntry(historyEntry);

                        tvOperation.setText(historyEntry);
                        tvResult.setText(formatResult(powerResult));
                        currentInput.setLength(0);
                        firstOperand = powerResult;
                        lastOperator = "";
                        newOperation = true;
                        isDecimalAdded = false;
                    } catch (NumberFormatException e) {
                        showError("Valor inválido para potencia.");
                    }
                }
            }
        });

        // Botón Raíz Cuadrada (√)
        findViewById(R.id.btnSqrt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentInput.length() > 0 || (!tvResult.getText().toString().equals("0") && !tvResult.getText().toString().isEmpty())) {
                    try {
                        double value;
                        if (currentInput.length() > 0) {
                            value = Double.parseDouble(currentInput.toString());
                        } else {
                            value = Double.parseDouble(tvResult.getText().toString());
                        }

                        if (value < 0) {
                            showError("No se puede calcular la raíz de un número negativo.");
                            clearAll(); // Opcional: limpiar si es un error grave
                            return;
                        }
                        double sqrtResult = Math.sqrt(value);
                        String historyEntry = "√" + formatResult(value) + " = " + formatResult(sqrtResult);
                        addHistoryEntry(historyEntry);

                        tvOperation.setText(historyEntry);
                        tvResult.setText(formatResult(sqrtResult));
                        currentInput.setLength(0);
                        firstOperand = sqrtResult;
                        lastOperator = "";
                        newOperation = true;
                        isDecimalAdded = false;
                    } catch (NumberFormatException e) {
                        showError("Valor inválido para raíz cuadrada.");
                    }
                } else {
                    showError("Ingrese un valor para calcular la raíz cuadrada.");
                }
            }
        });

        // Botón Historial Anterior (<)
        findViewById(R.id.btnHistoryPrev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!historyList.isEmpty()) {
                    if (historyIndex == -1) { // Si no estamos en historial, ir al último
                        historyIndex = historyList.size() - 1;
                    } else if (historyIndex > 0) {
                        historyIndex--;
                    }
                    tvOperation.setText("Historial:");
                    tvResult.setText(historyList.get(historyIndex));
                    // Deshabilitar entrada de número si estamos en historial
                    currentInput.setLength(0);
                    newOperation = true;
                    lastOperator = ""; // Para evitar operaciones encadenadas desde el historial
                } else {
                    showError("No hay historial.");
                }
            }
        });

        // Botón Historial Siguiente (>)
        findViewById(R.id.btnHistoryNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!historyList.isEmpty()) {
                    if (historyIndex != -1 && historyIndex < historyList.size() - 1) {
                        historyIndex++;
                        tvOperation.setText("Historial:");
                        tvResult.setText(historyList.get(historyIndex));
                        // Deshabilitar entrada de número si estamos en historial
                        currentInput.setLength(0);
                        newOperation = true;
                        lastOperator = "";
                    } else {
                        showError("Fin del historial.");
                        // Si llegamos al final, podemos volver al estado normal
                        if (historyList.size() > 0) {
                            historyIndex = -1; // Salir del modo historial
                            tvOperation.setText("");
                            tvResult.setText(historyList.get(historyList.size() - 1).split("=")[1].trim()); // Muestra el último resultado
                            currentInput.append(historyList.get(historyList.size() - 1).split("=")[1].trim());
                            newOperation = true; // Lista para nueva operación
                        }
                    }
                } else {
                    showError("No hay historial.");
                }
            }
        });
    }

    // --- Métodos de Lógica de la Calculadora ---

    private double calculate(double num1, double num2, String operator) {
        switch (operator) {
            case "+":
                return num1 + num2;
            case "-":
                return num1 - num2;
            case "*":
                return num1 * num2;
            case "/":
                if (num2 == 0) {
                    throw new ArithmeticException("División por cero");
                }
                return num1 / num2;
            default:
                return 0; // Esto no debería suceder
        }
    }

    private void clearAll() {
        currentInput.setLength(0);
        tvResult.setText("0");
        tvOperation.setText("");
        firstOperand = 0;
        lastOperator = "";
        newOperation = true;
        isDecimalAdded = false;
        historyIndex = -1; // Resetear el índice del historial
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        // Opcional: Podrías también mostrar el mensaje en tvResult o tvOperation
        // tvResult.setText("Error");
    }

    // Para formatear los resultados y evitar demasiados decimales
    private String formatResult(double value) {
        DecimalFormat df = new DecimalFormat("#.##########"); // Hasta 10 decimales, elimina ceros finales
        return df.format(value);
    }

    // --- Métodos de Historial ---

    private void addHistoryEntry(String entry) {
        historyList.add(entry);
        // Si la lista es muy grande, podríamos limitar su tamaño aquí
        // if (historyList.size() > 20) {
        //    historyList.remove(0);
        // }
        historyIndex = -1; // Al agregar, salimos del modo historial
    }
}