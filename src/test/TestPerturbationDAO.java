package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Perturbation;
import models.PerturbationDAO;

public class TestPerturbationDAO {

    private PerturbationDAO perturbationDAO;

    @BeforeEach
    public void setUp() {
        perturbationDAO = new PerturbationDAO();
    }

    @Test
    public void testGetAllPerturbations() throws Exception {
        List<Perturbation> perturbations =
                perturbationDAO.getAllPerturbations();

        assertNotNull(perturbations,
                "La liste des perturbations ne doit pas être null");

        for (Perturbation perturbation : perturbations) {
            assertNotNull(perturbation);
            assertTrue(perturbation.getId() > 0,
                    "L'identifiant doit être positif");
            assertNotNull(perturbation.getLibelle(),
                    "Le libellé ne doit pas être null");

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

        assertFalse(perturbations.isEmpty(),
                "La base doit contenir au moins une perturbation");

        Perturbation attendue = perturbations.get(0);

        Perturbation trouvee =
                perturbationDAO.getPerturbationByID(attendue.getId());

        assertNotNull(trouvee,
                "La perturbation doit être retrouvée");

        assertEquals(attendue.getId(), trouvee.getId());
        assertEquals(attendue.getLibelle(), trouvee.getLibelle());
        assertEquals(attendue.getDateHeureDebut(),
                trouvee.getDateHeureDebut());
        assertEquals(attendue.getDateHeureFin(),
                trouvee.getDateHeureFin());
    }

    @Test
    public void testGetPerturbationByIDInexistant() throws Exception {
        Perturbation perturbation =
                perturbationDAO.getPerturbationByID(-1);

        assertNull(perturbation,
                "Un identifiant inexistant doit renvoyer null");
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

        // Ajout
        boolean ajoutee =
                perturbationDAO.addPerturbation(perturbation);

        assertTrue(ajoutee,
                "L'ajout de la perturbation doit réussir");
        assertTrue(perturbation.getId() > 0,
                "L'identifiant doit être généré");

        try {
            // Vérification de l'ajout
            Perturbation trouvee =
                    perturbationDAO.getPerturbationByID(
                            perturbation.getId());

            assertNotNull(trouvee);
            assertEquals("PerturbationTestJUnit",
                    trouvee.getLibelle());

            // Modification
            perturbation.setLibelle("PerturbationTestJUnitModifie");
            perturbation.setDateHeureDebut(
                    LocalDateTime.of(2099, 5, 10, 9, 0));
            perturbation.setDateHeureFin(
                    LocalDateTime.of(2099, 5, 10, 13, 0));

            boolean modifiee =
                    perturbationDAO.updatePerturbation(perturbation);

            assertTrue(modifiee,
                    "La modification doit réussir");

            // Vérification de la modification
            Perturbation modifieeEnBDD =
                    perturbationDAO.getPerturbationByID(
                            perturbation.getId());

            assertNotNull(modifieeEnBDD);
            assertEquals("PerturbationTestJUnitModifie",
                    modifieeEnBDD.getLibelle());

            assertEquals(
                    LocalDateTime.of(2099, 5, 10, 9, 0),
                    modifieeEnBDD.getDateHeureDebut());

            assertEquals(
                    LocalDateTime.of(2099, 5, 10, 13, 0),
                    modifieeEnBDD.getDateHeureFin());

        } finally {
            /*
             * Nettoyage de secours.
             * La suppression dépend de l'implémentation de
             * deletePerturbation() et de ses associations.
             */
            List<Perturbation> perturbations =
                    perturbationDAO.getAllPerturbations();

            for (Perturbation p : perturbations) {
                if (p.getId() == perturbation.getId()) {
                    perturbationDAO.deletePerturbation(p);
                    break;
                }
            }
        }

        // Vérification finale de la suppression
        assertNull(perturbationDAO.getPerturbationByID(
                perturbation.getId()));
    }
}