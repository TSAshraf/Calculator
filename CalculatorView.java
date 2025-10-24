import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class CalculatorView extends JFrame {
    private JTextField displayField;
    private Map<String, JButton> buttons = new HashMap<>();
    private JPanel buttonPanel;
    private JPanel scientificPanel;

    public CalculatorView() {
        setTitle("Scientific Calculator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupDisplay();
        setupButtons();
        setupLayout();

        setPreferredSize(new Dimension(400, 500));
        pack();
        setLocationRelativeTo(null);
    }

    private void setupDisplay() {
        displayField = new JTextField();
        displayField.setEditable(false);
        displayField.setHorizontalAlignment(JTextField.RIGHT);
        add(displayField, BorderLayout.NORTH);
    }

    private void setupButtons() {
        String[] basicLabels = {
                "AC", "Del", "(", ")",
                "7", "8", "9", "/",
                "4", "5", "6", "*",
                "1", "2", "3", "-",
                "0", ".", "=", "+"
        };

        String[] scientificLabels = {
                "sin", "cos", "tan", "sqrt",
                "log", "ln", "^", "π"
        };

        buttonPanel = new JPanel(new GridLayout(5, 4));
        for (String label : basicLabels) {
            JButton btn = new JButton(label);
            buttons.put(label, btn);
            buttonPanel.add(btn);
        }

        scientificPanel = new JPanel(new GridLayout(4, 2));
        for (String label : scientificLabels) {
            JButton btn = new JButton(label);
            buttons.put(label, btn);
            scientificPanel.add(btn);
        }
    }

    private void setupLayout() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(buttonPanel, BorderLayout.CENTER);
        centerPanel.add(scientificPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void setDisplayText(String text) {
        displayField.setText(text);
    }

    public String getDisplayText() {
        return displayField.getText();
    }

    public void appendDisplay(String value) {
        displayField.setText(displayField.getText() + value);
    }

    public JButton getButton(String label) {
        return buttons.get(label);
    }

    public void addButtonListener(ActionListener listener) {
        for (JButton btn : buttons.values()) {
            btn.addActionListener(listener);
        }
    }

    public void addKeyListenerToDisplay(KeyListener listener) {
        displayField.addKeyListener(listener);
    }
}

