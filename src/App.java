import java.io.IOException;
import java.time.LocalDate;

public class App {
    public static void main(String[] args) {
        Stockage stockage = new StockageCsv("depenses.csv");
        GestionnaireDepenses gestionnaire = new GestionnaireDepenses(stockage);

        try {
            gestionnaire.charger();

            gestionnaire.ajouterDepense(1000, LocalDate.now(),
                    Categorie.TRANSPORT, "Ticket de bus");

            System.out.println("--- Toutes les opérations ---");
            for (Transaction t : gestionnaire.lister()) {
                System.out.println(t);
            }

            System.out.println("Total dépenses : " + gestionnaire.totalDepenses() + " FCFA");
            System.out.println("Total revenus : " + gestionnaire.totalRevenus() + " FCFA");
            System.out.println("Solde : " + gestionnaire.solde() + " FCFA");
            System.out.println("Par catégorie : " + gestionnaire.totalParCategorie());

            gestionnaire.sauvegarder();
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}