/*
 * Team : AGF AM / OSI / SI / BO
 *
 * Copyright (c) 2001 AGF Asset Management.
 */
package net.codjo.agent;
import net.codjo.test.common.depend.Dependency;
import net.codjo.test.common.depend.PackageDependencyTestCase;
/**
 * Test les dépendances de cette API en utilisant JDepend.
 */
public class DependencyTest extends PackageDependencyTestCase {
    public void test_dependency() throws Exception {
        Dependency dependency = createDependency();
        dependency.assertDependency("dependency.txt");
        dependency.assertNoCycle();
    }


    public void test_dependencyTest() throws Exception {
        Dependency dependency = createTestDependency();
        dependency.assertDependency("dependencyTest.txt");
    }
}
