package InterfaceTerminale;

import metiers.Arret;
import metiers.Graphe;
import metiers.Ligne;
import models.ArretDAO;
import models.LigneDAO;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class Main {
    static void main() throws Exception {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));

        // DAOs
        ArretDAO arretDAO = new ArretDAO();
        LigneDAO ligneDAO = new LigneDAO();

        int facteurDuree = 0;
        int facteurDistance = 0;
        int facteurCoutMonetaire = 0;
        Arret arret1 = null;
        Arret arret2 = null;
        LocalTime horaireRecherche = null;

        Graphe leGraphe = null;

        // Chargement des lignes pour les recherches
        List<Ligne> lesLignes = ligneDAO.getAllLigne();

        while(true) {
            System.out.println("Ecrivez une commande : ('help' pour le message d'aide) : ");
            String command = r.readLine();

            switch (command) {
                case "help":
                    printHelp();
                    break;

                case "setFacteurDuree":
                    try {
                        System.out.println("Entrez le facteur : ");
                        String facteur = r.readLine();
                        int facteurDureeTemp = Integer.parseInt(facteur);
                        if (facteurDureeTemp >= 1 && facteurDureeTemp <= 10) {
                            facteurDuree = facteurDureeTemp;
                            System.out.println("Facteur chargé à " + facteurDuree + ".");
                        } else {
                            System.out.println("Erreur : L'entrée n'est pas compris entre 1 et 10.");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : L'entrée n'est pas un nombre.");
                    }
                    break;

                case "setFacteurDistance":
                    try {
                        System.out.println("Entrez le facteur : ");
                        String facteur = r.readLine();
                        int facteurDistanceTemp = Integer.parseInt(facteur);
                        if (facteurDistanceTemp >= 1 && facteurDistanceTemp <= 10) {
                            facteurDistance = facteurDistanceTemp;
                            System.out.println("Facteur chargé à " + facteurDistance + ".");
                        } else {
                            System.out.println("Erreur : L'entrée n'est pas compris entre 1 et 10.");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : L'entrée n'est pas un nombre.");
                    }
                    break;

                case "setFacteurCoutMonetaire":
                    try {
                        System.out.println("Entrez le facteur : ");
                        String facteur = r.readLine();
                        int facteurCoutMonetaireTemp = Integer.parseInt(facteur);
                        if (facteurCoutMonetaireTemp >= 1 && facteurCoutMonetaireTemp <= 10) {
                            facteurCoutMonetaire = facteurCoutMonetaireTemp;
                            System.out.println("Facteur chargé à " + facteurCoutMonetaire + ".");
                        } else {
                            System.out.println("Erreur : L'entrée n'est pas compris entre 1 et 10.");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : L'entrée n'est pas un nombre.");
                    }
                    break;

                case "loadGraphe":
                    if (facteurDuree == 0 && facteurDistance == 0 && facteurCoutMonetaire == 0) {
                        System.out.println("Impossible de charger le graphe, facteur(s) manquant(s).");
                    } else {
                        try {
                            leGraphe = Graphe.getGraphe(facteurDuree, facteurDistance, facteurCoutMonetaire);

                            System.out.println("=== CHARGEMENT DE LA CARTE ===");

                            long startTime = System.nanoTime();
                            leGraphe.chargerGrapheBDD();
                            long endTime = System.nanoTime();

                            System.out.println("=== FIN CHARGEMENT DE LA CARTE ===\nTemps de chargement : " + (endTime - startTime) + " ns");
                        } catch (Exception e) {
                            System.out.println("Erreur inconnue lors du chargement du graphe...");
                        }
                    }
                    break;

                case "setArret1":
                    try {
                        System.out.println("Entrez l'ID de l'arrêt souhaité : ");
                        int idArret = Integer.parseInt(r.readLine());
                        Arret arret1Temp = arretDAO.getArretByID(idArret);
                        if (arret1Temp == null) {
                            System.out.println("Erreur : Arrêt introuvable en BDD.");
                        } else {
                            arret1 = arret1Temp;
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'arrêt en question.");
                    }
                    break;

                case "setArret2":
                    try {
                        System.out.println("Entrez l'ID de l'arrêt souhaité : ");
                        int idArret = Integer.parseInt(r.readLine());
                        Arret arret2Temp = arretDAO.getArretByID(idArret);
                        if (arret2Temp == null) {
                            System.out.println("Erreur : Arrêt introuvable en BDD.");
                        } else {
                            arret2 = arret2Temp;
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'arrêt en question.");
                    }
                    break;

                case "setHoraireDepart":
                    try {
                        DateTimeFormatter parser = DateTimeFormatter.ofPattern("hh:mm:ss");

                        System.out.println("Entrez l'heure de départ à l'arrêt [Format hh:mm'] : ");
                        String heureDepartStr = r.readLine();
                        if (Objects.equals(heureDepartStr, "")) {
                            System.out.println("Erreur : Aucune horaire d'entrée.");
                        } else {
                            horaireRecherche = LocalTime.parse(heureDepartStr + ":00", parser);
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'horaire...");
                    }
                    break;

                case "doDjikstra":
                    try {
                        if (leGraphe == null) {
                            System.out.println("Erreur : Chargez le graphe.");
                        } else if (arret1 == null || arret2 == null) {
                            System.out.println("Erreur : Un de vos arrêt n'est pas défini...");
                        } else {
                            System.out.println("=== RECHERCHE DU TRAJET - DJIKSTRA ===");

                            long startTime = System.nanoTime();
                            List<List<Object>> cheminDjikstra = leGraphe.cheminDjikstra(arret1, arret2, lesLignes, horaireRecherche);
                            long endTime = System.nanoTime();

                            System.out.println("=== FIN RECHERCHE DU TRAJET ===");
                            System.out.println("Chemin : " + cheminDjikstra);
                            System.out.println("Temps de recherche : " + (endTime - startTime) + "ns");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'horaire...");
                    }
                    break;

                case "doA*":
                    try {
                        if (leGraphe == null) {
                            System.out.println("Erreur : Chargez le graphe.");
                        } else if (arret1 == null || arret2 == null) {
                            System.out.println("Erreur : Un de vos arrêt n'est pas défini...");
                        } else {
                            System.out.println("=== RECHERCHE DU TRAJET A* ===");

                            long startTime = System.nanoTime();
                            List<List<Object>> cheminDjikstra = leGraphe.cheminAStar(arret1, arret2, lesLignes, horaireRecherche);
                            long endTime = System.nanoTime();

                            System.out.println("=== FIN RECHERCHE DU TRAJET ===");
                            System.out.println("Chemin : " + cheminDjikstra);
                            System.out.println("Temps de recherche : " + (endTime - startTime) + "ns");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'horaire...");
                    }
                    break;

                case "doBFS":
                    try {
                        if (leGraphe == null) {
                            System.out.println("Erreur : Chargez le graphe.");
                        } else if (arret1 == null || arret2 == null) {
                            System.out.println("Erreur : Un de vos arrêt n'est pas défini...");
                        } else {
                            System.out.println("=== RECHERCHE DU TRAJET - BFS ===");

                            long startTime = System.nanoTime();
                            List<List<Object>> cheminBfs = leGraphe.cheminBfs(arret1, arret2, horaireRecherche);
                            long endTime = System.nanoTime();

                            System.out.println("=== FIN RECHERCHE DU TRAJET ===");
                            System.out.println("Chemin : " + cheminBfs);
                            System.out.println("Temps de recherche : " + (endTime - startTime) + "ns");
                        }
                    } catch (Exception e) {
                        System.out.println("Erreur : Impossible de charge l'horaire...");
                    }
                    break;

                default:
                    printHelp();
            }
        }
    }

    private static void printHelp() {
        System.out.println("help : Affiche ce message.\n" +
            "setFacteurDuree : définie l'importance de la durée lors du calcul du score.\n" +
            "setFacteurDistance : définie l'importance de la distance lors du calcul du score.\n" +
            "setFacteurCoutMonetaire : définie l'importance du coût monétaire lors du calcul du score.\n" +
            "loadGraphe : Charge le graphe.\n" +
            "setArret1 : définie l'arrêt de départ pour la recherche.\n" +
            "setArret2 : définie l'arrêt d'arrivé pour la recherche.\n" +
            "setHoraireRecherche : définie l'horaire à partir duquel faire la recherche (facultatif).\n" +
            "doDjikstra : Lance la recherche avec Djikstra.\n" +
            "doA* : Lance la recherche avec A*.\n" +
            "doBFS : Lance la recherche avec BFS.\n");
    }
}
