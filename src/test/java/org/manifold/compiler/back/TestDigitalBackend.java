package org.manifold.compiler.back;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.manifold.compiler.back.digital.DigitalBackend;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.serialization.SchematicDeserializer;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class CaptureAppender extends AppenderSkeleton {

  private List<LoggingEvent> loggedEvents = new LinkedList<LoggingEvent>();
  public void clear() {
    loggedEvents.clear();
  }
  public List<LoggingEvent> getEvents() {
    return loggedEvents;
  }

  @Override
  public void close() {
    // no-op
  }

  @Override
  public boolean requiresLayout() {
    return false;
  }

  @Override
  protected void append(LoggingEvent event) {
    loggedEvents.add(event);
  }

}

public class TestDigitalBackend {

  private CaptureAppender logCapture;

  @Before
  public void beforeTest() {
    logCapture = new CaptureAppender();
    LogManager.getRootLogger().addAppender(logCapture);
  }

  @After
  public void afterTest() {
    logCapture.clear();
    LogManager.getRootLogger().removeAppender(logCapture);
  }

  @AfterClass
  public static void afterClass() {
    LogManager.getRootLogger().removeAllAppenders();
  }

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void testIntegration_DigitalBackendVHDLCodeGeneration()
      throws Exception {
    // Create a schematic in a specified (temporary) folder
    // and invoke the digital backend as though it were called
    // from the command line. Then check for the presence of an output
    // product and that no errors were produced in the log.
    File tempdir = folder.getRoot();
    File tempSchematic = folder.newFile("test.schematic");
    String temppath = tempdir.getAbsolutePath();
    String tempSchematicPath = tempSchematic.getAbsolutePath();
    // write a very simple schematic
    // TODO replace this with a serialized Schematic object
    // (depends on Schematic serialization working correctly)
    URL url = Resources
        .getResource("org/manifold/compiler/back/data/"
            + "schematic-InToOut.json");
    String schematicSerial = Resources.toString(url, Charsets.UTF_8);
    BufferedWriter writer = Files.newBufferedWriter(tempSchematic.toPath(),
        Charset.forName("ASCII"));
    writer.write(schematicSerial);
    writer.close();
    String[] args = {
        "--hdl", "vhdl",
        "--output", temppath
    };

    SchematicDeserializer deserializer = new SchematicDeserializer();
    FileReader inFile = new FileReader(tempSchematic.getAbsolutePath());
    JsonObject inputJson = new JsonParser().parse(inFile).getAsJsonObject();
    Schematic schematic = deserializer.deserialize(inputJson);

    DigitalBackend backend = new DigitalBackend();
    Options options = new Options();
    backend.registerArguments(options);
    CommandLineParser parser = new org.apache.commons.cli.BasicParser();
    CommandLine cmd = parser.parse(options, args);
    backend.invokeBackend(schematic, cmd);
    // this should not emit any error messages
    for (LoggingEvent ev : logCapture.getEvents()) {
      if (ev.getLevel().isGreaterOrEqual(Level.ERROR)) {
        fail("errors logged during execution: first is '"
            + ev.getMessage().toString() + "'");
      }
    }
    // check for the presence of an output file "InToOut.vhd"
    List<Path> outputFiles = new ArrayList<Path>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths
        .get(temppath))) {
      for (Path file : stream) {
        outputFiles.add(file);
      }
    } catch (IOException | DirectoryIteratorException x) {
      fail(x.getMessage());
    }

    boolean found = false;
    Path testVHD = null;
    for (Path file : outputFiles) {
      String filename = file.getFileName().toString();
      if (filename.equals("InToOut.vhd")) {
        found = true;
        testVHD = file;
        break;
      }
    }
    assertTrue("output product not found", found);
  }

}
