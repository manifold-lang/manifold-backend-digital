package org.manifold.compiler.back.digital;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.manifold.compiler.BooleanValue;
import org.manifold.compiler.NodeValue;
import org.manifold.compiler.PortValue;
import org.manifold.compiler.UndeclaredAttributeException;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.back.digital.smt2.Bitstring;
import org.manifold.compiler.back.digital.smt2.QFABV;
import org.manifold.compiler.back.digital.smt2.SExpression;
import org.manifold.compiler.back.digital.smt2.Symbol;
import org.manifold.compiler.middle.Schematic;

public class SMT2CodeGenerator {
  private static Logger log = LogManager.getLogger("SMT2CodeGenerator");

  private Schematic schematic;
  private Netlist netlist;
  private PrimitiveTypeTable typeTable;
  
  private String outputDirectory;
  public void setOutputDirectory(String dir) {
    this.outputDirectory = dir;
  }
  
  private boolean runChecks = true;
  public void setRunChecks(boolean run) {
    this.runChecks = run;
  }
  
  // valid states are 0..numberOfStates inclusive
  private int numberOfStates = 200;
  public void setNumberOfStates(int n) {
    this.numberOfStates = n;
  }
  
  private List<SExpression> declarations;
  private List<SExpression> assertions;
  
  public SMT2CodeGenerator(Schematic schematic, Netlist netlist,
      PrimitiveTypeTable typeTable) {
    this.schematic = schematic;
    this.netlist = netlist;
    this.typeTable = typeTable;
    // by default, output to current working directory
    this.outputDirectory = Paths.get("").toAbsolutePath().toString();
    
    this.declarations = new LinkedList<SExpression>();
    this.assertions = new LinkedList<SExpression>();
  }
  
  private void err(String message) {
    throw new CodeGenerationError(message);
  }
  
  private void generateInputPin(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException {
    // find out what net we drive
    PortValue inputPort = node.getPort("out");
    Net inputNet = netlist.getConnectedNet(inputPort);
    String netName = inputNet.getName();
    for (int i = 0; i <= numberOfStates; ++i) {
      Symbol symInput = QFABV.getStateVariable(nodeName, i);
      Symbol symNet = QFABV.getStateVariable(netName, i);
      // declare state variables
      declarations.add(QFABV.declareBitVector(symInput, 1));
      declarations.add(QFABV.declareBitVector(symNet, 1));
      // the input drives the net on every timestep
      assertions.add(QFABV.assertThat(QFABV.equal(symInput, symNet)));
    }
  }
  
  private void generateOutputPin(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException {
    // find out what net drives the input
    PortValue outputPort = node.getPort("in");
    Net outputNet = netlist.getConnectedNet(outputPort);
    String netName = outputNet.getName();
    for (int i = 0; i <= numberOfStates; ++i) {
      Symbol symOutput = QFABV.getStateVariable(nodeName, i);
      Symbol symNet = QFABV.getStateVariable(netName, i);
      // declare state variables (outputs only)
      declarations.add(QFABV.declareBitVector(symOutput, 1));
      // the input drives the net on every timestep
      assertions.add(QFABV.assertThat(QFABV.equal(symOutput, symNet)));
    }
  }
  
  private void generateAndGate(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException {
    // out = in0 AND in1
    PortValue in0Port = node.getPort("in0");
    PortValue in1Port = node.getPort("in1");
    PortValue outPort = node.getPort("out");
    
    Net in0Net = netlist.getConnectedNet(in0Port);
    Net in1Net = netlist.getConnectedNet(in1Port);
    Net outNet = netlist.getConnectedNet(outPort);
    
    String in0NetName = in0Net.getName();
    String in1NetName = in1Net.getName();
    String outNetName = outNet.getName();
    
    for (int i = 0; i <= numberOfStates; ++i) {
      Symbol symIn0 = QFABV.getStateVariable(in0NetName, i);
      Symbol symIn1 = QFABV.getStateVariable(in1NetName, i);
      Symbol symOut = QFABV.getStateVariable(outNetName, i);
      // declare net for output
      declarations.add(QFABV.declareBitVector(symOut, 1));
      assertions.add(QFABV.assertThat(QFABV.equal(
          symOut, QFABV.and(symIn0, symIn1))));
    }
  }
  
  private void generateOrGate(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException {
    // out = in0 OR in1
    PortValue in0Port = node.getPort("in0");
    PortValue in1Port = node.getPort("in1");
    PortValue outPort = node.getPort("out");
    
    Net in0Net = netlist.getConnectedNet(in0Port);
    Net in1Net = netlist.getConnectedNet(in1Port);
    Net outNet = netlist.getConnectedNet(outPort);
    
    String in0NetName = in0Net.getName();
    String in1NetName = in1Net.getName();
    String outNetName = outNet.getName();
    
    for (int i = 0; i <= numberOfStates; ++i) {
      Symbol symIn0 = QFABV.getStateVariable(in0NetName, i);
      Symbol symIn1 = QFABV.getStateVariable(in1NetName, i);
      Symbol symOut = QFABV.getStateVariable(outNetName, i);
      // declare net for output
      declarations.add(QFABV.declareBitVector(symOut, 1));
      assertions.add(QFABV.assertThat(QFABV.equal(
          symOut, QFABV.or(symIn0, symIn1))));
    }
  }
  
  private void generateNotGate(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException {
    // out = NOT in
    PortValue inPort = node.getPort("in");
    PortValue outPort = node.getPort("out");
    
    Net inNet = netlist.getConnectedNet(inPort);
    Net outNet = netlist.getConnectedNet(outPort);
    
    String inNetName = inNet.getName();
    String outNetName = outNet.getName();
    
    for (int i = 0; i <= numberOfStates; ++i) {
      Symbol symIn = QFABV.getStateVariable(inNetName, i);
      Symbol symOut = QFABV.getStateVariable(outNetName, i);
      // declare net for output
      declarations.add(QFABV.declareBitVector(symOut, 1));
      assertions.add(QFABV.assertThat(QFABV.equal(
          symOut, QFABV.not(symIn))));
    }
  }
  
  private void generateRegister(String nodeName, NodeValue node) 
      throws UndeclaredIdentifierException, UndeclaredAttributeException {
    // ports: in, out, reset
    // attributes: initialValue, resetActiveHigh
    PortValue inPort = node.getPort("in");
    PortValue outPort = node.getPort("out");
    PortValue resetPort = node.getPort("reset");
    
    Net inNet = netlist.getConnectedNet(inPort);
    Net outNet = netlist.getConnectedNet(outPort);
    Net resetNet = netlist.getConnectedNet(resetPort);
    
    String inNetName = inNet.getName();
    String outNetName = outNet.getName();
    String resetNetName = resetNet.getName();
    
    boolean initialValue = ((BooleanValue) node
        .getAttribute("initialValue")).toBoolean();
    boolean resetActiveHigh = ((BooleanValue) node
        .getAttribute("resetActiveHigh")).toBoolean();
    
    // t=0: define out net, drive output(t) = initial value
    {
      Symbol symOut = QFABV.getStateVariable(outNetName, 0);
      declarations.add(QFABV.declareBitVector(symOut, 1));
      if (initialValue) {
        assertions.add(QFABV.assertBitOne(symOut));
      } else {
        assertions.add(QFABV.assertBitZero(symOut));
      }
    }
    // t>0: define out net
    // t>0: if reset(t-1) asserted, drive output(t) = initial value;
    //      else, drive output(t) = input(t-1)
    for (int i = 1; i <= numberOfStates; ++i) {
      Symbol symIn = QFABV.getStateVariable(inNetName, i - 1);
      Symbol symReset = QFABV.getStateVariable(resetNetName, i - 1);
      Symbol symOut = QFABV.getStateVariable(outNetName, i);
      
      declarations.add(QFABV.declareBitVector(symOut, 1));
      SExpression resetActive;
      if (resetActiveHigh) {
        resetActive = new Bitstring("1");
      } else {
        resetActive = new Bitstring("0");
      }
      SExpression init;
      if (initialValue) {
        init = new Bitstring("1");
      } else {
        init = new Bitstring("0");
      }
      
      assertions.add(QFABV.equal(symOut,
          QFABV.conditional(QFABV.equal(symReset, resetActive), init, symIn)));
    }
  }
  
  public void generateOutputProducts() {
    if (numberOfStates < 0) {
      err("invalid number of states specified; must be non-negative");
    }
    
    String entityName = schematic.getName();
    String filename = entityName + ".smt2";
    Path outpath = Paths.get(outputDirectory + File.separator + filename);
    File outfile = new File(outpath.toString());
    log.info("Generating " + filename);
    
    try (PrintWriter writer = new PrintWriter(outfile, "US-ASCII");) {
      // SMT2 logic header: QF_ABV
      // TODO if no component is modelled as an array, emit QF_BV
      // TODO check to make sure all nets are driven
      // TODO check that all resets are synchronous
      // TODO check that all registers clock on the same edge
      // TODO check that all registers are in the same clock domain
      writer.println("(set-logic QF_ABV)");
      writer.println("(set-info :smt-lib-version 2.0)");
      // for each node, populate two sets:
      // * declarations: all outputs declared by this node
      // * assertions: all equations that model this node
      // note that we end up with 'numberOfStates'+1 copies of
      // each declaration (which each generator creates on its own)
      try {
        for (Entry<String, NodeValue> entry : schematic.getNodes().entrySet()) {
          String nodeName = entry.getKey();
          NodeValue node = entry.getValue();
          if (node.getType().equals(typeTable.getInputPinType())) {
            generateInputPin(nodeName, node);
          } else if (node.getType().equals(typeTable.getOutputPinType())) {
            generateOutputPin(nodeName, node);
          } else if (node.getType().equals(typeTable.getAndType())) {
            generateAndGate(nodeName, node);
          } else if (node.getType().equals(typeTable.getOrType())) {
            generateOrGate(nodeName, node);
          } else if (node.getType().equals(typeTable.getNotType())) {
            generateNotGate(nodeName, node);
          } else if (node.getType().equals(typeTable.getRegisterType())) {
            generateRegister(nodeName, node);
          } else {
            err("node " + nodeName + " has unknown node type");
          }
        }
      } catch (UndeclaredIdentifierException | UndeclaredAttributeException e) {
        err(e.getMessage());
      }
      
      // then write out all generated expressions, starting with declarations
      // followed by assertions
      for (SExpression expr : declarations) {
        expr.write(writer);
        writer.println();
      }
      for (SExpression expr : assertions) {
        expr.write(writer);
        writer.println();
      }
      
    } catch (IOException e) {
      err(e.getMessage());
    }
    
    log.info("Finished generating " + filename);
  }
  
}
