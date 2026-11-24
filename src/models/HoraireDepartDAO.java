package models;

import metiers.Arret;
import metiers.HoraireDepart;
import metiers.Ligne;
import metiers.TypeHoraire;

import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HoraireDepartDAO {

    private Connection connexion;
    private static TypeHoraireDAO typeHoraireDAO = new TypeHoraireDAO();

    public HoraireDepartDAO() { this.connexion = ConnexionBDD.getConnexion(); }

    public HoraireDepart getHoraireDepartByID(int idHoraireDepart, Ligne uneLigne) throws Exception {

        String query = "SELECT heuredepart, idtypehoraire FROM \"pitt-bdd\".horairedepart WHERE idhorairedepart = ? AND idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idHoraireDepart);
            ps.setInt(2, uneLigne.getId());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalTime heuredepart = rs.getObject("heuredepart", LocalTime.class);
                    int idTypeHoraire = rs.getInt("idtypehoraire");

                    TypeHoraire unType = typeHoraireDAO.getTypeHoraireByID(idTypeHoraire);
                    return new HoraireDepart(idHoraireDepart, uneLigne, heuredepart, unType);
                } else {
                    return null; // aucun horaire trouvé
                }
            }
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de l'horaire ID=" + idHoraireDepart + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de l'horaire avec ID : " + idHoraireDepart, e);
        }
    }

    /**
     * Tous les horaires de départ d'une ligne, triés par heure.
     */
    public List<HoraireDepart> getAllHoraireDepartByLigne(Ligne uneLigne) throws Exception {
        List<HoraireDepart> lesHoraires = new ArrayList<>();

        String query = "SELECT idhorairedepart, heuredepart, idtypehoraire FROM \"pitt-bdd\".horairedepart WHERE idligne = ? ORDER BY heuredepart";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, uneLigne.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idHoraireDepart = rs.getInt("idhorairedepart");

                    // Gestion du cache
                    HoraireDepart horaireDepart = HoraireDepart.getHoraireDepartEnCache(idHoraireDepart, uneLigne.getId());

                    if (horaireDepart == null) {
                        LocalTime heuredepart = rs.getObject("heuredepart", LocalTime.class);
                        int idTypeHoraire = rs.getInt("idtypehoraire");
                        TypeHoraire unType = typeHoraireDAO.getTypeHoraireByID(idTypeHoraire);
                        horaireDepart = new HoraireDepart(idHoraireDepart, uneLigne, heuredepart, unType);
                    }

                    lesHoraires.add(horaireDepart);
                }
            }
            return lesHoraires;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche des horaires de la ligne ID=" + uneLigne.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche des horaires de la ligne : " + uneLigne.getId(), e);
        }
    }

    /**
     * Insère un horaire de départ (l'id de l'objet est utilisé tel quel).
     */
    public boolean insertHoraireDepart(HoraireDepart horaire) throws Exception {
        String query = "INSERT INTO \"pitt-bdd\".horairedepart (idligne, heuredepart, idtypehoraire) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, horaire.getLigneAffecte().getId());
            ps.setTime(2, Time.valueOf(horaire.getHeureDepart()));
            ps.setInt(3, horaire.getTypeHoraire().getId());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de l'insertion de l'horaire ID=" + horaire.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'insertion de l'horaire avec ID : " + horaire.getId(), e);
        }
    }

    /**
     * Modifie l'heure et le type d'un horaire.
     * @return true si une ligne a été modifiée, false si l'horaire n'existe pas
     */
    public boolean updateHoraireDepart(HoraireDepart horaire) throws Exception {
        String query = "UPDATE \"pitt-bdd\".horairedepart SET heuredepart = ?, idtypehoraire = ? WHERE idhorairedepart = ? AND idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setTime(1, Time.valueOf(horaire.getHeureDepart()));
            ps.setInt(2, horaire.getTypeHoraire().getId());
            ps.setInt(3, horaire.getId());
            ps.setInt(4, horaire.getLigneAffecte().getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la modification de l'horaire ID=" + horaire.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la modification de l'horaire avec ID : " + horaire.getId(), e);
        }
    }

    /**
     * Supprime un horaire de départ.
     * @return true si une ligne a été supprimée, false si l'horaire n'existe pas
     */
    public boolean deleteHoraireDepart(HoraireDepart horaire, Ligne uneLigne) throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".horairedepart WHERE idhorairedepart = ? AND idligne = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, horaire.getId());
            ps.setInt(2, uneLigne.getId());

            int HoraireModifie = ps.executeUpdate();

            if (HoraireModifie > 0) {
                HoraireDepart.supprimerDuCache(horaire);
            }
            return HoraireModifie > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de l'horaire ID=" + horaire.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de l'horaire avec ID : " + horaire.getId(), e);
        }
    }

    public boolean resetHoraireDepart() throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".horairedepart";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            int horairesDepartModifies = ps.executeUpdate();

            HoraireDepart.resetCache();

            return horairesDepartModifies > 0;
        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la suppression de la table HoraireDepart, Détails : ");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la table HoraireDepart" , e);
        }
    }
}