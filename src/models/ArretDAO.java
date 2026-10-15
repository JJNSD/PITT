package models;

import metiers.Arret;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
}
