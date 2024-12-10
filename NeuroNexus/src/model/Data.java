package model;

import java.util.*;

public class Data {
    private static final ArrayList<String> HEADER = new ArrayList<>();
    private static final Map<String, Set<String>> CAN_CLASSIFY_BY = new HashMap<>();
    private final HashMap<String, Double> numericData;
    private final HashMap<String, Double> normalizedData;
    private final HashMap<String, String> textData;


    public Data(final HashMap<String, Double> numericData, final HashMap<String, String> textData) {
        this.numericData = numericData;
        this.textData = textData;
        this.normalizedData = new HashMap<>();
    }

    /**
     * Retourne la carte statique `canClassifyBy`.
     *
     * @return une Map contenant les classifications possibles
     */
    public static Map<String, Set<String>> getCanClassifyBy() {
        return CAN_CLASSIFY_BY;
    }

    /**
     * Retourne l'en-tête statique.
     *
     * @return une ArrayList contenant l'en-tête
     */
    public static List<String> getHeader() {
        return HEADER;
    }

    /**
     * Retourne les données numériques.
     *
     * @return une HashMap contenant les données numériques
     */

    public HashMap<String, Double> getNumericData() {
        return numericData;
    }

    /**
     * Retourne les données textuelles.
     *
     * @return une HashMap contenant les données textuelles
     */
    public HashMap<String, String> getTextData() {
        return textData;
    }

    public HashMap<String, Double> getNormalizedData() {
        return normalizedData;
    }

    /**
     * Retourne une représentation sous forme de chaîne de caractères des données.
     *
     * @return une chaîne de caractères représentant les données
     */

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();

        for (final String value : numericData.keySet()) {
            res.append(value).append(" : ").append(numericData.get(value)).append(" - ");
        }

        for (final String value : textData.keySet()) {
            res.append(value).append(" : ").append(textData.get(value)).append(" - ");
        }

        return res.toString();

    }


}
