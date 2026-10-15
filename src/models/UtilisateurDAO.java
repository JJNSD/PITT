package models;

import metiers.Utilisateur;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    private Connection connexion;

    public UtilisateurDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    public Utilisateur getUtilisateurByUsername(String username) throws Exception {

        String query = "SELECT username, password, prenom, nom, isAdmin FROM \"bdd-pitt\".utilisateur WHERE username = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    String prenom = rs.getString("prenom");
                    String nom = rs.getString("nom");
                    boolean isAdmin = rs.getBoolean("isAdmin");

                    Utilisateur user = new Utilisateur(username, password, prenom, nom, isAdmin);
                    return user;
                } else {
                    return null; // aucun arret trouvé
                }
            }

        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche du user " + username + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche du user avec ID : " + username, e);
        }
    }

    public List<Utilisateur> getAllArrets() throws Exception {
        List<Utilisateur> lesUsers = new ArrayList<>();

        String query = "SELECT username, password, prenom, nom, isAdmin FROM \"bdd-pitt\".utilisateur";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String prenom = rs.getString("prenom");
                    String nom = rs.getString("nom");
                    boolean isAdmin = rs.getBoolean("isAdmin");

                    Utilisateur user = new Utilisateur(username, password, prenom, nom, isAdmin);
                    lesUsers.add(user);
                }
            }
            return lesUsers;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les users. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les users", e);
        }
    }

    public boolean addUser(Utilisateur user) throws Exception {
        String query = "INSERT INTO \"bdd-pitt\".utilisateur (username, password, prenom, nom, isAdmin) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getPrenom());
            ps.setString(4, user.getPrenom());
            ps.setBoolean(5, user.isAdmin());

            int arretsModifies = ps.executeUpdate();

            return arretsModifies != 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la création d'un arrêt. Détails:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(Utilisateur user) throws Exception {
        String query = "UPDATE \"bdd-pitt\".utilisateur SET password = ?, prenom = ?, nom = ?, isAdmin = ? WHERE username = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setString(1, user.getPassword());
            ps.setString(2, user.getPrenom());
            ps.setString(3, user.getNom());
            ps.setBoolean(4, user.isAdmin());
            ps.setString(5, user.getUsername());

            int arretsModifies = ps.executeUpdate();

            return arretsModifies > 0;

        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de l'update du user " + user.getUsername() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'update du user avec ID : " + user.getUsername(), e);
        }
    }

    public boolean deleteUser(Utilisateur user) throws Exception {

        String query = "DELETE FROM \"bdd-pitt\".utilisateur WHERE username = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setString(1, user.getUsername());

            int arretsModifies = ps.executeUpdate();

            return arretsModifies > 0;

        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la suppression de l'arrêt ID=" + user.getUsername() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de l'arrêt avec ID : " + user.getUsername(), e);
        }
    }
}
