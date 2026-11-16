import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class StockageCsv implements Stockage {
    private Path fichier;

    public StockageCsv(String chemin) {
        this.fichier = Paths.get(chemin);
    }

    @Override
    public void sauvegarder(List<Transaction> transactions) throws IOException {
        List<String> lignes = new ArrayList<>();
        for (Transaction t : transactions) {
            String detail = "";
            if (t instanceof Depense) {
                detail = ((Depense) t).getCategorie().name();
            } else if (t instanceof Revenu) {
                detail = ((Revenu) t).getSource();
            }
            lignes.add(t.getType() + ";" + t.getId() + ";" + t.getMontant() + ";"
                    + t.getDate() + ";" + detail.replace(";", ",") + ";"
                    + t.getDescription().replace(";", ","));
        }
        Files.write(fichier, lignes, StandardCharsets.UTF_8);
    }

    @Override
    public List<Transaction> charger() throws IOException {
        List<Transaction> resultat = new ArrayList<>();
        if (!Files.exists(fichier)) {
            return resultat; // premier lancement : rien à charger
        }
        for (String ligne : Files.readAllLines(fichier, StandardCharsets.UTF_8)) {
            if (ligne.isBlank()) {
                continue;
            }
            String[] p = ligne.split(";", 6);
            if (p.length < 6) {
                continue;
            }
            try {
                int id = Integer.parseInt(p[1]);
                double montant = Double.parseDouble(p[2]);
                LocalDate date = LocalDate.parse(p[3]);
                if (p[0].equals("DEPENSE")) {
                    resultat.add(new Depense(id, montant, date, Categorie.valueOf(p[4]), p[5]));
                } else if (p[0].equals("REVENU")) {
                    resultat.add(new Revenu(id, montant, date, p[4], p[5]));
                }
            } catch (DateTimeParseException | IllegalArgumentException e) {
                System.out.println("Ligne ignorée : " + ligne);
            }
        }
        return resultat;
    }
}