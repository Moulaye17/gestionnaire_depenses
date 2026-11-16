import java.time.LocalDate;

public class Depense extends Transaction {
    private Categorie categorie;

    public Depense(int id, double montant, LocalDate date,
                   Categorie categorie, String description) {
        super(id, montant, date, description);
        this.categorie = categorie;
    }

    public Categorie getCategorie() { return categorie; }

    @Override
    public String getType() { return "DEPENSE"; }

    @Override
    public String toString() {
        return super.toString() + " | " + categorie;
    }
}