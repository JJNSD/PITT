package models;

import metiers.Arret;
import metiers.Desserte;
import metiers.HoraireDepart;
import metiers.Ligne;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DesserteDAO {

    private Connection connexion;

    private static ArretDAO arretDAO = new ArretDAO();
    private static LigneDAO ligneDAO = new LigneDAO();

    public DesserteDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    // 1. ID arrêt actuel -> arrêts suivants + lignes
    public List<Desserte> getDesserteUnArretByID(int idArretEnCours) throws Exception {

        String query = "SELECT idArret_est_precede, idArret_est_suivi,idLigne, duree, distance FROM \"pitt-bdd\".desserte WHERE idArret_est_precede = ?";

        List<Desserte> lesDessertes = new ArrayList<>();

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idArretEnCours);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Arret arretPrecedent = arretDAO.getArretByID(rs.getInt("idArret_est_precede"));
                    Arret arretSuivant = arretDAO.getArretByID(rs.getInt("idArret_est_suivi"));
                    Ligne ligne = ligneDAO.getLigneByID(rs.getInt("idLigne"));

                    // Gestion du cache
                    Desserte desserte = Desserte.getDesserteEnCache(arretPrecedent, arretSuivant, ligne);

                    if (desserte == null) {
                        desserte = new Desserte(arretPrecedent, arretSuivant, ligne, rs.getInt("duree"), rs.getInt("distance"));
                    }

                    lesDessertes.add(desserte);
                }
            }
            return lesDessertes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche des dessertes.", e);
        }
    }

    // 2. ID arrêt actuel + ID ligne -> arrêt suivant
    public Arret getArretSuivant(int idArretEnCours, int idLigne) throws Exception {

        String query = "SELECT d.idArret_est_suivi " +
                "FROM \"pitt-bdd\".desserte d " +
                "WHERE d.idArret_est_precede = ? AND d.idLigne = ? AND (d.idArret_est_suivi NOT IN " +
                    "(SELECT d2.idArret_est_suivi " +
                    "FROM \"pitt-bdd\".desserte d2 " +
                    "WHERE d2.idArret_est_suivi = d.idArret_est_precede AND d2.idligne = ?)" +
                "OR (d.idArret_est_precede = " +
                    "(SELECT idarretdepart FROM \"pitt-bdd\".ligne WHERE d.idligne = ?) " +
                "AND " +
                "d.idArret_est_precede = " +
                    "(SELECT idarretarrivee FROM \"pitt-bdd\".ligne WHERE d.idligne = ?)))";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idArretEnCours);
            ps.setInt(2, idLigne);
            ps.setInt(3, idLigne);
            ps.setInt(4, idLigne);
            ps.setInt(5, idLigne);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return arretDAO.getArretByID(rs.getInt("idArret_est_suivi"));
                }
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de l'arrêt suivant.", e);
        }
    }

    // 3. ID ligne -> arrêts desservis
    public List<Desserte> getArretsDesservis(int idLigne) throws Exception {

        List<Desserte> lesDessertes = new ArrayList<>();

        String query = "SELECT idArret_est_precede, idArret_est_suivi, duree, distance FROM \"pitt-bdd\".desserte WHERE idLigne = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idLigne);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Arret arretPrecedent = arretDAO.getArretByID(rs.getInt("idArret_est_precede"));
                    Arret arretSuivant = arretDAO.getArretByID(rs.getInt("idArret_est_suivi"));
                    Ligne ligne = ligneDAO.getLigneByID(rs.getInt("idLigne"));

                    // Gestion du cache
                    Desserte desserte = Desserte.getDesserteEnCache(arretPrecedent, arretSuivant, ligne);

                    if (desserte == null) {
                        desserte = new Desserte(arretPrecedent, arretSuivant, ligne, rs.getInt("duree"), rs.getInt("distance"));
                    }

                    lesDessertes.add(desserte);
                }
            }
            return lesDessertes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche des arrêts desservis.", e);
        }
    }

    // 4. ID arrêt actuel + ID arrêt suivant + ID ligne -> durée et distance
    public Desserte getDureeDistance(int idArretActuel, int idArretSuivant, int idLigne) throws Exception {
        // Vérification du cache
        Desserte desserte = Desserte.getDesserteEnCacheByID(idArretActuel, idArretSuivant, idLigne);

        if (desserte == null) {

            String query = "SELECT idArret_est_suivi, idLigne, duree, distance FROM \"pitt-bdd\".desserte WHERE idArret_est_precede = ? AND idArret_est_suivi = ? AND idLigne = ?";

            try (PreparedStatement ps = connexion.prepareStatement(query)) {

                ps.setInt(1, idArretActuel);
                ps.setInt(2, idArretSuivant);
                ps.setInt(3, idLigne);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        Arret arretPrecedent = arretDAO.getArretByID(idArretActuel);
                        Arret arretSuivant = arretDAO.getArretByID(idArretSuivant);
                        Ligne ligne = ligneDAO.getLigneByID(idLigne);

                        desserte = new Desserte(arretPrecedent, arretSuivant, ligne, rs.getInt("duree"), rs.getInt("distance"));
                        return desserte;
                    }
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("Erreur lors de la recherche de la durée et de la distance.", e);
            }
        } else {
            return desserte;
        }
    }

    // 5. Obtenir toute la table
    public List<Desserte> getAllDessertes() throws Exception {

        List<Desserte> lesDesertes = new ArrayList<>();

        String query = "SELECT idArret_est_precede, idArret_est_suivi, idLigne, duree, distance FROM \"pitt-bdd\".desserte";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Arret arretPrecedent = arretDAO.getArretByID(rs.getInt("idArret_est_precede"));
                    Arret arretSuivant = arretDAO.getArretByID(rs.getInt("idArret_est_suivi"));
                    Ligne ligne = ligneDAO.getLigneByID(rs.getInt("idLigne"));

                    // Gestion du cache
                    Desserte desserte = Desserte.getDesserteEnCache(arretPrecedent, arretSuivant, ligne);

                    if (desserte == null) {
                        desserte = new Desserte(arretPrecedent, arretSuivant, ligne, rs.getInt("duree"), rs.getInt("distance"));
                    }

                    lesDesertes.add(desserte);
                }
            }
            return lesDesertes;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche des arrêts desservis.", e);
        }
    }

    /*
    public boolean deleteUneDesserte(Desserte uneDesserte) throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".desserte WHERE idarret_est_precede = ? AND idarret_est_suivi = ? AND idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, horaire.getId());
            ps.setInt(2, horaire.getId());
            ps.setInt(3, uneLigne.getId());

            int desserteModifie = ps.executeUpdate();

            if (desserteModifie > 0) {
                HoraireDepart.supprimerDuCache(horaire);
            }
            return HoraireModifie > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de l'horaire ID=" + horaire.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de l'horaire avec ID : " + horaire.getId(), e);
        }
    }*/
}