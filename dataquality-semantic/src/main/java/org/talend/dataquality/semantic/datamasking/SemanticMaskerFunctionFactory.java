// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.semantic.datamasking;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.dataquality.datamasking.FunctionFactory;
import org.talend.dataquality.datamasking.FunctionType;
import org.talend.dataquality.datamasking.TypeTester;
import org.talend.dataquality.datamasking.functions.DateVariance;
import org.talend.dataquality.datamasking.functions.Function;
import org.talend.dataquality.datamasking.semantic.DateFunctionAdapter;
import org.talend.dataquality.datamasking.semantic.FluctuateNumericString;
import org.talend.dataquality.datamasking.semantic.GenerateFromRegex;
import org.talend.dataquality.datamasking.semantic.ReplaceCharactersWithGeneration;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.model.CategoryType;

import java.util.Date;
import java.util.List;
import java.util.Random;

public class SemanticMaskerFunctionFactory {

    private static final Logger LOGGER = Logger.getLogger(SemanticMaskerFunctionFactory.class);

    public static Function<String> createMaskerFunctionForSemanticCategory(String semanticCategory, String dataType) {
        return createMaskerFunctionForSemanticCategory(semanticCategory, dataType, null);
    }

    @SuppressWarnings("unchecked")
    public static Function<String> createMaskerFunctionForSemanticCategory(String semanticCategory, String dataType,
            List<String> params) {
        Function<String> function = null;
        final MaskableCategoryEnum cat = MaskableCategoryEnum.getCategoryById(semanticCategory);
        if (cat != null) {
            try {
                function = (Function<String>) cat.getFunctionType().getClazz().newInstance();
                if (cat.getParameter() == null) {
                    function.parse("X", true, null); //$NON-NLS-1$
                } else {
                    function.parse(cat.getParameter(), true, null);
                }
                function.setKeepFormat(true);
            } catch (InstantiationException e) {
                LOGGER.debug(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        org.talend.dataquality.semantic.model.DQCategory category = CategoryRegistryManager.getInstance()
                .getCategoryMetadataByName(semanticCategory);
        if (function == null && "string".equals(dataType)) {
            if (category != null && CategoryType.DICT.equals(category.getType())) {
                function = new GenerateFromDictionaries();
                function.parse(semanticCategory, true, null);
            }
        }

        if (category != null && CategoryType.REGEX.equals(category.getType())) {
            UserDefinedClassifier userDefinedClassifier = new UserDefinedClassifier();
            String patternString = userDefinedClassifier.getPatternStringByCategoryId(category.getId());
            if (GenerateFromRegex.isValidPattern(patternString)) {
                function = new GenerateFromRegex();
                function.parse(patternString, true, new Random(100l));
            }
        }

        if (function == null) {
            switch (dataType) {
            case "numeric":
            case "integer":
            case "float":
            case "double":
            case "decimal":
                function = new FluctuateNumericString();
                function.parse("10", true, null);
                break;
            case "date":
                DateVariance df = new DateVariance();
                function = adaptForDateFunction(params, df, "61");
                break;
            case "string":
                function = new ReplaceCharactersWithGeneration();
                function.parse("X", true, null);
                break;
            default:
                break;

            }
        }
        if (function == null) {
            throw new IllegalArgumentException("No masking function available for the current column! SemanticCategory: "
                    + semanticCategory + " DataType: " + dataType);
        }
        return function;
    }

    private static Function<String> adaptForDateFunction(List<String> params, Function<Date> functionToAdapt, String extraParam) {
        functionToAdapt.parse(extraParam, true, null);
        Function<String> function = new DateFunctionAdapter(functionToAdapt, params);
        return function;
    }

    public static Function<String> getMaskerFunctionByFunctionName(String functionName, String dataType, String semanticCategory,
            String param) {
        FunctionFactory factory = new FunctionFactory();
        TypeTester tester = new TypeTester();
        Function<String> function = null;
        try {
            if (FunctionType.KEEP_YEAR.name().equals(functionName)) {
                function = adaptForDateFunction(null,
                        (Function<Date>) factory.getFunction(FunctionType.valueOf(functionName), tester.getTypeByName(dataType)),
                        param);
            } else {
                function = (Function<String>) factory.getFunction(FunctionType.valueOf(functionName),
                        tester.getTypeByName(dataType));
            }
            if (StringUtils.isNotEmpty(param)) {
                function.parse(param, true, null);
            }
            function.setKeepFormat(true);
            function.setKeepEmpty(true);

        } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                    "No masking function available for the current column!  " + " DataType: " + dataType);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                    "No masking function available for the current column!  " + " DataType: " + dataType);
        }

        return function;
    }
}
