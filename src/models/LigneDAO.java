package models;

import metiers.Arret;
import metiers.Ligne;
import metiers.TypeTransport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LigneDAO {
    private Connection connexion;
    private static ArretDAO arretDAO =  new ArretDAO();
    private static TypeTransportDAO typeTransportDAO = new TypeTransportDAO();

    public LigneDAO() { this.connexion = ConnexionBDD.getConnexion(); }

    public Ligne getLigneByID(int idLigne) throws Exception {
        // Vérification du cache
        Ligne ligne = Ligne.getLigneEnCache(idLigne);

        if (ligne == null) {

            String query = "SELECT libelle, hexcolor, idtypetransport, idarretdepart, idarretarrivee FROM \"pitt-bdd\".ligne WHERE idligne = ?";
            try (PreparedStatement ps = connexion.prepareStatement(query)) {
                ps.setInt(1, idLigne);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String libelle = rs.getString("libelle");
                        String hexcolor = rs.getString("hexcolor");

                        TypeTransport type = typeTransportDAO.getTypeTransportByID(rs.getInt("idtypetransport"));
                        Arret depart = arretDAO.getArretByID(rs.getInt("idarretdepart"));
                        Arret arrivee = arretDAO.getArretByID(rs.getInt("idarretarrivee"));

                        ligne = new Ligne(idLigne, libelle, hexcolor, depart, arrivee, type);
                        return ligne;
                    } else {
                        return null; // aucune ligne trouvée
                    }
                }
            } catch (Exception e) {
                System.err.println("ERREUR BDD lors de la recherche de la ligne ID=" + idLigne + ". Détails:");
                e.printStackTrace();
                throw new Exception("Erreur lors de la recherche de la ligne avec ID : " + idLigne, e);
            }
        } else {
            return ligne;
        }
    }

    public List<Ligne> getAllLigne() throws Exception {
        List<Ligne> lesLignes = new ArrayList<>();

        String query = "SELECT idligne, libelle, hexcolor, idtypetransport, idarretdepart, idarretarrivee FROM \"pitt-bdd\".ligne";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idLigne = rs.getInt("idligne");

                    Ligne uneLigne = Ligne.getLigneEnCache(idLigne);

                    if (uneLigne == null) {
                        String libelle = rs.getString("libelle");
                        String hexcolor = rs.getString("hexcolor");
                        int idType = rs.getInt("idtypetransport");
                        int idDepart = rs.getInt("idarretdepart");
                        int idArrivee = rs.getInt("idarretarrivee");
                        TypeTransport type = typeTransportDAO.getTypeTransportByID(idType);
                        Arret depart =  arretDAO.getArretByID(idDepart);
                        Arret arrivee = arretDAO.getArretByID(idArrivee);

                        uneLigne = new Ligne(idLigne, libelle, hexcolor, depart, arrivee, type);
                    }

                    lesLignes.add(uneLigne);
                }
            }
            return lesLignes;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de toutes les lignes. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de toutes les lignes", e);
        }
    }

    public boolean insertLigne(Ligne ligne) throws Exception {
        String query = "INSERT INTO \"pitt-bdd\".ligne (libelle, hexcolor, idtypetransport, idarretdepart, idarretarrivee) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, ligne.getLibelle());
            ps.setString(2, ligne.getHexColor());
            ps.setInt(3, ligne.getTypeTransport().getId());
            ps.setInt(4, ligne.getTerminusDepart().getId());
            ps.setInt(5, ligne.getTerminusArrivee().getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de l'insertion de la ligne '" + ligne.getLibelle() + "'. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'insertion de la ligne : " + ligne.getLibelle(), e);
        }
    }

    public boolean updateLigne(Ligne ligne) throws Exception {
        String query = "UPDATE \"pitt-bdd\".ligne SET libelle = ?, hexcolor = ?, idtypetransport = ?, idarretdepart = ?, idarretarrivee = ? WHERE idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, ligne.getLibelle());
            ps.setString(2, ligne.getHexColor());
            ps.setInt(3, ligne.getTypeTransport().getId());
            ps.setInt(4, ligne.getTerminusDepart().getId());
            ps.setInt(5, ligne.getTerminusArrivee().getId());
            ps.setInt(6, ligne.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la modification de la ligne ID=" + ligne.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la modification de la ligne avec ID : " + ligne.getId(), e);
        }
    }

    public boolean deleteLigne(Ligne ligne) throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".ligne WHERE idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, ligne.getId());

            int lignesModifies = ps.executeUpdate();

            if (lignesModifies > 0) {
                Ligne.supprimerDuCache(ligne);
            }
            return lignesModifies > 0;

        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de la ligne ID=" + ligne.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la ligne avec ID : " + ligne.getId(), e);
        }
    }

    public boolean resetLigne() throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".ligne";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            int lignesModifies = ps.executeUpdate();

            Ligne.resetCache();

            return lignesModifies > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de la table Ligne, Détails : ");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la table Ligne" , e);
        }
    }
}