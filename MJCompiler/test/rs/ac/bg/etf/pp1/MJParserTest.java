package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
//import java.io.PrintWriter;
import java.io.Reader;
//import java.util.Scanner;

import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;

public class MJParserTest {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(MJParserTest.class);
		
		Reader br = null;
		try {
			File sourceCode = new File("test/program.mj");
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
	        Symbol s = p.parse();  //pocetak parsiranja
	        
	        Program prog = (Program)(s.value); 
	        MyTab.init();
			// ispis sintaksnog stabla
	        log.info("================== SINTAKSNO  STABLO ======================" );
			log.info(prog.toString(""));
	    //    prog.toString("");
			log.info("================== SEMANTICKA OBRADA ======================");

			// ispis prepoznatih programskih konstrukcija
			SemanticAnalyzer v = new SemanticAnalyzer();
			prog.traverseBottomUp(v); 
			
			log.info("================== SINTAKSNA ANALIZA ===========================");
			log.info(" Broj deklarisanih globalnih konstanti = " + v.getConstantCnt());
			log.info(" Broj deklarisanih globalnih promenljivih = " + v.getGlobalVarCnt() );
			log.info(" Broj deklarisanih lokalnih promenljivih = " + v.getLocalVarCnt() );
			log.info(" Broj deklarisanih iskaza u funkciji main = " + v.getStatementsCnt() );
			
			MyTab.dump();
			
			if(!p.errorDetected && v.passed()) {
				
				// ubacivanje generisanja koda ako su prve 3 faze uspesno zavrsene
				File objFile = new File("test/program.obj");
				if(objFile.exists()) objFile.delete();
				
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.dataSize = v.nVars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));
			//	System.out.println("Moj ispis stabla\n");
			//	MyTab.dump();
				
				log.info("Parsiranje uspesno zavrseno!");
			} else {
				log.error("Parsiranje NIJE uspesno zavrseno!");
			}
			
			// izlaz.out
//			try {
//		        File input = new File("logs/mj-test.log");
//		        File output = new File("test/ispis.txt");
//		        Scanner sc = new Scanner(input);
//		        PrintWriter printer = new PrintWriter(output);
//		        while(sc.hasNextLine()) {
//		            String str = sc.nextLine();
//		            printer.write(str + "\n");
//		        }
//		        sc.close();
//		        printer.close();
//		    }
//		    catch(FileNotFoundException e) {
//		        System.err.println("File not found. Please scan in new file.");
//		    }
			  
	      
		} 
		finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}

	}
	
	
}
