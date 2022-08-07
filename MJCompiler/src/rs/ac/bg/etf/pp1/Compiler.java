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
//import java.util.ArrayList;

import java_cup.runtime.Symbol;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}

	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		
		Logger log = Logger.getLogger(Compiler.class);
		Reader br = null;
		
		try {		
			String inputFile = args[0];
			String outputFile = args[1];
			
			File sourceCode = new File(inputFile);
			log.info("Compiling source file: " + sourceCode.getAbsolutePath());
			
			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);
			
			MJParser p = new MJParser(lexer);
			Symbol s = p.parse();  //pocetak parsiranja
	        
	        if (p.errorDetected) {
	        	log.error( "Parsiranje NIJE uspesno zavrseno!" );
	        } else {
	        	
	        	Program prog = (Program)(s.value); 
		        MyTab.init();
				// ispis sintaksnog stabla
		        log.info("================== SINTAKSNO  STABLO ======================" );
				log.info(prog.toString(""));
		   
				log.info("================== SEMANTICKA OBRADA ======================");
				// ispis prepoznatih programskih konstrukcija
				SemanticAnalyzer v = new SemanticAnalyzer();
				prog.traverseBottomUp(v); 
				
				log.info("================== SINTAKSNA ANALIZA ===========================");
				log.info(" Broj deklarisanih globalnih konstanti = " + v.getConstantCnt());
				log.info(" Broj deklarisanih globalnih promenljivih = " + v.getGlobalVarCnt() );
				log.info(" Broj deklarisanih lokalnih promenljivih = " + v.getLocalVarCnt() );
				log.info(" Broj deklarisanih iskaza u funkciji main = " + v.getStatementsCnt() );
				
				// ispis sadrzaja tabele simbola
				tsdump();
				
				if (!v.passed()) {
					log.error( "Semanticka analiza NIJE uspesno zavrsena!" );
				} else {
					// ubacivanje generisanja koda ako su prve 3 faze uspesno zavrsene
					File objFile = new File(outputFile);
					if(objFile.exists()) objFile.delete();
					
					CodeGenerator codeGenerator = new CodeGenerator();
					prog.traverseBottomUp(codeGenerator);
					Code.dataSize = v.nVars;
					Code.mainPc = codeGenerator.getMainPc();
					Code.write(new FileOutputStream(objFile));
					
					log.info( "Parsiranje uspesno zavrseno!" );
				}	
				
				
	        }
	        
		} finally {
			if (br != null) try { br.close(); } catch (IOException e1) { log.error(e1.getMessage(), e1); }
		}
	
	}
	
	public static void tsdump() {
		MyTab.dump();
	}
	
}
