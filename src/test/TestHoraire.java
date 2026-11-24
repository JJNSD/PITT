package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Horaire;
import metiers.Ligne;
import metiers.TypeHoraire;
import metiers.TypeTransport;

import java.lang.reflect.Field;
import java.util.ArrayList;

class TestHoraire {

    private Arret arret;
    private Arret autreArret;
    private TypeTransport typeTransport;
    private Ligne ligne;
    private TypeHoraire typeHoraire;
    private Horaire horaire;


    @BeforeEach
    void setUp() throws Exception {
        Horaire.resetCache();

        arret = new Arret(
                2001, "Arret_Horaire_1", 47.463532f, -0.545509f
        );

        autreArret = new Arret(
                2002, "Arret_Horaire_2", 47.464661f, -0.548248f
        );

        typeTransport = new TypeTransport(2001, "Bus", 1.70f);

        ligne = new Ligne(
                2001,
                "Ligne_Horaire",
                "#FF0000",
                arret,
                autreArret,
                typeTransport
        );

        typeHoraire = new TypeHoraire(2001, "Départ");

        horaire = new Horaire(
                2001,
                ligne,
                LocalTime.of(8, 30),
                arret,
                typeHoraire
        );

        // Ajout de l'horaire au cache pour les tests
        Field champ = Horaire.class.getDeclaredField("lesHoraires");
        champ.setAccessible(true);

        List<Horaire> cache = (List<Horaire>) champ.get(null);
        cache.add(horaire);
    }

    @Test
    void testConstructeur() {
        assertEquals(2001, horaire.getId());
        assertEquals(ligne, horaire.getLigneAffecte());
        assertEquals(arret, horaire.getArretAffecte());
        assertEquals(LocalTime.of(8, 30), horaire.getHeure());
        assertEquals(typeHoraire, horaire.getTypeHoraire());
    }

    @Test
    void testGettersEtSetters() {
        TypeHoraire nouveauType = new TypeHoraire(2002, "Arrivée");

        horaire.setId(2002);
        horaire.setLigneAffecte(ligne);
        horaire.setArretAffecte(autreArret);
        horaire.setHeure(LocalTime.of(9, 15));
        horaire.setTypeHoraire(nouveauType);

        assertEquals(2002, horaire.getId());
        assertEquals(ligne, horaire.getLigneAffecte());
        assertEquals(autreArret, horaire.getArretAffecte());
        assertEquals(LocalTime.of(9, 15), horaire.getHeure());
        assertEquals(nouveauType, horaire.getTypeHoraire());
    }

    @Test
    void testGetHoraires() {
        List<Horaire> resultat = Horaire.getHoraires(arret, ligne);

        assertNotNull(resultat);
        assertTrue(resultat.contains(horaire));
    }

    @Test
    void testGetHorairesAvecUnAutreArret() {
        List<Horaire> resultat = Horaire.getHoraires(autreArret, ligne);

        assertNotNull(resultat);
        assertFalse(resultat.contains(horaire));
    }

    @Test
    void testGetHoraireProchainHoraireDisponible() {
        Horaire resultat = Horaire.getHoraire(
                arret,
                ligne,
                typeHoraire,
                LocalTime.of(8, 0)
        );

        assertNotNull(resultat);
        assertEquals(horaire, resultat);
    }

    @Test
    void testGetHoraireAHeureExacte() {
        Horaire resultat = Horaire.getHoraire(
                arret,
                ligne,
                typeHoraire,
                LocalTime.of(8, 30)
        );

        assertNotNull(resultat);
        assertEquals(horaire, resultat);
    }

    @Test
    void testGetHoraireSiHeureDepassee() {
        Horaire resultat = Horaire.getHoraire(
                arret,
                ligne,
                typeHoraire,
                LocalTime.of(9, 0)
        );

        assertNull(resultat);
    }

    @Test
    void testGetHoraireAvecUnMauvaisType() {
        TypeHoraire autreType = new TypeHoraire(2003, "Autre");

        Horaire resultat = Horaire.getHoraire(
                arret,
                ligne,
                autreType,
                LocalTime.of(8, 0)
        );

        assertNull(resultat);
    }
}