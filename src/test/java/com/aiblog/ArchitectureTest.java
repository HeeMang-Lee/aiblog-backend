package com.aiblog;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import java.util.Set;

@AnalyzeClasses(packages = "com.aiblog", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  // === Rule 1: 레이어 의존성 방향 ===
  // Controller → Service → Repository 방향만 허용
  // Repository에서 Controller/Service 역방향 의존 금지
  @ArchTest
  static final ArchRule 레이어_의존성_방향 =
      Architectures.layeredArchitecture()
          .consideringOnlyDependenciesInLayers()
          .withOptionalLayers(true)
          .layer("Controller").definedBy("..controller..")
          .layer("Service").definedBy("..service..")
          .layer("Repository").definedBy("..repository..")
          .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
          .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service");

  // === Rule 2: 패키지 구조 ===
  // domain 하위의 Controller/Service/Repository는 각각 올바른 패키지에 위치해야 한다
  @ArchTest
  static final ArchRule Controller는_controller_패키지에_위치해야_한다 =
      classes().that().haveSimpleNameEndingWith("Controller")
          .and().resideInAnyPackage("com.aiblog.domain..")
          .should().resideInAPackage("..controller..");

  @ArchTest
  static final ArchRule Service는_service_패키지에_위치해야_한다 =
      classes().that().haveSimpleNameEndingWith("Service")
          .and().areNotInterfaces()
          .and().resideInAnyPackage("com.aiblog.domain..")
          .should().resideInAPackage("..service..");

  @ArchTest
  static final ArchRule Repository는_repository_패키지에_위치해야_한다 =
      classes().that().haveSimpleNameEndingWith("Repository")
          .and().resideInAnyPackage("com.aiblog.domain..")
          .should().resideInAPackage("..repository..");

  // === Rule 3: 도메인 간 순환 의존 금지 ===
  @ArchTest
  static final ArchRule 도메인_간_순환_의존_금지 =
      slices().matching("com.aiblog.domain.(*)..").should().beFreeOfCycles();

  // === Rule 4: Entity @Setter 금지 ===
  // 클래스 레벨, 필드 레벨 모두 금지
  @ArchTest
  static final ArchRule Entity_클래스에_Setter_금지 =
      noClasses().that().areAnnotatedWith("jakarta.persistence.Entity")
          .should().beAnnotatedWith("lombok.Setter");

  @ArchTest
  static final ArchRule Entity_필드에_Setter_금지 =
      noFields().that().areDeclaredInClassesThat()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should().beAnnotatedWith("lombok.Setter");

  // === Rule 5: @OneToMany/@ManyToMany 금지, cascade 금지 ===
  // 연관관계는 @ManyToOne 단방향만 허용
  @ArchTest
  static final ArchRule Entity에_OneToMany_사용_금지 =
      noFields().that().areDeclaredInClassesThat()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should().beAnnotatedWith("jakarta.persistence.OneToMany");

  @ArchTest
  static final ArchRule Entity에_ManyToMany_사용_금지 =
      noFields().that().areDeclaredInClassesThat()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should().beAnnotatedWith("jakarta.persistence.ManyToMany");

  @ArchTest
  static final ArchRule Entity에_cascade_사용_금지 =
      fields().that().areDeclaredInClassesThat()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should(notHaveCascade());

  // === Rule 6: AI 도메인 헥사고날 경계 ===
  // Service/Port → Adapter 직접 의존 금지 (Port를 통해서만 접근)
  @ArchTest
  static final ArchRule AI_서비스는_어댑터에_직접_의존_금지 =
      noClasses().that().resideInAPackage("..ai.service..")
          .should().dependOnClassesThat().resideInAPackage("..ai.adapter..");

  @ArchTest
  static final ArchRule AI_포트는_어댑터에_의존_금지 =
      noClasses().that().resideInAPackage("..ai.port..")
          .should().dependOnClassesThat().resideInAPackage("..ai.adapter..");

  // === Rule 7: @ManyToOne FetchType.EAGER 금지 ===
  // @ManyToOne은 반드시 fetch = FetchType.LAZY를 명시해야 한다
  // (JPA 기본값이 EAGER이므로 미지정 시에도 위반)
  @ArchTest
  static final ArchRule ManyToOne은_반드시_LAZY여야_한다 =
      fields().that().areDeclaredInClassesThat()
          .areAnnotatedWith("jakarta.persistence.Entity")
          .should(notUseManyToOneWithEagerFetch());

  // === Custom Conditions ===

  private static final Set<String> JPA_RELATIONSHIP_ANNOTATIONS = Set.of(
      "jakarta.persistence.OneToOne",
      "jakarta.persistence.OneToMany",
      "jakarta.persistence.ManyToOne",
      "jakarta.persistence.ManyToMany"
  );

  private static ArchCondition<JavaField> notHaveCascade() {
    return new ArchCondition<>("not have cascade on relationship annotations") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        for (JavaAnnotation<?> annotation : field.getAnnotations()) {
          String annotationName = annotation.getRawType().getName();
          if (JPA_RELATIONSHIP_ANNOTATIONS.contains(annotationName)) {
            annotation.tryGetExplicitlyDeclaredProperty("cascade")
                .ifPresent(cascade ->
                    events.add(SimpleConditionEvent.violated(field,
                        field.getFullName() + "에 cascade 사용 금지 ("
                            + annotation.getRawType().getSimpleName() + ")")));
          }
        }
      }
    };
  }

  private static ArchCondition<JavaField> notUseManyToOneWithEagerFetch() {
    return new ArchCondition<>("not use @ManyToOne with EAGER fetch type") {
      @Override
      public void check(JavaField field, ConditionEvents events) {
        for (JavaAnnotation<?> annotation : field.getAnnotations()) {
          if (!"jakarta.persistence.ManyToOne".equals(
              annotation.getRawType().getName())) {
            continue;
          }
          var fetch = annotation.tryGetExplicitlyDeclaredProperty("fetch");
          if (fetch.isEmpty()) {
            events.add(SimpleConditionEvent.violated(field,
                field.getFullName()
                    + "에 @ManyToOne fetch 타입 미지정 (기본값 EAGER)"));
          } else if (fetch.get() instanceof JavaEnumConstant enumConstant
              && "EAGER".equals(enumConstant.name())) {
            events.add(SimpleConditionEvent.violated(field,
                field.getFullName()
                    + "에 @ManyToOne(fetch = FetchType.EAGER) 사용 금지"));
          }
        }
      }
    };
  }
}
