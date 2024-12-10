package model;


import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Robustesse {
    public static Plateforme p = new Plateforme();
    static int k = 1;
    static String classify = "type1";

    public static double validationCroise(int numero) {
        p.setCsvData(new File("res/pokemon.csv"));

        List<Data> tier1 = new ArrayList<>();
        List<Data> tier2 = new ArrayList<>();
        List<Data> tier3 = new ArrayList<>();

        splitDataIntoTiers(tier1, tier2, tier3);

        List<Data> verif = cloneData(getTierByNumber(numero, tier1, tier2, tier3));
        List<Data> test = new ArrayList<>(getTierByNumber(numero,tier1,tier2,tier3));

        updatePlatformData(numero, tier1, tier2, tier3);

        setClassifyToNull(test);

        List<Data> trie = classifyData(test);

        return calculateSuccessRate(trie, verif);
    }

    private static void splitDataIntoTiers(List<Data> tier1, List<Data> tier2, List<Data> tier3) {
        int taille = p.getData().size();
        int j = 0;
        for (Data d : p.getData()) {
            if (j < taille / 3) {
                tier1.add(d);
            } else if (j < (taille / 3 * 2)) {
                tier2.add(d);
            } else {
                tier3.add(d);
            }
            j++;
        }
        p.getData().clear();
    }

    private static List<Data> getTierByNumber(int numero, List<Data> tier1, List<Data> tier2, List<Data> tier3) {
        switch (numero) {
            case 1:
                return tier1;
            case 2:
                return tier2;
            case 3:
            default:
                return tier3;
        }
        }

    private static List<Data> cloneData(List<Data> tier) {
        List<Data> clonedData = new ArrayList<>();
        for (Data d : tier) {
            Data copiedData = new Data(
                    new HashMap<>(d.getNumericData()),
                    new HashMap<>(d.getTextData())
            );
            clonedData.add(copiedData);
        }
        return clonedData;
    }

    private static void updatePlatformData(int numero, List<Data> tier1, List<Data> tier2, List<Data> tier3) {
        if (numero == 1) {
            p.getData().addAll(tier3);
            p.getData().addAll(tier2);
        } else if (numero == 2) {
            p.getData().addAll(tier3);
            p.getData().addAll(tier1);
        } else {
            p.getData().addAll(tier2);
            p.getData().addAll(tier1);
        }
    }

    private static void setClassifyToNull(List<Data> tier) {
        for (Data d : tier) {
            d.getTextData().put(classify, "NULL");
        }
    }

    private static List<Data> classifyData(List<Data> test) {
        List<Data> trie = new ArrayList<>();
        for (Data d : test) {
            try {
                p.classifData(d, k, classify, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            trie.add(d);
        }
        return trie;
    }

    private static double calculateSuccessRate(List<Data> trie, List<Data> verif) {
        int cpt = 0;
        for (int i = 0; i < trie.size(); i++) {
            if (trie.get(i).getTextData().get(classify).equals(verif.get(i).getTextData().get(classify))) {
                cpt++;
            }
        }
        return (((double) cpt / trie.size())) * 100;
    }

    public static void main(String[] args) {
//k=6
        k = 7;
        System.out.println("Debut du test de robustesse (durée 40sec");
        double total = 0;
        p = new Plateforme();
        total += validationCroise(1);
        p.getData().clear();
        total += validationCroise(2);
        p.getData().clear();
        total += validationCroise(3);

        System.out.println(  "Le taux de réussite est de -> " + total / 3+"% ");





    }
}
