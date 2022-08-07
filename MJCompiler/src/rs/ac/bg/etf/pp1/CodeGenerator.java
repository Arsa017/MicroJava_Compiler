package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.*;
//import rs.ac.bg.etf.pp1.MyTab;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.concepts.*;
import rs.ac.bg.etf.pp1.SemanticAnalyzer;

import java.util.ArrayList;
//import rs.ac.bg.etf.pp1.LabelStruct;


public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	
	private ArrayList<LabelStruct> definisaneLabele = new ArrayList<>();
	private ArrayList<LabelStruct> nedefinisaneLabele = new ArrayList<>();

	public int getMainPc() {
		return mainPc;
	}
	
	// akcije vezane za ulazak u metodu
	public void visit(MethodTypeName methodTypeName) {
		if (methodTypeName.getMethodName().equalsIgnoreCase("main")) {
			this.mainPc = Code.pc;
		}
		
		Obj methObj = methodTypeName.obj;
		methObj.setAdr(Code.pc);
		
		int numOfArg = methObj.getLevel();
		int nummOfLocals = methObj.getLocalSymbols().size();
		
		Code.put(Code.enter);
		Code.put(numOfArg);
		Code.put(numOfArg + nummOfLocals);
	}
	
	// akcije vezane za izlazak iz metode
	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
		
//		for (LabelStruct ls : definisaneLabele) {
//			System.out.println("ime: " + ls.nazivLabele);
//		}
//		
//		for (LabelStruct ls : nedefinisaneLabele) {
//			System.out.println("ime: " + ls.nazivLabele);
//		}
		
	}
	
	// Print
	public void visit(PrintStmt printStmt) {
		Struct p = printStmt.getExpr().struct;
		if (p == MyTab.intType || p == MyTab.boolType) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}
	
	public void visit(PrintStmtNumber printStmtNum) {
		Struct p = printStmtNum.getExpr().struct;
		int num = printStmtNum.getNumCnst().intValue();
		if (p == MyTab.intType || p == MyTab.boolType) {
			Code.loadConst(num);
			Code.put(Code.print);
		} else {
			Code.loadConst(num);
			Code.put(Code.bprint);
		}
	}
	
	// Read
	public void visit(ReadStmt readStmt) {
		Obj readObj = readStmt.getDesignator().obj;
		if (readObj.getType() == MyTab.charType) 
			Code.put(Code.bread);
		else 
			Code.put(Code.read);
		
		Code.store(readObj);
	}
	
	// Constante
	public void visit(NumFactor numFact) {
		Obj con = new Obj(Obj.Con, "numCnst", MyTab.intType, numFact.getNumCnst().intValue(), 0);
		Code.load(con);
	}
	
	public void visit(CharFactor charFact) {
		Obj con = new Obj(Obj.Con, "charCnst", MyTab.charType, charFact.getCharCnst().charValue(), 0);
		Code.load(con);
	}
	
	public void visit(BoolFactor boolFact) {
		Obj con = new Obj(Obj.Con, "boolCnst", MyTab.boolType, boolFact.getBoolCnst().compareTo(false), 0);
		Code.load(con);
	}

	// Izraz dodele vrednosti
	public void visit(DesignatorAssignment assignment) {
		Code.store(assignment.getDesignator().obj);
	//	System.out.println("Linija: " + assignment.getLine() + " Tip: " + assignment.getDesignator().obj.getKind());
	}
	
	// akcije za smestanje na ExprStek PROMENLJIVE koja ucestvuje u izrazima
	public void visit(DesignatorNoArray dna) {
		SyntaxNode parent = dna.getParent();	// parent.class = DesigFactor.Class
		
		if (DesignatorAssignment.class != parent.getClass() && ReadStmt.class != parent.getClass()
				&& DesignatorFunc.class != parent.getClass() && FuncCallFactor.class != parent.getClass()) {
			Code.load(dna.obj);
		}	
	}
	
	// akcije za smestanje na ExprStek: ELEMENTI NIZA(promenljive) koji ucestvuju u izrazima
	public void visit(DesignatorArray da) {
		// smesta se prvo ADRESA niza, tj. obradi ArrayName pa se obradi Expr i smesti indeks elementa
	}
	
	public void visit(ArrayName arrayName) {
		Code.load(arrayName.obj);	// ucitavanje adrese niza na stek; pri koriscenju u izrazima
	}
	
	// ukoliko nam kao promenljiva u izrazu ucestvuje element niza, ovde ga ucitavamo na ExprStack; za obicne tipove njihovo ucitavanje se vrsi u akcijama za DesignatorNoArray
	public void visit(DesigFactor desigFact) {	
		Designator d = desigFact.getDesignator();
		if (d instanceof DesignatorArray) {
			if (d.obj.getType() == MyTab.charType) {
				Code.put(Code.baload);
			} else {
				Code.put(Code.aload);
			}
		} 
	}
	
	// akcije za konstrukciju niza
	public void visit(ConstructorFact conFact) {
		Struct tip = conFact.getType().struct;
		int b = (tip == MyTab.charType) ? 0 : 1;
		
		Code.put(Code.newarray);
		Code.loadConst(b);
	}
	
	// ExpopFactor -> izraz za stepenovanje
	// Obilaskom ove metode na ExprStack-u ce se nalaziti osnova i eksponent za operaciju stepenovanja ----->   2 ^ 3
	public void visit(ExpopFactor expFactor) {
		int preskoci=0; 			    // fixup(preskoci);	-> ovde pamtimo poziciju za prepravku ako ne treba da dodje do obrade; vec je EXP = 0
		int adr1=0;						// fixup(adr1);	-> ovde pamtimo poziciju za prepravku u instrukciji skoka za skok UNAPRED 
		int adr2=0;						// adresa pocetka petlje; za skok UNAZAD na novu iteraciju OBRADE
		int adr3=0;						// adresa na koju se skace nakon zavrsetka petlja OBRADA, tj nakon zavrsetka racunanja ukoliko EXP nije bio NULA
		
		// a, b
		Code.put(Code.dup);				// a, b, b
		Code.loadConst(0);  			// a, b, b, 0
		Code.put(Code.jcc + Code.eq);   
		Code.put2(0);					// a, b			*** U slucaju da je EXP = 0, rezultat operacije je uvek 1
		preskoci = Code.pc - 2;
		
		// ako uslov nije ispunjen, krece se u obradu
		// transformacija steka --->  a, b    ==>  a, a, b
		
		Code.put(Code.dup_x1);			// b, a, b
		Code.put(Code.pop);				// b, a
		Code.put(Code.dup_x1);			// a, b, a
		Code.put(Code.dup_x1);			// a, a, b, a
		Code.put(Code.pop);				// a, a, b							  <-- deo koji se izvrsi samo jednom
		
		adr2 = Code.pc;					// <--- **Obrada**        Pamcenje adrese za novu iteraciju, tj. za skok unazad; inicijalno rez = a
		
		Code.put(Code.dup);				// rez, a, b, b
		Code.loadConst(1); 				// rez, a, b, b, 1
		Code.put(Code.jcc + Code.eq);	
		Code.put2(0);					// rez, a, b
		adr1 = Code.pc - 2;				// <--- vrednost koju prepravljamo za izlazak iz petlje obrada; tj. skace se na ovaj pomeraj kada uslov postane ispunjen
		
		Code.put(Code.dup_x2);			// b, rez, a, b
		Code.put(Code.pop);				// b, rez, a
		Code.put(Code.dup);				// b, rez, a, a
		Code.put(Code.dup_x2);			// b, a, rez, a, a 
		Code.put(Code.pop);				// b, a, rez, a
		Code.put(Code.mul);				// b, a, rez*a
		Code.put(Code.dup_x2);			// rez*a, b, a, rez*a
		Code.put(Code.pop);				// rez*a, b, a
		Code.put(Code.dup_x1);			// rez*a, a, b, a
		Code.put(Code.pop);				// rez*a, a, b	
		Code.loadConst(1);				// rez*a, a, b, 1
		Code.put(Code.sub);				// rez*a, a, b-1
		
		Code.putJump(adr2);				// skok na novu iteraciju
		Code.fixup(adr1);
		
		// *** Ovde je kraj obrade i rezultat se nalazi na steku u redosledu ;   										
										// res, base, 1
		Code.put(Code.pop);				// res, base
		Code.put(Code.pop);				// res
		
		Code.put(Code.jmp);				// EXP nije bio 0 pa taj kod treba preskociti					
		Code.put2(0);
		adr3 = Code.pc - 2;				// <--- ** pamcenje adrese BEZUSLOVNOG skoka koji treba da	se izvrsi nakon zavrsavanja OBRADE; 
		
		Code.fixup(preskoci);			// a, b		
		Code.put(Code.pop); 			// a
		Code.put(Code.pop);				// 
		Code.loadConst(1);				// 1
		
		Code.fixup(adr3);
	
	}
	
	// MulopTerm -> izraz za mnozenje, deljenje i ostatak pri deljenju
	public void visit(MulopTerm mulTerm) {
		Mulop mulop = mulTerm.getMulop();		
		if (mulop instanceof Mul) 
			Code.put(Code.mul);
		if (mulop instanceof Div)
			Code.put(Code.div);		
		if (mulop instanceof Mod)
			Code.put(Code.rem);
	}
		
	// ExprDecl -> akcije za izraze sabiranja/oduzimanja
	public void visit(AddSubExpr addSubExpr) {
		Addop op = addSubExpr.getAddop();
		if (op instanceof Plus) 
			Code.put(Code.add);
		if (op instanceof Minus)
			Code.put(Code.sub);
	}
	
	public void visit(MinusTermExpr minusTermExpr) {
		Code.put(Code.neg);
	}
	
	// Designator INC	-> inkrementrianje
	public void visit(DesignatorInc desigInc) {
		Obj d = desigInc.getDesignator().obj;
		Designator designator = desigInc.getDesignator();
		
		if (designator instanceof DesignatorArray) {
			Code.put(Code.dup2);
			if (d.getType() != MyTab.charType) 
				Code.put(Code.aload);
			else 
				Code.put(Code.baload);
		}
		
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(d);
	}
	
	// Designator DEC   -> dekrementiranje
	public void visit(DesignatorDec desigDec) {
		Obj d = desigDec.getDesignator().obj;
		Designator designator = desigDec.getDesignator();
		
		if (designator instanceof DesignatorArray) {
			Code.put(Code.dup2);
			if (d.getType() != MyTab.charType) 
				Code.put(Code.aload);
			else 
				Code.put(Code.baload);
		}
		
		Code.loadConst(1);
		Code.put(Code.sub);
		Code.store(d);
	}
	
	// Skokovi GOTO
	
	public void visit(Label label) {
		SyntaxNode parent = label.getParent();
		String nazivLabele = label.getLabelName();
		
		if (StmtWithLabel.class == parent.getClass()) {
			boolean nedefinisana = false;
			LabelStruct nadjenaLabela = null;
			
			for (LabelStruct ls : nedefinisaneLabele) {
				if (ls.nazivLabele.equals(nazivLabele)) {
					nadjenaLabela = ls;
					nedefinisana = true;
				}
			}
			
			if (nedefinisana) {
				// skok unapred
				int offset = Code.pc - (nadjenaLabela.adresa - 1);	// -1 je zbog postavljene instrukcije jmp koja vec zauzima 1 bajt; a pomeraj racunamo u odnosu na pocetak instrukcije
				Code.put2(nadjenaLabela.adresa, offset);
				
		//		nedefinisaneLabele.remove(nadjenaLabela);
			} else {
				// skok unazad
				int adresa = Code.pc;
				LabelStruct struct = new LabelStruct(nazivLabele, adresa); 		// nailazkom na labelu dodajemo je u listu definisanih labela
				definisaneLabele.add(struct);
			}
		}
		
		
		if (GotoStmt.class == parent.getClass()) {
			LabelStruct nadjenaLabela = null;
			boolean nadjena = false;
			
			// da li je skok unazad
			for (LabelStruct ls : definisaneLabele) {
				if (ls.nazivLabele.equals(nazivLabele)) {
					nadjenaLabela = ls;			
					nadjena = true;
				}
			}
			
			if (nadjena) {
				// skok unazad
				int offset = nadjenaLabela.adresa - Code.pc;
				Code.put(Code.jmp);
				Code.put2(offset);				
		//		definisaneLabele.remove(nadjenaLabela);
			} else {
				// skok unapred
				Code.put(Code.jmp);
				int myPC = Code.pc;		// adresa koju je potrebno prepraviti
				Code.put2(0); 		
							
				LabelStruct nepoznata = new LabelStruct(nazivLabele, myPC);
				nedefinisaneLabele.add(nepoznata);
			}			
		}	
		
	}
	

}

/*
 Postupak u slucaju da nam treba instrukcija skoka (uslovnog/bezuslovnog), za skok UNAPRED
	
	   int adr = 0;				// adresa prvog bajta koja se treba prepraviti   ---> JMP _ _;   
																						  ^
																						  |
																						  adr 	
	
	   Code.put(Code.jmp)		// Code.put(Code.jcc + Code.eq)..
	   Code.put2(0)				// ovo je pomeraj koji trenutno ne znamo i koji kasnije treba prepraviti	
	   adr = Code.pc - 2;
	   
	   ...
	   ...
	   ...
	   
	   fixup(adr);				// ova f-ja se poziva u trenutku kad znamo ODREDISTE skoka, tj na kraju petlje.. ili na kraju koda koji preskacemo 
	   
********************************************************************************************************************************************

Postupak u slucaju da nam treba instrukcija skoka (uslovnog/bezuslovnog), za skok UNAZAD

		int adr = 0;			// adresa instrukcije na koju treba da se skoci(unazad)

		....
		
		adr = Code.pc;			// adr ce pokazivati na prvi bajt instrukcije instr_1;
		instr_1;
		instr_2;
		
		...
		...
		
		Code.putJump(adr);
		
 
 */ 
