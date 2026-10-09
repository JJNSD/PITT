# PITT

Planificateur de trajet intelligent

TECHNIQUE & Gestion du projet :  
Jeu de données via data.gouv.fr -> Réseau Irigo (GG Maps)

Backend : Java avec Spring  
Tests unitaires : JUnit  
Frontend : vue.js  
Sans site : Insomnium / Postman  
BDD : Postgres  
Dockploy : Mise sur serveur  
Github : Versionning  
Jira : Gestion de projet  

=> Compte rendu (15-20 pages) à rendre le 20/11 + Soutenance fin Novembre le 23/11  

FONCTIONNALITES :  
–	Modélisation d’un réseau sous forme de graphe (avec stations, connexions, distances, durées, coûts)  
–	Coût = calculé distance & durée  
–	Recherche entre 2 points avec le moins de coûts et le plus rapidement via Djikstra & BFS + Algorithme en A*  
–	Permettre la recherche par critère (en durée, distance, coût)  
–	Gestion des contraintes & perturbations (connexions indisponible, modification des coûts & durées, etc...)  
–	Comparer les perfs des différents algos sur des graphes de tailles différentes  
–	Interface simple -> Ligne de commandes  

Développement du projet :  
–	Recherche de plusieurs trajets  
–	Optimisation multicritères  
–	Réseau multimodal  
–	Visualisation du graphe  
–	Etc...
