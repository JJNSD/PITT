package models;

import metiers.Arret;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArretDAO {
    private Connection connexion;

    public ArretDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    public Arret getArretByID(int idArret) throws Exception {

        String query = "SELECT idArret, nomArret, lattitude, longitude FROM \"bdd-pitt\".arret WHERE idArret = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idArret);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nomArret");
                    float lattitude = rs.getFloat("lattitude");
                    float longitude = rs.getFloat("longitude");

                    Arret arret = new Arret(idArret, nom, lattitude, longitude);
                    return arret;
                } else {
                    return null; // aucun arret trouvé
                }
            }

        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de l'arrêt ID=" + idArret + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de l'arrêt avec ID : " + idArret, e);
        }
    }

    public List<Arret> getAllArrets() throws Exception {
        List<Arret> lesArrets = new ArrayList<>();

        String query = "SELECT idArret, nomArret, lattitude, longitude FROM \"bdd-pitt\".arret";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idArret = rs.getInt("idArret");
                    String nom = rs.getString("nomArret");
                    float lattitude = rs.getFloat("lattitude");
                    float longitude = rs.getFloat("longitude");

                    Arret arret = new Arret(idArret, nom, lattitude, longitude);
                    lesArrets.add(arret);
                }
            }
            return lesArrets;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les arrets. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les arrets", e);
        }
    }

    public boolean addArret(Arret arret) throws Exception {
        String query = "INSERT INTO \"bdd-pitt\".arret (nomArret,lattitude,longitude) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connexion.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, arret.getNomArret());
            ps.setDouble(2, arret.getLattitude());
            ps.setDouble(3, arret.getLongitude());

            int arretsModifies = ps.executeUpdate();

            if (arretsModifies == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                //si id a bien été crée
                if (generatedKeys.next()) {
                    arret.setId(generatedKeys.getInt(1)); //1 = le num de la colonne, car la seule colonne ds ce ResultSet)
                } else {
                    throw new Exception("Création de l'arrêt a échoué, aucun identifiant n'a pu être récupéré.");
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la création d'un arrêt. Détails:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateArret(Arret unArret) throws Exception {
        String query = "UPDATE \"bdd-pitt\".arret SET nomArret = ?, lattitude = ?, longitude = ? WHERE idArret = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setString(1, unArret.getNomArret());
            ps.setDouble(2, unArret.getLattitude());
            ps.setDouble(3, unArret.getLongitude());
            ps.setInt(4, unArret.getId());

            int arretsModifies = ps.executeUpdate();

            return arretsModifies > 0;

        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la mise à jour de l'arrêt ID=" + unArret.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la mise à jour de l'arrêt avec ID : " + unArret.getId(), e);
        }
    }

    public boolean deleteArret(Arret arret) throws Exception {

        String query = "DELETE FROM \"bdd-pitt\".arret WHERE idArret = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, arret.getId());

            int arretsModifies = ps.executeUpdate();

            return arretsModifies > 0;

        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la suppression de l'arrêt ID=" + arret.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de l'arrêt avec ID : " + arret.getId(), e);
        }
    }
}
