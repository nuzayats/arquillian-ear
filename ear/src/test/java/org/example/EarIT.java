package org.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.IOException;

@RunWith(Arquillian.class)
public class EarIT {

    @Deployment
    public static Archive<?> createDeploymentPackage() throws IOException {
        final JavaArchive ejbJar = ShrinkWrap.create(JavaArchive.class, "ejb-jar.jar").addClass(SomeEjb.class);

        // Embedding war package which contains the test class is needed
        // So that Arquillian can invoke test class through its servlet test runner
        final WebArchive testWar = ShrinkWrap.create(WebArchive.class, "test.war").addClass(EarIT.class);
        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class)
                .setApplicationXML("test-application.xml")
                .addAsModule(ejbJar)
                .addAsModule(testWar);
        return ear;
    }

    @EJB
    private SomeEjb someEjb;

    @Test
    public void test() {
        Assert.assertEquals("Hello, Kyle", someEjb.hello("Kyle"));
    }
}
