package rs.ac.bg.etf.pp1;

import org.apache.log4j.*;
import rs.ac.bg.etf.pp1.ast.*;

public class RuleVisitor extends VisitorAdaptor {

	Logger log = Logger.getLogger(getClass());
	
	int constDeclCount = 0;
	int numberConstCount = 0;
	
	int varCount = 0;
	int numOfArray = 0;
	
//	public void visit(ConstDecl ConstDecl) { 
//	 	constDeclCount++;
//	 	log.info("Prepoznata deklaracija konstante!");
//	}
//	
//	public void visit(NumberConst NumberConst) { 
//		numberConstCount++;
//	}

	public void visit(VarIdentifierDecl VarIdentifierDecl) {
		varCount++;
	}
	
	public void visit(ArrayIdentDecl ArrayIdentDecl) { 
		numOfArray++;
	}
	
}
