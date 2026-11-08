package FileUploader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

final class LecteurCSV {
    private LecteurCSV() {
    }

    static void lireFichier(Path fichier, List<String> colonnesAttendues, RowConsumer consommateur)
            throws IOException, SQLException {
        Objects.requireNonNull(consommateur, "Le consommateur CSV est obligatoire.");

        try (BufferedReader reader = Files.newBufferedReader(fichier, StandardCharsets.UTF_8)) {
            String ligneTexte;
            long numeroLigne = 0;
            boolean enteteLue = false;

            while ((ligneTexte = reader.readLine()) != null) {
                numeroLigne++;
                if (ligneTexte.isBlank()) {
                    continue;
                }

                List<String> champs = lireChamps(ligneTexte, fichier, numeroLigne);
                if (!enteteLue) {
                    if (!champs.isEmpty() && champs.get(0).startsWith("\uFEFF")) {
                        champs.set(0, champs.get(0).substring(1));
                    }
                    verifierColonnes(champs, colonnesAttendues, fichier);
                    enteteLue = true;
                    continue;
                }

                if (champs.size() != colonnesAttendues.size()) {
                    throw erreur(fichier, numeroLigne, "nombre de colonnes invalide : "
                            + champs.size() + " au lieu de " + colonnesAttendues.size());
                }
                try {
                    consommateur.accept(numeroLigne, champs);
                } catch (IOException e) {
                    throw erreur_cause(fichier, numeroLigne, e.getMessage(), e);
                } catch (SQLException e) {
                    throw new SQLException(
                            fichier.getFileName() + ":" + numeroLigne + " : " + e.getMessage(), e);
                }
            }

            if (!enteteLue) {
                throw erreur(fichier, 1, "fichier vide ou sans en-tête");
            }
        }
    }

    private static void verifierColonnes(List<String> entete, List<String> attendue, Path fichier) throws IOException {
        if (entete.size() != attendue.size()) {
            throw erreur(fichier, 1, "en-tête invalide. Colonnes attendues dans cet ordre : "
                    + String.join(";", attendue));
        }
        for (int i = 0; i < attendue.size(); i++) {
            String valeur = entete.get(i).trim().toLowerCase(Locale.ROOT);
            if (!attendue.get(i).equals(valeur)) {
                throw erreur(fichier, 1, "en-tête invalide. Colonnes attendues dans cet ordre : "
                        + String.join(";", attendue));
            }
        }
    }

    private static List<String> lireChamps(String texte, Path fichier, long numeroLigne) throws IOException {
        List<String> champs = new ArrayList<>();
        StringBuilder champ = new StringBuilder();
        boolean entreGuillemets = false;
        boolean guillemetFerme = false;

        for (int i = 0; i < texte.length(); i++) {
            char caractere = texte.charAt(i);
            if (entreGuillemets) {
                if (caractere == '"') {
                    if (i + 1 < texte.length() && texte.charAt(i + 1) == '"') {
                        champ.append('"');
                        i++;
                    } else {
                        entreGuillemets = false;
                        guillemetFerme = true;
                    }
                } else {
                    champ.append(caractere);
                }
            } else if (caractere == ';') {
                champs.add(champ.toString());
                champ.setLength(0);
                guillemetFerme = false;
            } else if (caractere == '"') {
                if (!champ.isEmpty() || guillemetFerme) {
                    throw erreur(fichier, numeroLigne, "guillemet inattendu");
                }
                entreGuillemets = true;
            } else {
                if (guillemetFerme && !Character.isWhitespace(caractere)) {
                    throw erreur(fichier, numeroLigne, "caractère inattendu après un champ entre guillemets");
                }
                if (!guillemetFerme) {
                    champ.append(caractere);
                }
            }
        }

        if (entreGuillemets) {
            throw erreur(fichier, numeroLigne, "champ entre guillemets non terminé");
        }
        champs.add(champ.toString());
        return champs;
    }

    private static IOException erreur(Path fichier, long numeroLigne, String message) {
        return new IOException(fichier.getFileName() + ":" + numeroLigne + " : " + message);
    }

    private static IOException erreur_cause(Path fichier, long numeroLigne, String message, Exception cause) {
        return new IOException(fichier.getFileName() + ":" + numeroLigne + " : " + message, cause);
    }

    @FunctionalInterface
    interface RowConsumer {
        void accept(long numeroLigne, List<String> champs) throws IOException, SQLException;
    }
}
