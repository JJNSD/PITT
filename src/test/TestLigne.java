package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Ligne;
import metiers.TypeTransport;

class TestLigne {

    private Arret terminusDepart;
    private Arret terminusArrivee;
    private TypeTransport typeTransport;
    private Ligne ligne;

    @BeforeEach
    void setUp() {
        Arret.resetCache();
        TypeTransport.resetCache();
        Ligne.resetCache();

        terminusDepart = new Arret(
                4001, "Terminus_Depart", 47.463532f, -0.545509f
        );

        terminusArrivee = new Arret(
                4002, "Terminus_Arrivee", 47.464661f, -0.548248f
        );

        typeTransport = new TypeTransport(
                4001, "Bus", 1.70f
        );

        ligne = new Ligne(
                4001,
                "Ligne_Test",
                "#FF0000",
                terminusDepart,
                terminusArrivee,
                typeTransport
        );
    }

    @Test
    void testConstructeur() {
        assertEquals(4001, ligne.getId());
        assertEquals("Ligne_Test", ligne.getLibelle());
        assertEquals("#FF0000", ligne.getHexColor());
        assertEquals(terminusDepart, ligne.getTerminusDepart());
        assertEquals(terminusArrivee, ligne.getTerminusArrivee());
        assertEquals(typeTransport, ligne.getTypeTransport());
    }

    @Test
    void testGettersEtSetters() {
        Arret nouveauTerminusDepart = new Arret(
                4003, "Nouveau_Depart", 47.466327f, -0.552460f
        );

        Arret nouveauTerminusArrivee = new Arret(
                4004, "Nouvelle_Arrivee", 47.466782f, -0.552612f
        );

        TypeTransport nouveauType = new TypeTransport(
                4002, "Tram", 1.80f
        );

        ligne.setId(4002);
        ligne.setLibelle("Nouvelle_Ligne");
        ligne.setHexColor("#0000FF");
        ligne.setTerminusDepart(nouveauTerminusDepart);
        ligne.setTerminusArrivee(nouveauTerminusArrivee);
        ligne.setTypeTransport(nouveauType);

        assertEquals(4002, ligne.getId());
        assertEquals("Nouvelle_Ligne", ligne.getLibelle());
        assertEquals("#0000FF", ligne.getHexColor());
        assertEquals(nouveauTerminusDepart, ligne.getTerminusDepart());
        assertEquals(nouveauTerminusArrivee, ligne.getTerminusArrivee());
        assertEquals(nouveauType, ligne.getTypeTransport());
    }

    @Test
    void testToString() {
        assertEquals("Ligne_Test", ligne.toString());
    }

    @Test
    void testGetLigneEnCache() {
        Ligne resultat = Ligne.getLigneEnCache(4001);

        assertNotNull(resultat);
        assertEquals(ligne, resultat);
    }

    @Test
    void testLigneAbsenteDuCache() {
        Ligne resultat = Ligne.getLigneEnCache(-4001);

        assertNull(resultat);
    }

    @Test
    void testSupprimerDuCache() {
        Ligne.supprimerDuCache(ligne);

        Ligne resultat = Ligne.getLigneEnCache(4001);

        assertNull(resultat);
    }
}