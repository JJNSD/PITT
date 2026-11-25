
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Perturbation;

class TestPerturbation {

    private Arret arret;
    private Arret autreArret;
    private Perturbation perturbation;

    @BeforeEach
    void setUp() {
        Arret.resetCache();
        Perturbation.resetCache();

        LocalDateTime debut = LocalDateTime.of(2026, 10, 23, 8, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 10, 23, 12, 0);

        arret = new Arret(
                5001, "Arret_Perturbe_1", 47.463532f, -0.545509f
        );

        autreArret = new Arret(
                5002, "Arret_Perturbe_2", 47.464661f, -0.548248f
        );

        perturbation = new Perturbation(
                5001,
                "Travaux sur la ligne",
                debut,
                fin
        );
    }

    @Test
    void testConstructeur() {
        assertEquals(5001, perturbation.getId());
        assertEquals("Travaux sur la ligne", perturbation.getLibelle());
        assertEquals(
                LocalDateTime.of(2026, 10, 23, 8, 0),
                perturbation.getDateHeureDebut()
        );
        assertEquals(
                LocalDateTime.of(2026, 10, 23, 12, 0),
                perturbation.getDateHeureFin()
        );

        assertNotNull(perturbation.getArretsPerturbes());
        assertTrue(perturbation.getArretsPerturbes().isEmpty());
    }

    @Test
    void testGettersEtSetters() {
        LocalDateTime nouveauDebut =
                LocalDateTime.of(2026, 10, 24, 9, 0);

        LocalDateTime nouvelleFin =
                LocalDateTime.of(2026, 10, 24, 14, 0);

        List<Arret> arrets = new ArrayList<>();
        arrets.add(arret);

        perturbation.setId(5002);
        perturbation.setLibelle("Incident technique");
        perturbation.setDateHeureDebut(nouveauDebut);
        perturbation.setDateHeureFin(nouvelleFin);
        perturbation.setArretsPerturbes(arrets);

        assertEquals(5002, perturbation.getId());
        assertEquals("Incident technique", perturbation.getLibelle());
        assertEquals(nouveauDebut, perturbation.getDateHeureDebut());
        assertEquals(nouvelleFin, perturbation.getDateHeureFin());
        assertEquals(arrets, perturbation.getArretsPerturbes());
    }

    @Test
    void testGetPerturbationEnCache() {
        Perturbation resultat =
                Perturbation.getPerturbationEnCache(5001);

        assertNotNull(resultat);
        assertSame(perturbation, resultat);
    }

    @Test
    void testPerturbationAbsenteDuCache() {
        Perturbation resultat =
                Perturbation.getPerturbationEnCache(-5001);

        assertNull(resultat);
    }

    @Test
    void testArretPresent() {
        perturbation.getArretsPerturbes().add(arret);

        assertTrue(perturbation.arretPresent(arret));
    }

    @Test
    void testArretAbsent() {
        assertFalse(perturbation.arretPresent(autreArret));
    }

    @Test
    void testAjouterUnArretPerturbe() {
        perturbation.ajouterUnArretPerturbe(arret);

        assertTrue(perturbation.getArretsPerturbes().contains(arret));
        assertTrue(arret.perturbationPresente(perturbation));
    }

    @Test
    void testSupprimerDuCache() {
        Perturbation.supprimerDuCache(perturbation);

        assertNull(Perturbation.getPerturbationEnCache(5001));
    }
}
