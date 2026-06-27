/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package insertPages;

/**
 *
 * @author Administrator
 */
import db.DBConnection;
import java.sql.*;
import mainPages.ContractsPage;

public class ExtendContractPage extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(ExtendContractPage.class.getName());
    private int idUgovora;
    private double osnovnaCena;
    /**
     * Creates new form ExtendContract
     * @param idUgovora
     */
    public ExtendContractPage(int idUgovora) {
        this.idUgovora = idUgovora;
        initComponents();
        this.setResizable(false); 
        this.setLocationRelativeTo(null);
        ucitajPodatkeOUgovoru();
        produzetakLbl.setText("");
        produzetakCena.setText("");
        produziBtn.setEnabled(false);
        
        datumProduzetka.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { proveriDatum(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { proveriDatum(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { proveriDatum(); }
        });
    }
    
    private void proveriDatum() {
        String tekst = datumProduzetka.getText().trim();
        if (tekst.isEmpty()) return;

        // Pokušaj parsiranja kroz nekoliko mogućih formata koje korisnik može uneti
        java.time.format.DateTimeFormatter[] formati = {
            java.time.format.DateTimeFormatter.ofPattern("M/d/yy"),
            java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy"),
            java.time.format.DateTimeFormatter.ISO_LOCAL_DATE // yyyy-MM-dd
        };

        java.time.LocalDate noviDatum = null;
        for (java.time.format.DateTimeFormatter dtf : formati) {
            try {
                noviDatum = java.time.LocalDate.parse(tekst, dtf);
                break; // Ako uspe, izađi iz petlje
            } catch (Exception ignored) {}
        }

        if (noviDatum != null) {
            try {
                java.time.LocalDate stariDatum = java.time.LocalDate.parse(datum2.getText().trim());
                long brojDana = java.time.temporal.ChronoUnit.DAYS.between(stariDatum, noviDatum);

                if (brojDana + 2 > 0) {
                    produziBtn.setEnabled(true);
                    produzetakLbl.setVisible(true); // Očisti labelu
                    izracunajCenuSaPopustom(brojDana + 2);
                } else {
                    produziBtn.setEnabled(false);
                    produzetakLbl.setText("Datum mora biti u budućnosti!");
                }
            } catch (Exception e) {
                produzetakLbl.setText("Greška u formatu starog datuma");
            }
        } else {
            produziBtn.setEnabled(false);
            produzetakLbl.setText("Neispravan format datuma");
        }
    }
        

    public ExtendContractPage() {
        initComponents();
        this.setResizable(false); 
        this.setLocationRelativeTo(null);
    }
    
    private void izracunajCenuSaPopustom(long brojDana) {
        String sqlCount = "SELECT COUNT(*) AS BrojProduzenja FROM ProduzenjaUgovora WHERE BrojUgovora = ?";

        try (Connection con = DBConnection.getConnection();
            PreparedStatement psCount = con.prepareStatement(sqlCount)) {

            psCount.setInt(1, idUgovora);
            ResultSet rsCount = psCount.executeQuery();

            if (rsCount.next()) {
                int brProduzenja = rsCount.getInt("BrojProduzenja");

                // Osnovna cena ovde predstavlja cenu po danu
                double ukupnaCena = this.osnovnaCena * brojDana;

                // Provera da li ima popusta (3. produženje ili više, tj. >= 2 prethodna zapisa)
                if (brProduzenja >= 2) { 
                    double staraCena = ukupnaCena;
                    double novaCena = ukupnaCena * 0.8; // 20% popusta

                    // HTML format: <strike> precrtava tekst, <font color='green'> daje vizuelni akcenat
                    String htmlTekst = String.format("<html><strike>%.2f €</strike> <font color='green'>%.2f €</font></html>", 
                                                       staraCena, novaCena);

                    produzetakCena.setText(htmlTekst);
                    produzetakLbl.setText("Cena sa popustom:");
                } else {
                    // Ako nema popusta, prikaži samo normalnu izračunatu cenu
                    produzetakCena.setText(String.format("%.2f €", ukupnaCena));
                    produzetakLbl.setText("Iznos novog ugovora:");
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Greška pri kalkulaciji cene", ex);
        }
    }
    
    private void ucitajPodatkeOUgovoru() {
        String sql = "SELECT DatumSklapanja, PlaniraniDatumVracanja, TroskoviNajma FROM Ugovori WHERE BrojUgovora = ?";
        String sqlCount = "SELECT COUNT(*) AS BrojProduzenja FROM ProduzenjaUgovora WHERE BrojUgovora = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             PreparedStatement psCount = con.prepareStatement(sqlCount)) {

            // Učitaj osnovne podatke
            ps.setInt(1, idUgovora);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                datum1.setText(rs.getString("DatumSklapanja"));
                datum2.setText(rs.getString("PlaniraniDatumVracanja"));
                double osnovnaCena = rs.getDouble("TroskoviNajma");
                this.osnovnaCena = rs.getDouble("TroskoviNajma");

                // Provera broja produženja za popust
                psCount.setInt(1, idUgovora);
                ResultSet rsCount = psCount.executeQuery();
                if (rsCount.next()) {
                    int brProduzenja = rsCount.getInt("BrojProduzenja");
                    double finalnaCena = osnovnaCena;

                    if (brProduzenja >= 2) { // Ako je ovo 3. produženje (prethodna 2 + ovo)
                        finalnaCena = osnovnaCena * 0.8; // 20% popusta
                    }
                    produzetakLbl.setText("Iznos sa proudzetkom: ");
                    produzetakCena.setText(String.format("%.2f €", (double) finalnaCena));
                }
            }
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Greška pri učitavanju", ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        datum1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        datum2 = new javax.swing.JLabel();
        datumProduzetka = new javax.swing.JFormattedTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        produziBtn = new javax.swing.JButton();
        produzetakLbl = new javax.swing.JLabel();
        produzetakCena = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Datum pocetka ugovora:");

        datum1.setText("jLabel2");

        jLabel2.setText("Datum zavrsetka ugovora:");

        datum2.setText("datum2");

        datumProduzetka.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.DateFormatter(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT))));
        datumProduzetka.addActionListener(this::datumProduzetkaActionPerformed);

        jLabel3.setText("Unesite datum produzetka:");

        jLabel15.setText("Format: M/D/YY (npr. 6/27/26)");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(datum2, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(datum1, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(datumProduzetka, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(datum1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(datum2))
                .addGap(32, 32, 32)
                .addComponent(jLabel15)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(datumProduzetka, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        produziBtn.setText("Produzi");
        produziBtn.addActionListener(this::produziBtnActionPerformed);

        produzetakLbl.setText("Iznos sa produzetkom:");

        produzetakCena.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        produzetakCena.setText("jLabel5");

        jButton1.setText("<- Nazad");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(176, 176, 176)
                        .addComponent(produziBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(125, 125, 125)
                        .addComponent(produzetakLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(produzetakCena, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addComponent(jButton1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(produzetakLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(produzetakCena, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addComponent(produziBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(51, 51, 51))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void datumProduzetkaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_datumProduzetkaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_datumProduzetkaActionPerformed

    private void produziBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_produziBtnActionPerformed
        // TODO add your handling code here:
        String korisnickiDatum = datumProduzetka.getText().trim();
    
        try {
            // 1. Parsiraj ono što je korisnik uneo (M/d/yy)
            java.time.format.DateTimeFormatter dtfInput = java.time.format.DateTimeFormatter.ofPattern("M/d/yy");
            java.time.LocalDate datum = java.time.LocalDate.parse(korisnickiDatum, dtfInput);

            // 2. Formatiraj u format koji MySQL zahteva (yyyy-MM-dd)
            String mysqlDatum = datum.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE);

            String sqlInsert = "INSERT INTO produzenjaugovora (BrojUgovora, NoviDatumVracanja) VALUES (?, ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sqlInsert)) {

                ps.setInt(1, idUgovora);
                ps.setString(2, mysqlDatum); // Šaljemo formatiran datum '2026-06-30'

                int redovi = ps.executeUpdate();
                if (redovi > 0) {
                    javax.swing.JOptionPane.showMessageDialog(this, "Ugovor uspešno produžen!");
                    ContractsPage contracts = new ContractsPage();
                    contracts.setVisible(true);
                    this.dispose();
                }
            }
        } catch (java.time.format.DateTimeParseException e) {
            javax.swing.JOptionPane.showMessageDialog(this, "Pogrešan format datuma! Molim unesite npr. 6/30/26");
        } catch (SQLException ex) {
            logger.log(java.util.logging.Level.SEVERE, "Greška pri čuvanju produženja", ex);
            javax.swing.JOptionPane.showMessageDialog(this, "Došlo je do greške pri snimanju u bazu.");
        }
    }//GEN-LAST:event_produziBtnActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        ContractsPage contracts = new ContractsPage();
        contracts.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new ExtendContractPage().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel datum1;
    private javax.swing.JLabel datum2;
    private javax.swing.JFormattedTextField datumProduzetka;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel produzetakCena;
    private javax.swing.JLabel produzetakLbl;
    private javax.swing.JButton produziBtn;
    // End of variables declaration//GEN-END:variables
}
