package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.TypeHoraire;

class TestTypeHoraire {

    private TypeHoraire typeHoraire;

    @BeforeEach
    void setUp() {
        TypeHoraire.resetCache();
        typeHoraire = new TypeHoraire(
                6001,
                "Départ"
        );
    }

    @Test
    void testConstructeur() {
        assertEquals(6001, typeHoraire.getId());
        assertEquals("Départ", typeHoraire.getLibelle());
    }

    @Test
    void testGettersEtSetters() {
        typeHoraire.setId(6002);
        typeHoraire.setLibelle("Arrivée");

        assertEquals(6002, typeHoraire.getId());
        assertEquals("Arrivée", typeHoraire.getLibelle());
    }

    @Test
    void testGetTypeHoraireEnCache() {
        TypeHoraire resultat =
                TypeHoraire.getTypeHoraireEnCache(6001);

        assertNotNull(resultat);
        assertSame(typeHoraire, resultat);
    }

    @Test
    void testTypeHoraireAbsentDuCache() {
        TypeHoraire resultat =
                TypeHoraire.getTypeHoraireEnCache(-6001);

        assertNull(resultat);
    }

    @Test
    void testSupprimerDuCache() {
        TypeHoraire.supprimerDuCache(typeHoraire);

        TypeHoraire resultat =
                TypeHoraire.getTypeHoraireEnCache(6001);

        assertNull(resultat);
    }
}