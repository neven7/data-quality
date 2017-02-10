package org.talend.dataquality.semantic.classifier.custom;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.junit.Test;

public class InvalidRegexCategorizerTest {

    @Test
    public void testReadJsonFileWithInvalidRegex() throws IOException, URISyntaxException {
        InputStream uri = InvalidRegexCategorizerTest.class.getResourceAsStream("invalid_regex_categorizer.json");
        try {
            UserDefinedClassifier userDefinedClassifier = UDCategorySerDeser.readJsonFile(uri);
            assertNotNull(userDefinedClassifier);
            int nbCat = userDefinedClassifier.getClassifiers().size();
            assertEquals("Expected to read at least 0 category but only get " + nbCat, 1, nbCat); //$NON-NLS-1$
            assertFalse("Any data should be invalid when the pattern is invalid",
                    userDefinedClassifier.classify("AZERTY").contains("CATEGORY_WITH_INVALID_REGEX"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
