package com.lucascodedevs.nuevacalculadora;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import net.objecthunter.exp4j.Expression; // Importar la clase Expression
import net.objecthunter.exp4j.ExpressionBuilder; // Importar la clase ExpressionBuilder

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvOperation, tvResult;
    // currentInput ya no es StringBuilder, será String para la expresión completa
    private StringBuilder currentFullExpression = new StringBuilder();

    // No necesitamos lastOperator, firstOperand, newOperation, isDecimalAdded directamente para exp4j
    // isDecimalAdded y newOperation los manejaremos de forma más local o implícita.

    // Variables de control de estado para la entrada
    private boolean isDecimalAllowed = true; // Controla si se puede añadir un punto decimal al número actual
    private boolean isNewOperandStarting = true; // True si el siguiente dígito inicia un nuevo número/operando

    // Historial
    private ArrayList<String> historyList = new ArrayList<>();
    private int historyIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvOperation = findViewById(R.id.tvOperation);
        tvResult = findViewById(R.id.tvResult);

        // Configurar listeners para botones numéricos
        setNumericButtonListeners();
        // Configurar listeners para botones de operadores
        setOperatorButtonListeners();
        // Configurar listeners para botones especiales (C, AC, Parenthesis, =)
        setSpecialButtonListeners();

        // Inicializar el tvResult a "0" al inicio
        tvResult.setText("0");
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
                String digit = b.getText().toString();

                if (isNewOperandStarting) { // Si el siguiente dígito inicia un nuevo operando
                    tvResult.setText(digit); // Reemplaza lo que haya en tvResult (0, operador, o resultado anterior)
                    isNewOperandStarting = false; // Ya no estamos empezando un nuevo operando
                } else if (tvResult.getText().toString().equals("0") && !digit.equals(".")) {
                    tvResult.setText(digit); // Para reemplazar el 0 inicial con el primer dígito
                } else {
                    tvResult.append(digit); // Agrega el dígito
                }
                currentFullExpression.append(digit); // Siempre agrega a la expresión completa
                tvOperation.setText(currentFullExpression.toString()); // Muestra la expresión completa en tvOperation

                isDecimalAllowed = !tvResult.getText().toString().contains("."); // Actualizar si el número actual tiene decimal
            }
        };

        for (int id : numericButtonIds) {
            findViewById(id).setOnClickListener(numericListener);
        }

        // Listener para el punto decimal (pequeño ajuste)
        findViewById(R.id.btnDot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDecimalAllowed) {
                    if (isNewOperandStarting || tvResult.getText().toString().isEmpty() || isOperator(tvResult.getText().charAt(0))) {
                        // Si estamos empezando un nuevo operando o tvResult está vacío o tiene un operador,
                        // añadimos "0." para empezar un decimal.
                        tvResult.setText("0.");
                        currentFullExpression.append("0.");
                    } else {
                        tvResult.append(".");
                        currentFullExpression.append(".");
                    }
                    isDecimalAllowed = false; // Solo un decimal por número
                    isNewOperandStarting = false; // No es un nuevo operando si se añade un decimal
                }
                tvOperation.setText(currentFullExpression.toString());
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

                // Manejar el caso donde no hay nada en la expresión pero hay un resultado en tvResult
                if (currentFullExpression.length() == 0 && !tvResult.getText().toString().equals("0") && !tvResult.getText().toString().isEmpty()) {
                    currentFullExpression.append(tvResult.getText().toString());
                }

                if (currentFullExpression.length() > 0) {
                    char lastChar = currentFullExpression.charAt(currentFullExpression.length() - 1);
                    if (isOperator(lastChar)) {
                        // Reemplazar el último operador si se presiona otro
                        currentFullExpression.setLength(currentFullExpression.length() - 1);
                    } else if (lastChar == '(') {
                        // No permitir operador después de abrir paréntesis sin número, a menos que sea un signo negativo
                        if (!operator.equals("-")) { // Permite "(-"
                            showError("Error: Operador después de abrir paréntesis.");
                            return;
                        }
                    }
                    currentFullExpression.append(operator);
                    tvOperation.setText(currentFullExpression.toString());
                    // NO CAMBIAR tvResult AQUÍ. Se mantiene el último número o el resultado.
                    // tvResult.setText(operator); // <--- QUITAR ESTA LÍNEA O COMENTARLA
                    isDecimalAllowed = true; // Permitir decimal en el siguiente número
                    isNewOperandStarting = true; // El siguiente dígito inicia un nuevo operando
                } else if (tvResult.getText().toString().equals("0")) {
                    // Si todo esta a cero, se puede empezar con un operador (ej. -5)
                    // Si el operador es -, lo añade directamente para permitir "-5"
                    if (operator.equals("-")) {
                        currentFullExpression.append(operator);
                        tvOperation.setText(currentFullExpression.toString());
                        tvResult.setText(operator); // Muestra el '-' en tvResult
                        isDecimalAllowed = true;
                        isNewOperandStarting = true;
                    } else {
                        // Para otros operadores como +, *, / al inicio, añadir un 0 implícito
                        currentFullExpression.append("0").append(operator);
                        tvOperation.setText(currentFullExpression.toString());
                        tvResult.setText("0" + operator); // Muestra "0+" etc.
                        isDecimalAllowed = true;
                        isNewOperandStarting = true;
                    }
                }
            }
        };

        for (int id : operatorButtonIds) {
            findViewById(id).setOnClickListener(operatorListener);
        }
    }

    private void setSpecialButtonListeners() {
        // Botón C (Clear) - Ahora borra un solo carácter
        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentFullExpression.length() > 0) {
                    char lastChar = currentFullExpression.charAt(currentFullExpression.length() - 1);
                    if (lastChar == '.') {
                        isDecimalAllowed = true; // Permitir el punto de nuevo si se borra
                    }
                    currentFullExpression.setLength(currentFullExpression.length() - 1); // Borrar el último carácter

                    if (currentFullExpression.length() == 0) {
                        tvOperation.setText("");
                        tvResult.setText("0");
                        isDecimalAllowed = true;
                        isNewOperandStarting = true;
                    } else {
                        tvOperation.setText(currentFullExpression.toString());
                        // Actualizar tvResult con el último número si es posible
                        String lastPart = getLastNumberOrOperator(currentFullExpression.toString());
                        tvResult.setText(lastPart);
                        // Re-evaluar si se puede añadir decimal al último número restante
                        isDecimalAllowed = !lastPart.contains(".");
                    }
                } else {
                    tvResult.setText("0"); // Si no hay nada, asegurar que tvResult sea 0
                    tvOperation.setText("");
                }
            }
        });

        // Botón AC (All Clear)
        findViewById(R.id.btnAllClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAll();
            }
        });

        // Botón Parenthesis (abre y cierra inteligente)
        findViewById(R.id.btnParenthesis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expression = currentFullExpression.toString();
                int openParenthesesCount = 0;
                int closeParenthesesCount = 0;

                for (char c : expression.toCharArray()) {
                    if (c == '(') {
                        openParenthesesCount++;
                    } else if (c == ')') {
                        closeParenthesesCount++;
                    }
                }

                char lastChar = ' ';
                if (expression.length() > 0) {
                    lastChar = expression.charAt(expression.length() - 1);
                }

                // Lógica para abrir paréntesis
                // Se puede abrir si:
                // 1. La expresión está vacía.
                // 2. El último carácter es un operador.
                // 3. El último carácter es un '('.
                boolean canOpen = expression.isEmpty() || isOperator(lastChar) || lastChar == '(';

                // Lógica para cerrar paréntesis
                // Se puede cerrar si:
                // 1. Hay más paréntesis abiertos que cerrados.
                // 2. El último carácter es un número o ')'.
                boolean canClose = openParenthesesCount > closeParenthesesCount && (Character.isDigit(lastChar) || lastChar == ')');

                if (canClose) {
                    currentFullExpression.append(")");
                } else if (canOpen) {
                    currentFullExpression.append("(");
                } else {
                    // Caso como "5(" - no permite abrir directamente después de un número sin operador
                    // Opcional: mostrar un Toast o simplemente no hacer nada.
                    // Para una calculadora científica, "5(" a menudo se interpreta como "5 * ("
                    // Por ahora, solo insertamos '('.
                    currentFullExpression.append("(");
                }

                tvOperation.setText(currentFullExpression.toString());
                tvResult.setText(currentFullExpression.toString()); // Mostrar la expresión actual en tvResult
                isDecimalAllowed = true; // Permitir decimal en el siguiente número
                isNewOperandStarting = true; // El siguiente dígito inicia un nuevo operando
            }
        });


        // Botón Igual (=) - Aquí es donde usaremos exp4j
        findViewById(R.id.btnEquals).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String expressionToEvaluate = currentFullExpression.toString();

                if (expressionToEvaluate.isEmpty()) {
                    tvResult.setText("0");
                    return;
                }

                // Asegurarse de que los paréntesis estén balanceados antes de evaluar
                // Esto es una verificación básica. exp4j manejará la mayoría de los errores de sintaxis.
                int openCount = 0;
                int closeCount = 0;
                for (char c : expressionToEvaluate.toCharArray()) {
                    if (c == '(') openCount++;
                    if (c == ')') closeCount++;
                }
                while (openCount > closeCount) { // Auto-cerrar paréntesis faltantes al final
                    expressionToEvaluate += ")";
                    closeCount++;
                }

                try {
                    // Evaluar la expresión usando exp4j
                    Expression expression = new ExpressionBuilder(expressionToEvaluate)
                            .build();
                    double result = expression.evaluate();

                    String formattedResult = formatResult(result);
                    String historyEntry = expressionToEvaluate + " = " + formattedResult;
                    addHistoryEntry(historyEntry);

                    tvOperation.setText(historyEntry); // Muestra la operación completa y resultado
                    tvResult.setText(formattedResult);
                    currentFullExpression.setLength(0); // Limpiar para nueva operación
                    currentFullExpression.append(formattedResult); // El resultado es el inicio de la siguiente
                    isDecimalAllowed = !formattedResult.contains("."); // Si el resultado tiene decimal, no permitir otro
                    isNewOperandStarting = true; // Siguiente dígito sobreescribirá el resultado
                } catch (Exception e) {
                    showError("Error de expresión: " + e.getMessage());
                    clearAll(); // Limpiar en caso de error
                }
            }
        });

        // Botón Porcentaje (%)
        findViewById(R.id.btnPercent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Esta lógica necesita ser adaptada para trabajar con la expresión completa
                // y exp4j para ser verdaderamente "científica" o avanzada.
                // Por ahora, una implementación simple que aplica % al último número.
                String currentExp = currentFullExpression.toString();
                if (currentExp.isEmpty()) {
                    showError("Ingrese un número para porcentaje.");
                    return;
                }
                try {
                    // Intenta extraer el último número para aplicar el porcentaje
                    String lastNumberStr = getLastNumber(currentExp);
                    if (lastNumberStr.isEmpty()) {
                        showError("No hay número para porcentaje.");
                        return;
                    }
                    double value = Double.parseDouble(lastNumberStr);
                    double percentResult = value / 100.0;

                    // Reemplazar el último número en la expresión con su porcentaje
                    currentFullExpression.setLength(currentFullExpression.length() - lastNumberStr.length());
                    currentFullExpression.append(formatResult(percentResult));
                    tvOperation.setText(currentFullExpression.toString());
                    tvResult.setText(formatResult(percentResult));
                    isDecimalAllowed = !formatResult(percentResult).contains(".");
                    isNewOperandStarting = true;
                } catch (NumberFormatException e) {
                    showError("Valor inválido para porcentaje.");
                }
            }
        });

        // Botón Potencia (x²) - por ahora, sigue siendo x^2
        findViewById(R.id.btnPower).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentExp = currentFullExpression.toString();
                if (currentExp.isEmpty()) {
                    showError("Ingrese un número para elevar al cuadrado.");
                    return;
                }
                try {
                    String lastNumberStr = getLastNumber(currentExp);
                    if (lastNumberStr.isEmpty()) {
                        showError("No hay número para elevar al cuadrado.");
                        return;
                    }
                    double value = Double.parseDouble(lastNumberStr);
                    double powerResult = Math.pow(value, 2);

                    currentFullExpression.setLength(currentFullExpression.length() - lastNumberStr.length());
                    currentFullExpression.append(formatResult(powerResult));

                    tvOperation.setText(currentFullExpression.toString());
                    tvResult.setText(formatResult(powerResult));
                    isDecimalAllowed = !formatResult(powerResult).contains(".");
                    isNewOperandStarting = true;

                } catch (NumberFormatException e) {
                    showError("Valor inválido para potencia.");
                }
            }
        });

        // Botón Raíz Cuadrada (√)
        findViewById(R.id.btnSqrt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentExp = currentFullExpression.toString();
                if (currentExp.isEmpty()) {
                    showError("Ingrese un número para calcular la raíz.");
                    return;
                }
                try {
                    String lastNumberStr = getLastNumber(currentExp);
                    if (lastNumberStr.isEmpty()) {
                        showError("No hay número para calcular la raíz.");
                        return;
                    }
                    double value = Double.parseDouble(lastNumberStr);

                    if (value < 0) {
                        showError("No se puede calcular la raíz de un número negativo.");
                        clearAll();
                        return;
                    }
                    double sqrtResult = Math.sqrt(value);

                    currentFullExpression.setLength(currentFullExpression.length() - lastNumberStr.length());
                    currentFullExpression.append(formatResult(sqrtResult));

                    tvOperation.setText("sqrt(" + lastNumberStr + ") = " + formatResult(sqrtResult));
                    tvResult.setText(formatResult(sqrtResult));
                    isDecimalAllowed = !formatResult(sqrtResult).contains(".");
                    isNewOperandStarting = true;

                } catch (NumberFormatException e) {
                    showError("Valor inválido para raíz cuadrada.");
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
                    currentFullExpression.setLength(0); // Deshabilitar entrada directa
                    isNewOperandStarting = true; // Para que el siguiente número borre el historial
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
                        currentFullExpression.setLength(0); // Deshabilitar entrada directa
                        isNewOperandStarting = true;
                    } else {
                        showError("Fin del historial.");
                        // Si llegamos al final, salimos del modo historial y volvemos a la expresión normal
                        if (historyList.size() > 0) {
                            historyIndex = -1;
                            String lastResult = historyList.get(historyList.size() - 1).split("=")[1].trim();
                            tvOperation.setText(""); // Opcional: mostrar la última operación de historial
                            tvResult.setText(lastResult);
                            currentFullExpression.setLength(0);
                            currentFullExpression.append(lastResult);
                            isDecimalAllowed = !lastResult.contains(".");
                            isNewOperandStarting = true;
                        }
                    }
                } else {
                    showError("No hay historial.");
                }
            }
        });
    }

    // --- Métodos de Lógica de la Calculadora ---

    // Este metodo calculate() ya no será tan usado para la evaluación principal
    // pero lo mantendremos por si otras partes lo necesitan (ej. en funciones futuras).
    private double calculate(double num1, double num2, String operator) {
        switch (operator) {
            case "+": return num1 + num2;
            case "-": return num1 - num2;
            case "*": return num1 * num2;
            case "/":
                if (num2 == 0) {
                    throw new ArithmeticException("División por cero");
                }
                return num1 / num2;
            default:
                throw new IllegalArgumentException("Operador desconocido: " + operator);
        }
    }

    private void clearLast() {
        // Implementación más robusta para C (borrar un carácter)
        if (currentFullExpression.length() > 0) {
            char lastChar = currentFullExpression.charAt(currentFullExpression.length() - 1);
            currentFullExpression.setLength(currentFullExpression.length() - 1);

            // Ajustar estado de isDecimalAllowed si se borra un punto
            if (lastChar == '.') {
                isDecimalAllowed = true;
            } else if (isOperator(lastChar) || lastChar == '(' || lastChar == ')') {
                // Si borramos un operador o paréntesis, el siguiente número puede tener decimal
                isDecimalAllowed = true;
                isNewOperandStarting = false; // El último borrado no implica un nuevo operando
            }

            if (currentFullExpression.length() == 0) {
                tvOperation.setText("");
                tvResult.setText("0");
                isDecimalAllowed = true;
                isNewOperandStarting = true;
            } else {
                tvOperation.setText(currentFullExpression.toString());
                // Intenta mostrar el último número o el final de la expresión en tvResult
                tvResult.setText(getLastNumberOrOperator(currentFullExpression.toString()));
            }
        } else {
            tvResult.setText("0");
            tvOperation.setText("");
            isDecimalAllowed = true;
            isNewOperandStarting = true;
        }
    }


    private void clearAll() {
        currentFullExpression.setLength(0);
        tvResult.setText("0");
        tvOperation.setText("");
        isDecimalAllowed = true;
        isNewOperandStarting = true;
        historyIndex = -1;
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Para formatear los resultados y evitar demasiados decimales
    private String formatResult(double value) {
        // Puedes ajustar la cantidad de decimales si es necesario
        DecimalFormat df = new DecimalFormat("#.##########");
        return df.format(value);
    }

    // --- Métodos de Historial ---

    private void addHistoryEntry(String entry) {
        historyList.add(entry);
        historyIndex = -1;
    }

    // --- Métodos Auxiliares para el Parser ---

    // Método auxiliar para verificar si un carácter es un operador
    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    // Método auxiliar para obtener el último número de la expresión
    // Útil para % y x² donde solo se opera sobre el último valor.
    private String getLastNumber(String expression) {
        int i = expression.length() - 1;
        while (i >= 0 && (Character.isDigit(expression.charAt(i)) || expression.charAt(i) == '.')) {
            i--;
        }
        return expression.substring(i + 1);
    }

    // Metodo auxiliar para obtener el último número o operador para mostrar en tvResult
    private String getLastNumberOrOperator(String expression) {
        if (expression.isEmpty()) return "";
        char lastChar = expression.charAt(expression.length() - 1);
        if (Character.isDigit(lastChar) || lastChar == '.') {
            return getLastNumber(expression);
        } else if (isOperator(lastChar) || lastChar == '(' || lastChar == ')') {
            return String.valueOf(lastChar);
        }
        return ""; // Caso por defecto
    }
}