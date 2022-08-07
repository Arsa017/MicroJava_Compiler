package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
//import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {

	Logger log = Logger.getLogger(getClass());
	
	Obj currentMethod = null;		// trenutno obradjivana metoda
	Struct currentType = null;		// trenutni tip kod; za pomoc kod deklaracije veceg broja promenljivih u jednoj liniji; npr: int x, y, z....
	
	public static int globalVarCnt = 0;
	public static int localVarCnt = 0;
	public static int constantCnt = 0;
	public static int statementsCnt = 0;
	
	private static boolean errorDetected = false;
	int nVars;
	private static boolean mainExists = false;
	
	private ArrayList<String> defLabelsList = null;	// lista definisanih labela
	private ArrayList<String> undefLabelsList = null;	// lista nedefinisanih labela
	
	
	
	public static int getGlobalVarCnt() {
		return globalVarCnt;
	}
	public static int getLocalVarCnt() {
		return localVarCnt;
	}
	public static int getConstantCnt() {
		return constantCnt;
	}
	public static int getStatementsCnt() {
		return statementsCnt;
	}
	
//	public static Scope myScope = null;		// pomocno polje za MyTab.Find f-ju u CodeGeneratoru
	
	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message); 
		int line = (info == null) ? 0: info.getLine();
		if (line != 0)
			msg.append (" na liniji ").append(line);
		log.info(msg.toString());
	}
		
	// Program
	
	public void visit(ProgName progName) {
		progName.obj = MyTab.insert(Obj.Prog, progName.getProgName(), MyTab.noType); // u objektni cvor neterminala progName ubacujemo obj. cvor koji smo dodali u tabelu simbola
		MyTab.openScope();
		
//		this.myScope = MyTab.currentScope;
	}
	
	public void visit(Program program) {
		nVars = MyTab.currentScope.getnVars();
		
		// provera da li u programu postoji main f-ja; ako ne postoji prijavljujemo gresku
		if(!mainExists) {
			report_error("Greska! U programu nije deklarisana funkcija main koja je obavezna!", null);
		}
		
		MyTab.chainLocalSymbols(program.getProgName().obj); // ulancavanje svih deklarisanih simbola u locals programa
		MyTab.closeScope();
	}
	
	// Type
	
	public void visit(Type type) {
		// provera da li se radi o Tipu; pokusavamo da dohvatimo iz tabele simbola objektni cvor po imenu typeName ugradjenog u neterminal
		Obj typeNode = MyTab.find(type.getTypeName());
		if (typeNode == MyTab.noObj) {		// noObj se vraca u slucaju da nije pronadjen objektni cvor za prosledjeni parametar u tabeli simbola
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola", type);
			type.struct = currentType = MyTab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {					// da li je objektni cvor, koji je pronadjen, po tipu Type
				type.struct = currentType = typeNode.getType();		// ako jeste azuriramo tip strukturnog cvora
			} else {
				// nadjen je objektni cvor koji po tipu nije Type nego nesto drugo, npr Var...
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja naziv tipa!", type);
				type.struct = currentType = MyTab.noType;				
			}
		}	
	}
	
	// Constant
	
	public void visit(ConstDecl constDecl) {		// const int x = 2;
		if(constDecl.getType().struct != constDecl.getConstType().obj.getType()) {
			report_error("Greska na liniji " + constDecl.getLine() + ": Konstanta " + constDecl.getConstName() + " nije odgovarajuceg tipa!", null);
		} else {
			Obj constNode = MyTab.find(constDecl.getConstName());
			if (constNode != MyTab.noObj) {
				report_error("Greska na liniji " + constDecl.getLine() + ": Konstanta " + constDecl.getConstName() + " je vec deklarisana!", null);
			} else {
				// prvo pojavljivanje konstante, pa je dodajemo u tabelu simbola;
				Obj newConst = new Obj(Obj.Con, constDecl.getConstName(), constDecl.getType().struct, constDecl.getConstType().obj.getAdr(), 0);
				MyTab.currentScope.addToLocals(newConst);
				report_info("Pretraga na liniji: "+ constDecl.getLine() + "; Pronadjena konstanta " + constDecl.getConstName(), null);
				constantCnt++;
			}
		}
		
	}
	
	// slucaj kada je vise konstanti definisano u jednom redu  -> const int x = 2, y = 3;   --> ovde se obrada odnosi na y
	public void visit(ConstIdentifierDecl constIdent) {   
		if(!currentType.compatibleWith(constIdent.getConstType().obj.getType())) {
			report_error("Greska na liniji " + constIdent.getLine() + ": Konstanta " + constIdent.getConstName() + " nije odgovarajuceg tipa!", null);
		} else {
			Obj constNode = MyTab.find(constIdent.getConstName());
			if (constNode != MyTab.noObj) {
				report_error("Greska na liniji " + constIdent.getLine() + ": Konstanta(Identifikator) " + constIdent.getConstName() + " je vec deklarisana!", null);
			} else {
				Obj newConst = new Obj(Obj.Con, constIdent.getConstName(), constIdent.getConstType().obj.getType(), constIdent.getConstType().obj.getAdr(), 0);
				MyTab.currentScope.addToLocals(newConst);
				report_info("Pretraga na liniji: "+ constIdent.getLine() + "; Pronadjena konstanta " + constIdent.getConstName(), null);
				constantCnt++;
			}
		}
	}
	
	// ConstType ce nam biti objektnog tipa da bi u njenom objektu sacuvali TIP konstante i njenu VREDNOST, kako bi ih dodali u tabelu simbola
	public void visit(NumberConst numCnst) {
		numCnst.obj = new Obj(Obj.Con, "numCnst", MyTab.intType, numCnst.getNumVal().intValue(), 0);
	}
	
	public void visit(CharacterConst charCnst) {
		charCnst.obj = new Obj(Obj.Con, "charCnst", MyTab.charType, charCnst.getCharVal().charValue(), 0);
	}
	
	public void visit(BooleanConst boolCnst) {
		boolCnst.obj = new Obj(Obj.Con, "boolCnst", MyTab.boolType, boolCnst.getBoolVal().compareTo(false), 0);
	}
	
	// Variable
	
	public void visit(VarIdentifierDecl varIdentDecl) {
		Obj varIdentNode = MyTab.find(varIdentDecl.getVarName());
		
		// u slucaju da je pronadjen identifikator znaci da je promenljiva vec bila deklarisana
		if(varIdentNode != MyTab.noObj) {	
			report_error("Greska na liniji " + varIdentDecl.getLine() + ": Promenljiva(Identifikator) " + varIdentDecl.getVarName() + " je vec deklarisan", null);
		} else {
			// u slucaju da promenljiva nije deklarisana dodajemo je u tabelu simbola
			MyTab.insert(Obj.Var, varIdentDecl.getVarName(), currentType);
			report_info("Pretraga na liniji: "+ varIdentDecl.getLine() + "; Pronadjena promenljiva " + varIdentDecl.getVarName(), null);
			if(currentMethod == null) {
				globalVarCnt++;
			} else {
				localVarCnt++;
			}
		}	
	}
	
	// nizovi
	public void visit(ArrayIdentDecl arrayIdentDecl) {
		Obj arrayNode = MyTab.find(arrayIdentDecl.getArrayName());
		
		// da li je niz sa istim nazivom vec deklarisan
		if (arrayNode != MyTab.noObj) {
			report_error("Greska na liniji " + arrayIdentDecl.getLine() + ": Niz(Identifikator) " + arrayIdentDecl.getArrayName() + " je vec deklarisan", null);
		} else {
			// u slucaju da niz nije deklarisan dodajtemo ga u tabelu simbola
			Obj obj = MyTab.insert(Obj.Var, arrayIdentDecl.getArrayName(), new Struct(Struct.Array, currentType));
			report_info("Pretraga na liniji: "+ arrayIdentDecl.getLine() + "; Pronadjena promenljiva(Niz) " + arrayIdentDecl.getArrayName(), null);
	//		System.out.println("Naziv niza: " + obj.getName() + " tip = " + obj.getKind() + " adr = " + obj.getAdr() + " glob/lok = " + obj.getLevel());
			if(currentMethod == null) {
				globalVarCnt++;
			} else {
				localVarCnt++;
			}
		}		
	}
	
	// Method - void main(); funkcija u program.mj mora da postoji
	
	public void visit(MethodTypeName methodTypeName) {
		if(methodTypeName.getMethodName().equalsIgnoreCase("main")) {
			mainExists = true;
		}
		
		if (methodTypeName.getMethodRetType().struct != MyTab.noType && mainExists) {
			report_error("Greska na liniji " + methodTypeName.getLine() + ": Povratni tip metode " + methodTypeName.getMethodName()  +  " nije odgovarajuci!", null);
		} else {
			methodTypeName.obj = currentMethod = MyTab.insert(Obj.Meth, methodTypeName.getMethodName(), methodTypeName.getMethodRetType().struct);
			MyTab.openScope();
			report_info("Pretraga na liniji: "+ methodTypeName.getLine() + "; Obradjuje se funkcija " + methodTypeName.getMethodName(), null);		
		}
		
		defLabelsList = new ArrayList<>();
		undefLabelsList = new ArrayList<>();
	}
	
	public void visit(MethodDecl methodDecl) {
		if (currentMethod != null) {
			if (!undefLabelsList.isEmpty()) {
				for (String l : undefLabelsList) {
					report_error("Greska u funciji " + currentMethod.getName() + "! Pokusava se skok na labelu *" + l + "* koja nije definisana!", null);
				}
			}
			
			MyTab.chainLocalSymbols(currentMethod);
			MyTab.closeScope();
		}
		
		defLabelsList = null;
		undefLabelsList = null;
		currentMethod = null;
	}
	
	public void visit(ReturnType retType) {
		retType.struct = retType.getType().struct;
	}
	
	public void visit(NoReturn noRetType) {
		noRetType.struct = MyTab.noType;
	}
	
	// koriscenje ubacenih imena iz tabele simbola za obradjivanje izraza(Statementa), tj. za proveru kontekstnih uslova
	
	// Designator
	
	// pomocne f-je za ispis tipova pronadjenih identifikatora
	public String getStructType(int kind) {	
		switch(kind) 
		{
		case 0 : return "none";
		case 1 : return "int";
		case 2 : return "char";
		case 3 : return "array";
		case 4 : return "class";
		case 5 : return "bool";
		}
		return "";	
	}
	
	public String getObjType(int kind) {
		switch(kind) 
		{
		case 0 : return "Con";
		case 1 : return "Var";
		case 2 : return "Type";
		case 3 : return "Meth";
		case 4 : return "Fld";
		case 5 : return "Elem";
		case 6 : return "Prog";
		}
		return "";
	}
	
	// promenljiva
	public void visit(DesignatorNoArray desigNoArr) {
		Obj obj = MyTab.find(desigNoArr.getDesName());
		if(obj != MyTab.noObj) {	// da li se designator koristi kao promenljiva/niz u nekom izrazu
			if(obj.getType().getKind() != Struct.Array) {
				// promenljiva
				report_info("Pretraga na liniji " + desigNoArr.getLine() + "(" + desigNoArr.getDesName() + "), nadjeno " + getObjType(obj.getKind()) + " " + desigNoArr.getDesName() + ": " + getStructType(obj.getType().getKind()) + ", " + obj.getAdr() + ", " + obj.getLevel(), null);                                                                                 
			} else {
				// referenca na niz (npr: niz = new int[size])
				report_info("Pretraga na liniji " + desigNoArr.getLine() + "(" + desigNoArr.getDesName() + "), nadjeno " + getObjType(obj.getKind()) + " " + desigNoArr.getDesName() + ": " + getStructType(obj.getType().getKind()) + " of " + getStructType(obj.getType().getElemType().getKind()) + ", " + obj.getAdr() + ", " + obj.getLevel(), null);
			}
		} else {
			// designator nije pronadjen u tabeli simbola; nije deklarisan
			report_error("Greska na liniji " + desigNoArr.getLine() + ": ime " + desigNoArr.getDesName() + " nije deklarisano! ", null);
		}		
		
		desigNoArr.obj = obj;
	}
	
	public void visit(ArrayName arrayName) {
		arrayName.obj = MyTab.find(arrayName.getNaziv());	
	}
	
	// niz <- ako smo u ovoj smeni sigurno se radi o pristupu elementu niza!!!
	public void visit(DesignatorArray desigArray) {
		Obj obj = MyTab.find(desigArray.getArrayName().getNaziv());
		if(obj == MyTab.noObj) { // da li je designator deklarisana promenljiva
			report_error("Greska na liniji " + desigArray.getLine() + ": Ime " + desigArray.getArrayName().getNaziv() + " nije deklarisano!", null);
		} else {
			// niz ne sme da bude konstantan(po gramatici); tj. niz moze samo da bude promenljiva
			if (obj.getKind() != Obj.Var) {
				report_error("Greska na liniji " + desigArray.getLine() + ": ime " + desigArray.getArrayName().getNaziv() + " nije Var tipa(promenljiva)!", null);
			}
			// da li je obj cvor zaista niz?
			if (obj.getType().getKind() != Struct.Array) {
				report_error("Greska na liniji " + desigArray.getLine() + ": ime " + desigArray.getArrayName().getNaziv() + " nije niz!", null);
				obj = MyTab.noObj;
			}
			// indeks niza mora da bude tipa int
			if (desigArray.getExpr().struct != MyTab.intType) {
				if(desigArray.getExpr().struct == null) {	
					System.out.println("Expr je null");
				}
				report_error("Greska na liniji " + desigArray.getLine() + ": Indeks elementa niza nije Int tipa!", null);
			}
			
			// npr: niz[13]; gde je ident. niz zaista ident niza
			// mozda ce ovaj ispis biti dupliran; proveriti da li je potreban!
			if(obj != MyTab.noObj) {
				report_info("Pretraga na liniji " + desigArray.getLine() + "(" + desigArray.getArrayName().getNaziv() + "), nadjeno " + getObjType(obj.getKind()) + " " + desigArray.getArrayName().getNaziv() + ": " + getStructType(obj.getType().getKind()) + " of " + getStructType(obj.getType().getElemType().getKind()) + ", " + obj.getAdr() + ", " + obj.getLevel(), null);
			}
		}
		
		desigArray.obj = new Obj(Obj.Elem, desigArray.getArrayName().getNaziv(), obj.getType().getElemType());	// Referenca ukazuje na ELEMENT niza i cuva informaciju kog je on tipa 
	}

	// BaseExp
	
	// zagrade kod izracunavanja matematickih izraza; izrazi moraju biti Int tipa
	public void visit(PriorityFact priorFact) {
		if(priorFact.getExpr().struct != MyTab.intType) {
			report_error("Greska na liniji " + priorFact.getLine() + ": izraz u zagradi nije tipa int!", null);
			priorFact.struct = MyTab.noType;
		} else {
			priorFact.struct = priorFact.getExpr().struct;
		}	
	}	
	
	// konstruktor niza
	public void visit(ConstructorFact constrFact) {
		if(constrFact.getExpr().struct != MyTab.intType) {
			report_error("Greska na liniji " + constrFact.getLine() + ": indeks niza nije tipa int! ", null);
			constrFact.struct = MyTab.noType;
		} else {
			constrFact.struct = new Struct(Struct.Array, constrFact.getType().struct);
		}
	}
	
	// konstruktor klase; ne za nivo A
	public void visit(ObjFactor objFact) {
		if(objFact.getType().struct.getKind() != Struct.Class) {
			report_error("Greska na liniji " + objFact.getLine() + ": Tip nije klasa!", null);
			objFact.struct = MyTab.noType;
		} else {
			objFact.struct = MyTab.nullType;
		}	
	}
	
	public void visit(BoolFactor boolFact) {
		boolFact.struct = MyTab.boolType;
	}
	
	public void visit(CharFactor charFact) {
		charFact.struct = MyTab.charType;
	}
	
	public void visit(NumFactor numFact) {
		numFact.struct = MyTab.intType;
	}
	
	// poziv f-je; obradjivanje f-ja ne za A nivo
	public void visit(FuncCallFactor funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if(func.getKind() == Obj.Meth) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		} else {
			report_error("Greska na liniji " + funcCall.getLine() + ": Ime " + func.getName() + " nije funkcija!", null);
			funcCall.struct = MyTab.noType;
		}
	}
	
	public void visit(DesigFactor desigFact) {
		desigFact.struct = desigFact.getDesignator().obj.getType();
	}
	
	// Factor
	
	public void visit(BaseExpFactor baseExpFactor) {
		baseExpFactor.struct = baseExpFactor.getBaseExp().struct;
	}
	
	public void visit(ExpopFactor expopFactor) {			// provera da li su osnova i exponent kompatibilni tipovi u izrazu za stepenovanje
		Struct ef = expopFactor.getFactor().struct;
		Struct b = expopFactor.getBaseExp().struct;
		if (ef.equals(b) && ef == MyTab.intType) {
			expopFactor.struct = ef;
//			report_info("******  Pronadjen operator za stepenovanje na liniji: " + expopFactor.getLine() + " ********", null);
		} else {
			report_error("Greska na liniji " + expopFactor.getLine() + ": Nekompatibilni tipovi u izrazu za stepenovanje; Neki operator nije int tipa", null);
			expopFactor.struct = MyTab.noType;
		}
	}
	
	// Term
	
	public void visit(FactorTerm facTerm) {
		facTerm.struct = facTerm.getFactor().struct;
	}
	
	// izraz za mnozenje
	public void visit(MulopTerm mulTerm) {
		Struct tf = mulTerm.getTerm().struct;
		Struct f = mulTerm.getFactor().struct;
		if(tf.equals(f) &&  tf == MyTab.intType) {
			mulTerm.struct = tf;
		} else {
			report_error("Greska na liniji " + mulTerm.getLine() + ": Nekompatibilni tipovi u izrazu; Neki operator nije int tipa! ", null);
			mulTerm.struct = MyTab.noType;
		}
	}
	
	// ExprDecl
	
	public void visit(MinusTermExpr minusExpr) {
		minusExpr.struct = minusExpr.getTerm().struct;
	}
	
	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}
	
	// izraz za sabiranje
	public void visit(AddSubExpr asExpr) {
		Struct te = asExpr.getExprDecl().struct;
		Struct t = asExpr.getTerm().struct;
		if(te.equals(t) && te == MyTab.intType) {
			asExpr.struct = te;
		} else {
			report_error("Greska na liniji " + asExpr.getLine() + ": Nekompatibilni tipovi u izrazu; Neki operator nije int tipa! ", null);
			asExpr.struct = MyTab.noType;
		}
	}
	
	// Expr
	
	public void visit(Expresion expr) {
		expr.struct = expr.getExprDecl().struct;
	}
	
	// DesignatorStatement
	
	// dodela vrednost; assignment
	public void visit(DesignatorAssignment desigAssign) {
		Obj d = desigAssign.getDesignator().obj;	// designator
		Struct e = desigAssign.getExpr().struct;	// expresion
		
		if((d.getKind() != Obj.Var) && (d.getKind() != Obj.Elem)) {
			report_error("Greska na liniji " + desigAssign.getLine() + ": Vrednost se moze dodeliti samo promenljivoj ili elementu niza!", null);
		} else {
			if(!e.assignableTo(d.getType())) {
				report_error("Greska na liniji " + desigAssign.getLine() + ": Nekompatibilni tipovi u izrazu za dodelu vrednosti!", null);
			}
		}
	}
	
	// globale funkcije
	public void visit(DesignatorFunc desigFunc) {
		Obj func = desigFunc.getDesignator().obj;
		if(func.getKind() == Obj.Meth) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + desigFunc.getLine(), null);
		} else {
			report_error("Greska na liniji " + desigFunc.getLine()+" : ime " + func.getName() + " nije funkcija!", null);
		}		
	}
	
	// Increment
	public void visit(DesignatorInc desigInc) {
		// mogu se inkrementirati promenljive ili elementi niza koji su int tipa
		Obj obj = desigInc.getDesignator().obj;
		if ((obj.getKind() != Obj.Var) && (obj.getKind() != Obj.Elem)) {
			report_error("Greska na liniji " + desigInc.getLine() + ": mogu se inkrementirati samo promenljive i elementi niza! ", null);
		} else {
			if(obj.getType() != MyTab.intType) {
				report_error("Greska na liniji " + desigInc.getLine() + ": mogu se inkrementirati promenljive i elementi niza koji su tipa INT! ", null);
			}
		}				
	}
	
	// Decrement
	public void visit(DesignatorDec desigDec) {
		Obj obj = desigDec.getDesignator().obj;
		if((obj.getKind() != Obj.Var) && (obj.getKind() != Obj.Elem)) {
			report_error("Greska na liniji " + desigDec.getLine() + ": mogu se dekrementirati promenljive i elementi niza! ", null);
		} else {
			if(obj.getType() != MyTab.intType) {
				report_error("Greska na liniji " + desigDec.getLine() + ": mogu se dekrementirati promenljive i elementi niza koji su tipa INT! ", null);
			}
		}
	}
	
	// READ Statement
	
	public void visit(ReadStmt readStmt) {
		Obj d = readStmt.getDesignator().obj;
		if (d.getKind() != Obj.Var && d.getKind() != Obj.Elem) {
			report_error("Greska na liniji " + readStmt.getLine() + ": Argument funkcije read mora biti promenljiva ili element niza! ", null);
		} else {
			if (d.getType() != MyTab.intType && d.getType() != MyTab.charType && d.getType() != MyTab.boolType) {
				report_error("Greska na liniji " + readStmt.getLine() + ": Argument funckije read(" + d.getName() + ") mora biti int, char ili bool tipa!", null);
			} else {
				report_info("Pretraga na liniji " + readStmt.getLine() + ": Obradjuje se funkcija read! ", null);
			}
		}
	}
	
	// PRINT Statement
	
	public void visit(PrintStmt printStmt) {
		Struct e = printStmt.getExpr().struct;
		if (e != MyTab.intType && e != MyTab.charType && e != MyTab.boolType) {
			report_error("Greska na liniji " + printStmt.getLine() + ": Argument funcije print mora biti int, char ili bool tipa!", null);
		} else {
			report_info("Pretraga na liniji " + printStmt.getLine() + ": Obradjuje se funkcija print! ", null);
		}
	}
	
	public void visit(PrintStmtNumber printStmtNum) {
		Struct e  = printStmtNum.getExpr().struct;
		if (e != MyTab.intType && e != MyTab.charType && e != MyTab.boolType) {
			report_error("Greska na liniji " + printStmtNum.getLine() + ": Argument funcije print mora biti int, char ili bool tipa!", null);
		} else {
			report_info("Pretraga na liniji " + printStmtNum.getLine() + ": Obradjuje se funkcija print! ", null);
		}
	}
	
	// Label Statement and GOTO Label
	
	// Izraz sa labelom
	public void visit(StmtWithLabel labelStmt) {
		String labelName = labelStmt.getLabel().getLabelName();
		if (currentMethod != null) {
			// da li je bilo nekog skoka UNAPRED na labelu koju sada obilazimo; ako jeste nju brisemo iz liste nedefinisanih labela
			if (undefLabelsList.contains(labelName)) {	
				undefLabelsList.remove(labelName);
			}
			
			// da li postoji dvostruka deklaracija labele sa istim imenom
			if (defLabelsList.contains(labelName)) {	
				report_error("Greska na liniji " + labelStmt.getLine() + ": Labela " + labelName + " je vec deklarisana! ", null);
			} else {
				// prvo pojavljivanje labele pa je dodajemo u listu deklarisanih labela
				defLabelsList.add(labelName);
			}		
		} else {
			report_error("Greska na liniji " + labelStmt.getLine() + ": labela mora biti definisana unutar tekuce funkcije! ", null);
		}
	}
	
	// Skok na labelu
	public void visit(GotoStmt gotoStmt) {
		String labelName = gotoStmt.getLabel().getLabelName();
		if(!defLabelsList.contains(labelName)) {
			// ako lista definisanih labela ne sadrzi labelu iz trenutnog skoka, onda nju dodajemo u listu nedefinisanih labela
			// Skok UNAPRED
			undefLabelsList.add(labelName);
		}
	}
	
	// brojac deklarisanih iskaza u funkciji
	public void visit(StmtList stmt) {
		//System.out.println("Ulazi!");
		if(currentMethod != null) {
			statementsCnt++;
		}
	}
	
	public boolean passed() {
		return !errorDetected;
	}
	
}
