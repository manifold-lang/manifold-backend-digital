package org.manifold.compiler.back;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.manifold.compiler.ConnectionValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.TypeMismatchException;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.back.digital.Netlist;
import org.manifold.compiler.back.digital.PrimitiveTypeTable;
import org.manifold.compiler.back.digital.SMT2CodeGenerator;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class TestSMT2CodeGenerator {

  @BeforeClass
  public static void setupClass() {
    LogManager.getRootLogger().setLevel(Level.ALL);
    PatternLayout layout = new PatternLayout(
        "%d{ISO8601} [%t] %-5p %c %x - %m%n");
    LogManager.getRootLogger().addAppender(
        new ConsoleAppender(layout, ConsoleAppender.SYSTEM_ERR));
  }

  @AfterClass
  public static void afterClass() {
    LogManager.getRootLogger().removeAllAppenders();
  }
  
  @Rule
  public TemporaryFolder folder = new TemporaryFolder();
  
  private List<String> schematicToSMT2(Schematic schematic, int nStates) 
      throws IOException, UndeclaredIdentifierException, TypeMismatchException {
    Netlist netlist = new Netlist(schematic);
    PrimitiveTypeTable typeTable = new PrimitiveTypeTable(schematic);
    SMT2CodeGenerator codegen = new SMT2CodeGenerator(
        schematic, netlist, typeTable);
    codegen.setNumberOfStates(nStates);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    // open generated code
    String testOutputFilename = temppath + "/" + schematic.getName() + ".smt2";
    Path testOutputPath = Paths.get(testOutputFilename);
    List<String> lines = Files.readAllLines(testOutputPath);
    return lines;
  }
  
  private int countMatches(List<String> block, String pattern){
    Pattern p = Pattern.compile(pattern);
    int matchCount = 0;
    for (String target : block) {
      Matcher mTarget = p.matcher(target);
      if (mTarget.find()) {
        ++matchCount;
      }
    }
    return matchCount;
  }
  
  @Test
  public void testOutputProductPresent() throws SchematicException {
    // Connect an input pin straight across to an output pin.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue in0ToOut0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("in0_to_out0", in0ToOut0);
    
    Netlist netlist = new Netlist(schematic);
    PrimitiveTypeTable typeTable = new PrimitiveTypeTable(schematic);
    
    SMT2CodeGenerator codegen = new SMT2CodeGenerator(
        schematic, netlist, typeTable);
    File tempdir = folder.getRoot();
    String temppath = tempdir.getAbsolutePath();
    codegen.setOutputDirectory(temppath);
    codegen.generateOutputProducts();

    List<Path> outputFiles = new ArrayList<Path>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths
        .get(temppath))) {
      for (Path file : stream) {
        outputFiles.add(file);
      }
    } catch (IOException | DirectoryIteratorException x) {
      fail(x.getMessage());
    }

    // check that "test.smt2" was generated
    boolean found = false;
    Path testSMT = null;
    for (Path file : outputFiles) {
      String filename = file.getFileName().toString();
      if (filename.equals("test.smt2")) {
        found = true;
        testSMT = file;
        break;
      }
    }
    assertTrue("output product 'test.smt2' not found", found);
  }
  
  @Test
  public void testORSchematic() throws SchematicException, IOException {
 // Connect two inputs through an OR gate to an output.
    Schematic schematic = UtilSchematicConstruction
        .instantiateSchematic("test");
    NodeValue in0 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in0", in0);
    NodeValue in1 = UtilSchematicConstruction.instantiateInputPin();
    schematic.addNode("in1", in1);
    NodeValue or0 = UtilSchematicConstruction.instantiateOr();
    schematic.addNode("or0", or0);
    NodeValue out0 = UtilSchematicConstruction.instantiateOutputPin();
    schematic.addNode("out0", out0);
    ConnectionValue net0 = UtilSchematicConstruction.instantiateWire(
        in0.getPort("out"), or0.getPort("in0"));
    schematic.addConnection("net0", net0);
    ConnectionValue net1 = UtilSchematicConstruction.instantiateWire(
        in1.getPort("out"), or0.getPort("in1"));
    schematic.addConnection("net1", net1);
    ConnectionValue net2 = UtilSchematicConstruction.instantiateWire(
        or0.getPort("out"), out0.getPort("in"));
    schematic.addConnection("net2", net2);

    List<String> testLines = schematicToSMT2(schematic, 0);
    
    // look for declarations of all the following:
    // in0__0, in1__0, out0__0
    
    int matchesIn0 = countMatches(testLines, "declare-fun\\s+in0__0");
    assertEquals("in0__0 not declared or multiply declared", 1, matchesIn0);
    int matchesIn1 = countMatches(testLines, "declare-fun\\s+in1__0");
    assertEquals("in1__0 not declared or multiply declared", 1, matchesIn1);
    int matchesOut0 = countMatches(testLines, "declare-fun\\s+out0__0");
    assertEquals("out0__0 not declared or multiply declared", 1, matchesOut0);
    
  }
  
}
