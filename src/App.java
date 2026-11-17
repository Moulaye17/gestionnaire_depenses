import java.io.IOException;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        Stockage stockage = new StockageCsv("depenses.csv");
        GestionnaireDepenses gestionnaire = new GestionnaireDepenses(stockage);

        try {
            gestionnaire.charger();
        } catch (IOException e) {
            System.out.println("Impossible de lire les données : " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> new FenetrePrincipale(gestionnaire).setVisible(true));
    }
}
