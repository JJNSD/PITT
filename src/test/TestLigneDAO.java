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
        Ligne D = new Ligne(0,"D", "000000", a1, a2, t1);

        boolean ok = ligneDAO.insertLigne(D);
        System.out.println("Insertion réussie : " + ok);

        Ligne l = ligneDAO.getLigneByID(38);
        l.setLibelle("E"); // si tu as un setter
        ligneDAO.updateLigne(l);

        System.out.println(l);

        boolean ok2 = ligneDAO.deleteLigne(ligneDAO.getLigneByID(38));
        System.out.println("Ligne delete : " + ok2);
    }
}

