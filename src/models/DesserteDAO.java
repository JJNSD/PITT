package models;

import metiers.Arret;
import metiers.Desserte;
import metiers.Ligne;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DesserteDAO {

    private Connection connexion;

    public DesserteDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    // 1. ID arrêt actuel -> arrêts suivants + lignes
    public List<Desserte> getDesserteUnArretByID(
            int idArretEnCours) throws Exception {

        String query =
                "SELECT idArret_est_precede, idArret_est_suivi,idLigne, duree, distance FROM \"pitt-bdd\".desserte WHERE idArret_est_precede = ?";

        List<Desserte> lesDessertes = new ArrayList<>();

        ArretDAO arretDAO = new ArretDAO();
        LigneDAO ligneDAO = new LigneDAO();

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idArretEnCours);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    int idArretPrecedent =
                            rs.getInt("idArret_est_precede");

                    int idArretSuivant =
                            rs.getInt("idArret_est_suivi");

                    int idLigne = rs.getInt("idLigne");
                    int duree = rs.getInt("duree");
                    int distance = rs.getInt("distance");

                    Arret arretPrecedent =
                            arretDAO.getArretByID(idArretPrecedent);

                    Arret arretSuivant =
                            arretDAO.getArretByID(idArretSuivant);

                    Ligne ligne = ligneDAO.getLigneByID(idLigne);

                    Desserte desserte = new Desserte(
                            arretPrecedent,
                            arretSuivant,
                            ligne,
                            duree,
                            distance
                    );

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
    public Arret getArretSuivant(
            int idArretEnCours, int idLigne) throws Exception {

        String query = "SELECT a.idArret, a.nomArret, a.lattitude, a.longitude FROM \"pitt-bdd\".desserte d JOIN \"pitt-bdd\".arret a ON a.idArret = d.idArret_est_suivi WHERE d.idArret_est_precede = ? AND d.idLigne = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idArretEnCours);
            ps.setInt(2, idLigne);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new Arret(
                            rs.getInt("idArret"),
                            rs.getString("nomArret"),
                            rs.getFloat("lattitude"),
                            rs.getFloat("longitude")
                    );
                }

                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de l'arrêt suivant.", e);
        }
    }

    // 3. ID ligne -> arrêts desservis
    public List<Arret> getArretsDesservis(
            int idLigne) throws Exception {

        List<Arret> lesArrets = new ArrayList<>();

        String query = "SELECT a.idArret, a.nomArret, a.lattitude, a.longitude FROM \"pitt-bdd\".arret a WHERE a.idArret IN (SELECT d.idArret_est_precede FROM \"pitt-bdd\".desserte d WHERE d.idLigne = ? UNION SELECT d.idArret_est_suivi FROM \"pitt-bdd\".desserte d WHERE d.idLigne = ?)";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idLigne);
            ps.setInt(2, idLigne);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Arret arret = new Arret(
                            rs.getInt("idArret"),
                            rs.getString("nomArret"),
                            rs.getFloat("lattitude"),
                            rs.getFloat("longitude")
                    );

                    lesArrets.add(arret);
                }
            }

            return lesArrets;

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche des arrêts desservis.", e);
        }
    }

    // 4. ID arrêt actuel + ID arrêt suivant + ID ligne
    // -> durée et distance
    public Desserte getDureeDistance(
            int idArretActuel,
            int idArretSuivant,
            int idLigne) throws Exception {

        String query = "SELECT idArret_est_suivi, idLigne, duree, distance FROM \"pitt-bdd\".desserte WHERE idArret_est_precede = ? AND idArret_est_suivi = ? AND idLigne = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idArretActuel);
            ps.setInt(2, idArretSuivant);
            ps.setInt(3, idLigne);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    ArretDAO arretDAO = new ArretDAO();
                    LigneDAO ligneDAO = new LigneDAO();

                    Arret arretPrecedent =
                            arretDAO.getArretByID(idArretActuel);

                    Arret arretSuivant =
                            arretDAO.getArretByID(idArretSuivant);

                    Ligne ligne =
                            ligneDAO.getLigneByID(idLigne);

                    return new Desserte(
                            arretPrecedent,
                            arretSuivant,
                            ligne,
                            rs.getInt("duree"),
                            rs.getInt("distance")
                    );
                }

                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de la durée et de la distance.", e);
        }
    }
}