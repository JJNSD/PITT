package test;

import models.TypeHoraireDAO;
import metiers.TypeHoraire;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeHoraireDAO {

    private final TypeHoraireDAO typeHoraireDAO = new TypeHoraireDAO();

    @Test
    public void testGetAllTypeHoraire() throws Exception {
        List<TypeHoraire> typesHoraire = typeHoraireDAO.getAllTypeHoraire();

        assertNotNull(typesHoraire, "La liste ne doit pas être null");

        for (TypeHoraire type : typesHoraire) {
            assertNotNull(type, "Un type d'horaire ne doit pas être null");
            assertTrue(type.getId() > 0, "L'identifiant doit être positif");
            assertNotNull(type.getLibelle(), "Le libellé ne doit pas être null");

            System.out.println(
                    "ID : " + type.getId()
                            + " | Libellé : " + type.getLibelle()
            );
        }
    }

    @Test
    public void testGetTypeHoraireByID() throws Exception {
        List<TypeHoraire> typesHoraire = typeHoraireDAO.getAllTypeHoraire();

        assertFalse(
                typesHoraire.isEmpty(),
                "La table doit contenir au moins un type d'horaire"
        );

        TypeHoraire typeAttendu = typesHoraire.get(0);
        TypeHoraire typeTrouve =
                typeHoraireDAO.getTypeHoraireByID(typeAttendu.getId());

        assertNotNull(typeTrouve);
        assertEquals(typeAttendu.getId(), typeTrouve.getId());
        assertEquals(typeAttendu.getLibelle(), typeTrouve.getLibelle());
    }

    @Test
    public void testGetTypeHoraireByIDInexistant() throws Exception {
        TypeHoraire type =
                typeHoraireDAO.getTypeHoraireByID(-1);

        assertNull(
                type,
                "Aucun type d'horaire ne devrait correspondre à cet ID"
        );
    }

    @Test
    public void testInsertUpdateDeleteTypeHoraire() throws Exception {
        String libelleInitial = "TypeHoraireTestJUnit";
        String libelleModifie = "TypeHoraireTestJUnitModifie";

        TypeHoraire type = new TypeHoraire(0, libelleInitial);

        // INSERTION
        boolean insertionReussie =
                typeHoraireDAO.insertTypeHoraire(type);

        assertTrue(
                insertionReussie,
                "L'insertion du type d'horaire doit réussir"
        );

        // Recherche du type inséré
        TypeHoraire typeInsere = null;

        for (TypeHoraire t : typeHoraireDAO.getAllTypeHoraire()) {
            if (libelleInitial.equals(t.getLibelle())) {
                typeInsere = t;
                break;
            }
        }

        assertNotNull(
                typeInsere,
                "Le type d'horaire inséré doit être retrouvé en base"
        );

        try {
            // MODIFICATION
            typeInsere.setLibelle(libelleModifie);

            boolean modificationReussie =
                    typeHoraireDAO.updateTypeHoraire(typeInsere);

            assertTrue(
                    modificationReussie,
                    "La modification doit réussir"
            );

            TypeHoraire typeModifie =
                    typeHoraireDAO.getTypeHoraireByID(typeInsere.getId());

            assertNotNull(typeModifie);
            assertEquals(libelleModifie, typeModifie.getLibelle());

            // SUPPRESSION
            boolean suppressionReussie =
                    typeHoraireDAO.deleteTypeHoraire(typeInsere);

            assertTrue(
                    suppressionReussie,
                    "La suppression doit réussir"
            );

            assertNull(
                    typeHoraireDAO.getTypeHoraireByID(typeInsere.getId()),
                    "Le type supprimé ne doit plus être retrouvé"
            );

        } finally {
            // Nettoyage si le test échoue avant la suppression
            List<TypeHoraire> typesRestants =
                    typeHoraireDAO.getAllTypeHoraire();

            for (TypeHoraire t : typesRestants) {
                if (libelleInitial.equals(t.getLibelle())
                        || libelleModifie.equals(t.getLibelle())) {
                    typeHoraireDAO.deleteTypeHoraire(t);
                }
            }
        }
    }
}
