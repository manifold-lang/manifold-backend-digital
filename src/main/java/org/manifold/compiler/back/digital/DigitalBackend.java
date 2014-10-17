package org.manifold.compiler.back.digital;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.manifold.compiler.Backend;
import org.manifold.compiler.OptionError;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class DigitalBackend implements Backend {

  private static Logger log = LogManager.getLogger("DigitalBackend");

  public enum TargetHDL {
    VHDL,
  };

  private TargetHDL targetHDL = null;
  public TargetHDL getTargetHDL() {
    return targetHDL;
  }

  private void createOptionTargetHDL(Options options) {
    Option hdl = new Option("h", "hdl", true, "target HDL type (vhdl)");
    options.addOption(hdl);
  }

  private void collectOptionTargetHDL(CommandLine cmd) {
    String hdl = cmd.getOptionValue("hdl");
    if (hdl == null) {
      log.warn("no target HDL specified, assuming VHDL");
      targetHDL = TargetHDL.VHDL;
    } else {
      hdl = hdl.toLowerCase();
      if (hdl.equals("vhdl")) {
        targetHDL = TargetHDL.VHDL;
      } else {
        throw new OptionError("target HDL '" + hdl + "' not recognized");
      }
    }
  }

  String outputDirectory = null;

  private void createOptionOutputDirectory(Options options) {
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
  private void createOptionNoChecks(Options options) {
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

  private void createOptionDefinitions(Options options) {
    createOptionTargetHDL(options);
    createOptionOutputDirectory(options);
    createOptionNoChecks(options);
  }

  private void collectOptions(CommandLine cmd) {
    collectOptionTargetHDL(cmd);
    collectOptionOutputDirectory(cmd);
    collectOptionNoChecks(cmd);
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
  public void invokeBackend(Schematic schematic, CommandLine cmd)
      throws Exception {
    collectOptions(cmd);
    run(schematic);
  }

  @Override
  public void registerArguments(Options options) {
    createOptionDefinitions(options);
  }

}
