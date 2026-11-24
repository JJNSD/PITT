
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import models.ArretDAO;

public class TestArretDAO {

    private ArretDAO arretDAO;

    @BeforeEach
    public void setUp() {
        arretDAO = new ArretDAO();
    }

    @Test
    public void testGetAllArrets() throws Exception {

        List<Arret> arrets = arretDAO.getAllArrets();

        assertNotNull(arrets,
                "La liste des arrêts ne doit pas être null");

        for (Arret arret : arrets) {

            assertNotNull(arret,
                    "Un arrêt ne doit pas être null");

            assertTrue(arret.getId() > 0,
                    "L'identifiant de l'arrêt doit être positif");

            assertNotNull(arret.getNomArret(),
                    "Le nom de l'arrêt ne doit pas être null");

            System.out.println(
                    "ID : " + arret.getId()
                            + " | Nom : " + arret.getNomArret()
                            + " | Latitude : " + arret.getLattitude()
                            + " | Longitude : " + arret.getLongitude()
            );
        }
    }

    @Test
    public void testGetArretByID() throws Exception {

        List<Arret> arrets = arretDAO.getAllArrets();

        assertFalse(arrets.isEmpty(),
                "La base doit contenir au moins un arrêt");

        Arret arretAttendu = arrets.get(0);

        Arret arretTrouve =
                arretDAO.getArretByID(arretAttendu.getId());

        assertNotNull(arretTrouve,
                "L'arrêt doit être retrouvé");

        assertEquals(arretAttendu.getId(),
                arretTrouve.getId());

        assertEquals(arretAttendu.getNomArret(),
                arretTrouve.getNomArret());

        assertEquals(arretAttendu.getLattitude(),
                arretTrouve.getLattitude(), 0.0001f);

        assertEquals(arretAttendu.getLongitude(),
                arretTrouve.getLongitude(), 0.0001f);
    }

    @Test
    public void testGetArretByIDInexistant() throws Exception {

        Arret arret = arretDAO.getArretByID(-1);

        assertNull(arret,
                "Un identifiant inexistant doit renvoyer null");
    }

    @Test
    public void testAddUpdateDeleteArret() throws Exception {

        // Création d'un arrêt de test
        Arret arret = new Arret(
                0,
                "ArretTestJUnit",
                48.0001f,
                -0.5001f
        );

        boolean ajoute = arretDAO.addArret(arret);

        assertTrue(ajoute,
                "L'ajout de l'arrêt doit réussir");

        assertTrue(arret.getId() > 0,
                "L'identifiant doit être généré");

        try {

            // Vérification de l'ajout
            Arret.resetCache();

            Arret arretAjoute =
                    arretDAO.getArretByID(arret.getId());

            assertNotNull(arretAjoute);

            assertEquals("ArretTestJUnit",
                    arretAjoute.getNomArret());

            // Modification de l'arrêt
            arret.setNomArret("ArretTestJUnitModifie");
            arret.setLattitude(48.0002f);
            arret.setLongitude(-0.5002f);

            boolean modifie = arretDAO.updateArret(arret);

            assertTrue(modifie,
                    "La modification de l'arrêt doit réussir");

            // Vérification de la modification
            Arret.resetCache();

            Arret arretModifie =
                    arretDAO.getArretByID(arret.getId());

            assertNotNull(arretModifie);

            assertEquals("ArretTestJUnitModifie",
                    arretModifie.getNomArret());

            assertEquals(48.0002f,
                    arretModifie.getLattitude(), 0.0001f);

            assertEquals(-0.5002f,
                    arretModifie.getLongitude(), 0.0001f);

        } finally {

            // Suppression de l'arrêt
            arretDAO.deleteArret(arret);

            Arret.resetCache();
        }

        // Vérification de la suppression
        assertNull(arretDAO.getArretByID(arret.getId()),
                "L'arrêt doit avoir été supprimé");
    }
}
