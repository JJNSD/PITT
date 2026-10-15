package test;

import java.util.Arrays;
import java.util.List;

import metiers.*;

public class TestGraphe {
    public static void main(String[] args) {
        Graphe g = new Graphe();

        // Type de la ligne de Test
        TypeTransport bus = new TypeTransport(1, "Bus", (float) 1.70);
        TypeTransport tram = new TypeTransport(1, "Tram", (float) 1.70);

        // Création d'une ligne et de ses arrêts
        Arret a1 = new Arret(1,  "Michelet", (float) 47.463532, (float) -0.545509);
        Arret a2 = new Arret(2,  "André Leroy", (float) 47.464661, (float) -0.548248);
        Arret a3 = new Arret(3,  "Paul Bert", (float) 47.466327, (float) -0.552460);
        Arret a4 = new Arret(4,  "Foch Haras", (float) 47.466782, (float) -0.552612);
        Arret a5 = new Arret(5,  "Foch St Aubin", (float) 47.468556, (float) -0.550032);
        Arret a6 = new Arret(6,  "Foch Maison Bleue", (float) 47.468962, (float) -0.549706);
        Arret a7 = new Arret(7,  "Lorraine", (float) 47.470190, (float) -0.547600);

        Ligne l1 = new Ligne(1, "Ligne 1", "FFFFFF", a1, a7, bus);

        Arret a10 = new Arret(10,  "Les Gares", (float) 47.463532, (float) -0.545509);
        Arret a11 = new Arret(11,  "Ralliement", (float) 47.463532, (float) -0.545509);
        Arret a12 = new Arret(12,  "Molière", (float) 47.463532, (float) -0.545509);
        Arret a13 = new Arret(13,  "Doutre Maine", (float) 47.463532, (float) -0.545509);
        Arret a14 = new Arret(14,  "Le Quai", (float) 47.463532, (float) -0.545509);
        Arret a15 = new Arret(15,  "Farcy", (float) 47.463532, (float) -0.545509);

        Ligne l2 = new Ligne(1, "Ligne C", "000000", a10, a15, tram);

        // Création des chemins

        // Aller L1
        g.ajouterCheminMAJSiExistant(a1, a2, l1, 5);
        g.ajouterCheminMAJSiExistant(a2, a3, l1, 1);
        g.ajouterCheminMAJSiExistant(a3, a4, l1, 0);
        g.ajouterCheminMAJSiExistant(a4, a5, l1, 1);
        g.ajouterCheminMAJSiExistant(a5, a6, l1, 1);
        g.ajouterCheminMAJSiExistant(a6, a7, l1, 0);

        // Retour L1
        g.ajouterCheminMAJSiExistant(a7, a6, l1, 0);
        g.ajouterCheminMAJSiExistant(a6, a5, l1, 1);
        g.ajouterCheminMAJSiExistant(a5, a4, l1, 1);
        g.ajouterCheminMAJSiExistant(a4, a2, l1, 1);
        g.ajouterCheminMAJSiExistant(a2, a1, l1, 1);

        // Aller C
        g.ajouterCheminMAJSiExistant(a10, a4, l2, 4);
        g.ajouterCheminMAJSiExistant(a4, a6, l2, 8);
        g.ajouterCheminMAJSiExistant(a6, a11, l2, 1);
        g.ajouterCheminMAJSiExistant(a11, a12, l2, 1);
        g.ajouterCheminMAJSiExistant(a12, a13, l2, 3);
        g.ajouterCheminMAJSiExistant(a13, a14, l2, 3);
        g.ajouterCheminMAJSiExistant(a14, a15, l2, 4);

        // Retour C
        g.ajouterCheminMAJSiExistant(a4, a10, l2, 2);
        g.ajouterCheminMAJSiExistant(a6, a4, l2, 2);
        g.ajouterCheminMAJSiExistant(a11, a6, l2, 1);
        g.ajouterCheminMAJSiExistant(a12, a11, l2, 1);
        g.ajouterCheminMAJSiExistant(a13, a12, l2, 3);
        g.ajouterCheminMAJSiExistant(a14, a13, l2, 3);
        g.ajouterCheminMAJSiExistant(a15, a14, l2, 4);

        //System.out.println(g);


        // Test MàJ
        //g.ajouterCheminMAJSiExistant(a1, a2, l1, 1);

        //System.out.println(g);

        /*System.out.println("Distance de Farcy à Le Quai : " + g.distanceVoisin(a15, a14, l2) + " | ATTENDU : 4");
        System.out.println("Distance de Farcy à Doutre Maine : " + g.distanceVoisin(a15, a13, l2) + " | ATTENDU : -1");
        System.out.println("Distance de Michelet à André Leroy : " + g.distanceVoisin(a1, a2, l1) + " | ATTENDU : 1");
        System.out.println("Distance de Foch Haras à Foch Maison Bleu '1' : " + g.distanceVoisin(a4, a6, l1) + " | ATTENDU : -1");
        System.out.println("Distance de Foch Haras à Foch Maison Bleu 'C' : " + g.distanceVoisin(a4, a6, l2) + " | ATTENDU : 2");*/

        // test bfs
        List<List<Object>> chemin = g.cheminBfs(a1, a7);
        List<List<Object>> chemin2 = g.cheminBfs(a7, a1);
        System.out.println("Chemin BFS Michelet -> Lorraine : " + chemin);
        System.out.println("Chemin BFS Lorraine -> Michelet : " + chemin2);

        // test djikstra

        System.out.println("Dijkstra Michelet -> Lorraine : " + g.cheminDjikstra(a1, a7, null));
        System.out.println("Dijkstra Lorraine -> Michelet : " + g.cheminDjikstra(a7, a1, null));

        // test a*

        System.out.println("A* Michelet -> Lorraine : " + g.cheminAStar(a1, a7, null));
        System.out.println("A* Lorraine -> Michelet : " + g.cheminDjikstra(a7, a1, null));

        // Tests correspondance
        System.out.println("Djikstra Ralliement -> Les Gares : " + g.cheminDjikstra(a11, a10, null));

        System.out.println("Djikstra Ralliement -> André Leroy : " + g.cheminDjikstra(a11, a2, null));
    }
}
