import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Interface graphique. Elle ne contient AUCUN calcul :
 * tout passe par le GestionnaireDepenses.
 */
public class FenetrePrincipale extends JFrame {
    private GestionnaireDepenses gestionnaire;

    private DefaultTableModel modeleTable;
    private JTable table;
    private JLabel labelTotaux = new JLabel();
    private JLabel labelBudget = new JLabel();
    private JLabel labelAlerte = new JLabel(" ");

    private JComboBox<Object> comboCategorie = new JComboBox<>();
    private JTextField champDebut = new JTextField(9);
    private JTextField champFin = new JTextField(9);

    public FenetrePrincipale(GestionnaireDepenses gestionnaire) {
        super("Gestionnaire de dépenses");
        this.gestionnaire = gestionnaire;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 560);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        add(creerHaut(), BorderLayout.NORTH);
        add(creerTable(), BorderLayout.CENTER);
        add(creerBas(), BorderLayout.SOUTH);

        rafraichir();
    }

    // ---------- Construction de la fenêtre ----------

    private JPanel creerHaut() {
        JPanel haut = new JPanel(new GridLayout(3, 1));

        JPanel ligneStats = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        ligneStats.add(labelTotaux);
        ligneStats.add(labelBudget);

        JPanel ligneAlerte = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        ligneAlerte.add(labelAlerte);

        haut.add(ligneStats);
        haut.add(ligneAlerte);
        haut.add(creerFiltres());
        return haut;
    }

    private JPanel creerFiltres() {
        comboCategorie.addItem("Toutes les opérations");
        for (Categorie c : Categorie.values()) {
            comboCategorie.addItem(c);
        }

        JButton boutonFiltrer = new JButton("Filtrer");
        JButton boutonTout = new JButton("Tout afficher");
        boutonFiltrer.addActionListener(e -> rafraichir());
        boutonTout.addActionListener(e -> {
            comboCategorie.setSelectedIndex(0);
            champDebut.setText("");
            champFin.setText("");
            rafraichir();
        });

        JPanel filtres = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        filtres.add(new JLabel("Catégorie :"));
        filtres.add(comboCategorie);
        filtres.add(new JLabel("Du (AAAA-MM-JJ) :"));
        filtres.add(champDebut);
        filtres.add(new JLabel("au :"));
        filtres.add(champFin);
        filtres.add(boutonFiltrer);
        filtres.add(boutonTout);
        return filtres;
    }

    private JScrollPane creerTable() {
        String[] colonnes = {"Id", "Type", "Date", "Montant (FCFA)",
                             "Catégorie / Source", "Description"};
        modeleTable = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false; // le tableau est en lecture seule
            }
        };
        table = new JTable(modeleTable);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel creerBas() {
        JButton boutonDepense = new JButton("Ajouter une dépense");
        JButton boutonRevenu = new JButton("Ajouter un revenu");
        JButton boutonSupprimer = new JButton("Supprimer la sélection");
        JButton boutonBudget = new JButton("Définir le budget");

        boutonDepense.addActionListener(e -> ajouterDepense());
        boutonRevenu.addActionListener(e -> ajouterRevenu());
        boutonSupprimer.addActionListener(e -> supprimerSelection());
        boutonBudget.addActionListener(e -> definirBudget());

        JPanel bas = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bas.add(boutonDepense);
        bas.add(boutonRevenu);
        bas.add(boutonSupprimer);
        bas.add(boutonBudget);
        return bas;
    }

    // ---------- Mise à jour de l'affichage ----------

    private void rafraichir() {
        // 1. Statistiques
        labelTotaux.setText("Dépenses : " + gestionnaire.totalDepenses()
                + " FCFA   |   Revenus : " + gestionnaire.totalRevenus()
                + " FCFA   |   Solde : " + gestionnaire.solde() + " FCFA");

        double budget = gestionnaire.getBudgetMensuel();
        labelBudget.setText(budget > 0
                ? "Budget mensuel : " + budget + " FCFA"
                : "Aucun budget défini");

        LocalDate auj = LocalDate.now();
        String alerte = gestionnaire.alerteBudget(auj.getYear(), auj.getMonthValue());
        if (alerte == null) {
            labelAlerte.setText(" ");
        } else {
            labelAlerte.setText(alerte);
            labelAlerte.setForeground(alerte.startsWith("ALERTE")
                    ? Color.RED : new Color(200, 120, 0));
        }

        // 2. Tableau
        List<Transaction> liste = listeFiltree();
        if (liste == null) {
            return; // dates invalides : message déjà affiché
        }
        modeleTable.setRowCount(0);
        for (Transaction t : liste) {
            String detail = "";
            if (t instanceof Depense) {
                detail = ((Depense) t).getCategorie().toString();
            } else if (t instanceof Revenu) {
                detail = ((Revenu) t).getSource();
            }
            modeleTable.addRow(new Object[] {
                t.getId(), t.getType(), t.getDate(), t.getMontant(),
                detail, t.getDescription()
            });
        }
    }

    // Applique les filtres choisis. Renvoie null si les dates sont invalides.
    private List<Transaction> listeFiltree() {
        String texteDebut = champDebut.getText().trim();
        String texteFin = champFin.getText().trim();

        List<Transaction> liste;
        if (texteDebut.isEmpty() && texteFin.isEmpty()) {
            liste = gestionnaire.lister();
        } else if (texteDebut.isEmpty() || texteFin.isEmpty()) {
            erreur("Remplissez les deux dates (début et fin), ou aucune.");
            return null;
        } else {
            try {
                LocalDate debut = LocalDate.parse(texteDebut);
                LocalDate fin = LocalDate.parse(texteFin);
                if (fin.isBefore(debut)) {
                    erreur("La date de fin doit être après la date de début.");
                    return null;
                }
                liste = gestionnaire.filtrerParPeriode(debut, fin);
            } catch (DateTimeParseException e) {
                erreur("Date invalide. Format attendu : AAAA-MM-JJ (ex. 2026-11-15).");
                return null;
            }
        }

        Object choix = comboCategorie.getSelectedItem();
        if (choix instanceof Categorie) {
            List<Transaction> gardees = new ArrayList<>();
            for (Transaction t : liste) {
                if (t instanceof Depense && ((Depense) t).getCategorie() == choix) {
                    gardees.add(t);
                }
            }
            liste = gardees;
        }
        return liste;
    }

    // ---------- Actions des boutons ----------

    private void ajouterDepense() {
        JTextField champMontant = new JTextField();
        JTextField champDate = new JTextField(LocalDate.now().toString());
        JComboBox<Categorie> champCategorie = new JComboBox<>(Categorie.values());
        JTextField champDescription = new JTextField();

        JPanel formulaire = new JPanel(new GridLayout(0, 2, 5, 5));
        formulaire.add(new JLabel("Montant (FCFA) :"));
        formulaire.add(champMontant);
        formulaire.add(new JLabel("Date (AAAA-MM-JJ) :"));
        formulaire.add(champDate);
        formulaire.add(new JLabel("Catégorie :"));
        formulaire.add(champCategorie);
        formulaire.add(new JLabel("Description :"));
        formulaire.add(champDescription);

        int reponse = JOptionPane.showConfirmDialog(this, formulaire, "Nouvelle dépense",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (reponse != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double montant = lireMontant(champMontant.getText());
            LocalDate date = LocalDate.parse(champDate.getText().trim());
            Categorie categorie = (Categorie) champCategorie.getSelectedItem();
            gestionnaire.ajouterDepense(montant, date, categorie, champDescription.getText());
            enregistrer();
            rafraichir();
        } catch (NumberFormatException | DateTimeParseException e) {
            erreur("Montant ou date invalide (montant positif, date AAAA-MM-JJ).");
        }
    }

    private void ajouterRevenu() {
        JTextField champMontant = new JTextField();
        JTextField champDate = new JTextField(LocalDate.now().toString());
        JTextField champSource = new JTextField();
        JTextField champDescription = new JTextField();

        JPanel formulaire = new JPanel(new GridLayout(0, 2, 5, 5));
        formulaire.add(new JLabel("Montant (FCFA) :"));
        formulaire.add(champMontant);
        formulaire.add(new JLabel("Date (AAAA-MM-JJ) :"));
        formulaire.add(champDate);
        formulaire.add(new JLabel("Source (salaire, bourse...) :"));
        formulaire.add(champSource);
        formulaire.add(new JLabel("Description :"));
        formulaire.add(champDescription);

        int reponse = JOptionPane.showConfirmDialog(this, formulaire, "Nouveau revenu",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (reponse != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double montant = lireMontant(champMontant.getText());
            LocalDate date = LocalDate.parse(champDate.getText().trim());
            gestionnaire.ajouterRevenu(montant, date,
                    champSource.getText(), champDescription.getText());
            enregistrer();
            rafraichir();
        } catch (NumberFormatException | DateTimeParseException e) {
            erreur("Montant ou date invalide (montant positif, date AAAA-MM-JJ).");
        }
    }

    private void supprimerSelection() {
        int ligne = table.getSelectedRow();
        if (ligne < 0) {
            JOptionPane.showMessageDialog(this, "Sélectionnez d'abord une ligne du tableau.");
            return;
        }
        int id = (int) modeleTable.getValueAt(ligne, 0);

        int reponse = JOptionPane.showConfirmDialog(this,
                "Supprimer l'opération n° " + id + " ?", "Confirmation",
                JOptionPane.YES_NO_OPTION);
        if (reponse == JOptionPane.YES_OPTION) {
            gestionnaire.supprimer(id);
            enregistrer();
            rafraichir();
        }
    }

    private void definirBudget() {
        String saisie = JOptionPane.showInputDialog(this, "Budget mensuel (FCFA) :");
        if (saisie == null) {
            return; // l'utilisateur a annulé
        }
        try {
            double budget = lireMontant(saisie);
            gestionnaire.definirBudget(budget);
            rafraichir();
        } catch (NumberFormatException e) {
            erreur("Budget invalide (nombre positif attendu).");
        } catch (IOException e) {
            erreur("Erreur de sauvegarde du budget : " + e.getMessage());
        }
    }

    // ---------- Outils ----------

    // Lit un montant positif (accepte la virgule). Lève NumberFormatException sinon.
    private double lireMontant(String texte) {
        double valeur = Double.parseDouble(texte.trim().replace(",", "."));
        if (valeur <= 0) {
            throw new NumberFormatException("Le montant doit être positif");
        }
        return valeur;
    }

    private void enregistrer() {
        try {
            gestionnaire.sauvegarder();
        } catch (IOException e) {
            erreur("Erreur de sauvegarde : " + e.getMessage());
        }
    }

    private void erreur(String message) {
        JOptionPane.showMessageDialog(this, message, "Erreur", JOptionPane.ERROR_MESSAGE);
    }
}
