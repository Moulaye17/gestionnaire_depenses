import java.time.LocalDate;

public class Depense {
    private int id;
    private double montant;
    private LocalDate date;
    private Categorie categorie;
    private String description;

    public Depense(int id, double montant, LocalDate date,
                   Categorie categorie, String description) {
        this.id = id;
        this.montant = montant;
        this.date = date;
        this.categorie = categorie;
        this.description = description;
    }

    public int getId() { return id; }
    public double getMontant() { return montant; }
    public LocalDate getDate() { return date; }
    public Categorie getCategorie() { return categorie; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return id + " | " + date + " | " + categorie + " | "
               + montant + " FCFA | " + description;
    }
}