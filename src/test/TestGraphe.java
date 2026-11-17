package test;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import metiers.*;
import models.DesserteDAO;
import models.LigneDAO;

public class TestGraphe {
    public static void main(String[] args) throws Exception {
        Graphe g = Graphe.getGraphe(1, 1, 1);

        // Chargement du graphe
        long startTime = System.nanoTime();

        boolean grapheEstCharge = g.chargerGrapheBDD();
        if (grapheEstCharge) {
            //System.out.println(g);
        } else {
            System.out.println("Chargement échoué...");
        }

        long endTime = System.nanoTime();

        System.out.println("Temps de chargement total : " + (endTime - startTime) + " ns");

        // ------ Test de trajets ------
        Arret gentric = Arret.getArretEnCache(76);
        Arret beaussier = Arret.getArretEnCache(40);

        // Exemple d'heure fixe pour tester (12:00) ou null pour prendre l'heure actuelle
        LocalTime heureTest = LocalTime.of(12, 0);

        // test bfs (paramètre heure : null = heure actuelle, ou heureTest)
        List<List<Object>> chemin = g.cheminBfs(gentric, beaussier, null);
        List<List<Object>> chemin2 = g.cheminBfs(beaussier, gentric, heureTest);
        System.out.println("Chemin BFS Gentric -> Beaussier (Heure actuelle) : " + chemin + "\n");
        System.out.println("Chemin BFS Beaussier -> Gentric (12:00) : " + chemin2 + "\n");

        // test djikstra (paramètre heure ajouté en fin de signature)
        System.out.println("Dijkstra Gentric -> Beaussier (Heure actuelle) : " + g.cheminDjikstra(gentric, beaussier, null, null) + "\n");
        System.out.println("Dijkstra Beaussier -> Gentric (12:00) : " + g.cheminDjikstra(beaussier, gentric, null, heureTest) + "\n");

        // test a* (paramètre heure ajouté en fin de signature)
        System.out.println("A* Gentric -> Beaussier (Heure actuelle) : " + g.cheminAStar(gentric, beaussier, null, null) + "\n");
        System.out.println("A* Beaussier -> Gentric (12:00) : " + g.cheminAStar(beaussier, gentric, null, heureTest) + "\n");
    }
}
