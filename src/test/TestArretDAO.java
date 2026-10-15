package test;

import metiers.*;
import models.*;

public class TestArretDAO {
    public static void main(String[] args) throws Exception {
        ArretDAO arretDAO = new ArretDAO();

        Arret a1 = arretDAO.getArretByID(1);

        System.out.println(a1);

        System.out.println(arretDAO.getAllArrets());
    }
}
