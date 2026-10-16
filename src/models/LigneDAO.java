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

                    return new Ligne(idLigne, libelle, hexcolor, depart, arrivee, type);
                } else {
                    return null; // aucune ligne trouvée
                }
            }
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de la ligne ID=" + idLigne + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de la ligne avec ID : " + idLigne, e);
        }
    }

    public List<Ligne> getAllLigne() throws Exception {
        List<Ligne> lesLignes = new ArrayList<>();
        // caches pour ne pas recharger plusieurs fois le même arrêt / type de transport
        Map<Integer, Arret> arretsCharges = new HashMap<>();
        Map<Integer, TypeTransport> typesCharges = new HashMap<>();

        String query = "SELECT idligne, libelle, hexcolor, idtypetransport, idarretdepart, idarretarrivee FROM \"pitt-bdd\".ligne";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idLigne = rs.getInt("idligne");
                    String libelle = rs.getString("libelle");
                    String hexcolor = rs.getString("hexcolor");
                    int idType = rs.getInt("idtypetransport");
                    int idDepart = rs.getInt("idarretdepart");
                    int idArrivee = rs.getInt("idarretarrivee");

                    TypeTransport type = typesCharges.get(idType);
                    if (type == null) {
                        type = typeTransportDAO.getTypeTransportByID(idType);
                        typesCharges.put(idType, type);
                    }

                    Arret depart = arretsCharges.get(idDepart);
                    if (depart == null) {
                        depart = arretDAO.getArretByID(idDepart);
                        arretsCharges.put(idDepart, depart);
                    }

                    Arret arrivee = arretsCharges.get(idArrivee);
                    if (arrivee == null) {
                        arrivee = arretDAO.getArretByID(idArrivee);
                        arretsCharges.put(idArrivee, arrivee);
                    }

                    lesLignes.add(new Ligne(idLigne, libelle, hexcolor, depart, arrivee, type));
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

    public boolean deleteLigne(int idLigne) throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".ligne WHERE idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idLigne);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de la ligne ID=" + idLigne + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la ligne avec ID : " + idLigne, e);
        }
    }
}