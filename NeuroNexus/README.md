![Logo](./res/view/icons/logo/logo_black_128.png)

# NeuroNexus

NeuroNexus est un logiciel permettant de classifier des données en utilisant un algorithme KNN. Les données peuvent être importées avec un fichier CSV.

## Auteurs
- Mathis MAGNIER : [mathis.magnier.etu@univ-lille.fr](mailto:mathis.magnier.etu@univ-lille.fr)
- Lony MICHAUX : [lony.michaux.etu@univ-lille.fr](mailto:lony.michaux.etu@univ-lille.fr)
- Gordon DELANGUE : [gordon.delangue.etu@univ-lille.fr](mailto:gordon.delangue.etu@univ-lille.fr)
- Timothée VARIN : [timothee.varin.etu@univ-lille.fr](mailto:timothee.varin.etu@univ-lille.fr)

## Créé avec
- [Java](https://www.java.com/)
- [JavaFX](https://openjfx.io/) - librairie open source permettant de créer des interfaces graphiques
- [OpenCSV](https://opencsv.sourceforge.net/) - librairie permettant la gestion des fichiers CSV

## Jalon 1

- Mathis MAGNIER : classification + choix Axes + multivue

- Lony MICHAUX : maquette figma + readme + observer observable + modèle  

- Gordon DELANGUE : différentes pages + multivue + addData 

- Timothée VARIN : affichage scatterView (nuages de points) + doc + tests


## Jalon 2 

### Repartition 

- Mathis s'occupera de modifier les méthodes d'import de CSV afin de pouvoir charger les différents fichiers, modifiera la classe ClassificationController afin de rendre le plus générique possible les méthodes et s'occupera de tester la robustesse de l'argorithme Knn.


- Lony quant à lui créera une classe Data pour remplacer la classe Fleur afin que les CSV puissent être compatibles dans notre programme et programmera les Tests et la doc.


- Gordon développera la liste qui sera à l'origine du choix utilisateur pour la classification des données et s'occupera du bonus implémentant un graphe 3D et l'affichage des images associé au point.


- Timothée élaborera le PDF pour le rendu de développement efficace et s'occupera des méthodes de calcul de distance (manhattan, euclidienne) pour le calcul des k plus proches, voisins.


### Fonctionnalité
Pour ce jalon 2, nous avons réussi à mettre en place toutes les fonctionnalités

- Pour que l'application puisse fonctionner avec d'autres fichier csv nous avons remplacé la classe Iris par la classe Data qui peut prendre tout type d'attribut par le biais de Map qui contiennent le nom de la colonne et la valeur.


- Dans ce jalon, nous pouvons maintenant choisir par quoi nous classifions les valeurs. Pour cela, nous avons simplement créé une liste contenant toutes les classifications possible (les noms des colonnes contenants des valeurs de types String) puis à l'aide d'une choiceBox l'utilisateur peut choisir sa propre classification.


- Nous pouvons classifier un point ajouté par l'utilisateur à partir de l'algorithme Knn, l'utilisateur peut choisir entre 2 méthodes, la méthode Euclidienne et la méthode Manhattan. Ces méthodes de calcul les plus proches voisins du point à classifier en fonction de tous ses attributs (après qu'ils ait tous était normalisé) et classifie avec la valeur majoritaire parmi ses plus proches voisins


- Pour tester la fiabilité de nos algorithmes Knn nous avons implémenté une classe robustesse qui fait une validation croisée sur un jeu de données passé en paramètre et qui calcule le taux de réussite de celui-ci


- Et pour finir, dans cette application, nous pouvons afficher les informations d'un point en cliquant dessus sur le graphe, une pop-up s'ouvrira et affichera toutes les informations liées à ce point.
### Bonus

- Dans cette application, nous avons développé 2 bonus :


- Le premier bonus est la possibilité d'afficher un graphe en 3D plutôt que le graphe de base en 2D qui permet de pouvoir mieux visualiser les attributs d'un point de par le fait qu'il y ait 3 axes.


- Et le deuxième bonus concerne l'affichage des informations d'un point lorsqu'on clique dessus, il permet d'afficher une photo de la donnée afin de mieux la visualiser et de voir à quoi elle ressemble. 

