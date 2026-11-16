import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GestionnaireDepenses {
    private Stockage stockage;
    private List<Transaction> transactions = new ArrayList<>();
    private int prochainId = 1;
    private double budgetMensuel = 0; // 0 = pas de budget défini

    public GestionnaireDepenses(Stockage stockage) {
        this.stockage = stockage;
    }

    // ---------- Chargement et sauvegarde ----------

    public void charger() throws IOException {
        transactions = new ArrayList<>(stockage.charger());
        prochainId = 1;
        for (Transaction t : transactions) {
            if (t.getId() >= prochainId) {
                prochainId = t.getId() + 1;
            }
        }
        budgetMensuel = stockage.chargerBudget();
    }

    public void sauvegarder() throws IOException {
        stockage.sauvegarder(transactions);
    }

    // ---------- Ajouter et supprimer ----------

    public Depense ajouterDepense(double montant, LocalDate date,
                                  Categorie categorie, String description) {
        Depense d = new Depense(prochainId, montant, date, categorie, description);
        prochainId++;
        transactions.add(d);
        return d;
    }

    public Revenu ajouterRevenu(double montant, LocalDate date,
                                String source, String description) {
        Revenu r = new Revenu(prochainId, montant, date, source, description);
        prochainId++;
        transactions.add(r);
        return r;
    }

    public boolean supprimer(int id) {
        return transactions.removeIf(t -> t.getId() == id);
    }

    public List<Transaction> lister() {
        return new ArrayList<>(transactions);
    }

    // ---------- Filtres ----------

    // Opérations comprises entre deux dates (bornes incluses)
    public List<Transaction> filtrerParPeriode(LocalDate debut, LocalDate fin) {
        List<Transaction> resultat = new ArrayList<>();
        for (Transaction t : transactions) {
            if (!t.getDate().isBefore(debut) && !t.getDate().isAfter(fin)) {
                resultat.add(t);
            }
        }
        return resultat;
    }

    // Dépenses d'une catégorie
    public List<Depense> filtrerParCategorie(Categorie categorie) {
        List<Depense> resultat = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t instanceof Depense) {
                Depense d = (Depense) t;
                if (d.getCategorie() == categorie) {
                    resultat.add(d);
                }
            }
        }
        return resultat;
    }

    // ---------- Totaux ----------

    public double totalDepenses() {
        double somme = 0;
        for (Transaction t : transactions) {
            if (t instanceof Depense) {
                somme += t.getMontant();
            }
        }
        return somme;
    }

    public double totalRevenus() {
        double somme = 0;
        for (Transaction t : transactions) {
            if (t instanceof Revenu) {
                somme += t.getMontant();
            }
        }
        return somme;
    }

    public double solde() {
        return totalRevenus() - totalDepenses();
    }

    public double totalDepensesDuMois(int annee, int mois) {
        double somme = 0;
        for (Transaction t : transactions) {
            if (t instanceof Depense
                    && t.getDate().getYear() == annee
                    && t.getDate().getMonthValue() == mois) {
                somme += t.getMontant();
            }
        }
        return somme;
    }

    public Map<Categorie, Double> totalParCategorie() {
        Map<Categorie, Double> totaux = new EnumMap<>(Categorie.class);
        for (Transaction t : transactions) {
            if (t instanceof Depense) {
                Depense d = (Depense) t;
                totaux.merge(d.getCategorie(), d.getMontant(), Double::sum);
            }
        }
        return totaux;
    }

    // ---------- Budget ----------

    public double getBudgetMensuel() {
        return budgetMensuel;
    }

    public void definirBudget(double budget) throws IOException {
        this.budgetMensuel = budget;
        stockage.sauvegarderBudget(budget);
    }

    // Renvoie un message d'alerte, ou null s'il n'y a rien à signaler
    public String alerteBudget(int annee, int mois) {
        if (budgetMensuel <= 0) {
            return null;
        }
        double total = totalDepensesDuMois(annee, mois);
        if (total > budgetMensuel) {
            return "ALERTE : budget dépassé de " + (total - budgetMensuel) + " FCFA";
        }
        if (total >= 0.8 * budgetMensuel) {
            return "ATTENTION : " + Math.round(total * 100 / budgetMensuel)
                    + " % du budget utilisé";
        }
        return null;
    }
}