package FileUploader;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

public final class UploaderTest {
    private static final String SCHEMA_TEST = "pitt_bdd_test";
    private static final Path DOSSIER_CSV = Path.of("src/FileUploader/file/test_reseau");

    private UploaderTest() {
    }

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("Import de démonstration vers le schéma de test : " + SCHEMA_TEST);
        System.out.println("Attention : les données réseau de ce schéma vont être remplacées.");

        Uploader uploader = new Uploader(SCHEMA_TEST);
        Uploader.ImportResult resultat =
                uploader.importer(DOSSIER_CSV, Uploader.ImportMode.REPLACE_NETWORK);

        System.out.println("Import terminé. Lignes ajoutées par table :");
        for (Map.Entry<String, Integer> entree : resultat.getCompteurs().entrySet()) {
            System.out.println("- " + entree.getKey() + " : " + entree.getValue());
        }
    }
}
