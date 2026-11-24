package test;

import metiers.Arret;
import metiers.Perturbation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestArret {

    private Arret arret;

    @BeforeEach
    void setUp() {
        Arret.resetCache();
        arret = new Arret(
                1,
                "Gare_de_Tours",
                47.389011f,
                0.6920146f
        );
    }

    @Test
    void testConstructeurAvecId() {
        assertEquals(1, arret.getId());
        assertEquals("Gare_de_Tours", arret.getNomArret());
        assertEquals(47.389011f, arret.getLattitude(), 0.000001f);
        assertEquals(0.6920146f, arret.getLongitude(), 0.000001f);
    }

    @Test
    void testSettersEtGetters() {
        arret.setId(2);
        arret.setNomArret("Nationale");
        arret.setLattitude(48.867f);
        arret.setLongitude(2.363f);

        assertEquals(2, arret.getId());
        assertEquals("Nationale", arret.getNomArret());
        assertEquals(48.867f, arret.getLattitude(), 0.000001f);
        assertEquals(2.363f, arret.getLongitude(), 0.000001f);
    }

    @Test
    void testConstructeurInitialiseLesPerturbations() {
        assertNotNull(arret.getLesPerturbations());
        assertTrue(arret.getLesPerturbations().isEmpty());
    }

    @Test
    void testSetLesPerturbations() {
        List<Perturbation> perturbations = new ArrayList<>();

        arret.setLesPerturbations(perturbations);

        assertSame(perturbations, arret.getLesPerturbations());
        assertTrue(arret.getLesPerturbations().isEmpty());
    }

    @Test
    void testToString() {
        assertEquals("[1] Gare_de_Tours", arret.toString());
    }

    @Test
    void testGetArretEnCache() {
        Arret arretEnCache = Arret.getArretEnCache(arret.getId());

        assertNotNull(arretEnCache);
        assertSame(arret, arretEnCache);
    }

    @Test
    void testGetArretEnCacheInexistant() {
        assertNull(Arret.getArretEnCache(-999999));
    }

    @Test
    void testSupprimerDuCache() {
        Arret.supprimerDuCache(arret);

        assertNull(Arret.getArretEnCache(arret.getId()));
    }
}