package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.HoraireDepart;
import metiers.Ligne;
import metiers.TypeHoraire;
import metiers.TypeTransport;
import metiers.Arret;

class TestHoraireDepart {

    private Arret terminusDepart;
    private Arret terminusArrivee;
    private TypeTransport typeTransport;
    private Ligne ligne;
    private TypeHoraire typeHoraire;
    private HoraireDepart horaireDepart;

    @BeforeEach
    void setUp() {
        HoraireDepart.resetCache();

        terminusDepart = new Arret(3001, "Terminus_Depart", 47.463532f, -0.545509f);
        terminusArrivee = new Arret(3002, "Terminus_Arrivee", 47.464661f, -0.548248f);
        typeTransport = new TypeTransport(3001, "Bus", 1.70f);

        ligne = new Ligne(3001, "Ligne_HoraireDepart", "#FF0000",
                terminusDepart, terminusArrivee, typeTransport);

        typeHoraire = new TypeHoraire(3001, "Départ");

        horaireDepart = new HoraireDepart(
                3001, ligne, LocalTime.of(8, 30), typeHoraire
        );
    }

    @Test
    void testConstructeur() {
        assertEquals(3001, horaireDepart.getId());
        assertEquals(ligne, horaireDepart.getLigneAffecte());
        assertEquals(LocalTime.of(8, 30), horaireDepart.getHeureDepart());
        assertEquals(typeHoraire, horaireDepart.getTypeHoraire());
    }

    @Test
    void testGettersEtSetters() {
        TypeHoraire nouveauType = new TypeHoraire(3002, "Arrivée");

        horaireDepart.setId(3002);
        horaireDepart.setLigneAffecte(ligne);
        horaireDepart.setHeureDepart(LocalTime.of(9, 15));
        horaireDepart.setTypeHoraire(nouveauType);

        assertEquals(3002, horaireDepart.getId());
        assertEquals(ligne, horaireDepart.getLigneAffecte());
        assertEquals(LocalTime.of(9, 15), horaireDepart.getHeureDepart());
        assertEquals(nouveauType, horaireDepart.getTypeHoraire());
    }

    @Test
    void testGetHoraireDepartEnCache() {
        HoraireDepart resultat = HoraireDepart.getHoraireDepartEnCache(
                3001, 3001
        );

        assertNotNull(resultat);
        assertEquals(horaireDepart, resultat);
    }

    @Test
    void testHoraireDepartAbsentDuCache() {
        HoraireDepart resultat = HoraireDepart.getHoraireDepartEnCache(
                -3001, -3001
        );

        assertNull(resultat);
    }

    @Test
    void testHoraireDepartAvecMauvaiseLigne() {
        HoraireDepart resultat = HoraireDepart.getHoraireDepartEnCache(
                3001, 9999
        );

        assertNull(resultat);
    }

    @Test
    void testSupprimerDuCache() {
        HoraireDepart.supprimerDuCache(horaireDepart);

        HoraireDepart resultat = HoraireDepart.getHoraireDepartEnCache(
                3001, 3001
        );

        assertNull(resultat);
    }
}