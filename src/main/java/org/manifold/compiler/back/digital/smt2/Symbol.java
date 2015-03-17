package org.manifold.compiler.back.digital.smt2;

import java.io.IOException;
import java.io.Writer;

public class Symbol extends SExpression {
  private final String name;
  public String getName() {
    return name;
  }
  
  public Symbol(String name) {
    this.name = name;
  }
  
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Symbol)) {
      return false;
    }
    Symbol that = (Symbol) other;
    return (this.getName().equals(that.getName()));
  }
  
  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public void write(Writer writer) throws IOException {
    writer.write(getName());
  }
  
}
