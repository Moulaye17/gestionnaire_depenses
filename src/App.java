import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        Stockage stockage = new StockageCsv("depenses.csv");

        List<Transaction> liste = new ArrayList<>();
        liste.add(new Depense(1, 2500, LocalDate.now(),
                              Categorie.ALIMENTATION, "Courses au marché"));
        liste.add(new Revenu(2, 10000, LocalDate.now(),
                             "Bourse", "Bourse du mois"));

        try {
            stockage.sauvegarder(liste);
            System.out.println("Sauvegarde faite.");

            List<Transaction> rechargees = stockage.charger();
            for (Transaction t : rechargees) {
                System.out.println(t);
            }
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}