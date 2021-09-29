package hackathon.settings;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;

public class SourcegraphLocationSettingsComponent {
    private JPanel panel;
    private final JBTextField uriField = new JBTextField();
    private final JBTextField nameField = new JBTextField();
    private final JBTextField tokenField = new JBTextField();

    public SourcegraphLocationSettingsComponent() {
        panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("Enter Sourcegraph Name: "), nameField)
                .addLabeledComponent(new JBLabel("Enter Sourcegraph URI: "), uriField)
                .addLabeledComponent(new JBLabel("Enter Auth Token: "), tokenField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    public JComponent getPreferredFocusedComponent() {
        return nameField;
    }

    public String getName() {
        return nameField.getText();
    }

    public void setName(String name) {
        nameField.setText(name);
    }

    public JPanel getPanel() {
        return panel;
    }
}
