package test;

import metiers.*;
import models.*;

public class TestLigneDAO {
    public static void main(String[] args) throws Exception {
        LigneDAO ligneDAO = new LigneDAO();
        ArretDAO arretDAO = new ArretDAO();
        TypeTransportDAO transportDAO = new TypeTransportDAO();
        Ligne l1 = ligneDAO.getLigneByID(1);
        Arret a1 = arretDAO.getArretByID(1);
        Arret a2 = arretDAO.getArretByID(18);
        TypeTransport t1 = transportDAO.getTypeTransportByID(1);

        System.out.println(l1);

        System.out.println(ligneDAO.getAllLigne());

        boolean idDelete = ligneDAO.deleteLigne(38);
        System.out.println("Ligne delete : " + idDelete);
    }
}

