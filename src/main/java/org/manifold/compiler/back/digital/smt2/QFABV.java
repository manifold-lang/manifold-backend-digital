package org.manifold.compiler.back.digital.smt2;


// Code generation helpers for SMT2 QF_ABV expressions
public class QFABV {

  public static Symbol getStateVariable(String name, int timestep) {
    String suffix = "__" + Integer.toString(timestep);
    return new Symbol(name + suffix);
  }
  
  public static SExpression declareBitVector(Symbol sym, long width) {
    SExpression exprs[] = new SExpression[] {
        new Symbol("declare-fun"),
        sym,
        new ParenList(),
        new ParenList(new SExpression[]{
            new Symbol("_"), new Symbol("BitVec"), new Numeral(width)})
        };
    return new ParenList(exprs);
  }
  
  private static SExpression infix(SExpression e1, String op, SExpression e2) {
    SExpression exprs[] = new SExpression[] {
        new Symbol(op),
        e1,
        e2
    };
    return new ParenList(exprs);
  }
  
  public static SExpression equal(SExpression e1, SExpression e2) {
    return infix(e1, "=", e2);
  }
  
  public static SExpression assertThat(SExpression term) {
    SExpression assertExprs[] = new SExpression[] {
        new Symbol("assert"),
        term
    };
    return new ParenList(assertExprs);
  }
  
  //(assert (= sym #b0))
  public static SExpression assertBitZero(Symbol sym) {
    return assertThat(equal(sym, new Bitstring("0")));
  }
  
  // (assert (= sym #b1))
  public static SExpression assertBitOne(Symbol sym) {
    return assertThat(equal(sym, new Bitstring("1")));
  }
  
  public static SExpression and(SExpression e1, SExpression e2) {
    return infix(e1, "bvand", e2);
  }
  
  public static SExpression or(SExpression e1, SExpression e2) {
    return infix(e1, "bvor", e2);
  }
  
  public static SExpression not(SExpression e) {
    SExpression exprs[] = new SExpression[] {
        new Symbol("bvnot"),
        e
    };
    return new ParenList(exprs);
  }
  
  public static SExpression conditional(
      SExpression cond, SExpression t, SExpression f) {
    SExpression exprs[] = new SExpression[] {
        new Symbol("ite"), // "if-then-else"
        cond,
        t,
        f
    };
    return new ParenList(exprs);
  }
  
}
