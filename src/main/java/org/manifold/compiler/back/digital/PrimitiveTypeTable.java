package org.manifold.compiler.back.digital;

import org.manifold.compiler.NodeTypeValue;
import org.manifold.compiler.PortTypeValue;
import org.manifold.compiler.UndeclaredIdentifierException;
import org.manifold.compiler.middle.Schematic;

public class PrimitiveTypeTable {

  private PortTypeValue inputPortType = null;
  private PortTypeValue outputPortType = null;
  
  private NodeTypeValue inputPinType = null;
  private NodeTypeValue outputPinType = null;

  private NodeTypeValue registerType = null;
  private NodeTypeValue andType = null;
  private NodeTypeValue orType = null;
  private NodeTypeValue notType = null;
  
  public PortTypeValue getInputPortType() {
    return inputPortType;
  }

  public PortTypeValue getOutputPortType() {
    return outputPortType;
  }

  public NodeTypeValue getInputPinType() {
    return inputPinType;
  }

  public NodeTypeValue getOutputPinType() {
    return outputPinType;
  }

  public NodeTypeValue getRegisterType() {
    return registerType;
  }

  public NodeTypeValue getAndType() {
    return andType;
  }

  public NodeTypeValue getOrType() {
    return orType;
  }

  public NodeTypeValue getNotType() {
    return notType;
  }
  
  public PrimitiveTypeTable(Schematic schematic) {
    // get information from the schematic about which node types to use
    try {
      inputPortType = schematic.getPortType("digitalIn");
      outputPortType = schematic.getPortType("digitalOut");
      inputPinType = schematic.getNodeType("inputPin");
      outputPinType = schematic.getNodeType("outputPin");
      registerType = schematic.getNodeType("register");
      andType = schematic.getNodeType("and");
      orType = schematic.getNodeType("or");
      notType = schematic.getNodeType("not");
    } catch (UndeclaredIdentifierException e) {
      throw new CodeGenerationError(
          "could not find required digital design type '"
          + e.getIdentifier() + "'; schematic version mismatch or "
          + " not a digital schematic");
    }
  }
  
}
