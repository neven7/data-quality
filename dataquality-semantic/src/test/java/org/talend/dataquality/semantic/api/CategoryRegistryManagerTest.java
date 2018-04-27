package org.talend.dataquality.semantic.api;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;
import org.talend.dataquality.semantic.CategoryRegistryManagerAbstract;
import org.talend.dataquality.semantic.classifier.ISubCategory;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.LuceneIndex;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.model.DQDocument;

public class CategoryRegistryManagerTest extends CategoryRegistryManagerAbstract {

    @Test
    public void testGetRegexClassifier() throws IOException, URISyntaxException {
        CategoryRegistryManager crm = CategoryRegistryManager.getInstance();
        final UserDefinedClassifier udc = crm.getRegexClassifier();
        final Set<ISubCategory> classifiers = udc.getClassifiers();
        assertEquals("Unexpected size of classifiers", 47, classifiers.size());

        // getRegexClassifier before deletion
        UserDefinedClassifier userDefinedClassifier = crm.getCustomDictionaryHolder().getRegexClassifier();
        assertEquals("Unexpected size of classifiers", 47, userDefinedClassifier.getClassifiers().size());

        DQCategory regexCategory = crm.getCategoryMetadataById(SemanticCategoryEnum.EMAIL.getTechnicalId());
        regexCategory.setCreator("TALEND");
        crm.getCustomDictionaryHolder().deleteCategory(regexCategory);

        crm.reloadCategoriesFromRegistry();
        userDefinedClassifier = crm.getCustomDictionaryHolder().getRegexClassifier();
        // getRegexClassifier after deletion
        assertEquals("Unexpected size of classifiers", 46, userDefinedClassifier.getClassifiers().size());

    }

    @Test
    public void testFindMostSimilarValue() {
        String result = CategoryRegistryManager.getInstance().findMostSimilarValue("Franc", "COUNTRY", 0.8);
        assertEquals("France", result);
    }

    @Test
    public void testFindMostSimilarValue_ValueIsNull() {
        String result = CategoryRegistryManager.getInstance().findMostSimilarValue(null, "COUNTRY", 0.8);
        assertEquals(null, result);
    }

    @Test
    public void testFindMostSimilarValue_CategoryIsNull() {
        String result = CategoryRegistryManager.getInstance().findMostSimilarValue("the input", null, 0.8);
        assertEquals("the input", result);
    }

    @Test
    public void testFindMostSimilarValue_ValueNotExist() {
        String result = CategoryRegistryManager.getInstance().findMostSimilarValue("Fran", "COUNTRY", 0.8);
        assertEquals("Fran", result);
    }

    @Test
    public void testFindMostSimilarValue_CategoryNotExist() {
        String result = CategoryRegistryManager.getInstance().findMostSimilarValue("Fran", "NonExistingCategory", 0.8);
        assertEquals("Fran", result);
    }

    @Test
    public void testFindMostSimilarValueWithCustomDataDict() throws IOException {
        CustomDictionaryHolder holder = CategoryRegistryManager.getInstance().getCustomDictionaryHolder();

        DQCategory answerCategory = holder.getMetadata().get(SemanticCategoryEnum.ANSWER.getTechnicalId());
        DQCategory categoryClone = SerializationUtils.clone(answerCategory); // make a clone instead of modifying the shared
                                                                             // category metadata
        categoryClone.setModified(true);
        holder.updateCategory(categoryClone);

        DQDocument newDoc = new DQDocument();
        newDoc.setCategory(categoryClone);
        newDoc.setId("the_doc_id");
        newDoc.setValues(new HashSet<>(Arrays.asList("true", "false")));
        holder.addDataDictDocuments(Collections.singletonList(newDoc));

        String result = CategoryRegistryManager.getInstance().findMostSimilarValue("TRUEL", SemanticCategoryEnum.ANSWER.name(),
                0.8);
        assertEquals("true", result);
    }

    @Test
    public void testGetLuceneIndex() {
        String categoryName = "COUNTRY";
        LuceneIndex luceneIndex = CategoryRegistryManager.getInstance().getLuceneIndex(categoryName);
        Assert.assertNotNull(luceneIndex);
    }

}
