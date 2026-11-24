
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Perturbation;
import models.ArretDAO;
import models.ConnexionBDD;
import models.PerturbationDAO;

public class TestPerturbationDAO {

    private PerturbationDAO perturbationDAO;

    @BeforeEach
    public void setUp() {
        Perturbation.resetCache();
        perturbationDAO = new PerturbationDAO();
    }

    @Test
    public void testGetAllPerturbations() throws Exception {
        List<Perturbation> perturbations =
                perturbationDAO.getAllPerturbations();

        assertNotNull(
                perturbations,
                "La liste des perturbations ne doit pas être null"
        );

        for (Perturbation perturbation : perturbations) {
            assertNotNull(perturbation);
            assertTrue(
                    perturbation.getId() > 0,
                    "L'identifiant doit être positif"
            );
            assertNotNull(
                    perturbation.getLibelle(),
                    "Le libellé ne doit pas être null"
            );

            System.out.println(
                    "ID : " + perturbation.getId()
                            + " | Libellé : " + perturbation.getLibelle()
                            + " | Début : " + perturbation.getDateHeureDebut()
                            + " | Fin : " + perturbation.getDateHeureFin()
                            + " | Arrêts perturbés : "
                            + perturbation.getArretsPerturbes().size()
            );
        }
    }

    @Test
    public void testGetPerturbationByID() throws Exception {
        List<Perturbation> perturbations =
                perturbationDAO.getAllPerturbations();

        assertFalse(
                perturbations.isEmpty(),
                "La base doit contenir au moins une perturbation"
        );

        Perturbation attendue = perturbations.get(0);

        Perturbation trouvee =
                perturbationDAO.getPerturbationByID(attendue.getId());

        assertNotNull(
                trouvee,
                "La perturbation doit être retrouvée"
        );

        assertEquals(attendue.getId(), trouvee.getId());
        assertEquals(attendue.getLibelle(), trouvee.getLibelle());
        assertEquals(
                attendue.getDateHeureDebut(),
                trouvee.getDateHeureDebut()
        );
        assertEquals(
                attendue.getDateHeureFin(),
                trouvee.getDateHeureFin()
        );
    }

    @Test
    public void testGetPerturbationByIDInexistant() throws Exception {
        Perturbation perturbation =
                perturbationDAO.getPerturbationByID(-1);

        assertNull(
                perturbation,
                "Un identifiant inexistant doit renvoyer null"
        );
    }

    @Test
    public void testAddUpdateDeletePerturbation() throws Exception {

        LocalDateTime debut =
                LocalDateTime.of(2099, 5, 10, 8, 0);

        LocalDateTime fin =
                LocalDateTime.of(2099, 5, 10, 12, 0);

        Perturbation perturbation = new Perturbation(
                0,
                "PerturbationTestJUnit",
                debut,
                fin
        );

        try {
            // AJOUT
            boolean ajoutee =
                    perturbationDAO.addPerturbation(perturbation);

            assertTrue(
                    ajoutee,
                    "L'ajout de la perturbation doit réussir"
            );

            assertTrue(
                    perturbation.getId() > 0,
                    "L'identifiant doit être généré"
            );

            // Ajout d'une association pour permettre
            // à deletePerturbation() de fonctionner.
            List<Arret> arrets = new ArretDAO().getAllArrets();

            assertFalse(
                    arrets.isEmpty(),
                    "La base doit contenir au moins un arrêt"
            );

            Arret arret = arrets.get(0);

            Connection connexion = ConnexionBDD.getConnexion();

            String queryAssociation =
                    "INSERT INTO \"pitt-bdd\".arretperturbe "
                            + "(idArret, idPerturbation) VALUES (?, ?)";

            try (PreparedStatement ps =
                         connexion.prepareStatement(queryAssociation)) {

                ps.setInt(1, arret.getId());
                ps.setInt(2, perturbation.getId());

                assertEquals(
                        1,
                        ps.executeUpdate(),
                        "L'association avec l'arrêt doit être créée"
                );
            }

            // VÉRIFICATION DE L'AJOUT
            Perturbation trouvee =
                    perturbationDAO.getPerturbationByID(
                            perturbation.getId()
                    );

            assertNotNull(trouvee);
            assertEquals(
                    "PerturbationTestJUnit",
                    trouvee.getLibelle()
            );

            // MODIFICATION
            perturbation.setLibelle("PerturbationTestJUnitModifie");

            perturbation.setDateHeureDebut(
                    LocalDateTime.of(2099, 5, 10, 9, 0)
            );

            perturbation.setDateHeureFin(
                    LocalDateTime.of(2099, 5, 10, 13, 0)
            );

            boolean modifiee =
                    perturbationDAO.updatePerturbation(perturbation);

            assertTrue(
                    modifiee,
                    "La modification doit réussir"
            );

            // Réinitialisation du cache pour relire les données BDD
            Perturbation.resetCache();

            // VÉRIFICATION DE LA MODIFICATION
            Perturbation modifieeEnBDD =
                    perturbationDAO.getPerturbationByID(
                            perturbation.getId()
                    );

            assertNotNull(modifieeEnBDD);

            assertEquals(
                    "PerturbationTestJUnitModifie",
                    modifieeEnBDD.getLibelle()
            );

            assertEquals(
                    LocalDateTime.of(2099, 5, 10, 9, 0),
                    modifieeEnBDD.getDateHeureDebut()
            );

            assertEquals(
                    LocalDateTime.of(2099, 5, 10, 13, 0),
                    modifieeEnBDD.getDateHeureFin()
            );

            // SUPPRESSION
            boolean supprimee =
                    perturbationDAO.deletePerturbation(modifieeEnBDD);

            assertTrue(
                    supprimee,
                    "La suppression de la perturbation doit réussir"
            );

            Perturbation.resetCache();

            assertNull(
                    perturbationDAO.getPerturbationByID(
                            perturbation.getId()
                    ),
                    "La perturbation supprimée ne doit plus être retrouvée"
            );

        } finally {
            // NETTOYAGE DE SECOURS
            // Suppression directe des associations et de la perturbation
            // si le test échoue avant la suppression normale.

            Connection connexion = ConnexionBDD.getConnexion();

            String supprimerAssociations =
                    "DELETE FROM \"pitt-bdd\".arretperturbe "
                            + "WHERE idPerturbation = ?";

            try (PreparedStatement ps =
                         connexion.prepareStatement(supprimerAssociations)) {

                ps.setInt(1, perturbation.getId());
                ps.executeUpdate();
            }

            String supprimerPerturbation =
                    "DELETE FROM \"pitt-bdd\".perturbation "
                            + "WHERE idPerturbation = ?";

            try (PreparedStatement ps =
                         connexion.prepareStatement(supprimerPerturbation)) {

                ps.setInt(1, perturbation.getId());
                ps.executeUpdate();
            }

            Perturbation.resetCache();
        }
    }
}
