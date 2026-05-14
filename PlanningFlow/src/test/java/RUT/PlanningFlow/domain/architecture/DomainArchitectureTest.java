package RUT.PlanningFlow.domain.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "RUT.PlanningFlow.domain")
class DomainArchitectureTest {

    @ArchTest
    static final ArchRule domain_does_not_depend_on_infrastructure_frameworks =
            noClasses()
                    .that().resideInAPackage("RUT.PlanningFlow.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "jakarta.validation..",
                            "RUT.PlanningFlow.adapter..",
                            "RUT.PlanningFlow.application.."
                    );

    @ArchTest
    static final ArchRule domain_does_not_depend_on_web_api =
            noClasses()
                    .that().resideInAPackage("RUT.PlanningFlow.domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.data.."
                    );
}
