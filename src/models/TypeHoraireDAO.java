package models;

import metiers.TypeHoraire;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TypeHoraireDAO {

    private Connection connexion;

    public TypeHoraireDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    public TypeHoraire getTypeHoraireByID(int idTypeHoraire) throws Exception {

        String query = "SELECT idtypehoraire, libelletypehoraire FROM \"bdd-pitt\".typehoraire WHERE idtypehoraire = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idTypeHoraire);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String libelletypehoraire = rs.getString("libelletypehoraire");

                    TypeHoraire horaire = new TypeHoraire(idTypeHoraire, libelletypehoraire);
                    return horaire;
                } else {
                    return null;
                }
            }

        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche du type d'horaire ID=" + idTypeHoraire + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche du type d'horaire avec ID : " + idTypeHoraire, e);
        }
    }

    public List<TypeHoraire> getAllTypeHoraire() throws Exception {
        List<TypeHoraire> lesHoraires = new ArrayList<>();

        String query = "SELECT idtypehoraire, libelletypehoraire FROM \"bdd-pitt\".typehoraire";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idTypeHoraire = rs.getInt("idtypehoraire");
                    String libelletypehoraire = rs.getString("libelletypehoraire");

                    TypeHoraire horaire = new TypeHoraire(idTypeHoraire, libelletypehoraire);
                    lesHoraires.add(horaire);
                }
            }
            return lesHoraires;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les types d'horaires. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les types d'horaires", e);
        }
    }

    public int insertTypeHoraire(TypeHoraire horaire) throws Exception {
        String query = "INSERT INTO \"bdd-pitt\".typehoraire (libelletypehoraire) VALUES (?) RETURNING idtypehoraire";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, horaire.getLibelle());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("idtypehoraire");
            }
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de l'insertion du type d'horaire '" + horaire.getLibelle() + "'. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'insertion du type d'horaire : " + horaire.getLibelle(), e);
        }
    }

    public boolean updateTypeHoraire(TypeHoraire horaire) throws Exception {
        String query = "UPDATE \"bdd-pitt\".typehoraire SET libelletypehoraire = ? WHERE idtypehoraire = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, horaire.getLibelle());
            ps.setInt(2, horaire.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la modification du type d'horaire ID=" + horaire.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la modification du type d'horaire avec ID : " + horaire.getId(), e);
        }
    }

    public boolean deleteTypeHoraire (int idTypeHoraire) throws Exception {
        String query = "DELETE FROM \"bdd-pitt\".typehoraire WHERE idtypehoraire = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idTypeHoraire);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression du type d'horaire ID=" + idTypeHoraire + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression du type d'horaire avec ID : " + idTypeHoraire, e);
        }
    }
}
