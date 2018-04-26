// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
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

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.talend.dataquality.datamasking.functions.Function;
import org.talend.dataquality.semantic.CategoryRegistryManagerAbstract;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.CustomDictionaryHolder;

@PrepareForTest({ CustomDictionaryHolder.class, CategoryRegistryManager.class })
public class SemanticMaskerFunctionFactoryTest extends CategoryRegistryManagerAbstract {

    @Test
    public void createMaskerFunctionForSemanticCategory() {
        Function<String> function = SemanticMaskerFunctionFactory.createMaskerFunctionForSemanticCategory("DE_POSTAL_CODE", null);
        Assert.assertEquals("ReplaceCharactersWithGeneration", function.getClass().getSimpleName());
    }

    @Test
    public void createDecimalMaskerFunctionForSemanticCategory() {
        Function<String> function = SemanticMaskerFunctionFactory.createMaskerFunctionForSemanticCategory("INVALID_NAME",
                "decimal");
        Assert.assertEquals("FluctuateNumericString", function.getClass().getSimpleName());
    }

    @Test
    public void createDateMaskerFunctionForSemanticCategory() {
        Function<String> function = SemanticMaskerFunctionFactory.createMaskerFunctionForSemanticCategory("INVALID_NAME", "date");
        Assert.assertEquals("DateFunctionAdapter", function.getClass().getSimpleName());
    }

    @Test
    public void createStringMaskerFunctionForSemanticCategory() {
        Function<String> function = SemanticMaskerFunctionFactory.createMaskerFunctionForSemanticCategory("INVALID_NAME",
                "string");
        Assert.assertEquals("ReplaceCharactersWithGeneration", function.getClass().getSimpleName());
    }

}
