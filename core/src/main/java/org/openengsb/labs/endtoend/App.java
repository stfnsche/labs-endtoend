package org.openengsb.labs.endtoend;

import org.openengsb.labs.endtoend.api.Karaf;
import org.openengsb.labs.endtoend.api.RemoteShell;
import org.openengsb.labs.endtoend.api.Shell;
import org.openengsb.labs.endtoend.distribution.DistributionExtractor;
import org.openengsb.labs.endtoend.distribution.UnsupportedArchiveTypeException;
import org.openengsb.labs.endtoend.karaf.KarafException;
import org.openengsb.labs.endtoend.testcontext.TestContext;
import org.openengsb.labs.endtoend.testcontext.TestContextLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class App {
    private static final String PROPERTY_FILE_MAC = getCurrentDir()
            + "/test_contexts/karaf/context_karaf_mac.properties";
    private static final String EXTRACTION_DIR = getCurrentDir()
            + "/tmp";


    public static void main(String[] args) throws KarafException, FileNotFoundException, IOException,
            UnsupportedArchiveTypeException {

        // builder syntact Distribution.withDistextracor().underContext().create()

        // DistributionExtractor ds = new DistributionExtractor(); (should use temp)
        DistributionExtractor ds = new DistributionExtractor(EXTRACTION_DIR);

        TestContextLoader testContextLoader = new TestContextLoader(ds);

        // TestContext context = testContextLoader.getTestContext(); // default context
        TestContext context = testContextLoader.getTestContext("specific context"); // specific context

        DistributionToTest().extractor().context().hablieb().create().

        // 1: check if correct execution context exists
        // 2: check extract
        // 3: check executable
        context.setup();

        Karaf k = context.getKaraf();
        try {
            System.out.println("Starting Karaf...");
            k.start(10L, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Shell shell = k.getShell();
        try {
            System.out.println("Executing list command...");
            String response = shell.execute("list", 10L, TimeUnit.SECONDS);
            System.out.println(response);
            System.out.println("Logout...");
        } catch (TimeoutException e1) {
            e1.printStackTrace();
        }

        RemoteShell remoteShell = null;
        try {
            System.out.println("Remote login...");
            remoteShell = k.login("root", "pw", 10L, TimeUnit.SECONDS);
            System.out.println("Executing list command...");
            String response = remoteShell.execute("list", 10L, TimeUnit.SECONDS);
            System.out.println(response);
            System.out.println("Logout...");
            remoteShell.logout();
        } catch (TimeoutException e1) {
            e1.printStackTrace();
        }

        try {
            System.out.println("Stopping Karaf...");
            k.shutdown(10L, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        context.teardown();

        System.out.println("Finished.");

    }

    private static File getCurrentDir() {
        String dir = System.getProperty("user.dir");
        return new File(dir);
    }
}
