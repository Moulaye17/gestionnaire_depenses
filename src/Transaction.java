import java.time.LocalDate;

public abstract class Transaction {
    private int id;
    private double montant;
    private LocalDate date;
    private String description;

    public Transaction(int id, double montant, LocalDate date, String description) {
        this.id = id;
        this.montant = montant;
        this.date = date;
        this.description = description;
    }

    public int getId() { return id; }
    public double getMontant() { return montant; }
    public LocalDate getDate() { return date; }
    public String getDescription() { return description; }

    // Chaque classe fille doit dire quel est son type
    public abstract String getType();

    @Override
    public String toString() {
        return id + " | " + getType() + " | " + date + " | "
               + montant + " FCFA | " + description;
    }
}