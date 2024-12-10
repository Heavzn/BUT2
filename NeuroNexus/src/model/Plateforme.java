package model;

import utils.Observable;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class Plateforme extends Observable {
    private static int lengthHeader = 0;
    private final Collection<String> csvHeader;
    private final Collection<Data> data;
    private String csvName = "";

    public Plateforme() {
        data = new ArrayList<>();
        csvHeader = new ArrayList<>();
    }

    /**
     * Ajoute une donnée à la collection des données.
     *
     * @param data une donnée
     */
    public void add(final Data data) {
        this.data.add(data);
        super.notifyObservers(this.data);
    }

    /**
     * Supprime une donnée de la collection des données.
     *
     * @param data une donnée
     */
    public void remove(final Data data) {
        this.data.remove(data);

        super.notifyObservers(this.data);
    }

    /**
     * Retourne la longueur de l'entête CSV.
     *
     * @return la longueur de l'entête CSV
     */
    public int getLengthHeader() {
        return lengthHeader;
    }

    /**
     * Renvoie la valeur minimale ou maximale d'une colonne spécifiée par son en-tête.
     *
     * @param header l'en-tête de la colonne
     * @param max    un booléen indiquant si l'on souhaite la valeur maximale (true) ou minimale (false)
     * @return la valeur minimale ou maximale de la colonne spécifiée
     */
    public double getValue(final String header, final boolean max) {
        double value ;
        if (max) {
            value = Double.MIN_VALUE;
        } else {
            value = Double.MAX_VALUE;
        }
        for (final Data d : this.data) {
            if (d.getNumericData().containsKey(header)) {
                if (max) {
                    if (d.getNumericData().get(header) > value) {
                        value = d.getNumericData().get(header);
                    }
                } else {
                    if (d.getNumericData().get(header) < value) {
                        value = d.getNumericData().get(header);
                    }
                }
            }
        }
        return value;
    }


    /**
     * Normalise une valeur numérique en utilisant la formule de normalisation.
     *
     * @param data la valeur à normaliser
     * @param min  la valeur minimale de la plage
     * @param max  la valeur maximale de la plage
     * @return la valeur normalisée
     */
    public Double normalizeData(final double data, final double min, final double max) {
        return (data - min) / (max - min);
    }

    /**
     * Calcule la distance textuelle normalisée entre deux données.
     *
     * @param data1 la première donnée
     * @param data2 la deuxième donnée
     * @return la distance textuelle normalisée
     */
    public Double normalizeTextData(final Data data1, final Data data2) {
        double distance = 0;
        for (final String key : data1.getTextData().keySet()) {
            if (data1.getTextData().containsKey(key) && data2.getTextData().containsKey(key)) {
                if (!data1.getTextData().get(key).equals(data2.getTextData().get(key))) {
                    distance += 1;
                }
            }
        }
        return distance;
    }

    /**
     * Normalise les données numériques de la collection `data`.
     * Pour chaque en-tête CSV, calcule les valeurs minimales et maximales,
     * puis applique la normalisation à chaque donnée numérique correspondante.
     */
    public void normalize() {
        for (final Data d : data) {
            normalize(d);
        }
    }

    /**
     * Normalise les données numériques d'une donnée spécifique.
     * Pour chaque en-tête CSV, calcule les valeurs minimales et maximales,
     * puis applique la normalisation à chaque donnée numérique correspondante.
     *
     * @param data la donnée à normaliser
     */
    public void normalize(final Data data) {
        for (final String header : csvHeader) {
            final double min = this.getValue(header, false);
            final double max = this.getValue(header, true);
            if (data.getNumericData().containsKey(header)) {
                data.getNormalizedData().put(header, this.normalizeData(data.getNumericData().get(header), min, max));
            }
        }
    }

    /**
     * Classifie une donnée en utilisant l'algorithme des k plus proches voisins (k-NN).
     *
     * @param d          la donnée à classifier
     * @param k          le nombre de voisins à considérer
     * @param classif    l'attribut de classification
     * @param manhattan  un booléen indiquant si l'on utilise la distance de Manhattan (true) ou la distance euclidienne (false)
     * @throws Exception si une erreur survient lors de la classification
     */
    public void classifData(final Data d, final int k, final String classif, final boolean manhattan) throws Exception {
        this.normalize();

        final ArrayList<Data> dataList = new ArrayList<>(this.getData());
        dataList.remove(d);

        this.normalize(d);

        final HashMap<Data, Double> distances = new HashMap<>();
        for (final Data element : dataList) {
            double distance = 0;
            for (final String value : element.getNormalizedData().keySet()) {
                if (manhattan) {
                    distance += Math.abs(d.getNormalizedData().get(value) - element.getNormalizedData().get(value));
                } else {
                    distance += Math.pow(d.getNormalizedData().get(value) - element.getNormalizedData().get(value), 2);
                }
            }
            distance += this.normalizeTextData(d, element);
            distances.put(element, distance);
        }

        final Stream<Map.Entry<Data, Double>> sorted = distances.entrySet().stream().sorted(Map.Entry.comparingByValue());

        final List<Data> nearest = new ArrayList<>();
        sorted.limit(k).forEach(e -> nearest.add(e.getKey()));

        final Map<String, Integer> counts = new HashMap<>();
        for (int x = 0; x < k; x++) {
            final String value = nearest.get(x).getTextData().get(classif);
            if (counts.containsKey(value)) {
                counts.put(value, counts.get(value) + 1);
            } else {
                counts.put(value, 1);
            }
        }

        final HashMap<String, String> answer = new HashMap<>();
        answer.put(classif, counts.entrySet().stream().max(Map.Entry.comparingByValue()).get().getKey());
        d.getTextData().put((String) answer.keySet().toArray()[0], answer.get(answer.keySet().toArray()[0]));
    }

    /**
     * Retourne la collection de données.
     *
     * @return une collection de données
     */
    public Collection<Data> getData() {
        return data;
    }

    /**
     * Importer le fichier CSV et son entête dans la plateforme
     *
     * @param file fichier CSV
     */
    public void setCsvData(final File file) {
        this.csvName = file.getName().replace(".csv", "");
        this.csvHeader.addAll(CsvUtils.loadCsvHeader(file));

        this.data.addAll(Objects.requireNonNull(CsvUtils.loadData(file)));
        lengthHeader = CsvUtils.length;
        System.out.println(this.getCsvHeader());

        notifyObservers();
    }

    /**
     * Retourne l'entête du fichier CSV.
     *
     * @return un tableau de chaînes de caractères représentant l'entête du fichier CSV
     */
    public Collection<String> getCsvHeader() {
        return csvHeader;
    }

    public String getCsvName() {
        return csvName;
    }
}
