
package test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.HoraireDepart;
import metiers.Ligne;
import metiers.TypeHoraire;
import models.HoraireDepartDAO;
import models.LigneDAO;
import models.TypeHoraireDAO;

public class TestHoraireDepartDAO {

    private HoraireDepartDAO horaireDAO;
    private LigneDAO ligneDAO;
    private TypeHoraireDAO typeHoraireDAO;

    @BeforeEach
    public void setUp() {
        horaireDAO = new HoraireDepartDAO();
        ligneDAO = new LigneDAO();
        typeHoraireDAO = new TypeHoraireDAO();

        HoraireDepart.resetCache();
    }

    @Test
    public void testGetAllHoraireDepartByLigne() throws Exception {

        Ligne ligne = trouverLigneAvecHoraire();

        assertNotNull(
                ligne,
                "Il faut au moins une ligne possédant un horaire"
        );

        List<HoraireDepart> horaires =
                horaireDAO.getAllHoraireDepartByLigne(ligne);

        assertNotNull(horaires);

        LocalTime heurePrecedente = null;

        for (HoraireDepart horaire : horaires) {

            assertNotNull(horaire);
            assertNotNull(horaire.getHeureDepart());
            assertNotNull(horaire.getLigneAffecte());
            assertNotNull(horaire.getTypeHoraire());

            assertEquals(
                    ligne.getId(),
                    horaire.getLigneAffecte().getId()
            );

            if (heurePrecedente != null) {
                assertFalse(
                        horaire.getHeureDepart().isBefore(heurePrecedente),
                        "Les horaires doivent être triés par heure"
                );
            }

            heurePrecedente = horaire.getHeureDepart();

            System.out.println(
                    "ID : " + horaire.getId()
                            + " | Ligne : " + ligne.getLibelle()
                            + " | Heure : " + horaire.getHeureDepart()
                            + " | Type : " + horaire.getTypeHoraire().getId()
            );
        }
    }

    @Test
    public void testGetHoraireDepartByID() throws Exception {

        Ligne ligne = trouverLigneAvecHoraire();

        assertNotNull(
                ligne,
                "Il faut au moins une ligne possédant un horaire"
        );

        List<HoraireDepart> horaires =
                horaireDAO.getAllHoraireDepartByLigne(ligne);

        assertFalse(
                horaires.isEmpty(),
                "La ligne doit posséder au moins un horaire"
        );

        HoraireDepart attendu = horaires.get(0);

        HoraireDepart trouve =
                horaireDAO.getHoraireDepartByID(
                        attendu.getId(),
                        ligne
                );

        assertNotNull(trouve);
        assertEquals(attendu.getId(), trouve.getId());
        assertEquals(
                attendu.getHeureDepart(),
                trouve.getHeureDepart()
        );
        assertEquals(
                attendu.getTypeHoraire().getId(),
                trouve.getTypeHoraire().getId()
        );
        assertEquals(
                ligne.getId(),
                trouve.getLigneAffecte().getId()
        );
    }

    @Test
    public void testGetHoraireDepartByIDInexistant() throws Exception {

        Ligne ligne = trouverPremiereLigne();

        assertNotNull(
                ligne,
                "La base doit contenir au moins une ligne"
        );

        HoraireDepart horaire =
                horaireDAO.getHoraireDepartByID(-1, ligne);

        assertNull(
                horaire,
                "Un identifiant inexistant doit renvoyer null"
        );
    }

    @Test
    public void testInsertUpdateDeleteHoraireDepart() throws Exception {

        Ligne ligne = trouverPremiereLigne();
        TypeHoraire type = trouverPremierTypeHoraire();

        assertNotNull(ligne, "Une ligne doit exister");
        assertNotNull(type, "Un type d'horaire doit exister");

        LocalTime heureInitiale = LocalTime.of(3, 17, 23);
        LocalTime heureModifiee = LocalTime.of(3, 18, 23);

        int idHoraireInsere = -1;

        try {

            // INSERTION
            HoraireDepart horaire = new HoraireDepart(
                    0,
                    ligne,
                    heureInitiale,
                    type
            );

            boolean insere =
                    horaireDAO.insertHoraireDepart(horaire);

            assertTrue(
                    insere,
                    "L'insertion de l'horaire doit réussir"
            );

            // RECHERCHE DE L'HORAIRE INSÉRÉ
            HoraireDepart.resetCache();

            List<HoraireDepart> horairesApresInsertion =
                    horaireDAO.getAllHoraireDepartByLigne(ligne);

            HoraireDepart horaireInsere = null;

            for (HoraireDepart h : horairesApresInsertion) {

                if (h.getHeureDepart().equals(heureInitiale)
                        && h.getTypeHoraire().getId() == type.getId()) {

                    horaireInsere = h;
                    break;
                }
            }

            assertNotNull(
                    horaireInsere,
                    "L'horaire inséré doit être retrouvé"
            );

            idHoraireInsere = horaireInsere.getId();

            // MODIFICATION
            horaireInsere.setHeureDepart(heureModifiee);

            boolean modifie =
                    horaireDAO.updateHoraireDepart(horaireInsere);

            assertTrue(
                    modifie,
                    "La modification doit réussir"
            );

            // VÉRIFICATION DE LA MODIFICATION
            HoraireDepart.resetCache();

            HoraireDepart horaireModifie =
                    horaireDAO.getHoraireDepartByID(
                            idHoraireInsere,
                            ligne
                    );

            assertNotNull(horaireModifie);

            assertEquals(
                    heureModifiee,
                    horaireModifie.getHeureDepart()
            );

            // SUPPRESSION
            boolean supprime =
                    horaireDAO.deleteHoraireDepart(
                            horaireModifie,
                            ligne
                    );

            assertTrue(
                    supprime,
                    "La suppression doit réussir"
            );

            // VÉRIFICATION DE LA SUPPRESSION
            HoraireDepart.resetCache();

            assertNull(
                    horaireDAO.getHoraireDepartByID(
                            idHoraireInsere,
                            ligne
                    ),
                    "L'horaire supprimé ne doit plus être retrouvé"
            );

            idHoraireInsere = -1;

        } finally {

            // NETTOYAGE SI LE TEST ÉCHOUE APRÈS L'INSERTION
            if (idHoraireInsere != -1) {

                HoraireDepart.resetCache();

                HoraireDepart horaireRestant =
                        horaireDAO.getHoraireDepartByID(
                                idHoraireInsere,
                                ligne
                        );

                if (horaireRestant != null) {
                    horaireDAO.deleteHoraireDepart(
                            horaireRestant,
                            ligne
                    );
                }
            }

            HoraireDepart.resetCache();
        }
    }

    private Ligne trouverPremiereLigne() throws Exception {

        List<Ligne> lignes = ligneDAO.getAllLigne();

        if (lignes.isEmpty()) {
            return null;
        }

        return lignes.get(0);
    }

    private Ligne trouverLigneAvecHoraire() throws Exception {

        List<Ligne> lignes = ligneDAO.getAllLigne();

        for (Ligne ligne : lignes) {

            List<HoraireDepart> horaires =
                    horaireDAO.getAllHoraireDepartByLigne(ligne);

            if (!horaires.isEmpty()) {
                return ligne;
            }
        }

        return null;
    }

    private TypeHoraire trouverPremierTypeHoraire() throws Exception {

        List<TypeHoraire> types =
                typeHoraireDAO.getAllTypeHoraire();

        if (types.isEmpty()) {
            return null;
        }

        return types.get(0);
    }
}
