package models;

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

        String query = "SELECT idtypetransport, libelletypetransport, cout FROM \"bdd-pitt\".typetransport WHERE idtypetransport = ?";
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
    }

    public List<TypeTransport> getAllTypeTransport() throws Exception {
        List<TypeTransport> lesTransports = new ArrayList<>();

        String query = "SELECT idtypetransport, libelletypetransport, cout FROM \"bdd-pitt\".typetransport";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idTypeTransport = rs.getInt("idtypetransport");
                    String libelletypetransport = rs.getString("libelletypetransport");
                    float cout = rs.getFloat("cout");

                    TypeTransport transport = new TypeTransport(idTypeTransport, libelletypetransport, cout);
                    lesTransports.add(transport);
                }
            }
            return lesTransports;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la recherche de tous les types de transports. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la recherche de tous les types de transports", e);
        }
    }

    public int insertTypeTransport(TypeTransport transport) throws Exception {
        String query = "INSERT INTO \"bdd-pitt\".typetransport (libelletypetransport, cout) VALUES (?, ?) RETURNING idtypetransport";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, transport.getNomType());
            ps.setFloat(2, transport.getCout());

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("idtypetransport");
            }
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de l'insertion du type de transport '" + transport.getNomType() + "'. Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de l'insertion du type de transport : " + transport.getNomType(), e);
        }
    }

    public boolean updateTypeTransport(TypeTransport transport) throws Exception {
        String query = "UPDATE \"bdd-pitt\".typetransport SET libelletypetransport = ?, cout = ? WHERE idtypetransport = ?";
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

    public boolean deleteTypeTransport(int idTypeTransport) throws Exception {
        String query = "DELETE FROM \"bdd-pitt\".typetransport WHERE idtypetransport = ?";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, idTypeTransport);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("ERREUR BDD lors de la suppression du type de transport ID=" + idTypeTransport + ". Détails:");
            e.printStackTrace();
            throw new Exception("Erreur lors de la suppression du type de transport avec ID : " + idTypeTransport, e);
        }
    }
}
