# Import CSV

En gros, ce truc prend six fichiers CSV et remplit les tables du réseau dans la base.

```text
ENTRÉE                         CE QUI SE PASSE                         SORTIE
6 fichiers CSV  ──> vérification ──> remplacement en base ──> compte les lignes importées
                   des données       (en une seule fois)
```

## Ce qu'il faut lui donner

Un dossier contenant ces six fichiers, avec leurs en-têtes :

```text
types_transport.csv
types_horaires.csv
arrets.csv
lignes.csv
dessertes.csv
horaires_depart.csv
```
Le format des données CSV :
Exemples de lignes :

```csv
idtypetransport;libelletypetransport;cout
0;bus;1.7000
```

```csv
idarret;nomarret;lattitude;longitude
1;Avrillé-Ardenne;47.512;-0.596
```




## Comment lancer le runner

Ouvre `NaimeEstNwarr.java`, lance le ou je te niggarifie ! Le runner utilise les fichiers de `file/test_reseau` et le schéma `pitt_bdd_test`(tu peux le changer au besoin).

Je l'ai indiquer dans le fichier '[Uploadeur de nègre.com](Uploader.java)' comme ci-dessous :

```java
Uploader uploader = new Uploader("pitt_bdd_test");
Uploader.ImportResult resultat = uploader.importer(
        Path.of("src/FileUploader/file/test_reseau"),
        Uploader.ImportMode.REPLACE_NETWORK
);
System.out.println(resultat.getCompteurs());
```

## Si ça beug 

Déjà Je dois dire que le skill issue est galactique chef !!  

| Problème | Traduction pour le chef |
|---|---|
| Un CSV manque, son en-tête ou une valeur est incorrecte | Skill issue de CSV : vérifie le nom du fichier, les colonnes, les nombres et les heures. Le message te donne le fichier ou la ligne à corriger. |
| Un identifiant référencé n'existe pas | ID introuvable, chef t'es perdu dans le 67 verse ouuuu : une ligne, une desserte ou un horaire vise un arrêt ou un type absent des CSV. |
| Connexion ou table introuvable | Chef t'es pas connecté : vérifie `.env`, l'accès à PostgreSQL et que le schéma et les tables existent. |
| Des perturbations sont liées aux arrêts | L'import freine pour ne pas casser ces liens. Traite les associations avant de remplacer le réseau, chef. |
| Une erreur arrive pendant l'import | Rollback activé : les changements de cet essai sont annulés. Rien n'est enregistré à moitié, le 67 est sauf. |
