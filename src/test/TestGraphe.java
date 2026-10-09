package test;

import metiers.*;

public class TestGraphe {
    public static void main(String[] args) {
        Graphe g = new Graphe();

        // Type de la ligne de Test
        TypeTransport bus = new TypeTransport(1, "Bus", (float) 1.70);

        // Création d'une ligne et de ses arrêts
        Arret a1 = new Arret(1,  "Michelet", (float) 47.463532, (float) -0.545509);
        Arret a2 = new Arret(2,  "André Leroy", (float) 47.464661, (float) -0.548248);
        Arret a3 = new Arret(3,  "Paul Bert", (float) 47.466327, (float) -0.552460);
        Arret a4 = new Arret(4,  "Foch Haras", (float) 47.466782, (float) -0.552612);
        Arret a5 = new Arret(5,  "Foch St Aubin", (float) 47.468556, (float) -0.550032);
        Arret a6 = new Arret(6,  "Foch Maison Bleue", (float) 47.468962, (float) -0.549706);
        Arret a7 = new Arret(7,  "Lorraine", (float) 47.470190, (float) -0.547600);

        Ligne l1 = new Ligne(1, "Ligne 1", "FFFFFF", a1, a7, bus);

        // Création des chemins

        // Aller
        g.ajouterCheminMAJSiExistant(a1, a2, l1, 5);
        g.ajouterCheminMAJSiExistant(a2, a3, l1, 1);
        g.ajouterCheminMAJSiExistant(a3, a4, l1, 0);
        g.ajouterCheminMAJSiExistant(a4, a5, l1, 1);
        g.ajouterCheminMAJSiExistant(a5, a6, l1, 1);
        g.ajouterCheminMAJSiExistant(a6, a7, l1, 0);

        // Retour
        g.ajouterCheminMAJSiExistant(a7, a6, l1, 0);
        g.ajouterCheminMAJSiExistant(a6, a5, l1, 1);
        g.ajouterCheminMAJSiExistant(a5, a4, l1, 1);
        g.ajouterCheminMAJSiExistant(a4, a2, l1, 1);
        g.ajouterCheminMAJSiExistant(a2, a1, l1, 1);

        System.out.println(g);

        // Test MàJ
        g.ajouterCheminMAJSiExistant(a1, a2, l1, 1);

        System.out.println(g);
    }
}
