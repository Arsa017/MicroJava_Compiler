// generated with ast extension for cup
// version 0.8
// 25/5/2022 12:9:21


package rs.ac.bg.etf.pp1.ast;

public class VarDeclaration extends VarDecl {

    private Type Type;
    private VarIdentifier VarIdentifier;
    private VarIdentifierList VarIdentifierList;

    public VarDeclaration (Type Type, VarIdentifier VarIdentifier, VarIdentifierList VarIdentifierList) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.VarIdentifier=VarIdentifier;
        if(VarIdentifier!=null) VarIdentifier.setParent(this);
        this.VarIdentifierList=VarIdentifierList;
        if(VarIdentifierList!=null) VarIdentifierList.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public VarIdentifier getVarIdentifier() {
        return VarIdentifier;
    }

    public void setVarIdentifier(VarIdentifier VarIdentifier) {
        this.VarIdentifier=VarIdentifier;
    }

    public VarIdentifierList getVarIdentifierList() {
        return VarIdentifierList;
    }

    public void setVarIdentifierList(VarIdentifierList VarIdentifierList) {
        this.VarIdentifierList=VarIdentifierList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
        if(VarIdentifier!=null) VarIdentifier.accept(visitor);
        if(VarIdentifierList!=null) VarIdentifierList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(VarIdentifier!=null) VarIdentifier.traverseTopDown(visitor);
        if(VarIdentifierList!=null) VarIdentifierList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(VarIdentifier!=null) VarIdentifier.traverseBottomUp(visitor);
        if(VarIdentifierList!=null) VarIdentifierList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarDeclaration(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarIdentifier!=null)
            buffer.append(VarIdentifier.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarIdentifierList!=null)
            buffer.append(VarIdentifierList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarDeclaration]");
        return buffer.toString();
    }
}
