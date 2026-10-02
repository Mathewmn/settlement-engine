package com.enterprise.settlement;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.enterprise.settlement", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ArchitectureArchUnitTest {

    @ArchTest
    public static final ArchRule domainMustNotDependOnInfrastructureOrFrameworks =
        noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..api..", "org.springframework..");
}
