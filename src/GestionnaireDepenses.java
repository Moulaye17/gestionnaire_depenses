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
}