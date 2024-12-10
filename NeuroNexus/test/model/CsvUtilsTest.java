package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import view.MainApp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CsvUtilsTest {

    private File testFile;

    @BeforeEach
    void setUp() throws IOException {
        testFile = File.createTempFile("test", ".csv");

        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Nom,Age,Ville\n");
            writer.write("\"Alice\",30,\"Paris\"\n");
            writer.write("\"Bob\",25,\"London\"\n");
            writer.write("\"Charlie\",35,\"New York\"\n");
        }

        testFile.deleteOnExit();
    }

    @Test
    void testLoadCsvHeader() {
        CsvUtils.loadCsvHeader(testFile);

        assertEquals(3, CsvUtils.length, "La longueur des colonnes doit être correcte");
        assertTrue(MainApp.plateforme.getCsvHeader().contains("Age"), "L'en-tête numérique doit être détecté");
        assertTrue(Data.getHeader().contains("Nom"), "L'en-tête général doit inclure toutes les colonnes");
        assertTrue(Data.getCanClassifyBy().containsKey("Ville"), "La colonne textuelle doit être catégorisée");
    }

    @Test
    void testLoadData() {
        MainApp.plateforme.getCsvHeader().add("Age");

        List<Data> datas = CsvUtils.loadData(testFile);

        assertNotNull(datas, "La liste de données ne doit pas être nulle");
        assertEquals(3, datas.size(), "Le nombre de lignes chargées doit être correct");
        assertEquals(30.0, datas.get(0).getNumericData().get("Age"), "La donnée numérique doit être correctement parsée");
        assertEquals("Paris", datas.get(0).getTextData().get("Ville"), "La donnée textuelle doit être correctement parsée");
    }

    @Test
    void testGetColumnPossibleValues() {
        Set<String> possibleValues = CsvUtils.getColumnPossibleValues(testFile, 2);

        assertNotNull(possibleValues, "Le set de valeurs possibles ne doit pas être nul");
        assertEquals(3, possibleValues.size(), "Le nombre de valeurs uniques doit être correct");
        assertTrue(possibleValues.contains("Paris"), "La valeur 'Paris' doit être dans le set");
        assertTrue(possibleValues.contains("London"), "La valeur 'London' doit être dans le set");
        assertTrue(possibleValues.contains("New York"), "La valeur 'New York' doit être dans le set");
    }

    @Test
    void testLoadCsvHeaderWithInvalidFile() {
        File invalidFile = new File("nonexistent.csv");

        assertDoesNotThrow(() -> CsvUtils.loadCsvHeader(invalidFile), "La méthode ne doit pas lever d'exception pour un fichier invalide");
    }

}
