// generated with ast extension for cup
// version 0.8
// 25/5/2022 12:9:21


package rs.ac.bg.etf.pp1.ast;

public class ArrayParameterDecl extends FormalParamDecl {

    private Type Type;
    private String arrParamName;

    public ArrayParameterDecl (Type Type, String arrParamName) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.arrParamName=arrParamName;
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public String getArrParamName() {
        return arrParamName;
    }

    public void setArrParamName(String arrParamName) {
        this.arrParamName=arrParamName;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ArrayParameterDecl(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+arrParamName);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ArrayParameterDecl]");
        return buffer.toString();
    }
}
