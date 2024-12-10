package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlateformeTest {

    private Plateforme plateforme;
    private Data data1;
    private Data data2;

    @BeforeEach
    void setUp() {
        plateforme = new Plateforme();

        HashMap<String, Double> numericData1 = new HashMap<>();
        numericData1.put("valeur", 10.0);

        HashMap<String, String> textData1 = new HashMap<>();
        textData1.put("nom", "Data1");

        data1 = new Data(numericData1, textData1);

        HashMap<String, Double> numericData2 = new HashMap<>();
        numericData2.put("valeur", 20.0);

        HashMap<String, String> textData2 = new HashMap<>();
        textData2.put("nom", "Data2");

        data2 = new Data(numericData2, textData2);
    }

    @Test
    void testAddAndNotifyObservers() {
        plateforme.add(data1);
        assertTrue(plateforme.getData().contains(data1));
    }

    @Test
    void testRemoveAndNotifyObservers() {
        plateforme.add(data1);
        plateforme.remove(data1);
        assertFalse(plateforme.getData().contains(data1));
    }

    @Test
    void testGetvaleurMax() {
        plateforme.add(data1);
        plateforme.add(data2);
        double maxvaleur = plateforme.getValue("valeur", true);
        assertEquals(20.0, maxvaleur);
    }

    @Test
    void testGetvaleurMin() {
        plateforme.add(data1);
        plateforme.add(data2);
        double minvaleur = plateforme.getValue("valeur", false);
        assertEquals(10.0, minvaleur);
    }

    @Test
    void testNormalizeData() {
        double normalizedvaleur = plateforme.normalizeData(15.0, 10.0, 20.0);
        assertEquals(0.5, normalizedvaleur);
    }

    @Test
    void testNormalize() {
        plateforme.add(data1);
        plateforme.add(data2);
        plateforme.getCsvHeader().add("valeur");

        plateforme.normalize();

        Map<String, Double> normalizedData1 = data1.getNormalizedData();
        Map<String, Double> normalizedData2 = data2.getNormalizedData();

        assertEquals(0.0, normalizedData1.get("valeur"));
        assertEquals(1.0, normalizedData2.get("valeur"));
    }

    @Test
    void testNormalizeTextData() {
        HashMap<String, String> textData2 = new HashMap<>();
        textData2.put("nom", "Data5");
        textData2.put("autre", "Différent");

        Data data3 = new Data(new HashMap<>(), textData2);
        double distance = plateforme.normalizeTextData(data1, data3);

        assertEquals(1.0, distance);
    }

    @Test
    void testClassifDataManhattan() throws Exception {
        plateforme.add(data1);
        plateforme.add(data2);

        plateforme.classifData(data1, 1,"nom", true);

        assertEquals("Data2", data1.getTextData().get("nom"));
    }

    @Test
    void testClassifDataEuclidean() throws Exception {
        plateforme.add(data1);
        plateforme.add(data2);

        plateforme.classifData(data1, 1,"nom", false);

        assertEquals("Data2", data1.getTextData().get("nom"));
    }
}
