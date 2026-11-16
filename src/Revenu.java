import java.time.LocalDate;

public class Revenu extends Transaction {
    private String source;

    public Revenu(int id, double montant, LocalDate date,
                  String source, String description) {
        super(id, montant, date, description);
        this.source = source;
    }

    public String getSource() { return source; }

    @Override
    public String getType() { return "REVENU"; }

    @Override
    public String toString() {
        return super.toString() + " | source : " + source;
    }
}