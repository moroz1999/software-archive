package com.archive.backend.architecture;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * Architecture rules from docs/backend.md and docs/value-objects.md.
 */
class ArchitectureTest {

    private static final String BUSINESS_EXCEPTION = "com.archive.backend.shared.domain.error.BusinessException";
    private static final String VALUE_OBJECT = "com.archive.backend.shared.domain.ddd.ValueObject";
    private static final String SHARED_KERNEL = "com.archive.backend.shared.domain";
    private static final Set<String> KIND_PACKAGES = Set.of(
            "exception",
            "exceptions",
            "error",
            "errors",
            "vo",
            "vos",
            "valueobject",
            "valueobjects",
            "entity",
            "entities",
            "model",
            "models",
            "repository",
            "repositories",
            "service",
            "services");

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.archive.backend");

    private static final Set<String> SCALARS = Set.of(
            String.class.getName(),
            UUID.class.getName(),
            BigDecimal.class.getName(),
            BigInteger.class.getName(),
            Integer.class.getName(),
            Long.class.getName(),
            Short.class.getName(),
            Byte.class.getName(),
            Double.class.getName(),
            Float.class.getName(),
            Boolean.class.getName(),
            Character.class.getName(),
            "int",
            "long",
            "short",
            "byte",
            "double",
            "float",
            "boolean",
            "char");

    private static final ArchRule layers = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Web")
            .definedBy("..web..")
            .layer("App")
            .definedBy("..app..")
            .layer("Domain")
            .definedBy("..domain..")
            .layer("Infra")
            .definedBy("..infra..")
            .layer("Read")
            .definedBy("..read..")
            .whereLayer("Web")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("App")
            .mayOnlyBeAccessedByLayers("Web")
            .whereLayer("Infra")
            .mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain")
            .mayOnlyBeAccessedByLayers("Web", "App", "Infra")
            .whereLayer("Read")
            .mayOnlyBeAccessedByLayers("Web", "App", "Infra");

    private static final ArchRule readSideIsFreeOfDomain = noClasses()
            .that()
            .resideInAPackage("..read..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("com.archive.backend..domain..")
            .because("reads use a plain read model, never domain entities (docs/backend.md)");

    private static final ArchRule domainIsFrameworkFree = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat(
                    resideInAnyPackage("org.springframework..", "jakarta..", "org.hibernate..", "com.fasterxml..")
                            .and(DescribedPredicate.not(resideInAPackage("org.springframework.data.domain.."))))
            .because("the domain is pure Java; only Spring Data Page/Pageable are allowed (docs/backend.md)");

    private static final ArchRule jpaEntitiesLiveInInfra = classes()
            .that()
            .areAnnotatedWith(Entity.class)
            .should()
            .resideInAPackage("..infra..")
            .andShould()
            .haveSimpleNameEndingWith("Entity");

    private static final ArchRule springDataRepositoriesLiveInInfra = classes()
            .that()
            .areAssignableTo(Repository.class)
            .should()
            .resideInAPackage("..infra..")
            .andShould()
            .haveSimpleNameEndingWith("JpaRepository");

    private static final ArchRule controllersLiveInWeb = classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideInAPackage("..web..")
            .andShould()
            .haveSimpleNameEndingWith("Controller");

    private static final ArchRule useCasesAreHandlers = classes()
            .that()
            .resideInAPackage("..app..")
            .and()
            .areAnnotatedWith(Service.class)
            .should()
            .haveSimpleNameEndingWith("Handler");

    private static final ArchRule exceptionsAreBusinessExceptions = classes()
            .that()
            .resideInAnyPackage("..domain..", "..app..")
            .and()
            .areAssignableTo(RuntimeException.class)
            .should()
            .beAssignableTo(BUSINESS_EXCEPTION)
            .allowEmptyShould(true)
            .because("use cases and the domain signal failures with business exceptions (docs/error-handling.md)");

    private static final ArchRule valueObjectsAreDomainRecords = classes()
            .that()
            .areAnnotatedWith(VALUE_OBJECT)
            .should(new ArchCondition<JavaClass>("be records, abstract shared value objects or final cases of one") {
                @Override
                public void check(JavaClass type, ConditionEvents events) {
                    boolean record = type.isRecord();
                    boolean abstractShared = type.getModifiers().contains(JavaModifier.ABSTRACT)
                            && type.getPackageName().equals(SHARED_KERNEL);
                    boolean finalCase = type.getModifiers().contains(JavaModifier.FINAL)
                            && type.getRawSuperclass()
                                    .map(s -> s.isAnnotatedWith(VALUE_OBJECT)
                                            && s.getModifiers().contains(JavaModifier.ABSTRACT))
                                    .orElse(false);
                    if (!record && !abstractShared && !finalCase) {
                        events.add(SimpleConditionEvent.violated(
                                type,
                                type.getName()
                                        + " is not a record, an abstract shared value object or a final case of one"));
                    }
                }
            })
            .andShould()
            .resideInAPackage("..domain..")
            .allowEmptyShould(true);

    private static final ArchRule noScalarsInDomainAndCommands = noFields()
            .that(
                    new DescribedPredicate<JavaField>(
                            "are instance fields of domain or app classes other than value objects and exceptions") {
                        @Override
                        public boolean test(JavaField field) {
                            JavaClass owner = field.getOwner();
                            return !field.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC)
                                    && resideInAnyPackage("..domain..", "..app..")
                                            .test(owner)
                                    && !owner.isAnnotatedWith(VALUE_OBJECT)
                                    && !owner.isAssignableTo(Throwable.class);
                        }
                    })
            .should()
            .haveRawType(new DescribedPredicate<JavaClass>("a scalar type") {
                @Override
                public boolean test(JavaClass type) {
                    return SCALARS.contains(type.getName());
                }
            })
            .because(
                    "domain entities, commands and results use value objects; scalars belong to DTOs and persistence (docs/value-objects.md)");

    private static final ArchRule domainIsGroupedByConcept = classes()
            .that(new DescribedPredicate<JavaClass>("reside in a domain package") {
                @Override
                public boolean test(JavaClass type) {
                    return type.getPackageName().contains(".domain.");
                }
            })
            .should(new ArchCondition<JavaClass>("not reside in a subpackage named after a kind of class") {
                @Override
                public void check(JavaClass type, ConditionEvents events) {
                    String name = type.getPackageName();
                    if (name.equals(SHARED_KERNEL + ".error") || name.equals(SHARED_KERNEL + ".ddd")) {
                        return;
                    }
                    String below = name.substring(name.indexOf(".domain.") + ".domain.".length());
                    if (Arrays.stream(below.split("\\.")).anyMatch(KIND_PACKAGES::contains)) {
                        events.add(SimpleConditionEvent.violated(
                                type, type.getName() + " resides in a package named after a kind of class"));
                    }
                }
            })
            .allowEmptyShould(true)
            .because("a domain is split by concept or aggregate, never by kind of class (docs/backend.md)");

    private static final DescribedPredicate<JavaClass> QUERY_HANDLERS = handlersOf("Query");
    private static final DescribedPredicate<JavaClass> COMMAND_HANDLERS = handlersOf("Command");

    private static DescribedPredicate<JavaClass> handlersOf(String kind) {
        return new DescribedPredicate<>("handle a " + kind) {
            @Override
            public boolean test(JavaClass type) {
                return type.getPackageName().contains(".app")
                        && type.getMethods().stream()
                                .anyMatch(method -> method.getName().equals("handle")
                                        && method.getRawParameterTypes().stream()
                                                .anyMatch(parameter -> parameter
                                                        .getSimpleName()
                                                        .endsWith(kind)));
            }
        };
    }

    private static final ArchRule useCaseInputsAreCommandsOrQueries = classes()
            .that()
            .resideInAPackage("..app..")
            .and()
            .areRecords()
            .should()
            .haveSimpleNameEndingWith("Command")
            .orShould()
            .haveSimpleNameEndingWith("Query")
            .allowEmptyShould(true)
            .because("a use case is either a command that changes state or a query that reads (docs/backend.md)");

    private static final ArchRule queriesUseOnlyTheReadSide = noClasses()
            .that(QUERY_HANDLERS)
            .should()
            .dependOnClassesThat(
                    new DescribedPredicate<JavaClass>(
                            "are domain classes other than value objects and business exceptions") {
                        @Override
                        public boolean test(JavaClass type) {
                            return resideInAPackage("com.archive.backend..domain..")
                                            .test(type)
                                    && !type.isAnnotatedWith(VALUE_OBJECT)
                                    && !type.isAssignableTo(BUSINESS_EXCEPTION);
                        }
                    })
            .allowEmptyShould(true)
            .because("a query reads the plain read model, never domain entities (docs/backend.md)");

    private static final ArchRule queriesAreReadOnly = methods()
            .that()
            .areDeclaredInClassesThat(QUERY_HANDLERS)
            .and()
            .haveName("handle")
            .should(new ArchCondition<JavaMethod>("be @Transactional(readOnly = true)") {
                @Override
                public void check(JavaMethod method, ConditionEvents events) {
                    boolean readOnly = method.tryGetAnnotationOfType(Transactional.class)
                            .map(Transactional::readOnly)
                            .orElse(false);
                    if (!readOnly) {
                        events.add(SimpleConditionEvent.violated(
                                method, method.getFullName() + " is not @Transactional(readOnly = true)"));
                    }
                }
            })
            .allowEmptyShould(true);

    private static final ArchRule commandsDoNotUseTheReadSide = noClasses()
            .that(COMMAND_HANDLERS)
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..read..")
            .allowEmptyShould(true)
            .because("a command changes domain entities through their repository (docs/backend.md)");

    @Test
    void useCaseInputsAreCommandsOrQueries() {
        useCaseInputsAreCommandsOrQueries.check(CLASSES);
    }

    @Test
    void queriesUseOnlyTheReadSide() {
        queriesUseOnlyTheReadSide.check(CLASSES);
    }

    @Test
    void queriesAreReadOnly() {
        queriesAreReadOnly.check(CLASSES);
    }

    @Test
    void commandsDoNotUseTheReadSide() {
        commandsDoNotUseTheReadSide.check(CLASSES);
    }

    @Test
    void layers() {
        layers.check(CLASSES);
    }

    @Test
    void domainIsGroupedByConcept() {
        domainIsGroupedByConcept.check(CLASSES);
    }

    @Test
    void readSideIsFreeOfDomain() {
        readSideIsFreeOfDomain.check(CLASSES);
    }

    @Test
    void domainIsFrameworkFree() {
        domainIsFrameworkFree.check(CLASSES);
    }

    @Test
    void jpaEntitiesLiveInInfra() {
        jpaEntitiesLiveInInfra.check(CLASSES);
    }

    @Test
    void springDataRepositoriesLiveInInfra() {
        springDataRepositoriesLiveInInfra.check(CLASSES);
    }

    @Test
    void controllersLiveInWeb() {
        controllersLiveInWeb.check(CLASSES);
    }

    @Test
    void useCasesAreHandlers() {
        useCasesAreHandlers.check(CLASSES);
    }

    @Test
    void exceptionsAreBusinessExceptions() {
        exceptionsAreBusinessExceptions.check(CLASSES);
    }

    @Test
    void valueObjectsAreDomainRecords() {
        valueObjectsAreDomainRecords.check(CLASSES);
    }

    @Test
    void noScalarsInDomainAndCommands() {
        noScalarsInDomainAndCommands.check(CLASSES);
    }
}
