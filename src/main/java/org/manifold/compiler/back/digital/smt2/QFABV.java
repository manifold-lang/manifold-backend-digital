package org.manifold.compiler.back.digital.smt2;


// Code generation helpers for SMT2 QF_ABV expressions
public class QFABV {

  public static Symbol getStateVariable(String name, int timestep) {
    String suffix = "__" + Integer.toString(timestep);
    return new Symbol(name + suffix);
  }
  
  public static SExpression declareBitVector(Symbol sym, long width) {
    return new ParenList(new Symbol("declare-fun"),
        sym,
        new ParenList(),
        new ParenList(new Symbol("_"), new Symbol("BitVec"), new Numeral(width)
        ));
  }
  
  private static SExpression infix(SExpression e1, String op, SExpression e2) {
    return new ParenList(new Symbol(op),
        e1,
        e2);
  }
  
  public static SExpression equal(SExpression e1, SExpression e2) {
    return infix(e1, "=", e2);
  }
  
  public static SExpression assertThat(SExpression term) {
    return new ParenList(new Symbol("assert"), term);
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
    return new ParenList(new Symbol("bvnot"), e);
  }
  
  public static SExpression conditional(
      SExpression cond, SExpression t, SExpression f) {
    return new ParenList(new Symbol("ite"), // "if-then-else"
        cond,
        t,
        f);
  }
  
}
