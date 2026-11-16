import java.io.IOException;
import java.time.LocalDate;

public class App {
    public static void main(String[] args) {
        Stockage stockage = new StockageCsv("depenses.csv");
        GestionnaireDepenses gestionnaire = new GestionnaireDepenses(stockage);
        LocalDate auj = LocalDate.now();

        try {
            gestionnaire.charger();
            gestionnaire.definirBudget(3000);

            gestionnaire.ajouterDepense(2500, auj, Categorie.ALIMENTATION, "Courses");
            afficherAlerte(gestionnaire, auj);

            gestionnaire.ajouterDepense(1000, auj, Categorie.TRANSPORT, "Ticket de bus");
            afficherAlerte(gestionnaire, auj);

            System.out.println("--- Opérations des 7 derniers jours ---");
            for (Transaction t : gestionnaire.filtrerParPeriode(auj.minusDays(7), auj)) {
                System.out.println(t);
            }

            System.out.println("--- Dépenses TRANSPORT ---");
            for (Depense d : gestionnaire.filtrerParCategorie(Categorie.TRANSPORT)) {
                System.out.println(d);
            }

            gestionnaire.sauvegarder();
        } catch (IOException e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }

    private static void afficherAlerte(GestionnaireDepenses g, LocalDate date) {
        String alerte = g.alerteBudget(date.getYear(), date.getMonthValue());
        if (alerte != null) {
            System.out.println(alerte);
        }
    }
}