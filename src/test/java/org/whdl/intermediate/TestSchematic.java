package org.whdl.intermediate;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestSchematic {

  @Test
  public void testAddTypeDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    UserDefinedType t1 = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.INTEGER));
    s.addTypeTypeDefinition("foo", t1);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddTypeDef_multipleDefinitions() throws MultipleDefinitionException{
    // We should not be able to add two type definitions whose first argument is the same string.
    Schematic s = new Schematic("test");
    try{
      UserDefinedType t1 = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.INTEGER));
      s.addTypeTypeDefinition("foo", t1);
    }catch(MultipleDefinitionException mde){
      fail("exception thrown too early: " + mde.getMessage());
    }
    UserDefinedType t2 = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.STRING));
    s.addTypeTypeDefinition("foo", t2);
  }
  
  @Test(expected = MultipleDefinitionException.class)
  public void testAddTypeDef_maskDefaultType() throws MultipleDefinitionException{
    // Suppose we create a new Schematic and then try to redefine the meaning of "Int".
    // Since "Int" is a built-in type, this should result in a MultipleDefinitionException being thrown.
    Schematic s = new Schematic("test");
    UserDefinedType td = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.STRING));
    s.addTypeTypeDefinition("Int", td);
  }
  
  @Test
  public void testGetTypeDef() throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    UserDefinedType expected = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.INTEGER));
    s.addTypeTypeDefinition("foo", expected);
    UserDefinedType actual = s.getTypeTypeDefinition("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected=UndeclaredIdentifierException.class)
  public void testGetTypeDef_notDeclared() throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    UserDefinedType bogus = s.getTypeTypeDefinition("does-not-exist");
  }
  
  @Test
  public void testAddEndpointDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    EndpointType e1 = new EndpointType();
    s.addEndpointTypeDefinition("n1",e1);
  }
  
  @Test(expected=MultipleDefinitionException.class)
  public void testAddEndpointDef_multipleDefinitions() throws MultipleDefinitionException {
    // We should not be able to add two endpoint definitions whose first argument is the same string.
    Schematic s = new Schematic("test");
    try{
      EndpointType n1 = new EndpointType();
      s.addEndpointTypeDefinition("foo", n1);
    }catch(MultipleDefinitionException mde){
      fail("exception thrown too early: " + mde.getMessage());
    }
    EndpointType n2 = new EndpointType();
    s.addEndpointTypeDefinition("foo", n2);
  }
  
  @Test
  public void testGetEndpointDef() throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    EndpointType expected = new EndpointType();
    s.addEndpointTypeDefinition("foo", expected);
    EndpointType actual = s.getEndpointTypeDefinition("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected=UndeclaredIdentifierException.class)
  public void testGetEndpointDef_notDeclared() throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    EndpointType bogus = s.getEndpointTypeDefinition("does-not-exist");
  }
  
  @Test
  public void testAddNodeDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    NodeType n1 = new NodeType();
    s.addNodeTypeDefinition("n1", n1);
  }
  
  @Test(expected=MultipleDefinitionException.class)
  public void testAddNodeDef_multipleDefinitions() throws MultipleDefinitionException {
    // We should not be able to add two node definitions whose first argument is the same string.
    Schematic s = new Schematic("test");
    try{
      NodeType n1 = new NodeType();
      s.addNodeTypeDefinition("foo", n1);
    }catch(MultipleDefinitionException mde){
      fail("exception thrown too early: " + mde.getMessage());
    }
    NodeType n2 = new NodeType();
    s.addNodeTypeDefinition("foo", n2);
  }
  
  @Test
  public void testGetNodeDef() throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    NodeType expected = new NodeType();
    s.addNodeTypeDefinition("foo", expected);
    NodeType actual = s.getNodeTypeDefinition("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected=UndeclaredIdentifierException.class)
  public void testGetNodeDef_notDeclared() throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    NodeType bogus = s.getNodeTypeDefinition("does-not-exist");
  }
  
  @Test
  public void testAddConnectionDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    ConnectionType c1 = new ConnectionType();
    s.addConnectionTypeDefinition("c1", c1);
  }
  
  @Test(expected=MultipleDefinitionException.class)
  public void testAddConnectionDef_multipleDefinitions() throws MultipleDefinitionException {
    // We should not be able to add two connection definitions whose first argument is the same string.
    Schematic s = new Schematic("test");
    try{
      ConnectionType c1 = new ConnectionType();
      s.addConnectionTypeDefinition("foo", c1);
    }catch(MultipleDefinitionException mde){
      fail("exception thrown too early: " + mde.getMessage());
    }
    ConnectionType c2 = new ConnectionType();
    s.addConnectionTypeDefinition("foo", c2);
  }
  
  @Test
  public void testGetConnectionDef() throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    ConnectionType expected = new ConnectionType();
    s.addConnectionTypeDefinition("foo", expected);
    ConnectionType actual = s.getConnectionTypeDefinition("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected=UndeclaredIdentifierException.class)
  public void testGetConnectionDef_notDeclared() throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    ConnectionType bogus = s.getConnectionTypeDefinition("does-not-exist");
  }
  
  @Test
  public void testAddConstraintDef() throws MultipleDefinitionException {
    Schematic s = new Schematic("test");
    ConstraintType e1 = new ConstraintType();
    s.addConstraintTypeDefinition("e1", e1);
  }
  
  @Test(expected=MultipleDefinitionException.class)
  public void testAddConstraintDef_multipleDefinitions() throws MultipleDefinitionException {
    // We should not be able to add two constraint definitions whose first argument is the same string.
    Schematic s = new Schematic("test");
    try{
      ConstraintType e1 = new ConstraintType();
      s.addConstraintTypeDefinition("foo", e1);
    }catch(MultipleDefinitionException mde){
      fail("exception thrown too early: " + mde.getMessage());
    }
    ConstraintType e2 = new ConstraintType();
    s.addConstraintTypeDefinition("foo", e2);
  }

  @Test
  public void testGetConstraintDef() throws UndeclaredIdentifierException, MultipleDefinitionException{
    Schematic s = new Schematic("test");
    ConstraintType expected = new ConstraintType();
    s.addConstraintTypeDefinition("foo", expected);
    ConstraintType actual = s.getConstraintTypeDefinition("foo");
    assertEquals(expected, actual);
  }
  
  @Test(expected=UndeclaredIdentifierException.class)
  public void testGetConstraintDef_notDeclared() throws UndeclaredIdentifierException {
    Schematic s = new Schematic("test");
    ConstraintType bogus = s.getConstraintTypeDefinition("does-not-exist");
  }
  
  @Test
  public void testSeparationOfNamespaces_Definitions() throws MultipleDefinitionException{
    // We should be able to add one of each of a TypeDefinition, ConstraintDefinition,
    // ConnectionDefinition, NodeDefinition, and EndpointDefinition with the same name
    // without encountering a "multiple definition" exception.
    Schematic s = new Schematic("test");
    
    UserDefinedType t1 = new UserDefinedType(new PrimitiveType(PrimitiveType.PrimitiveKind.STRING));
    ConstraintType ct1 = new ConstraintType();
    ConnectionType cn1 = new ConnectionType();
    NodeType n1 = new NodeType();
    EndpointType e1 = new EndpointType();
    
    s.addTypeTypeDefinition("foo", t1);
    s.addConstraintTypeDefinition("foo", ct1);
    s.addConnectionTypeDefinition("foo", cn1);
    s.addNodeTypeDefinition("foo", n1);
    s.addEndpointTypeDefinition("foo", e1);
  }
  
}
