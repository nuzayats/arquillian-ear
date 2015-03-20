package org.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;

@RunWith(Arquillian.class)
public class EjbJarIT {
    @Deployment
    public static Archive<?> createDeploymentPackage() {
        final Archive archive = ShrinkWrap.create(JavaArchive.class).addClass(SomeEjb.class);
//        System.out.println(archive.toString(true));
        return archive;
    }

    @EJB
    private SomeEjb someEjb;

    @Test
    public void test() {
        Assert.assertEquals("Hello, Kyle", someEjb.hello("Kyle"));
    }
}
