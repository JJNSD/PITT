package models;

import metiers.Arret;
import metiers.HoraireDepart;
import metiers.Perturbation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PerturbationDAO {
    private Connection connexion;
    private static ArretDAO arretDAO =  new ArretDAO();

    public PerturbationDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    private List<Arret> getArretsPerturbes(int idPerturbation) {
        List<Arret> lesArretsPerturbes = new ArrayList<>();

        String query = "SELECT idArret FROM \"pitt-bdd\".arretperturbe WHERE idperturbation = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idPerturbation);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idArret = rs.getInt("idArret");
                    lesArretsPerturbes.add(arretDAO.getArretByID(idArret));
                }
            }
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les users. Détails:");
            e.printStackTrace();
        }
        return lesArretsPerturbes;
    }

    private boolean deletePerturbationInArretPerturbe(int idPerturbation) {
        String query = "DELETE FROM \"pitt-bdd\".arretperturbe WHERE idPerturbation = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setInt(1, idPerturbation);

            int arretsModifies = ps.executeUpdate();

            return arretsModifies > 0;

        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la suppression de l'arrêt perturbé ID=" + idPerturbation + ". Détails:");
            e.printStackTrace();
            return false;
        }
    }

    public Perturbation getPerturbationByID(int idPerturbation) throws Exception {
        // Vérification du cache
        Perturbation perturbation = Perturbation.getPerturbationEnCache(idPerturbation);

        if (perturbation == null) {
            String query = "SELECT libelle, dateHeureDebut, dateHeureFin FROM \"pitt-bdd\".perturbation WHERE idPerturbation = ?";
            try (PreparedStatement ps = connexion.prepareStatement(query)) {
                ps.setInt(1, idPerturbation);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String libelle = rs.getString("libelle");
                        String dateHeureDebutString = rs.getString("dateHeureDebut");
                        String dateHeureFinString = rs.getString("dateHeureFin");

                        LocalDateTime dateHeureDebut;
                        LocalDateTime dateHeureFin;

                        if (dateHeureDebutString != null) {
                            dateHeureDebut = LocalDateTime.parse(dateHeureDebutString);
                        } else {
                            dateHeureDebut = null;
                        }
                        if (dateHeureDebutString != null) {
                            dateHeureFin = LocalDateTime.parse(dateHeureFinString);
                        } else {
                            dateHeureFin = null;
                        }

                        perturbation = new Perturbation(idPerturbation, libelle, dateHeureDebut, dateHeureFin);
                        perturbation.setArretsPerturbes(this.getArretsPerturbes(idPerturbation));
                        return perturbation;
                    } else {
                        return null; // aucune perturbation trouvée
                    }
                }

            } catch (Exception e) {
                System.err.println("ERREUR BDD lors de la recherche de la perturbation ID=" + idPerturbation + ". Détails:");
                e.printStackTrace();
                throw new Exception("Erreur lors de la recherche de la perturbation avec ID : " + idPerturbation, e);
            }
        } else {
            return perturbation;
        }
    }

    public List<Perturbation> getAllPerturbations() throws Exception {
        List<Perturbation> lesPerturbations = new ArrayList<>();

        String query = "SELECT idPerturbation, libelle, dateheuredebut, dateheurefin FROM \"pitt-bdd\".perturbation";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idPerturbation = rs.getInt("idPerturbation");

                    // Gestion du cache
                    Perturbation perturbation = Perturbation.getPerturbationEnCache(idPerturbation);

                    if (perturbation == null) {
                        String libelle = rs.getString("libelle");
                        String dateHeureDebutString = rs.getString("dateHeureDebut");
                        String dateHeureFinString = rs.getString("dateHeureFin");

                        LocalDateTime dateHeureDebut;
                        LocalDateTime dateHeureFin;

                        if (dateHeureDebutString != null) {
                            dateHeureDebut = LocalDateTime.parse(dateHeureDebutString);
                        } else {
                            dateHeureDebut = null;
                        }
                        if (dateHeureDebutString != null) {
                            dateHeureFin = LocalDateTime.parse(dateHeureFinString);
                        } else {
                            dateHeureFin = null;
                        }

                        perturbation = new Perturbation(idPerturbation, libelle, dateHeureDebut, dateHeureFin);
                        perturbation.setArretsPerturbes(this.getArretsPerturbes(idPerturbation));
                    }

                    lesPerturbations.add(perturbation);
                }
            }
            return lesPerturbations;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les users. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les users", e);
        }
    }

    public boolean addPerturbation(Perturbation perturbation) throws Exception {
        String query = "INSERT INTO \"pitt-bdd\".perturbation (libelle, dateheuredebut, dateheurefin) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, perturbation.getLibelle());
            ps.setString(2, perturbation.getDateHeureDebut().toString());
            ps.setString(3, perturbation.getDateHeureFin().toString());

            int perturbationsModifiees = ps.executeUpdate();

            if (perturbationsModifiees == 0) {
                return false;
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                //si id a bien été crée
                if (generatedKeys.next()) {
                    perturbation.setId(generatedKeys.getInt(1)); //1 = le num de la colonne, car la seule colonne ds ce ResultSet)
                } else {
                    throw new Exception("Création de la perturbation a échoué, aucun identifiant n'a pu être récupéré.");
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la création d'un arrêt. Détails:");
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePerturbation(Perturbation perturbation) throws Exception {
        String query = "UPDATE \"pitt-bdd\".perturbation SET libelle = ?, dateheuredebut = ?, dateheurefin = ? WHERE idPerturbation = ?";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            ps.setString(1, perturbation.getLibelle());
            ps.setString(2, perturbation.getDateHeureDebut().toString());
            ps.setString(3, perturbation.getDateHeureFin().toString());
            ps.setInt(4, perturbation.getId());

            int perturbationsModifiees = ps.executeUpdate();

            return perturbationsModifiees > 0;
        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de l'update de la perturbation " + perturbation.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'update de la perturbation avec ID : " + perturbation.getId(), e);
        }
    }

    public boolean deletePerturbation(Perturbation perturbation) throws Exception {
        if (deletePerturbationInArretPerturbe(perturbation.getId())) {
            String query = "DELETE FROM \"pitt-bdd\".perturbation WHERE idPerturbation = ?";

            try (PreparedStatement ps = connexion.prepareStatement(query)) {

                ps.setInt(1, perturbation.getId());

                int PerturbationModifie = ps.executeUpdate();

                if (PerturbationModifie > 0) {
                    Perturbation.supprimerDuCache(perturbation);
                }
                return PerturbationModifie > 0;
            } catch (SQLException e) {
                System.err.println("ERREUR BDD lors de la suppression de la perturbation ID=" + perturbation.getId() + ". Détails:");
                e.printStackTrace();
                throw new Exception("Erreur lors de la suppression de l'arrêt avec ID : " + perturbation.getId(), e);
            }
        } else {
            System.err.println("ERREUR BDD lors de la suppression de la perturbation ID=" + perturbation.getId() + " dans ArretPerturbe");
            return false;
        }
    }

    public boolean resetPerturbation() throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".arret";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            int arretsModifies = ps.executeUpdate();

            Perturbation.resetCache();

            return arretsModifies > 0;
        } catch (SQLException e) {
            System.err.println("ERREUR BDD lors de la suppression de la table Perturbation, Détails : ");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la table Perturbation" , e);
        }
    }
}
