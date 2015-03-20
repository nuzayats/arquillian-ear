package org.example;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.protocol.servlet.arq514hack.descriptors.api.application.ApplicationDescriptor;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.DescriptorImporter;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import java.io.*;

@RunWith(Arquillian.class)
public class EarFromZipFileIT {

    @Deployment
    public static Archive<?> createDeploymentPackage() throws IOException {
        final String testWarName = "test.war";

        // Build the ear with Maven by hand before run the test!
        final EnterpriseArchive ear = ShrinkWrap.createFromZipFile(
                EnterpriseArchive.class, new File("target/ear-1.0-SNAPSHOT.ear"));

        System.out.println("---ear---");
        addTestWar(ear, EarFromZipFileIT.class, testWarName);
        System.out.println(ear.toString(true));

        System.out.println();
        System.out.println("---war---");
        final WebArchive war = ear.getAsType(WebArchive.class, testWarName);
        System.out.println(war.toString(true));

        return ear;
    }

    private static void addTestWar(EnterpriseArchive ear, Class testClass, String testWarName) throws IOException {
        final WebArchive war = ShrinkWrap.create(WebArchive.class, testWarName);
        war.addClass(testClass);
        ear.addAsModule(war);

        modifyApplicationXMLFaster(ear, testWarName);
    }

    // taken from http://stackoverflow.com/questions/14713129/how-to-add-test-classes-to-an-imported-ear-file-and-run-server-side-with-arquill/17036383#17036383
    private static void modifyApplicationXML(EnterpriseArchive ear, String testWarName) throws IOException {
        Node node = ear.get("META-INF/application.xml");

        DescriptorImporter<ApplicationDescriptor> importer = Descriptors.importAs(ApplicationDescriptor.class);
        try (InputStream is = node.getAsset().openStream()) {
            ApplicationDescriptor desc = importer.fromStream(is); // slow

            // append test.war to application.xml
            desc.webModule(testWarName, "/test");
            final String s = desc.exportAsString();
            Asset asset = new StringAsset(s);

            ear.delete(node.getPath());
            ear.setApplicationXML(asset);
        }
    }

    // ugly but faster than original one
    private static void modifyApplicationXMLFaster(EnterpriseArchive ear, String testWarName) throws IOException {
        Node node = ear.get("META-INF/application.xml");

        try (InputStream is = node.getAsset().openStream();
             InputStreamReader r = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(r)) {

            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                if (!"</application>".equals(line)) {
                    sb.append(line).append('\n');
                    continue;
                }

                sb.append(createModuleFragment(testWarName, "/test"));
            }

            final String modified = sb.toString();
            System.out.println(modified);
            ear.delete(node.getPath());
            ear.setApplicationXML(new StringAsset(modified));
        }
    }

    private static String createModuleFragment(String webUri, String contextRoot) {
        return "  <module>\n" +
                "    <web>\n" +
                "      <web-uri>" + webUri + "</web-uri>\n" +
                "      <context-root>" + contextRoot + "</context-root>\n" +
                "    </web>\n" +
                "  </module>\n" +
                "</application>";
    }


    @EJB
    private SomeEjb someEjb;

    @Test
    public void test() {
        Assert.assertEquals("Hello, Kyle", someEjb.hello("Kyle"));
    }
}
