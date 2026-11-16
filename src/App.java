import java.time.LocalDate;

public class App {
    public static void main(String[] args) {
        Transaction t1 = new Depense(1, 2500, LocalDate.now(),
                                     Categorie.ALIMENTATION, "Courses au marché");
        Transaction t2 = new Revenu(2, 10000, LocalDate.now(),
                                    "Bourse", "Bourse du mois");

        System.out.println(t1);
        System.out.println(t2);
    }
}