package FileUploader;

import models.ConnexionBDD;
import metiers.Arret;
import metiers.Desserte;
import metiers.HoraireDepart;
import metiers.Ligne;
import metiers.Perturbation;
import metiers.TypeHoraire;
import metiers.TypeTransport;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Uploader {
    private static final String SCHEMA_PAR_DEFAUT = "pitt_bdd_test";
    private static final int BATCH_SIZE = 1000;

    private final Connection connexion;
    private final String schema;
    private final String nomSchema;

    public Uploader() {
        this(connexionDisponible(), SCHEMA_PAR_DEFAUT);
    }

    public Uploader(Connection connexion) {
        this(connexion, SCHEMA_PAR_DEFAUT);
    }

    public Uploader(String nomSchema) {
        this(connexionDisponible(), nomSchema);
    }

    public Uploader(Connection connexion, String nomSchema) {
        this.connexion = Objects.requireNonNull(connexion, "La connexion BDD est obligatoire.");
        if (nomSchema == null || !nomSchema.matches("[A-Za-z_][A-Za-z0-9_-]{0,62}")) {
            throw new IllegalArgumentException("Nom de schéma invalide.");
        }
        this.nomSchema = nomSchema;
        this.schema = "\"" + nomSchema + "\".";
    }

    public ImportResult importer(Path dossierCsv, ImportMode mode) throws IOException, SQLException {
        Objects.requireNonNull(dossierCsv, "Le dossier CSV est obligatoire.");
        Objects.requireNonNull(mode, "Le mode d'import est obligatoire.");
        if (mode != ImportMode.REPLACE_NETWORK) {
            throw new IllegalArgumentException("Mode d'import non pris en charge : " + mode);
        }

        Path dossier = dossierCsv.toRealPath();
        if (!Files.isDirectory(dossier)) {
            throw new IOException("Le chemin fourni n'est pas un dossier : " + dossierCsv);
        }
        verifierFichiers(dossier);

        synchronized (connexion) {
            if (connexion.isClosed()) {
                throw new SQLException("La connexion à la base de données est fermée.");
            }
            if (!connexion.getAutoCommit()) {
                throw new SQLException("L'import nécessite une connexion sans transaction déjà ouverte.");
            }

            connexion.setAutoCommit(false);
            Throwable erreurImport = null;
            try {
                verifierAbsenceDeLiensPerturbation();
                supprimerReseauExistant();

                Map<String, Integer> compteurs = new LinkedHashMap<>();
                importerTypesTransport(dossier, compteurs);
                importerTypesHoraire(dossier, compteurs);
                importerArrets(dossier, compteurs);
                importerLignes(dossier, compteurs);
                importerDessertes(dossier, compteurs);
                importerHorairesDepart(dossier, compteurs);
                verifierJeuNonVide(compteurs);
                synchroniserSequences();

                connexion.commit();
                reinitialiserCaches();
                return new ImportResult(compteurs);
            } catch (IOException | SQLException | RuntimeException | Error e) {
                erreurImport = e;
                try {
                    connexion.rollback();
                } catch (SQLException erreurRollback) {
                    e.addSuppressed(erreurRollback);
                }
                throw e;
            } finally {
                try {
                    connexion.setAutoCommit(true);
                } catch (SQLException erreurRestauration) {
                    if (erreurImport != null) {
                        erreurImport.addSuppressed(erreurRestauration);
                    } else {
                        throw erreurRestauration;
                    }
                }
            }
        }
    }

    private void verifierFichiers(Path dossier) throws IOException {
        for (String nom : List.of(
                "types_transport.csv",
                "types_horaires.csv",
                "arrets.csv",
                "lignes.csv",
                "dessertes.csv",
                "horaires_depart.csv")) {
            Path fichier = dossier.resolve(nom);
            if (Files.isSymbolicLink(fichier) || !Files.isRegularFile(fichier)) {
                throw new IOException("Fichier CSV manquant ou invalide : " + fichier);
            }
        }
    }

    private static Connection connexionDisponible() {
        Connection connexion = ConnexionBDD.getConnexion();
        if (connexion == null) {
            throw new IllegalStateException("La connexion à la base de données n'a pas pu être établie.");
        }
        return connexion;
    }

    private void verifierAbsenceDeLiensPerturbation() throws SQLException {
        String query = "SELECT EXISTS (SELECT 1 FROM " + schema + "arretperturbe)";
        try (Statement statement = connexion.createStatement();
             var result = statement.executeQuery(query)) {
            if (result.next() && result.getBoolean(1)) {
                throw new SQLException("Le remplacement est annulé : des perturbations référencent actuellement des arrêts. "
                        + "Supprimez ou migrez ces associations avant de remplacer le réseau.");
            }
        }
    }

    private void supprimerReseauExistant() throws SQLException {
        // La suppression du réseau reste dans la transaction d'import.
        for (String table : List.of(
                "horairedepart", "desserte", "ligne", "arret", "typehoraire", "typetransport")) {
            try (Statement statement = connexion.createStatement()) {
                statement.executeUpdate("DELETE FROM " + schema + table);
            }
        }
    }

    private void verifierJeuNonVide(Map<String, Integer> compteurs) throws IOException {
        for (String table : List.of(
                "typetransport", "typehoraire", "arret", "ligne", "desserte", "horairedepart")) {
            if (compteurs.getOrDefault(table, 0) == 0) {
                throw new IOException("Le jeu de données ne contient aucune ligne pour la table " + table + ".");
            }
        }
    }

    private void importerTypesTransport(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema
                + "typetransport (idtypetransport, libelletypetransport, cout) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("types_transport.csv"),
                    List.of("idtypetransport", "libelletypetransport", "cout"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idtypetransport", ligne, true));
                        ps.setString(2, texte(champs, 1, "libelletypetransport", ligne, 50));
                        ps.setBigDecimal(3, decimal(champs, 2, "cout", ligne, 15, 4, true));
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("typetransport", total[0]);
        }
    }

    private void importerTypesHoraire(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema + "typehoraire (idtypehoraire, libelle) VALUES (?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("types_horaires.csv"),
                    List.of("idtypehoraire", "libelle"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idtypehoraire", ligne, true));
                        ps.setString(2, texte(champs, 1, "libelle", ligne, 50));
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("typehoraire", total[0]);
        }
    }

    private void importerArrets(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema
                + "arret (idarret, nomarret, lattitude, longitude) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("arrets.csv"),
                    List.of("idarret", "nomarret", "lattitude", "longitude"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idarret", ligne, true));
                        ps.setString(2, texte(champs, 1, "nomarret", ligne, 50));
                        BigDecimal latitude = decimal(champs, 2, "lattitude", ligne, 10, 6, false);
                        BigDecimal longitude = decimal(champs, 3, "longitude", ligne, 10, 6, false);
                        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                                || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
                            throw erreurLigne("lattitude doit être comprise entre -90 et 90", ligne);
                        }
                        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                                || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                            throw erreurLigne("longitude doit être comprise entre -180 et 180", ligne);
                        }
                        ps.setBigDecimal(3, latitude);
                        ps.setBigDecimal(4, longitude);
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("arret", total[0]);
        }
    }

    private void importerLignes(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema
                + "ligne (idligne, libelle, hexcolor, idtypetransport, idarretdepart, idarretarrivee) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("lignes.csv"),
                    List.of("idligne", "libelle", "hexcolor", "idtypetransport", "idarretdepart", "idarretarrivee"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idligne", ligne, true));
                        ps.setString(2, texte(champs, 1, "libelle", ligne, 5));
                        String couleur = texte(champs, 2, "hexcolor", ligne, 6);
                        if (!couleur.matches("[0-9A-Fa-f]{6}")) {
                            throw erreurLigne("hexcolor doit contenir exactement 6 caractères hexadécimaux", ligne);
                        }
                        ps.setString(3, couleur);
                        ps.setInt(4, entier(champs, 3, "idtypetransport", ligne, true));
                        ps.setInt(5, entier(champs, 4, "idarretdepart", ligne, true));
                        ps.setInt(6, entier(champs, 5, "idarretarrivee", ligne, true));
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("ligne", total[0]);
        }
    }

    private void importerDessertes(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema
                + "desserte (idarret_est_precede, idarret_est_suivi, idligne, duree, distance) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("dessertes.csv"),
                    List.of("idarret_est_precede", "idarret_est_suivi", "idligne", "duree", "distance"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idarret_est_precede", ligne, true));
                        ps.setInt(2, entier(champs, 1, "idarret_est_suivi", ligne, true));
                        ps.setInt(3, entier(champs, 2, "idligne", ligne, true));
                        int duree = entier(champs, 3, "duree", ligne, false);
                        int distance = entier(champs, 4, "distance", ligne, false);
                        if (duree <= 0 || distance <= 0) {
                            throw erreurLigne("duree et distance doivent être strictement positives", ligne);
                        }
                        ps.setInt(4, duree);
                        ps.setInt(5, distance);
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("desserte", total[0]);
        }
    }

    private void importerHorairesDepart(Path dossier, Map<String, Integer> compteurs) throws IOException, SQLException {
        String sql = "INSERT INTO " + schema
                + "horairedepart (idligne, idhorairedepart, heuredepart, idtypehoraire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(sql)) {
            int[] enAttente = {0};
            int[] total = {0};
            LecteurCSV.lireFichier(dossier.resolve("horaires_depart.csv"),
                    List.of("idligne", "idhorairedepart", "heuredepart", "idtypehoraire"),
                    (ligne, champs) -> {
                        ps.setInt(1, entier(champs, 0, "idligne", ligne, true));
                        ps.setInt(2, entier(champs, 1, "idhorairedepart", ligne, true));
                        String heureTexte = valeur(champs, 2, "heuredepart", ligne);
                        try {
                            ps.setObject(3, LocalTime.parse(heureTexte));
                        } catch (RuntimeException e) {
                            throw erreurLigne("heuredepart invalide : " + heureTexte, ligne);
                        }
                        ps.setInt(4, entier(champs, 3, "idtypehoraire", ligne, true));
                        ps.addBatch();
                        ajouterAuLot(ps, enAttente, total);
                    });
            executerDernierLot(ps, enAttente);
            compteurs.put("horairedepart", total[0]);
        }
    }

    private void synchroniserSequences() throws SQLException {
        synchroniserSequence("arret", "idarret");
        synchroniserSequence("typetransport", "idtypetransport");
        synchroniserSequence("typehoraire", "idtypehoraire");
        synchroniserSequence("ligne", "idligne");
        synchroniserSequence("horairedepart", "idhorairedepart");
    }

    private void synchroniserSequence(String table, String colonne) throws SQLException {
        String relation = "\"" + nomSchema + "\"." + table;
        String query = "SELECT setval(pg_get_serial_sequence('" + relation + "', '" + colonne + "'), "
                + "GREATEST(COALESCE(MAX(" + colonne + "), 1), 1), COALESCE(MAX(" + colonne + "), 0) > 0) "
                + "FROM " + schema + table;
        try (Statement statement = connexion.createStatement()) {
            statement.executeQuery(query).close();
        }
    }

    private void reinitialiserCaches() {
        Arret.resetCache();
        Desserte.resetCache();
        HoraireDepart.resetCache();
        Ligne.resetCache();
        Perturbation.resetCache();
        TypeHoraire.resetCache();
        TypeTransport.resetCache();
    }

    private static void ajouterAuLot(PreparedStatement ps, int[] enAttente, int[] total) throws SQLException {
        enAttente[0]++;
        total[0]++;
        if (enAttente[0] == BATCH_SIZE) {
            ps.executeBatch();
            enAttente[0] = 0;
        }
    }

    private static void executerDernierLot(PreparedStatement ps, int[] enAttente) throws SQLException {
        if (enAttente[0] > 0) {
            ps.executeBatch();
        }
    }

    private static int entier(List<String> champs, int index, String nom, long ligne, boolean zeroAutorise)
            throws IOException {
        String valeur = valeur(champs, index, nom, ligne);
        try {
            int resultat = Integer.parseInt(valeur);
            if (resultat < 0 || (!zeroAutorise && resultat == 0)) {
                throw erreurLigne(nom + " doit être " + (zeroAutorise ? "positif ou nul" : "strictement positif"), ligne);
            }
            return resultat;
        } catch (NumberFormatException e) {
            throw erreurLigne(nom + " doit être un entier : " + valeur, ligne);
        }
    }

    private static BigDecimal decimal(List<String> champs, int index, String nom, long ligne,
                                      int precisionMax, int scaleMax, boolean nonNegative) throws IOException {
        String valeur = valeur(champs, index, nom, ligne);
        final BigDecimal resultat;
        try {
            resultat = new BigDecimal(valeur);
        } catch (NumberFormatException e) {
            throw erreurLigne(nom + " doit être un nombre décimal : " + valeur, ligne);
        }
        if (resultat.precision() > precisionMax || resultat.scale() > scaleMax
                || resultat.precision() - resultat.scale() > precisionMax - scaleMax) {
            throw erreurLigne(nom + " dépasse la précision autorisée (" + precisionMax + "," + scaleMax + ")", ligne);
        }
        if (nonNegative && resultat.signum() < 0) {
            throw erreurLigne(nom + " doit être positif ou nul", ligne);
        }
        return resultat;
    }

    private static String texte(List<String> champs, int index, String nom, long ligne, int tailleMax)
            throws IOException {
        String valeur = valeur(champs, index, nom, ligne);
        if (valeur.length() > tailleMax) {
            throw erreurLigne(nom + " dépasse " + tailleMax + " caractères", ligne);
        }
        return valeur;
    }

    private static String valeur(List<String> champs, int index, String nom, long ligne) throws IOException {
        String valeur = champs.get(index).trim();
        if (valeur.isEmpty()) {
            throw erreurLigne(nom + " est obligatoire", ligne);
        }
        return valeur;
    }

    private static IOException erreurLigne(String message, long ligne) {
        return new IOException("Ligne " + ligne + " : " + message);
    }

    public enum ImportMode {
        REPLACE_NETWORK
    }

    public static final class ImportResult {
        private final Map<String, Integer> compteurs;

        private ImportResult(Map<String, Integer> compteurs) {
            this.compteurs = Collections.unmodifiableMap(new LinkedHashMap<>(compteurs));
        }

        public Map<String, Integer> getCompteurs() {
            return compteurs;
        }

        @Override
        public String toString() {
            return "Import terminé : " + compteurs;
        }
    }
}
