package com.archive.backend;

import org.flywaydb.core.Flyway;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puts the database of a test JVM back to what the migrations make of it after every test class that commits, so no
 * class sees what another left behind (docs/testing.md). A class whose tests roll back leaves nothing and is passed
 * over.
 */
public class ResetDatabaseAfterCommittingTests implements TestExecutionListener {

    @Override
    public void afterTestClass(TestContext context) {
        if (rollsBack(context.getTestClass())) {
            return;
        }
        ApplicationContext application = context.getApplicationContext();
        if (application.getBeanNamesForType(Flyway.class).length == 0) {
            return;
        }
        Flyway flyway = application.getBean(Flyway.class);
        flyway.clean();
        flyway.migrate();
    }

    private static boolean rollsBack(Class<?> testClass) {
        Transactional transactional = AnnotatedElementUtils.findMergedAnnotation(testClass, Transactional.class);
        return transactional != null
                && transactional.propagation() != Propagation.NOT_SUPPORTED
                && transactional.propagation() != Propagation.NEVER;
    }
}
