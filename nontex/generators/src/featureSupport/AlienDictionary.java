package featureSupport;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class AlienDictionary {

	HashMap<Integer, String> intToCodeMap;
	HashMap<Integer, String> intToVerbMap;

	public AlienDictionary() {
		initializeIntToCodeMap();
		initializeIntToVerbMap();
	}

	public Object getCode(int ID) {
		return intToCodeMap.get(ID);
	}

	public Object getVerbatim(int  ID) {
		return intToVerbMap.get(ID);
	}

	public static boolean hasAlienFeature(int ID, String languageName) {
		switch (languageName) {
		case "Python":
			return pythonContainsA(ID);
		case "Perl":
			return perlContainsA(ID);
		case ".Net":
			return dotNetContainsA(ID);
		case "JavaScript":
			return javaScriptContainsA(ID);
		case "Java":
			return javaContainsA(ID);
		case "POSIX ERE":
			return ereContainsA(ID);
		case "Ruby":
			return rubyContainsA(ID);
		case "RE2":
			return re2ContainsA(ID);
		default:
			throw new RuntimeException("language name not found in switch: " +
				languageName);
		}
	}

	private static boolean pythonContainsA(int ID) {
		List<Integer> missingFeatures_py = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \k<name>	named backreference  (net has)
				KBRK,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,		
				
				//java and perl has:
//				end of previous match position: \G(net has)(not re2)
				PRV,//		0xhhhh long hex values: \\uhhhh (js has)(net has)
				LHX,//		posessive modifiers: Z++ (NSRE has, .Net does not have)
				POSS,		//net-style named groups (?<name>X) (net has) (rub has) (not re2)
				NNCG,//		option modulation (?i)z(?-i)z (net has)
				MOD,//		atomic NCG (?>X)(net has)(not re2)
				ATOM,//		CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,//		end of input, or before last newline: \Z(net has)
				LNLZ,//		end of input \z (re2 has)
				FINL,//		quotation \Q...\E (re2 has)
				QUOT,//		4 Java defaults \p{javaMirrored}
				JAVM,		
				
				//RE2 has:
				//\pN Unicode character class (one-letter name)
				UNI,		//\PN Negated Unicode character class (one-letter name)
				NUNI,// (?flags:re) flags just for re
				OPTG,		
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
//				12 POSIX classes [:alpha:] (ere, re2, ruby has)
				PXCC,//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//		net style conditionals (?(<n>)X|else)
				NCND,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures_py.contains(ID);
	}

	private static boolean re2ContainsA(int ID) {
		List<Integer> missingFeatures_re2 = Arrays.asList( 
				//perl has:
							// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \g<name>	subroutine call
				GSUB,		// \k<name>	named backreference  (net has)
				KBRK,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,//		(?# embedded comments) (net has) (not re2)
				ECOM,		
				
				//java and perl has:
//				end of previous match position: \G(net has)(not re2)
				PRV,//		0xhhhh long hex values: \\uhhhh (js has)(net has)
				LHX,//		posessive modifiers: Z++ (NSRE has, .Net does not have)
				POSS,		//net-style named groups (?<name>X) (net has) (rub has) (not re2)
				NNCG,//		atomic NCG (?>X)(net has)(not re2)
				ATOM,//		CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,//		4 Java defaults \p{javaMirrored}
				JAVM,			
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
				//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//		net style conditionals (?(<n>)X|else)
				NCND,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures_re2.contains(ID);
	}

	private static boolean rubyContainsA(int ID) {
		List<Integer> missingFeatures_rb = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,//				
				
				//java and perl has:
				//		quotation \Q...\E (re2 has)
				QUOT,//		4 Java defaults \p{javaMirrored}
				JAVM,		
				
				//RE2 has:
				//\pN Unicode character class (one-letter name)
				UNI,		//\PN Negated Unicode character class (one-letter name)
				NUNI,		
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
//				//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//			
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES);
		return !missingFeatures_rb.contains(ID);
	}

	private static boolean ereContainsA(int ID) {
		List<Integer> missingFeatures_ere = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \g<name>	subroutine call
				GSUB,		// \k<name>	named backreference  (net has)
				KBRK,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,//		(?# embedded comments) (net has) (not re2)
				ECOM,		
				
				//java and perl has:
//				end of previous match position: \G(net has)(not re2)
				PRV,//maybe?	0xhhhh long hex values: \\uhhhh (js has)(net has)
				LHX,//		posessive modifiers: Z++ (NSRE has, .Net does not have)
				POSS,		//net-style named groups (?<name>X) (net has) (rub has) (not re2)
				NNCG,//		option modulation (?i)z(?-i)z (net has)
				MOD,//		atomic NCG (?>X)(net has)(not re2)
				ATOM,//		CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,//		beginning of input \A(net has)(re2 has)
				STRA,//		end of input, or before last newline: \Z(net has)
				LNLZ,//		end of input \z (re2 has)
				FINL,//		quotation \Q...\E (re2 has)
				QUOT,//		4 Java defaults \p{javaMirrored}
				JAVM,		
				
				//RE2 has:
				//\pN Unicode character class (one-letter name)
				UNI,		//\PN Negated Unicode character class (one-letter name)
				NUNI,// (?flags:re) flags just for re
				OPTG,		
				
				//		javascript has (not java)
//				//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//		net style conditionals (?(<n>)X|else)
				NCND,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures_ere.contains(ID);
	}

	private static boolean javaContainsA(int ID) {
		List<Integer> missingFeatures = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \g<name>	subroutine call
				GSUB,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,//		(?# embedded comments) (net has) (not re2)
				ECOM,		
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
//				12 POSIX classes [:alpha:] (ere, re2, ruby has)
				PXCC,//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//		net style conditionals (?(<n>)X|else)
				NCND,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures.contains(ID);
	}

	private static boolean javaScriptContainsA(int ID) {
		List<Integer> missingFeatures_js = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \g<name>	subroutine call
				GSUB,		// \k<name>	named backreference  (net has)
				KBRK,//		(?(cond)X) if conditionals (not re2)
				IFC,//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
				IFEC,//		(?{code}) embedded code (not re2)
				ECOD,//		(?# embedded comments) (net has) (not re2)
				ECOM,		
				
				//java and perl has:
//				end of previous match position: \G(net has)(not re2)
				PRV,//		posessive modifiers: Z++ (NSRE has, .Net does not have)
				POSS,		//net-style named groups (?<name>X) (net has) (rub has) (not re2)
				NNCG,//		option modulation (?i)z(?-i)z (net has)
				MOD,//		atomic NCG (?>X)(net has)(not re2)
				ATOM,//		CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,//		beginning of input \A(net has)(re2 has)
				STRA,//		end of input, or before last newline: \Z(net has)
				LNLZ,//		end of input \z (re2 has)
				FINL,//		quotation \Q...\E (re2 has)
				QUOT,//		4 Java defaults \p{javaMirrored}
				JAVM,		
				
				//RE2 has:
				//\pN Unicode character class (one-letter name)
				UNI,		//\PN Negated Unicode character class (one-letter name)
				NUNI,
				
				
				
				
				// (?flags:re) flags just for re
				OPTG,		
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,//		net style conditionals (?(<n>)X|else)
				NCND,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures_js.contains(ID);
	}

	private static boolean dotNetContainsA(int ID) {
		List<Integer> missingFeatures_dn = Arrays.asList( 
				//perl has:
				// (?n) recursive call to group n (not re2)
				RCUN,		// (?R) recursive call to group 0 (not re2)
				RCUZ,		// \g{+1}	backreference
				GPLS,		// \g{name}	named backreference
				GBRK,		// \g<name>	subroutine call
				GSUB,//		(?{code}) embedded code (not re2)
				ECOD,		
				
				//java and perl has:
//				//		posessive modifiers: Z++ (NSRE has, .Net does not have)
				POSS,//		CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,
				
				
				//		quotation \Q...\E (re2 has)
				QUOT,//		4 Java defaults \p{javaMirrored}
				JAVM,		
				
				//RE2 has:
				//\pN Unicode character class (one-letter name)
				UNI,		//\PN Negated Unicode character class (one-letter name)
				NUNI,		
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
//				12 POSIX classes [:alpha:] (ere, re2, ruby has)
				PXCC,//		trivial char class [^] ???
				TRIV,
				
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES);
		return !missingFeatures_dn.contains(ID);
	}

	private static boolean perlContainsA(int ID) {
		List<Integer> missingFeatures_pr = Arrays.asList( 
							//CCC intersection [a-z&&[^f]] (perl does not have)
				CCCI,//		4 Java defaults \p{javaMirrored}
				JAVM,			
				
				//ere has
//				equivalence classes [[=o=]]
				EREQ,
				
				//		javascript has (not java)
				//		trivial char class [^] ???
				TRIV,
				
				//		.net has:
//				char class subtraction [a-f-[c]]
				CCSB,//		variable width look behinds (?<=ab.+)
				VLKB,//		balancing groups (?<close-open>)
				BAL,	
				// others:
				// (?|x|y|z)	branch numbering reset
				BRES,		// (?'name're) single-quote named groups
				QNG);
		return !missingFeatures_pr.contains(ID);
	}
	
	private void initializeIntToCodeMap() {
		intToCodeMap = new HashMap<Integer,String>();		

		intToCodeMap.put(RCUN,"RCUN");
		
		intToCodeMap.put(RCUZ,"RCUZ");
		
		intToCodeMap.put(GPLS,"GPLS");
		
		intToCodeMap.put(GBRK,"GBRK");
		
		intToCodeMap.put(GSUB,"GSUB");
		
		intToCodeMap.put(KBRK,"KBRK");
		
		intToCodeMap.put(IFC,"IFC");
		
		intToCodeMap.put(IFEC,"IFEC");
		
		intToCodeMap.put(ECOD,"ECOD");
		
		intToCodeMap.put(ECOM,"ECOM");
		
		intToCodeMap.put(PRV,"PRV");
		
		intToCodeMap.put(LHX,"LHX");
		
		intToCodeMap.put(POSS,"POSS");
		
		intToCodeMap.put(NNCG,"NNCG");
		
		intToCodeMap.put(MOD,"MOD");
		 
		intToCodeMap.put(ATOM,"ATOM");
		
		intToCodeMap.put(CCCI,"CCCI");
		
		intToCodeMap.put(STRA,"STRA");
		
		intToCodeMap.put(LNLZ,"LNLZ");
		
		intToCodeMap.put(FINL,"FINL");

		intToCodeMap.put(QUOT,"QUOT");

		intToCodeMap.put(JAVM,"JAVM");
		
		intToCodeMap.put(UNI,"UNI");
		
		intToCodeMap.put(NUNI,"NUNI");
		
		intToCodeMap.put(OPTG,"OPTG");
		
		intToCodeMap.put(EREQ,"EREQ");
		
		intToCodeMap.put(PXCC,"PXCC");
		
		intToCodeMap.put(TRIV,"TRIV");
		
		intToCodeMap.put(CCSB,"CCSB");
		
		intToCodeMap.put(VLKB,"VLKB");
		
		intToCodeMap.put(BAL,"BAL");
		
		intToCodeMap.put(NCND,"NCND");
		
		intToCodeMap.put(BRES,"BRES");
		
		intToCodeMap.put(QNG,"QNG");
		
	}
	
	private void initializeIntToVerbMap() {

		intToVerbMap = new HashMap<Integer,String>(34);	
		
		//perl has:
		// (?n) recursive call to group n (not re2)
		intToVerbMap.put(RCUN,pad("(?n)"));
		
		// (?R) recursive call to group 0 (not re2)
		intToVerbMap.put(RCUZ,pad("(?R)"));
		
		// \g{+1}	backreference
		intToVerbMap.put(GPLS,pad("\\g{+1}"));
		
		// \g{name}	named backreference
		intToVerbMap.put(GBRK,pad("\\g{name}"));
		
		// \g<name>	subroutine call
		intToVerbMap.put(GSUB,pad("\\g<name>"));
		
		// \k<name>	named backreference  (net has)
		intToVerbMap.put(KBRK,pad("\\k<name>"));
		
//		(?(cond)X) if conditionals (not re2)
		intToVerbMap.put(IFC ,pad("(?(cond)X)"));
		
//		(?(cond)X|else) ifelse conditionals (net has)(not re2)
		intToVerbMap.put(IFEC,pad("(?(cnd)X|else)"));
		
//		(?{code}) embedded code (not re2)
		intToVerbMap.put(ECOD,pad("(?{code})"));
		
//		(?# embedded comments) (net has) (not re2)
		intToVerbMap.put(ECOM,pad("(?#comment)"));
		
		
		
		
		//java and perl has:
//		end of previous match position: \G(net has)(not re2)
		intToVerbMap.put(PRV,pad("\\G"));
		
//		0xhhhh long hex values: \\uhhhh (js has)(net has)
		intToVerbMap.put(LHX,pad("\\uFFFF"));
		
//		posessive modifiers: Z++ (NSRE has, .Net does not have)
		intToVerbMap.put(POSS,pad("a?+"));
		
		//net-style named groups (?<name>X) (net has) (rub has) (not re2)
		intToVerbMap.put(NNCG,pad("(?<name>X)"));
		
//		option modulation (?i)z(?-i)z (net has)
		intToVerbMap.put(MOD,pad("(?i)z(?-i)z"));
		 
//		atomic NCG (?>X)(net has)(not re2)
		intToVerbMap.put(ATOM,pad("(?>X)"));
		
//		CCC intersection [a-z&&[^f]] (perl does not have)
		intToVerbMap.put(CCCI,pad("[a-z&&[^f]]"));
		
//		beginning of input \A(net has)(re2 has)
		intToVerbMap.put(STRA,pad("\\A"));
		
//		end of input, or before last newline: \Z(net has)
		intToVerbMap.put( LNLZ,pad("\\Z"));
		
//		end of input \z (re2 has)
		intToVerbMap.put(FINL,pad("\\z"));
		
		
		
//		quotation \Q...\E (re2 has)
		intToVerbMap.put( QUOT,pad("\\Q...\\E"));
		
//		4 Java defaults \p{javaMirrored}
		intToVerbMap.put(JAVM,pad("\\p{javaMirrored}"));
		
		//RE2 has:
		//\pN Unicode character class (one-letter name)
		intToVerbMap.put(UNI,pad("\\pL"));
		
		//\PN Negated Unicode character class (one-letter name)
		intToVerbMap.put( NUNI,pad("\\PS"));
		
		// (?flags:re) flags just for re
		intToVerbMap.put(OPTG,pad("(?flags:re)"));

		
		//ere has
//		equivalence classes [[=o=]]
		intToVerbMap.put(EREQ,pad("[[=o=]]"));
		
//		javascript has (not java)
//		12 POSIX classes [:alpha:] (ere, re2, ruby has)
		intToVerbMap.put(PXCC,pad("[:alpha:]"));
		
//		trivial char class [^] ???
		intToVerbMap.put(TRIV,pad("[^]"));
		
//		.net has:
//		char class subtraction [a-f-[c]]
		intToVerbMap.put(CCSB,pad("[a-f-[c]]"));
		
//		variable width look behinds (?<=ab.+)
		intToVerbMap.put(VLKB,pad("(?<=ab.+)"));
		
		
		
		
//		balancing groups (?<close-open>)
		intToVerbMap.put(BAL,pad("(?<close-open>)"));
		
//		net style conditionals (?(<n>)X|else)
		intToVerbMap.put(NCND,pad("(?(<n>)X|else)"));
		
	// others:
		// (?|x|y|z)	branch numbering reset
		intToVerbMap.put(BRES,pad("(?|(A)|(B))"));
		
		// (?'name're) single-quote named groups
		intToVerbMap.put(QNG,pad("(?'name're)"));
	}
	
	private String pad(String s){
		String pre = "\\begin{minipage}{0.8in}\\begin{verbatim}";
		String suf = "\\end{verbatim}\\end{minipage}";
		return pre + s + suf;
	}
	
	//perl has:
	// (?n) recursive call to group n (not re2)
	public static final int RCUN = 1;
	
	// (?R) recursive call to group 0 (not re2)
	public static final int RCUZ = 2;
	
	// \g{+1}	backreference
	public static final int GPLS = 3;
	
	// \g{name}	named backreference
	public static final int GBRK = 4;
	
	// \g<name>	subroutine call
	public static final int GSUB = 5;
	
	// \k<name>	named backreference  (net has)
	public static final int KBRK = 6;
	
//	(?(cond)X) if conditionals (not re2)
	public static final int IFC = 7;
	
//	(?(cond)X|else) ifelse conditionals (net has)(not re2)
	public static final int IFEC = 8;
	
//	(?{code}) embedded code (not re2)
	public static final int ECOD = 9;
	
//	(?# embedded comments) (net has) (not re2)
	public static final int ECOM = 10;
	
	
	
	
	//java and perl has:
//	end of previous match position: \G(net has)(not re2)
	public static final int PRV = 11;
	
//	0xhhhh long hex values: \\uhhhh (js has)(net has)
	public static final int LHX = 12;
	
//	posessive modifiers: Z++ (NSRE has, .Net does not have)
	public static final int POSS = 13;
	
	//net-style named groups (?<name>X) (net has) (rub has) (not re2)
	public static final int NNCG = 14;
	
//	option modulation (?i)z(?-i)z (net has)
	public static final int MOD = 15;
	 
//	atomic NCG (?>X)(net has)(not re2)
	public static final int ATOM = 16;
	
//	CCC intersection [a-z&&[^f]] (perl does not have)
	public static final int CCCI = 17;
	
//	beginning of input \A(net has)(re2 has)
	public static final int STRA = 18;
	
//	end of input, or before last newline: \Z(net has)
	public static final int LNLZ = 19;
	
//	end of input \z (re2 has)
	public static final int FINL = 20;
	
	
	
//	quotation \Q...\E (re2 has)
	public static final int QUOT = 21;
	
	
//	4 Java defaults \p{javaMirrored}
	public static final int JAVM = 22;
	
	//RE2 has:
	//\pN Unicode character class (one-letter name)
	public static final int UNI = 23;
	
	//\PN Negated Unicode character class (one-letter name)
	public static final int NUNI = 24;
	
	// (?flags:re) flags just for re
	public static final int OPTG = 25;

	
	//ere has
//	equivalence classes [[=o=]]
	public static final int EREQ = 26;
	
//	javascript has (not java)
//	12 POSIX classes [:alpha:] (ere, re2, ruby has)
	public static final int PXCC = 27;
	
//	trivial char class [^] ???
	public static final int TRIV = 28;
	
//	.net has:
//	char class subtraction [a-f-[c]]
	public static final int CCSB = 29;
	
//	variable width look behinds (?<=ab.+)
	public static final int VLKB = 30;
	
	
	
	
//	balancing groups (?<close-open>)
	public static final int BAL = 31;
	
//	net style conditionals (?(<name>)ifMatch|else)
	public static final int NCND = 32;
	
// others:
	// (?|x|y|z)	branch numbering reset
	public static final int BRES = 33;
	
	// (?'name're) single-quote named groups
	public static final int QNG = 34;
	
	public static int[] alienFeatures = {
		RCUN,
		RCUZ,
		GPLS,
		GBRK,
		GSUB,
		KBRK,
		IFC,
		IFEC,
		ECOD,
		ECOM,
		PRV,
		LHX,
		POSS,
		NNCG,
		MOD,
		ATOM,
		CCCI,
		STRA,
		LNLZ,
		FINL,
		QUOT,
		JAVM,
		UNI,
		NUNI,
		OPTG,
		EREQ,
		PXCC,
		TRIV,
		CCSB,
		VLKB,
		BAL,
		NCND,
		BRES,
		QNG};
	
	
	
	
	
	//RE2 has:
	//\pN Unicode character class (one-letter name)
	//\PN Negated Unicode character class (one-letter name)
	// no posessives
	// no (?'name're) single-quote named groups
	// (?flags:re) flags just for re
	// no (?|x|y|z)	branch numbering reset
	// \C match single style byte even in UTF-8 mode
	// no \g1	backreference
	// no \g{1}	backreference
	
	// no \g{-1}	backreference
	// no \g{name}	named backreference
	// no \g<name>	subroutine call
	// no \g'name'	subroutine call
	// no \k<name>	named backreference
	// no \k'name'	named backreference
	
	
	//also none o these:
//	\lX	lowercase X
//	\\ux	uppercase x
//	\L...\E	lowercase text ...
//	\K	reset beginning of $0
//	\N{name}	named Unicode character
//	\R	line break
//	\U...\E	upper case text ...
	
	
	
	//net-style named groups (?<name>X) (net has) (rub has) (not re2)
	//net-style named backreferences \k<name> (net has)
//	0xhhhh long hex values: \\uhhhh (js has)(net has)
//	escape char: \e (js)(net has) (not re2)
//	control char x: \cx(net has) (not re2)
//	posessive modifiers: Z++ (NSRE has, .Net does not have)
//	end of previous match position: \G(net has)(not re2)
//	beginning of input \A(net has)(re2 has)
//	end of input, or before last newline: \Z(net has)
//	end of input \z (re2 has)
//	quotation \Q...\E (re2 has)
//	option modulation (?i)z(?-i)z (net has)
//	atomic NCG (?>X)(net has)(not re2)
//	CCC intersection [a-z&&[^f]] (perl does not have)
//	12 POSIX defaults \p{Alpha}
//	ASCII default \p{ASCII}
//	4 Java defaults \p{javaMirrored}
//	6 Unicode defaults \p{IsLatin} (re2 has)

//	Perl has (all of java, and these that are not in Java)
//	\h horiz whsp(not re2)
//	\H(not re2)
//	\v vert wsp (js has)(not re2)
//	\V(not re2)
//	\R unicode line breaks
//	\X extended grapheme clusters (not re2)
//	\g1...\g{name}
//	\N{name} unicode named characters(net has)
//	(?(cond)X) if conditionals (not re2)
//	(?(cond)X|else) ifelse conditionals (net has)(not re2)
//	(?{code}) embedded code (not re2)
//	(?# embedded comments) (net has) (not re2)
	
	//does java have what perl has?
// (?n) recursive call to group n (not re2)
// (?R) recursive call to group 0 (not re2)


//	javascript has (not java)
//	12 POSIX classes [:alpha:] (ere, re2, ruby has)
//	3 more classes (w,s,d)  [:d:]
//	trivial char class [^] 
//	>>>no named groups or backreferences

//	.net has:
//	char class subtraction [a-f-[c]]
//	variable width look behinds (?<=ab.+)
//	balancing groups (?<close-open>)
//	net style conditionals (?(<name>)ifMatch|else)

//	ere has:
//	equivalence classes [[=o=]]


}
