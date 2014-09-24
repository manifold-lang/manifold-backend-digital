package org.manifold.compiler.back.digital;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.manifold.compiler.Backend;
import org.manifold.compiler.OptionError;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class DigitalBackend implements Backend {

  private static Logger log = LogManager.getLogger("DigitalBackend");

  private Options options;

  public enum TARGET_HDL {
    VHDL,
  };

  private TARGET_HDL targetHDL = null;
  public TARGET_HDL getTargetHDL() {
    return targetHDL;
  }

  private void createOptionTargetHDL() {
    Option hdl = new Option("h", "hdl", true, "target HDL type (vhdl)");
    options.addOption(hdl);
  }

  private void collectOptionTargetHDL(CommandLine cmd) {
    String hdl = cmd.getOptionValue("hdl");
    if (hdl == null) {
      log.warn("no target HDL specified, assuming VHDL");
      targetHDL = TARGET_HDL.VHDL;
    } else {
      hdl = hdl.toLowerCase();
      if (hdl.equals("vhdl")) {
        targetHDL = TARGET_HDL.VHDL;
      } else {
        throw new OptionError("target HDL '" + hdl + "' not recognized");
      }
    }
  }

  String outputDirectory = null;

  private void createOptionOutputDirectory() {
    Option outDir = new Option("o", "output", true,
        "directory for output products");
    options.addOption(outDir);
  }

  private void collectOptionOutputDirectory(CommandLine cmd) {
    String outDir = cmd.getOptionValue("output");
    if (outDir != null) {
      outputDirectory = outDir;
    }
  }

  boolean noChecks = false;

  @SuppressWarnings("static-access")
  private void createOptionNoChecks() {
    Option noChecks = OptionBuilder
        .withLongOpt("no-checks")
        .withDescription(
            "do not run design checks "
                + "(may result in incorrect code generation)").create();
    options.addOption(noChecks);
  }

  private void collectOptionNoChecks(CommandLine cmd) {
    if (cmd.hasOption("no-checks")) {
      noChecks = true;
    }
  }

  private void createOptionDefinitions() {
    options = new Options();
    createOptionTargetHDL();
    createOptionOutputDirectory();
    createOptionNoChecks();
  }

  private void collectOptions(CommandLine cmd) {
    collectOptionTargetHDL(cmd);
    collectOptionOutputDirectory(cmd);
    collectOptionNoChecks(cmd);
  }

  public void readArguments(String[] args) throws ParseException {
    // set up options for command-line parsing
    createOptionDefinitions();
    // parse command line
    CommandLineParser parser = new org.apache.commons.cli.BasicParser();
    CommandLine cmd = parser.parse(options, args);
    // retrieve command-line options
    collectOptions(cmd);
  }

  public void run(Schematic schematic) throws SchematicException {
    switch (targetHDL) {
        case VHDL: {
          VHDLCodeGenerator vhdlGen = new VHDLCodeGenerator(schematic);
          if (outputDirectory != null) {
            vhdlGen.setOutputDirectory(outputDirectory);
          }
          if (noChecks) {
            vhdlGen.setRunChecks(false);
          }
          vhdlGen.generateOutputProducts();
        } // end case VHDL
    }
  }

  public DigitalBackend() { }
  
  @Override
  public String getBackendName() {
    return "digital";
  }

  @Override
  public void invokeBackend(Schematic schematic, String[] args) 
      throws Exception {
    readArguments(args);
    run(schematic);
  }

}
