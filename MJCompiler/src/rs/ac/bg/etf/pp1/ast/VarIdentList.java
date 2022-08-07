// generated with ast extension for cup
// version 0.8
// 25/5/2022 12:9:21


package rs.ac.bg.etf.pp1.ast;

public class VarIdentList extends VarIdentifierList {

    private VarIdentifierList VarIdentifierList;
    private VarIdentifier VarIdentifier;

    public VarIdentList (VarIdentifierList VarIdentifierList, VarIdentifier VarIdentifier) {
        this.VarIdentifierList=VarIdentifierList;
        if(VarIdentifierList!=null) VarIdentifierList.setParent(this);
        this.VarIdentifier=VarIdentifier;
        if(VarIdentifier!=null) VarIdentifier.setParent(this);
    }

    public VarIdentifierList getVarIdentifierList() {
        return VarIdentifierList;
    }

    public void setVarIdentifierList(VarIdentifierList VarIdentifierList) {
        this.VarIdentifierList=VarIdentifierList;
    }

    public VarIdentifier getVarIdentifier() {
        return VarIdentifier;
    }

    public void setVarIdentifier(VarIdentifier VarIdentifier) {
        this.VarIdentifier=VarIdentifier;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(VarIdentifierList!=null) VarIdentifierList.accept(visitor);
        if(VarIdentifier!=null) VarIdentifier.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(VarIdentifierList!=null) VarIdentifierList.traverseTopDown(visitor);
        if(VarIdentifier!=null) VarIdentifier.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(VarIdentifierList!=null) VarIdentifierList.traverseBottomUp(visitor);
        if(VarIdentifier!=null) VarIdentifier.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("VarIdentList(\n");

        if(VarIdentifierList!=null)
            buffer.append(VarIdentifierList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(VarIdentifier!=null)
            buffer.append(VarIdentifier.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [VarIdentList]");
        return buffer.toString();
    }
}
