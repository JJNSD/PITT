package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Desserte;
import metiers.Ligne;
import metiers.TypeTransport;

class TestDesserte {

    private Arret arretPrecedent;
    private Arret arretSuivant;
    private TypeTransport typeTransport;
    private Ligne ligne;
    private Desserte desserte;

    @BeforeEach
    void setUp() {
        Arret.resetCache();
        TypeTransport.resetCache();
        Ligne.resetCache();
        Desserte.resetCache();

        arretPrecedent = new Arret(
                1001, "Arret_Test_1", 47.463532f, -0.545509f
        );

        arretSuivant = new Arret(
                1002, "Arret_Test_2", 47.464661f, -0.548248f
        );

        typeTransport = new TypeTransport(1, "Bus", 1.70f);

        ligne = new Ligne(
                1001,
                "Ligne_Test",
                "#FF0000",
                arretPrecedent,
                arretSuivant,
                typeTransport
        );

        desserte = new Desserte(
                arretPrecedent,
                arretSuivant,
                ligne,
                5,
                800
        );
    }

    @Test
    void testConstructeur() {
        assertNotNull(desserte);
        assertEquals(arretPrecedent, desserte.getArretPrecedent());
        assertEquals(arretSuivant, desserte.getArretSuivant());
        assertEquals(ligne, desserte.getLigne());
        assertEquals(5, desserte.getDuree());
        assertEquals(800, desserte.getDistance());
    }

    @Test
    void testGettersEtSetters() {
        Arret nouvelArretPrecedent = new Arret(
                1003, "Arret_Test_3", 47.466327f, -0.552460f
        );

        Arret nouvelArretSuivant = new Arret(
                1004, "Arret_Test_4", 47.466782f, -0.552612f
        );

        TypeTransport nouveauType = new TypeTransport(
                2, "Tram", 1.80f
        );

        Ligne nouvelleLigne = new Ligne(
                1002,
                "Ligne_Test_2",
                "#0000FF",
                nouvelArretPrecedent,
                nouvelArretSuivant,
                nouveauType
        );

        desserte.setArretPrecedent(nouvelArretPrecedent);
        desserte.setArretSuivant(nouvelArretSuivant);
        desserte.setLigne(nouvelleLigne);
        desserte.setDuree(10);
        desserte.setDistance(1500);

        assertEquals(nouvelArretPrecedent, desserte.getArretPrecedent());
        assertEquals(nouvelArretSuivant, desserte.getArretSuivant());
        assertEquals(nouvelleLigne, desserte.getLigne());
        assertEquals(10, desserte.getDuree());
        assertEquals(1500, desserte.getDistance());
    }

    @Test
    void testGetDesserteEnCache() {
        Desserte resultat = Desserte.getDesserteEnCache(
                arretPrecedent,
                arretSuivant,
                ligne
        );

        assertNotNull(resultat);
        assertSame(desserte, resultat);
    }

    @Test
    void testGetDesserteEnCacheByID() {
        Desserte resultat = Desserte.getDesserteEnCacheByID(
                arretPrecedent.getId(),
                arretSuivant.getId(),
                ligne.getId()
        );

        assertNotNull(resultat);
        assertEquals(desserte, resultat);
    }

    @Test
    void testDesserteAbsenteDuCache() {
        Desserte resultat = Desserte.getDesserteEnCacheByID(
                -1001, -1002, -1003
        );

        assertNull(resultat);
    }
}