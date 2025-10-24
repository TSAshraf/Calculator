import javax.swing.*;
import java.awt.event.*;
import java.util.Set;

public class CalculatorController implements ActionListener, KeyListener {
    private final CalculatorView view;
    private final CalculatorEngine engine;

    private static final Set<String> SCIENTIFIC_FUNCS = Set.of("sin", "cos", "tan", "sqrt", "log", "ln");

    public CalculatorController(CalculatorView view, CalculatorEngine engine) {
        this.view = view;
        this.engine = engine;
        this.view.addButtonListener(this);
        this.view.addKeyListenerToDisplay(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = ((JButton) e.getSource()).getText();

        switch (command) {
            case "AC":
                view.setDisplayText("");
                break;
            case "Del":
                String current = view.getDisplayText();
                if (!current.isEmpty()) {
                    view.setDisplayText(current.substring(0, current.length() - 1));
                }
                break;
            case "=":
                String expression = view.getDisplayText();
                String result = engine.evaluate(expression);
                view.setDisplayText(result);
                break;
            case "π":
                view.appendDisplay("π");
                break;
            case "^":
                view.appendDisplay("^");
                break;
            default:
                if (SCIENTIFIC_FUNCS.contains(command)) {
                    view.appendDisplay(command + "(");
                } else {
                    view.appendDisplay(command);
                }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String expression = view.getDisplayText();
            String result = engine.evaluate(expression);
            view.setDisplayText(result);
        } else {
            char keyChar = e.getKeyChar();
            if (Character.isDefined(keyChar)) {
                view.appendDisplay(String.valueOf(keyChar));
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}