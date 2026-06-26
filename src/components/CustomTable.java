/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package components;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import javax.swing.JPanel;

/**
 *
 * @author Administrator
 */
public class CustomTable extends JPanel {
    private final JTable table;
    private final DefaultTableModel model;

    public CustomTable(String[] columnNames) {
        setLayout(new BorderLayout());
        
        // Inicijalizacija modela sa prosleđenim kolonama
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);
        
        // Primena stila
        applyStyle();
        
        // Dodavanje u ScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void applyStyle() {
    table.setShowGrid(false);
    table.setIntercellSpacing(new Dimension(0, 0));
    table.setRowHeight(35);
    table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

    // --- KLJUČNO ZA RASTEZANJE ---
    // Ovo govori tabeli da rastegne kolone tako da popune celu širinu komponente
    table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
    // -----------------------------

    JTableHeader header = table.getTableHeader();
    header.setBackground(new Color(165, 225, 215));
    header.setForeground(Color.BLACK);
    header.setFont(new Font("Segoe UI", Font.BOLD, 14));
    header.setOpaque(true);

    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
    
    // VAŽNO: Pošto se kolone mogu dodavati dinamički, 
    // ovo treba primeniti na sve kolone koje postoje u modelu
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    // Metoda za punjenje tabele
    public void addRow(Object[] rowData) {
        model.addRow(rowData);
    }

    public void clearTable() {
        model.setRowCount(0);
    }

    public DefaultTableModel getModel() {
        return model;
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(750, 400);
    }
    
    public boolean isCellEditable(int row, int column) {
        return false; // 🚫 sve zabranjeno editovanje
    }
}
