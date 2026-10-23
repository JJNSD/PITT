package models;

import metiers.TypeHoraire;
import metiers.TypeTransport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class TypeTransportDAO {
    private Connection connexion;

    public TypeTransportDAO() {
        this.connexion = ConnexionBDD.getConnexion();
    }

    public TypeTransport getTypeTransportByID(int idTypeTransport) throws Exception {
        // Vérification du cache
        TypeTransport typeTransport = TypeTransport.getTypeTransportEnCache(idTypeTransport);

        if (typeTransport == null) {
            String query = "SELECT idtypetransport, libelletypetransport, cout FROM \"pitt-bdd\".typetransport WHERE idtypetransport = ?";
            try (PreparedStatement ps = connexion.prepareStatement(query)) {
                ps.setInt(1, idTypeTransport);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String libelletypetransport = rs.getString("libelletypetransport");
                        float cout = rs.getFloat("cout");

                        TypeTransport transport = new TypeTransport(idTypeTransport, libelletypetransport, cout);
                        return transport;
                    } else {
                        return null;
                    }
                }

            } catch (Exception e) {
                System.err.println("ERREUR BDD lors de la recherche du type de transport ID=" + idTypeTransport + ". Détails:");
                e.printStackTrace();
                throw new Exception("Erreur lors de la recherche du type de transport avec ID : " + idTypeTransport, e);
            }
        } else {
            return typeTransport;
        }
    }

    public List<TypeTransport> getAllTypeTransport() throws Exception {
        List<TypeTransport> lesTransports = new ArrayList<>();

        String query = "SELECT idtypetransport, libelletypetransport, cout FROM \"pitt-bdd\".typetransport";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idTypeTransport = rs.getInt("idtypetransport");

                    // Gestion du cache
                    TypeTransport typeTransport = TypeTransport.getTypeTransportEnCache(idTypeTransport);

                    if (typeTransport == null) {
                        String libelletypetransport = rs.getString("libelletypetransport");
                        float cout = rs.getFloat("cout");

                        typeTransport = new TypeTransport(idTypeTransport, libelletypetransport, cout);
                    }

                    lesTransports.add(typeTransport);
                }
            }
            return lesTransports;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les types de transports. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les types de transports", e);
        }
    }

    public boolean insertTypeTransport(TypeTransport transport) throws Exception {
        String query = "INSERT INTO \"pitt-bdd\".typetransport (libelletypetransport, cout) VALUES (?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, transport.getNomType());
            ps.setFloat(2, transport.getCout());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de l'insertion du type de transport '" + transport.getNomType() + "'. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'insertion du type de transport : " + transport.getNomType(), e);
        }
    }

    public boolean updateTypeTransport(TypeTransport transport) throws Exception {
        String query = "UPDATE \"pitt-bdd\".typetransport SET libelletypetransport = ?, cout = ? WHERE idtypetransport = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, transport.getNomType());
            ps.setFloat(2, transport.getCout());
            ps.setInt(3, transport.getId());

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la modification du type de transport ID=" + transport.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la modification du type de transport avec ID : " + transport.getId(), e);
        }
    }

    public boolean deleteTypeTransport(TypeTransport typeTransport) throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".typetransport WHERE idtypetransport = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, typeTransport.getId());

            int TypeTransportModifie = ps.executeUpdate();

            if (TypeTransportModifie > 0) {
                TypeTransport.supprimerDuCache(typeTransport);
            }
            return TypeTransportModifie > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression du type de transport ID=" + typeTransport.getId() + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression du type de transport avec ID : " + typeTransport.getId(), e);
        }
    }

    public boolean resetTypeTransport() throws Exception {
        String query = "DELETE FROM \"pitt-bdd\".typetransport";

        try (PreparedStatement ps = connexion.prepareStatement(query)) {

            int typeTransportModifies = ps.executeUpdate();

            TypeTransport.resetCache();

            return typeTransportModifies > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression de la table TypeTransport, Détails : ");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression de la table TypeTransport" , e);
        }
    }
}
