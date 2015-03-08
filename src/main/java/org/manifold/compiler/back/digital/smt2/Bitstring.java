package org.manifold.compiler.back.digital.smt2;

import java.io.IOException;
import java.io.Writer;

public class Bitstring extends SExpression {

  private final String bitstring;
  
  public Bitstring(String bitstring) {
    this.bitstring = bitstring;
    // TODO verify well-formed bitstring
  }
  
  @Override
  public void write(Writer writer) throws IOException {
    writer.write("#b");
    writer.write(bitstring);
  }

}
