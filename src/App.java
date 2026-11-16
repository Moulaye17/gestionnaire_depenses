import java.time.LocalDate;

public class App {
    public static void main(String[] args) {
        Depense d = new Depense(1, 2500, LocalDate.now(),
                                Categorie.ALIMENTATION, "Courses au marché");
        System.out.println(d);
    }
}