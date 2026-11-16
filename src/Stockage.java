import java.io.IOException;
import java.util.List;

public interface Stockage {
    void sauvegarder(List<Transaction> transactions) throws IOException;

    List<Transaction> charger() throws IOException;
}