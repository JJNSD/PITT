package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Arret;
import metiers.Desserte;
import metiers.Ligne;
import models.ArretDAO;
import models.DesserteDAO;
import models.LigneDAO;

public class TestDesserteDAO {

    private DesserteDAO desserteDAO;
    private ArretDAO arretDAO;
    private LigneDAO ligneDAO;

    @BeforeEach
    public void setUp() {
        desserteDAO = new DesserteDAO();
        arretDAO = new ArretDAO();
        ligneDAO = new LigneDAO();
    }

    @Test
    public void testGetAllDessertes() throws Exception {
        List<Desserte> dessertes = desserteDAO.getAllDessertes();

        assertNotNull(dessertes,
                "La liste des dessertes ne doit pas être null");

        for (Desserte desserte : dessertes) {
            assertNotNull(desserte);
            assertNotNull(desserte.getArretPrecedent());
            assertNotNull(desserte.getArretSuivant());
            assertNotNull(desserte.getLigne());

            System.out.println(
                    "Départ : " + desserte.getArretPrecedent().getNomArret()
                            + " | Arrivée : " + desserte.getArretSuivant().getNomArret()
                            + " | Ligne : " + desserte.getLigne().getLibelle()
                            + " | Durée : " + desserte.getDuree() + " min"
                            + " | Distance : " + desserte.getDistance() + " m"
            );
        }
    }

    @Test
    public void testGetDesserteUnArretByID() throws Exception {
        List<Arret> arrets = arretDAO.getAllArrets();

        assertFalse(arrets.isEmpty(),
                "La base de données doit contenir au moins un arrêt");

        Arret arretDepart = arrets.get(0);

        List<Desserte> dessertes =
                desserteDAO.getDesserteUnArretByID(arretDepart.getId());

        assertNotNull(dessertes,
                "La liste des dessertes ne doit pas être null");

        for (Desserte desserte : dessertes) {
            assertEquals(arretDepart.getId(),
                    desserte.getArretPrecedent().getId());

            assertNotNull(desserte.getArretSuivant());
            assertNotNull(desserte.getLigne());
        }

        System.out.println(
                "Nombre de dessertes au départ de "
                        + arretDepart.getNomArret()
                        + " : " + dessertes.size()
        );
    }

    @Test
    public void testGetArretSuivant() throws Exception {
        Desserte desserte = trouverDesserteExistante();

        assertNotNull(desserte,
                "Il faut au moins une desserte dans la base");

        int idArretDepart = desserte.getArretPrecedent().getId();
        int idLigne = desserte.getLigne().getId();

        Arret arretSuivant =
                desserteDAO.getArretSuivant(idArretDepart, idLigne);

        assertNotNull(arretSuivant,
                "Un arrêt suivant doit être trouvé");

        System.out.println(
                "Arrêt de départ : "
                        + desserte.getArretPrecedent().getNomArret()
                        + " | Arrêt suivant retourné : "
                        + arretSuivant.getNomArret()
        );
    }

    @Test
    public void testGetArretsDesservis() throws Exception {

        Desserte desserte = trouverDesserteExistante();

        assertNotNull(desserte,
                "Il faut au moins une desserte dans la base");

        int idLigne = desserte.getLigne().getId();

        List<Desserte> dessertes =
                desserteDAO.getArretsDesservis(idLigne);

        assertNotNull(dessertes,
                "La liste des dessertes ne doit pas être null");

        assertFalse(dessertes.isEmpty(),
                "La ligne doit posséder au moins une desserte");

        for (Desserte d : dessertes) {
            assertNotNull(d, "Une desserte ne doit pas être null");
            assertNotNull(d.getArretPrecedent(),
                    "L'arrêt précédent ne doit pas être null");
            assertNotNull(d.getArretSuivant(),
                    "L'arrêt suivant ne doit pas être null");
        }

        System.out.println(
                "Nombre de dessertes pour la ligne "
                        + desserte.getLigne().getLibelle()
                        + " : " + dessertes.size()
        );
    }

    @Test
    public void testGetDureeDistance() throws Exception {
        Desserte desserteAttendue = trouverDesserteExistante();

        assertNotNull(desserteAttendue,
                "Il faut au moins une desserte dans la base");

        int idDepart = desserteAttendue.getArretPrecedent().getId();
        int idArrivee = desserteAttendue.getArretSuivant().getId();
        int idLigne = desserteAttendue.getLigne().getId();

        Desserte desserteTrouvee =
                desserteDAO.getDureeDistance(idDepart, idArrivee, idLigne);

        assertNotNull(desserteTrouvee,
                "La desserte doit être retrouvée");

        assertEquals(desserteAttendue.getDuree(),
                desserteTrouvee.getDuree());

        assertEquals(desserteAttendue.getDistance(),
                desserteTrouvee.getDistance());

        System.out.println(
                "Durée : " + desserteTrouvee.getDuree() + " min"
                        + " | Distance : " + desserteTrouvee.getDistance() + " m"
        );
    }

    private Desserte trouverDesserteExistante() throws Exception {
        List<Desserte> dessertes = desserteDAO.getAllDessertes();

        if (dessertes.isEmpty()) {
            return null;
        }

        return dessertes.get(0);
    }
}
