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
    }

    @Test
    public void testGetAllHoraireDepartByLigne() throws Exception {
        Ligne ligne = trouverLigneAvecHoraire();

        assertNotNull(ligne,
                "Il faut au moins une ligne possédant un horaire");

        List<HoraireDepart> horaires =
                horaireDAO.getAllHoraireDepartByLigne(ligne);

        assertNotNull(horaires,
                "La liste des horaires ne doit pas être null");

        LocalTime heurePrecedente = null;

        for (HoraireDepart horaire : horaires) {
            assertNotNull(horaire);
            assertNotNull(horaire.getHeureDepart());
            assertNotNull(horaire.getLigneAffecte());
            assertNotNull(horaire.getTypeHoraire());

            assertEquals(ligne.getId(),
                    horaire.getLigneAffecte().getId());

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

        assertNotNull(ligne,
                "Il faut au moins une ligne possédant un horaire");

        List<HoraireDepart> horaires =
                horaireDAO.getAllHoraireDepartByLigne(ligne);

        assertFalse(horaires.isEmpty());

        HoraireDepart attendu = horaires.get(0);

        HoraireDepart trouve = horaireDAO.getHoraireDepartByID(
                attendu.getId(), ligne);

        assertNotNull(trouve,
                "L'horaire doit être retrouvé");

        assertEquals(attendu.getId(), trouve.getId());
        assertEquals(attendu.getHeureDepart(), trouve.getHeureDepart());
        assertEquals(attendu.getTypeHoraire().getId(),
                trouve.getTypeHoraire().getId());
        assertEquals(ligne.getId(), trouve.getLigneAffecte().getId());
    }

    @Test
    public void testGetHoraireDepartByIDInexistant() throws Exception {
        Ligne ligne = trouverPremiereLigne();

        assertNotNull(ligne,
                "La base doit contenir au moins une ligne");

        HoraireDepart horaire =
                horaireDAO.getHoraireDepartByID(-1, ligne);

        assertNull(horaire,
                "Un identifiant inexistant doit renvoyer null");
    }

    @Test
    public void testInsertUpdateDeleteHoraireDepart() throws Exception {
        Ligne ligne = trouverPremiereLigne();
        TypeHoraire type = trouverPremierTypeHoraire();

        assertNotNull(ligne, "Une ligne doit exister");
        assertNotNull(type, "Un type d'horaire doit exister");

        // Choisir une heure différente des horaires déjà présents.
        LocalTime heureTest = LocalTime.of(3, 17, 23);

        List<HoraireDepart> horaires =
                horaireDAO.getAllHoraireDepartByLigne(ligne);

        for (HoraireDepart h : horaires) {
            assertNotEquals(heureTest, h.getHeureDepart(),
                    "Choisir une heure de test non utilisée");
        }

        // Adapter le constructeur si sa signature est différente.
        HoraireDepart horaire = new HoraireDepart(
                0, ligne, heureTest, type
        );

        boolean insere = false;

        try {
            insere = horaireDAO.insertHoraireDepart(horaire);
            assertTrue(insere,
                    "L'insertion de l'horaire doit réussir");

            // Retrouver l'horaire créé pour récupérer son ID généré.
            List<HoraireDepart> horairesApresInsertion =
                    horaireDAO.getAllHoraireDepartByLigne(ligne);

            HoraireDepart horaireInsere = null;

            for (HoraireDepart h : horairesApresInsertion) {
                if (h.getHeureDepart().equals(heureTest)
                        && h.getTypeHoraire().getId() == type.getId()) {
                    horaireInsere = h;
                    break;
                }
            }

            assertNotNull(horaireInsere,
                    "L'horaire inséré doit être retrouvé");

            // Modification de l'heure.
            horaireInsere.setHeureDepart(LocalTime.of(3, 18, 23));

            boolean modifie =
                    horaireDAO.updateHoraireDepart(horaireInsere);

            assertTrue(modifie,
                    "La modification doit réussir");

            HoraireDepart horaireModifie =
                    horaireDAO.getHoraireDepartByID(
                            horaireInsere.getId(), ligne);

            assertNotNull(horaireModifie);
            assertEquals(LocalTime.of(3, 18, 23),
                    horaireModifie.getHeureDepart());

            // Suppression de l'horaire de test.
            boolean supprime =
                    horaireDAO.deleteHoraireDepart(horaireInsere, ligne);

            assertTrue(supprime,
                    "La suppression doit réussir");

            assertNull(horaireDAO.getHoraireDepartByID(
                    horaireInsere.getId(), ligne));

            horaireInsere = null;

        } finally {
            /*
             * Si le test échoue avant la suppression, il peut rester
             * un horaire de test dans la base.
             * La suppression de secours nécessite de retrouver son ID.
             */
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
        /*
         * Remplacer getAllTypeHoraire() par le nom exact de la méthode
         * de TypeHoraireDAO qui récupère tous les types, si nécessaire.
         */
        List<TypeHoraire> types = typeHoraireDAO.getAllTypeHoraire();

        if (types.isEmpty()) {
            return null;
        }

        return types.get(0);
    }
}
