package hackathon;

import com.intellij.find.editorHeaderActions.Utils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionButtonLook;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.actionSystem.impl.IdeaActionButtonLook;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.intellij.find.SearchTextArea.JUST_CLEARED_KEY;
import static com.intellij.find.SearchTextArea.NEW_LINE_KEYSTROKE;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static javax.swing.BorderFactory.createEmptyBorder;

public class SourcegraphSearchTextComponent extends JPanel implements PropertyChangeListener {

    //    private final JTextArea myTextArea;
    private final EditorTextField textField;
    private final JPanel myIconsPanel = new NonOpaquePanel();
    private final ActionButton myClearButton;
    private final NonOpaquePanel myExtraActionsPanel = new NonOpaquePanel();
    private final ActionButton myHistoryPopupButton;
    private final Stack<String> queryHistory;

    public SourcegraphSearchTextComponent(EditorTextField textField, Stack<String> queryHistory) {
        this.textField = textField;
        textField.addPropertyChangeListener("background", this);
        textField.addPropertyChangeListener("font", this);

        textField.setBorder(createEmptyBorder());


        // somehow this is the cause of the tab focus problems - without it the tab focus doesn't work. with ancestor mask it allows tab autocomplet
        // to work, but tab focus change doesn't work. With focus selected mask it doesnt work at all.
        DumbAwareAction.create(event -> { textField.transferFocus(); })
                .registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), textField);
        DumbAwareAction.create(event -> textField.transferFocusBackward())
                .registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, SHIFT_DOWN_MASK)), textField);

        textField.setOpaque(false);

        myHistoryPopupButton = new MyActionButton(new ShowHistoryAction(), false);
        myClearButton = new MyActionButton(new ClearAction(), false);

        this.queryHistory = queryHistory;

        updateLayout();
    }

    public String getText() {
        return textField.getText();
    }

    public EditorTextField editorTextField() {
        return textField;
    }

    private class ShowHistoryAction extends DumbAwareAction {

        ShowHistoryAction() {
            super("History",
                    "Show recent Sourcegraph searches",
                    AllIcons.Actions.SearchWithHistory);
            registerCustomShortcutSet(KeymapUtil.getActiveKeymapShortcuts("ShowSearchHistory"), textField);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            JBList<String> historyList = new JBList<>();
            showCompletionPopup(SourcegraphSearchTextComponent.this, historyList, null, null);
        }
    }

    private void showCompletionPopup(JComponent toolbarComponent,
                                     final JList list,
                                     String title,
                                     String ad) {

        final Runnable callback = () -> {
            String selectedValue = (String) list.getSelectedValue();
            if (selectedValue != null) {
                textField.setText(selectedValue);
                IdeFocusManager.getGlobalInstance().requestFocus(textField, false);
            }
        };

        final PopupChooserBuilder builder = JBPopupFactory.getInstance().createListPopupBuilder(list);
        if (title != null) {
            builder.setTitle(title);
        }
        final JBPopup popup = builder.setMovable(false).setResizable(false)
                .setRequestFocus(true).setItemChoosenCallback(callback).createPopup();

        if (ad != null) {
            popup.setAdText(ad, SwingConstants.LEFT);
        }

        if (toolbarComponent != null) {
            popup.showUnderneathOf(toolbarComponent);
        } else {
            popup.showUnderneathOf(textField);
        }
    }

    protected void updateLayout() {
        JPanel historyButtonWrapper = new NonOpaquePanel(new BorderLayout());
        historyButtonWrapper.setBorder(JBUI.Borders.emptyTop(1));
        historyButtonWrapper.add(myHistoryPopupButton, BorderLayout.NORTH);
        JPanel iconsPanelWrapper = new NonOpaquePanel(new BorderLayout());
        iconsPanelWrapper.setBorder(JBUI.Borders.emptyTop(1));
        JPanel p = new NonOpaquePanel(new BorderLayout());
        p.add(myIconsPanel, BorderLayout.NORTH);
        iconsPanelWrapper.add(p, BorderLayout.WEST);
        iconsPanelWrapper.add(myExtraActionsPanel, BorderLayout.CENTER);

        removeAll();
        setLayout(new BorderLayout(JBUIScale.scale(3), 0));
        setBorder(JBUI.Borders.empty(SystemInfo.isLinux ? JBUI.scale(0) : JBUI.scale(0)));
        add(historyButtonWrapper, BorderLayout.WEST);

        System.out.println("components");
        for (Component component : textField.getComponents()) {
            System.out.println(component.getClass().getName());
            // trying to get the border of this scrollpane to go away.... but it just..wont? Or something is resetting it?
        }
//        myScrollPane.getViewport().setBorder(null);
//        myScrollPane.getViewport().setOpaque(false);
//        myScrollPane.setOpaque(false);

//        Editor e = textField.getEditor();
//        e.getContentComponent();
//        add(textField.get, BorderLayout.CENTER);
        add(textField, BorderLayout.CENTER);
        textField.addNotify();
        Editor e = textField.getEditor();
        if (e == null) {
            System.out.println("null editor");
        } else {
            textField.getEditor().setBorder(null);
            if (e instanceof EditorImpl) {
                // setting one line mode
                System.out.println("one line mode");
                ((EditorImpl) e).setOneLineMode(true);
                ((EditorImpl) e).getScrollPane().setBorder(null);

            }
//            textField.getEditor().getSettings().set
            System.out.println("setting border");
        }

        textField.addContainerListener(new ContainerListener() {
            @Override
            public void componentAdded(ContainerEvent e) {
                System.out.println("added");
                System.out.println(e.getComponent().getClass());
                if (e.getComponent() instanceof Editor) {
                    System.out.println("editor found");
                    ((Editor) e.getComponent()).setBorder(null);
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {

            }
        });
        add(iconsPanelWrapper, BorderLayout.EAST);
        updateIconsLayout();
    }

    private void updateIconsLayout() {
        if (myIconsPanel.getParent() == null) {
            return;
        }

        boolean showClearIcon = !StringUtil.isEmpty(textField.getText());
        boolean wrongVisibility = (myClearButton.getParent() == null) == showClearIcon;

        boolean multiline = StringUtil.getLineBreakCount(textField.getText()) > 0;
        if (wrongVisibility) {
            myIconsPanel.removeAll();
            myIconsPanel.setLayout(new BorderLayout());
            myIconsPanel.add(myClearButton, BorderLayout.CENTER);
            myIconsPanel.setPreferredSize(myIconsPanel.getPreferredSize());
            if (!showClearIcon) myIconsPanel.remove(myClearButton);
            myIconsPanel.revalidate();
            myIconsPanel.repaint();
        }
        doLayout();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("background".equals(evt.getPropertyName())) {
            repaint();
        }
        if ("font".equals(evt.getPropertyName())) {
            updateLayout();
        }
    }

    public void fixEditor() {
        if (textField.getEditor() != null) {
            textField.getEditor().setBorder(null);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        updateFont();
        setBackground(UIUtil.getTextFieldBackground());
    }

    private void updateFont() {
        if (textField != null) {
            if (Registry.is("ide.find.use.editor.font", false)) {
                textField.setFont(EditorUtil.getEditorFont());
            } else {
                textField.setFont(UIManager.getFont("TextField.font"));
            }
        }
    }

    private class ClearAction extends DumbAwareAction {
        ClearAction() {
            super(AllIcons.Actions.Close);
            getTemplatePresentation().setHoveredIcon(AllIcons.Actions.CloseHovered);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            textField.putClientProperty(JUST_CLEARED_KEY, !textField.getText().isEmpty());
            textField.setText("");
        }
    }

    public List<Component> setExtraActions(AnAction... actions) {

        myExtraActionsPanel.removeAll();
        myExtraActionsPanel.setBorder(JBUI.Borders.empty());
        ArrayList<Component> addedButtons = new ArrayList<>();
        if (actions != null && actions.length > 0) {
            JPanel buttonsGrid = new NonOpaquePanel(new GridLayout(1, actions.length, 0, 0));
            for (AnAction action : actions) {
                ActionButton button = new MyActionButton(action, true);
                addedButtons.add(button);
                buttonsGrid.add(button);
            }
            myExtraActionsPanel.setLayout(new BorderLayout());
            myExtraActionsPanel.add(buttonsGrid, BorderLayout.NORTH);
            myExtraActionsPanel.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBColor.border(), 0, 1, 0, 0), JBUI.Borders.emptyLeft(4)));
        }
        return addedButtons;
    }

    private static final ActionButtonLook FIELD_INPLACE_LOOK = new IdeaActionButtonLook() {
        @Override
        public void paintBorder(Graphics g, JComponent component, @ActionButtonComponent.ButtonState int state) {
            if (component.isFocusOwner() && component.isEnabled()) {
                Rectangle rect = new Rectangle(component.getSize());
                JBInsets.removeFrom(rect, component.getInsets());
                SYSTEM_LOOK.paintLookBorder(g, rect, JBUI.CurrentTheme.ActionButton.focusedBorder());
            } else {
                super.paintBorder(g, component, ActionButtonComponent.NORMAL);
            }
        }

        @Override
        public void paintBackground(Graphics g, JComponent component, int state) {
            if (((MyActionButton) component).isRolloverState()) {
                super.paintBackground(g, component, state);
            }
        }
    };

    private static class MyActionButton extends ActionButton {

        private MyActionButton(@NotNull AnAction action, boolean focusable) {
            super(action, action.getTemplatePresentation().clone(), ActionPlaces.UNKNOWN, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE);

            setLook(focusable ? FIELD_INPLACE_LOOK : ActionButtonLook.INPLACE_LOOK);
            setFocusable(focusable);
            updateIcon();
        }

        @Override
        protected DataContext getDataContext() {
            return DataManager.getInstance().getDataContext(this);
        }

        @Override
        public int getPopState() {
            return isSelected() ? SELECTED : super.getPopState();
        }

        boolean isRolloverState() {
            return super.isRollover();
        }

        @Override
        public Icon getIcon() {
            if (isEnabled() && isSelected()) {
                Icon selectedIcon = myPresentation.getSelectedIcon();
                if (selectedIcon != null) return selectedIcon;
            }
            return super.getIcon();
        }
    }
}
