package test;

import models.TypeTransportDAO;
import metiers.TypeTransport;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeTransportDAO {

    private final TypeTransportDAO typeTransportDAO = new TypeTransportDAO();

    @Test
    public void testGetAllTypeTransport() throws Exception {
        List<TypeTransport> typesTransport =
                typeTransportDAO.getAllTypeTransport();

        assertNotNull(typesTransport, "La liste ne doit pas être null");

        for (TypeTransport type : typesTransport) {
            assertNotNull(type, "Un type de transport ne doit pas être null");
            assertTrue(type.getId() > 0, "L'identifiant doit être positif");
            assertNotNull(type.getNomType(), "Le nom ne doit pas être null");
            assertTrue(type.getCout() >= 0, "Le coût doit être positif ou nul");

            System.out.println(
                    "ID : " + type.getId()
                            + " | Nom : " + type.getNomType()
                            + " | Coût : " + type.getCout()
            );
        }
    }

    @Test
    public void testGetTypeTransportByID() throws Exception {
        List<TypeTransport> typesTransport =
                typeTransportDAO.getAllTypeTransport();

        assertFalse(
                typesTransport.isEmpty(),
                "La table doit contenir au moins un type de transport"
        );

        TypeTransport typeAttendu = typesTransport.get(0);

        TypeTransport typeTrouve =
                typeTransportDAO.getTypeTransportByID(typeAttendu.getId());

        assertNotNull(typeTrouve);
        assertEquals(typeAttendu.getId(), typeTrouve.getId());
        assertEquals(typeAttendu.getNomType(), typeTrouve.getNomType());
        assertEquals(typeAttendu.getCout(), typeTrouve.getCout(), 0.001f);
    }

    @Test
    public void testGetTypeTransportByIDInexistant() throws Exception {
        TypeTransport type =
                typeTransportDAO.getTypeTransportByID(-1);

        assertNull(
                type,
                "Aucun type de transport ne devrait correspondre à cet ID"
        );
    }

    @Test
    public void testInsertUpdateDeleteTypeTransport() throws Exception {
        String nomInitial = "TypeTransportTestJUnit";
        String nomModifie = "TypeTransportTestJUnitModifie";

        TypeTransport type = new TypeTransport(0, nomInitial, 1.50f);

        // INSERTION
        boolean insertionReussie =
                typeTransportDAO.insertTypeTransport(type);

        assertTrue(
                insertionReussie,
                "L'insertion du type de transport doit réussir"
        );

        // Recherche du type inséré
        TypeTransport typeInsere = null;

        for (TypeTransport t : typeTransportDAO.getAllTypeTransport()) {
            if (nomInitial.equals(t.getNomType())) {
                typeInsere = t;
                break;
            }
        }

        assertNotNull(
                typeInsere,
                "Le type de transport inséré doit être retrouvé en base"
        );

        try {
            // MODIFICATION
            typeInsere.setNomType(nomModifie);
            typeInsere.setCout(2.00f);

            boolean modificationReussie =
                    typeTransportDAO.updateTypeTransport(typeInsere);

            assertTrue(
                    modificationReussie,
                    "La modification doit réussir"
            );

            TypeTransport typeModifie =
                    typeTransportDAO.getTypeTransportByID(typeInsere.getId());

            assertNotNull(typeModifie);
            assertEquals(nomModifie, typeModifie.getNomType());
            assertEquals(2.00f, typeModifie.getCout(), 0.001f);

            // SUPPRESSION
            boolean suppressionReussie =
                    typeTransportDAO.deleteTypeTransport(typeInsere);

            assertTrue(
                    suppressionReussie,
                    "La suppression doit réussir"
            );

            assertNull(
                    typeTransportDAO.getTypeTransportByID(typeInsere.getId()),
                    "Le type supprimé ne doit plus être retrouvé"
            );

        } finally {
            // Nettoyage si le test échoue avant la suppression
            List<TypeTransport> typesRestants =
                    typeTransportDAO.getAllTypeTransport();

            for (TypeTransport t : typesRestants) {
                if (nomInitial.equals(t.getNomType())
                        || nomModifie.equals(t.getNomType())) {
                    typeTransportDAO.deleteTypeTransport(t);
                }
            }
        }
    }
}