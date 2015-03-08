package org.manifold.compiler.back.digital;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.manifold.compiler.Backend;
import org.manifold.compiler.OptionError;
import org.manifold.compiler.TypeMismatchException;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.middle.Schematic;
import org.manifold.compiler.middle.SchematicException;

public class DigitalBackend implements Backend {

  private static Logger log = LogManager.getLogger("DigitalBackend");

  private void err(String message) {
    throw new CodeGenerationError(message);
  }
  
  public enum TargetHDL {
    VHDL,
    SMT2,
  };

  private TargetHDL targetHDL = null;
  public TargetHDL getTargetHDL() {
    return targetHDL;
  }

  private void createOptionTargetHDL(Options options) {
    Option hdl = new Option("h", "hdl", true, "target HDL type (vhdl, smt2)");
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
      } else if (hdl.equals("smt2")) {
        targetHDL = TargetHDL.SMT2;
      } else {
        throw new OptionError("target HDL '" + hdl + "' not recognized");
      }
    }
  }

  // default to current working directory
  String outputDirectory = Paths.get("").toAbsolutePath().toString();;

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

  private List<Check> buildStandardChecks(
      Schematic schematic, Netlist netlist) {
    List<Check> checks = new ArrayList<Check>();
    checks.add(new NoMultipleDriversCheck(schematic, netlist));
    checks.add(new NoUnconnectedInputsCheck(schematic, netlist));
    return checks;
  }
  
  public void run(Schematic schematic) throws SchematicException {
    // check if directory exists
    Path outDir = Paths.get(outputDirectory);
    if (!Files.exists(outDir)) {
      err("output directory '" + outputDirectory + "' does not exist");
    }
    if (!Files.isWritable(outDir)) {
      err("output directory '" + outputDirectory + "' is not writable");
    }
    log.info("Will generate output products in '" + outputDirectory + "'");

    // build netlist from schematic
    Netlist netlist = null;
    try {
      log.info("Building netlist");
      netlist = new Netlist(schematic);
    } catch (UndeclaredIdentifierException | TypeMismatchException e) {
      err(e.getMessage());
    }
    
    log.info("Building type table");
    PrimitiveTypeTable typeTable = new PrimitiveTypeTable(schematic);
    
    if (!noChecks) {
      log.info("constructing design checklist");
      // checks we always run
      List<Check> checks = buildStandardChecks(schematic, netlist);
      int numChecks = checks.size();
      int successes = 0;
      int failures = 0;
      log.info(Integer.toString(numChecks) + " checks to run");
      for (Check check : checks) {
        log.info("running check: " + check.getName());
        boolean result = check.run();
        if (result) {
          ++successes;
          log.info("check passed: " + check.getName());
        } else {
          ++failures;
          log.error("check failed: " + check.getName());
        }
        log.info("check summary: "
            + Integer.toString(successes) + "/" + Integer.toString(numChecks)
            + " checks successful, "
            + Integer.toString(failures) + "/" + Integer.toString(numChecks)
            + " checks failed");
        // if there were any failures, abort
        if (failures > 0) {
          err("design check failed");
        }
      }
    } else {
      log.warn("skipping all design checks");
    }
    
    switch (targetHDL) {
        case VHDL: {
          VHDLCodeGenerator vhdlGen = new VHDLCodeGenerator(
              schematic, netlist, typeTable);
          if (outputDirectory != null) {
            vhdlGen.setOutputDirectory(outputDirectory);
          }
          if (noChecks) {
            vhdlGen.setRunChecks(false);
          }
          vhdlGen.generateOutputProducts();
        } // end case VHDL
          break;
        case SMT2: {
          SMT2CodeGenerator smtgen = new SMT2CodeGenerator(
              schematic, netlist, typeTable);
          if (outputDirectory != null) {
            smtgen.setOutputDirectory(outputDirectory);
          }
          if (noChecks) {
            smtgen.setRunChecks(false);
          }
          smtgen.generateOutputProducts();
        } // end case SMT2
          break;
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
