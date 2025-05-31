package laptopstore.screen.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

public class QueryOptionsDialog extends JDialog {
    private Consumer<String> onOptionSelected;
    private final JList<String> optionsList;

    public QueryOptionsDialog(JFrame parent, String title, String[] options) {
        super(parent, title, true);
        setLayout(new BorderLayout(10, 10));
        
        // Tạo list options với các tùy chọn được truyền vào
        optionsList = new JList<>(options);
        optionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        optionsList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(optionsList);
        scrollPane.setPreferredSize(new Dimension(300, 200));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton selectButton = new JButton("Select");
        JButton cancelButton = new JButton("Cancel");
        
        selectButton.addActionListener(e -> {
            String selectedOption = optionsList.getSelectedValue();
            if (selectedOption != null && onOptionSelected != null) {
                onOptionSelected.accept(selectedOption);
            }
            dispose();
        });
        
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);
        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(parent);
    }

    public void setOnOptionSelected(Consumer<String> handler) {
        this.onOptionSelected = handler;
    }
}
