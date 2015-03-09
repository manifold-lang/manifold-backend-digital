package org.manifold.compiler.back.digital.smt2;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

public class ParenList extends SExpression {

  private List<SExpression> exprs = new ArrayList<>();
  public List<SExpression> getExprs() {
    return ImmutableList.copyOf(exprs);
  }
  
  public ParenList() { }
  
  public ParenList(SExpression... exprs) {
    for (SExpression expr : exprs) {
      this.exprs.add(expr);
    }
  }
  
  public ParenList(List<SExpression> exprs) {
    this.exprs.addAll(exprs);
  }
  
  @Override
  public void write(Writer writer) throws IOException {
    writer.write('(');
    for (SExpression expr : exprs) {
      writer.write(' ');
      expr.write(writer);
    }
    writer.write(' ');
    writer.write(')');
  }

}
