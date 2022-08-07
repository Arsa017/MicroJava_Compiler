// generated with ast extension for cup
// version 0.8
// 25/5/2022 12:9:21


package rs.ac.bg.etf.pp1.ast;

public class PrintStmtNumber extends SingleStatement {

    private Expr Expr;
    private Integer numCnst;

    public PrintStmtNumber (Expr Expr, Integer numCnst) {
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
        this.numCnst=numCnst;
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr=Expr;
    }

    public Integer getNumCnst() {
        return numCnst;
    }

    public void setNumCnst(Integer numCnst) {
        this.numCnst=numCnst;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Expr!=null) Expr.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("PrintStmtNumber(\n");

        if(Expr!=null)
            buffer.append(Expr.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+numCnst);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [PrintStmtNumber]");
        return buffer.toString();
    }
}
