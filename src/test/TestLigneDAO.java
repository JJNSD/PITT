package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Ligne;
import models.LigneDAO;

public class TestLigneDAO {

    private LigneDAO ligneDAO;

    @BeforeEach
    public void setUp() {
        ligneDAO = new LigneDAO();
    }

    @Test
    public void testGetAllLigne() throws Exception {
        List<Ligne> lignes = ligneDAO.getAllLigne();

        assertNotNull(lignes, "La liste des lignes ne doit pas être null");

        for (Ligne ligne : lignes) {
            assertNotNull(ligne, "Une ligne ne doit pas être null");
            assertTrue(ligne.getId() > 0, "L'identifiant doit être positif");
            assertNotNull(ligne.getLibelle(), "Le libellé ne doit pas être null");

            System.out.println(
                    "ID : " + ligne.getId()
                            + " | Libellé : " + ligne.getLibelle()
            );
        }
    }

    @Test
    public void testGetLigneByID() throws Exception {
        List<Ligne> lignes = ligneDAO.getAllLigne();

        assertFalse(lignes.isEmpty(),
                "La base de données doit contenir au moins une ligne");

        Ligne ligneAttendue = lignes.get(0);
        Ligne ligneTrouvee = ligneDAO.getLigneByID(ligneAttendue.getId());

        assertNotNull(ligneTrouvee, "La ligne doit être retrouvée");
        assertEquals(ligneAttendue.getId(), ligneTrouvee.getId());
        assertEquals(ligneAttendue.getLibelle(), ligneTrouvee.getLibelle());
    }
}
