package org.talend.dataquality.semantic.index.utils;

import static org.talend.dataquality.semantic.api.CategoryRegistryManager.DICTIONARY_SUBFOLDER_NAME;
import static org.talend.dataquality.semantic.api.CategoryRegistryManager.KEYWORD_SUBFOLDER_NAME;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.talend.dataquality.semantic.api.DictionaryUtils;
import org.talend.dataquality.semantic.index.DictionarySearcher;
import org.talend.dataquality.semantic.index.utils.optimizer.CategoryOptimizer;

/**
 * @deprecated DO NOT run this class directly, because the generated DD indexes will not contain catId and docId.
 */
public class SemanticDictionaryGenerator {

    protected static final String DD_PATH = "src/main/resources/" + DICTIONARY_SUBFOLDER_NAME + "/";

    protected static final String KW_PATH = "src/main/resources/" + KEYWORD_SUBFOLDER_NAME + "/";

    protected static Pattern SPLITTER = Pattern.compile("\\|");

    protected Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

    protected static Set<String> STOP_WORDS = new HashSet<String>(
            Arrays.asList("yes", "no", "y", "o", "n", "oui", "non", "true", "false", "vrai", "faux", "null"));

    private void generateDictionaryForSpec(DictionaryGenerationSpec spec, IndexWriter writer) throws IOException {

        System.out.println("-------------------" + spec.name() + "---------------------");
        // load CSV file
        Reader reader = new FileReader(SemanticDictionaryGenerator.class.getResource(spec.getSourceFile()).getPath());
        CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(spec.getCsvConfig().getDelimiter());
        if (spec.getCsvConfig().isWithHeader()) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        // collect values
        Iterable<CSVRecord> records = csvFormat.parse(reader);
        List<Set<String>> valueSetList = getDictionaryForCategory(records, spec);

        int countCategory = 0;
        for (Set<String> valueSet : valueSetList) {
            writer.addDocument(generateDocument(spec.getCategoryName(), valueSet));
            countCategory++;
        }
        System.out.println("Total document count: " + countCategory + "\nExamples:");
        Iterator<Set<String>> it = valueSetList.iterator();
        int count = 0;
        while (it.hasNext() && count < 10) {
            System.out.println("- " + it.next());
            count++;
        }

        reader.close();
    }

    public static List<Set<String>> getDictionaryForCategory(Iterable<CSVRecord> records, DictionaryGenerationSpec spec) {
        List<Set<String>> results = new ArrayList<Set<String>>();
        final int[] columnsToIndex = spec.getColumnsToIndex();
        final CategoryOptimizer optimizer = spec.getOptimizer();
        Set<String> existingValuesOfCategory = new HashSet<String>();
        int ignoredCount = 0;

        for (CSVRecord record : records) {

            List<String> allInputColumns = new ArrayList<String>();
            if (DictionaryGenerationSpec.CITY.equals(spec)) { // For CITY index, take all columns
                for (int col = 0; col < record.size(); col++) {
                    final String colValue = record.get(col);
                    final String[] splits = SPLITTER.split(colValue);
                    for (String syn : splits) {
                        if (syn != null && syn.trim().length() > 0) {
                            allInputColumns.add(syn.trim());
                        }
                    }
                }
            } else {
                for (int col : columnsToIndex) {
                    if (col < record.size()) { // sometimes, the value of last column can be missing.
                        final String colValue = record.get(col);
                        final String[] splits = SPLITTER.split(colValue);
                        for (String value : splits) {
                            if (value != null && value.trim().length() > 0) {
                                allInputColumns.add(value.trim());
                            }
                        }
                    }
                }
            }

            if (optimizer != null) {
                allInputColumns = new ArrayList<String>(optimizer.optimize(allInputColumns.toArray(new String[0])));
            }

            Set<String> valuesInRecord = new HashSet<String>();
            for (String value : allInputColumns) {
                if (STOP_WORDS.contains(value.toLowerCase()) //
                        && (DictionaryGenerationSpec.COMPANY.equals(spec) //
                                || DictionaryGenerationSpec.FIRST_NAME.equals(spec) //
                                || DictionaryGenerationSpec.LAST_NAME.equals(spec) //
                        )) {
                    System.out.println("[" + value + "] is exclued from the category [" + spec.getCategoryName() + "]");
                    continue;
                }
                if (!existingValuesOfCategory.contains(value.toLowerCase())) {
                    valuesInRecord.add(value);
                    existingValuesOfCategory.add(value.toLowerCase());
                } else {
                    ignoredCount++;
                }
            }
            if (valuesInRecord.size() > 0) { // at least one synonym
                results.add(valuesInRecord);
            }
        }
        System.out.println("Ignored value count: " + ignoredCount);
        return results;

    }

    /**
     * generate a document.
     *
     * @param word
     * @param values
     * @return
     */
    Document generateDocument(String word, Set<String> values) {
        String tempWord = word.trim();
        Document doc = new Document();

        Field wordTermField = new StringField(DictionarySearcher.F_WORD, tempWord, Field.Store.YES);
        doc.add(wordTermField);
        for (String value : filterValues(word, values)) {
            List<String> tokens = DictionarySearcher.getTokensFromAnalyzer(value);
            doc.add(new StringField(DictionarySearcher.F_SYNTERM, StringUtils.join(tokens, ' '), Field.Store.NO));
            doc.add(new Field(DictionarySearcher.F_RAW, value, DictionaryUtils.FIELD_TYPE_RAW_VALUE));
        }
        return doc;
    }

    /**
     * generate a document.
     *
     * @param word
     * @param values
     * @return
     */
    Set<String> filterValues(String word, Set<String> values) {
        String tempWord = word.trim();
        Set<String> list = new HashSet<>();

        for (String value : values) {
            if (value != null) {
                boolean containsControlChars = false;
                for (char c : value.toCharArray()) {
                    if (Character.isISOControl(c)) {
                        containsControlChars = true;
                    }
                }
                if (containsControlChars) {
                    System.out.println("The value [" + value
                            + "] contains at least one ISO control character and is not added to the index of " + word + ".");
                    continue;
                }
                value = value.trim();
                if ("CITY".equals(tempWord)) { // ignore city abbreviations
                    if (value.length() == 3 && value.charAt(0) >= 'A' && value.charAt(0) <= 'Z'//
                            && value.charAt(1) >= 'A' && value.charAt(1) <= 'Z'//
                            && value.charAt(2) >= 'A' && value.charAt(2) <= 'Z') {
                        continue;
                    }
                }
                if (value.length() > 0 && !value.equals(tempWord)) {
                    list.add(value);
                }
            }
        }
        return list;
    }

    private void generate(GenerationType type, String path) {
        try {
            FileUtils.deleteDirectory(new File(path));
            FSDirectory outputDir = FSDirectory.open(new File(path));
            IndexWriterConfig writerConfig = new IndexWriterConfig(Version.LATEST, analyzer);
            IndexWriter writer = new IndexWriter(outputDir, writerConfig);
            for (DictionaryGenerationSpec spec : DictionaryGenerationSpec.values()) {
                if (spec.getGenerationType().equals(type)) {
                    try {
                        generateDictionaryForSpec(spec, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            writer.commit();
            writer.close();
            outputDir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        final String resourcePath = SemanticDictionaryGenerator.class.getResource(".").getFile();
        final String projectRoot = new File(resourcePath).getParentFile().getParentFile().getParentFile().getParentFile()
                .getParentFile().getParentFile().getParentFile().getParentFile().getPath() + File.separator;
        SemanticDictionaryGenerator generator = new SemanticDictionaryGenerator();
        generator.generate(GenerationType.DICTIONARY, projectRoot + DD_PATH);
        generator.generate(GenerationType.KEYWORD, projectRoot + KW_PATH);
    }

}
