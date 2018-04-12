package org.cups4j;

import cups4j.TestCups;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Unit tests for {@link CupsPrinter} class.
 *
 * @author oboehm
 */
public final class CupsPrinterTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(CupsPrinterTest.class);
    private static CupsPrinter printer;
    
    @BeforeClass
    public static void setUpPrinter() throws Exception {
        String name = System.getProperty("printer");
        if (name == null) {
            LOG.info("To specify printer please set system property 'printer'.");
            printer = TestCups.getCupsClient().getDefaultPrinter();
        } else {
            printer = TestCups.getCupsClient().getPrinter(name);
        }
        LOG.info("Printer {} was choosen.", printer);
    }

    @Test
    public void testPrintPDF() {
        print(printer, new File("src/test/resources/test.pdf"));
    }
    
    @Test
    public void testPrintText() {
        print(printer, new File("src/test/resources/test.txt"));
    }

    private PrintRequestResult print(CupsPrinter printer, File file) {
        PrintJob job = createPrintJob(file);
        LOG.info("Druckjob '{}' wird zu {} geschickt.", job, printer);
        try {
            return printer.print(job);
        } catch (Exception ex) {
            throw new IllegalStateException("print of '" + file + "' failed", ex);
        }
    }
    
    @Test
    public void testPrintList() {
        File file = new File("src/test/resources/test.txt");
        PrintJob job = createPrintJob(file);
        printer.print(job, job);
    }
    
    private PrintJob createPrintJob(File file) {
        String jobname = generateJobnameFor(file);
        try {
            byte[] content = FileUtils.readFileToByteArray(file);
            String userName = System.getProperty("user.name", CupsClient.DEFAULT_USER);
            return new PrintJob.Builder(content).jobName(jobname).userName(userName).build();
        } catch (IOException ioe) {
            throw new IllegalArgumentException("cannot read '" + file + "'", ioe);
        }
    }

    private static String generateJobnameFor(File file) {
        String basename = file.getName().split("\\.")[0];
        byte[] epochTime = Base64.encodeBase64(BigInteger.valueOf(System.currentTimeMillis()).toByteArray());
        return basename + new String(epochTime).substring(2);
    }
    
}
