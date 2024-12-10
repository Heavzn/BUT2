package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CsvUtils {
    public static int length = 0;

    /**
     * Charge l'en-tête du fichier CSV spécifié.
     *
     * @param file le fichier CSV à charger
     */
    public static List<String> loadCsvHeader(final File file) {
        final List<String> res=new ArrayList<>();
        int length = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            final String[] header = br.readLine().split("[,\t]");
            final String[] row = br.readLine().split("[,\t]");

            for (int columnIdx = 0; columnIdx < header.length; columnIdx++) {
                final String columnName = header[columnIdx].replaceAll("\"", "");
                try {
                    Double.parseDouble(row[columnIdx]);
                    res.add(columnName);
                } catch (NumberFormatException e) {
                    Data.getCanClassifyBy().put(columnName, getColumnPossibleValues(file, columnIdx));
                }
                Data.getHeader().add(columnName);
                length++;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        CsvUtils.length = length;
        return res;
    }


    /**
     * Charge les données du fichier CSV spécifié.
     *
     * @param file le fichier CSV à charger
     */
    public static List<Data> loadData(final File file) {
        final List<Data> datas=new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            final List<String> header = Arrays.stream(br.readLine().split("[,\t]")).toList();
            while (br.ready()) {
                final String[] row = br.readLine().replaceAll("\"", "").split("[,\t]");
                final HashMap<String, Double> numericData = new HashMap<>();
                final HashMap<String, String> textData = new HashMap<>();

                for (final String _columnName : header) {
                    final String columnName = _columnName.replaceAll("\"", "");
                    final int idx = header.indexOf(_columnName);

                    try {
                        numericData.put(columnName, Double.valueOf(row[idx]));
                    } catch (NumberFormatException e) {
                        textData.put(columnName, row[idx]);
                    }
                }
                datas.add(new Data(numericData, textData));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return datas;
    }

    /**
     * Récupère les valeurs possibles d'une colonne spécifiée dans un fichier CSV.
     *
     * @param file      le fichier CSV à analyser
     * @param columnIdx l'index de la colonne dont les valeurs possibles doivent être récupérées
     * @return un ensemble de valeurs possibles pour la colonne spécifiée
     */
    public static Set<String> getColumnPossibleValues(final File file, final int columnIdx) {
        final Set<String> possibleValues = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();
            while (br.ready()) {
                final String possibleValue = br.readLine().split("[,\t]")[columnIdx].replaceAll("\"", "");
                possibleValues.add(possibleValue);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return possibleValues;
    }
}
