
package test;

import models.TypeTransportDAO;
import metiers.TypeTransport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTypeTransportDAO {

    private final TypeTransportDAO typeTransportDAO = new TypeTransportDAO();

    @BeforeEach
    public void setUp() {
        TypeTransport.resetCache();
    }

    @Test
    public void testGetAllTypeTransport() throws Exception {
        List<TypeTransport> typesTransport =
                typeTransportDAO.getAllTypeTransport();

        assertNotNull(typesTransport, "La liste ne doit pas être null");
        assertFalse(typesTransport.isEmpty(), "La liste doit contenir au moins un type de transport");

        for (TypeTransport type : typesTransport) {
            assertNotNull(type);
            assertTrue(type.getId() > 0);
            assertNotNull(type.getNomType());
            assertFalse(type.getNomType().isEmpty());
            assertTrue(type.getCout() >= 0);

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

        assertFalse(typesTransport.isEmpty());

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

        int idTypeInsere = -1;

        try {
            // INSERTION
            boolean insertionReussie =
                    typeTransportDAO.insertTypeTransport(type);

            assertTrue(insertionReussie);

            // RECHERCHE DE L'OBJET INSÉRÉ
            TypeTransport typeInsere = null;

            for (TypeTransport t : typeTransportDAO.getAllTypeTransport()) {
                if (nomInitial.equals(t.getNomType())) {
                    typeInsere = t;
                    break;
                }
            }

            assertNotNull(
                    typeInsere,
                    "Le type inséré doit être retrouvé en base"
            );

            idTypeInsere = typeInsere.getId();

            // MODIFICATION
            typeInsere.setNomType(nomModifie);
            typeInsere.setCout(2.00f);

            boolean modificationReussie =
                    typeTransportDAO.updateTypeTransport(typeInsere);

            assertTrue(modificationReussie);

            // Réinitialisation du cache pour relire les données en BDD
            TypeTransport.resetCache();

            TypeTransport typeModifie =
                    typeTransportDAO.getTypeTransportByID(idTypeInsere);

            assertNotNull(typeModifie);
            assertEquals(nomModifie, typeModifie.getNomType());
            assertEquals(2.00f, typeModifie.getCout(), 0.001f);

            // SUPPRESSION
            boolean suppressionReussie =
                    typeTransportDAO.deleteTypeTransport(typeModifie);

            assertTrue(suppressionReussie);

            TypeTransport.resetCache();

            assertNull(
                    typeTransportDAO.getTypeTransportByID(idTypeInsere),
                    "Le type supprimé ne doit plus être retrouvé"
            );

        } finally {
            // NETTOYAGE EN CAS D'ÉCHEC
            TypeTransport.resetCache();

            List<TypeTransport> typesRestants =
                    typeTransportDAO.getAllTypeTransport();

            for (TypeTransport t : typesRestants) {
                if (nomInitial.equals(t.getNomType())
                        || nomModifie.equals(t.getNomType())) {
                    typeTransportDAO.deleteTypeTransport(t);
                }
            }

            TypeTransport.resetCache();
        }
    }
}
