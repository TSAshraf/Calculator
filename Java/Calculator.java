import javax.swing.*;

public class Calculator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CalculatorView view = new CalculatorView();
            CalculatorEngine engine = new CalculatorEngine();
            new CalculatorController(view, engine);
            view.setVisible(true);
        });
    }
}
