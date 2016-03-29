package featureSupport;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import c.IOUtil;
import metric.FeatureDictionary;
import build_corpus.CorpusUtil;
import build_corpus.RegexProjectSet;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class MakeTables {
	private static String thesisPath = "/Users/carlchapman/git/thesis/";
	private static String nonTexPath = thesisPath + "nontex/";
	private static String tablePath = thesisPath + "table/";
	private static String langTable = "rankedFeatureSupport.tex";
	private static String[] langNames = { "Python", "Perl", ".Net",
		"Ruby", "Java", "RE2", "JavaScript", "POSIX ERE"};

	private static String YES = "\\yes";
	private static String NO = "\\no";

	public static void main(String[] args) throws IllegalArgumentException,
			IOException, QuoteRuleException, PythonParsingException, ClassNotFoundException, SQLException {
		makeRankedFeatureTable();
		makeAlienFeatureTable();
		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
		File featureStats = new File(tablePath,"featureStatsOnly.tex");
		String homePath = "/Users/carlchapman/Documents/SoftwareProjects/tour_de_source/";
		String connectionString = "jdbc:sqlite:" + homePath +
			"tools/merged/merged_report.db";
		IOUtil.createAndWrite(featureStats, FeatureStatsTable.featureStats(corpus, connectionString));
	}
	


	private static void makeAlienFeatureTable(){
		AlienDictionary ad = new AlienDictionary();
		String between = " & ";
		String tableHeader = "\\begin{table*}[h!tb]\n\\centering\n\\begin{small}\n\\caption{What other features are supported in various languages?}"
			+ "\n\\label{table:alienFeatureSupport}\n\\begin{tabular}{l@{  \\horiz}lc @{   \\horiz} c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c} \\\\ \n"
			+ "code & example & Python & Perl & .Net  & Ruby &  Java & RE2 & \\begin{footnotesize}JavaScript\\end{footnotesize} & \\begin{footnotesize}POSIX ERE\\end{footnotesize}\\\\\n";
		StringBuilder sb = new StringBuilder();
		sb.append(tableHeader);
		for(int i=0; i<AlienDictionary.alienFeatures.length;i++){
			int ID = AlienDictionary.alienFeatures[i];
			sb.append(ad.getCode(ID));
			sb.append(between);
			sb.append(ad.getVerbatim(ID));
			sb.append(between);
			sb.append(ad.hasAlienFeature(ID,langNames[0]) ? YES : NO);
			for(int j=1;j<langNames.length;j++){
				String answer = ad.hasAlienFeature(ID,langNames[j]) ? YES : NO;
				sb.append(between);
				sb.append(answer);
			}
			sb.append("  \\\\\n");
			if(i<rankedFeatures.length-1){
				sb.append("\\midrule\n");
			}else{
				sb.append("\\bottomrule\n");
			}
		}
		sb.append("\\end{tabular}\n\\end{small}\n\\vspace{-12pt}\n\\end{table*}\n");
		
		File alienFeatureSupport = new File(tablePath,"alienFeatureSupport.tex");
		IOUtil.createAndWrite(alienFeatureSupport, sb.toString());
	}
	
	//	private static String[] langNames = { "Python", "Perl", ".Net", "Ruby", "Java", "RE2", "JavaScript", "POSIX ERE"};
	private static void makeRankedFeatureTable(){
		FeatureDictionary fd = new FeatureDictionary();
		String between = " & ";
		String tableHeader = "\\begin{table*}[h!tb]\n\\centering\n\\begin{small}\n\\caption{What regular expression languages support features studied in this thesis?}"
			+ "\n\\label{table:rankedFeatureSupport}\n\\begin{tabular}{l@{  \\horiz}clc@{  \\horiz}lc @{   \\horiz} c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c @{   \\horiz}c}"
			+ "rank & code & example & Python & Perl & .Net  & Ruby &  Java & RE2 & \\begin{footnotesize}JavaScript\\end{footnotesize} & \\begin{footnotesize}POSIX ERE\\end{footnotesize}\\\\\n";
		StringBuilder sb = new StringBuilder();
		sb.append(tableHeader);
		for(int i=0; i<rankedFeatures.length;i++){
			int ID = rankedFeatures[i];
			sb.append(i+1);
			sb.append(between);
			sb.append(fd.getCode(ID));
			sb.append(between);
			sb.append(fd.getVerbatim(ID));
			sb.append(between);
			sb.append(hasFeature(ID,langNames[0]) ? YES : NO);
			for(int j=1;j<langNames.length;j++){
				String answer = hasFeature(ID,langNames[j]) ? YES : NO;
				sb.append(between);
				sb.append(answer);
			}
			sb.append("\\\\\n");
			if(i<rankedFeatures.length-1){
				sb.append("\\midrule\n");
			}else{
				sb.append("\\bottomrule\n");
			}
		}
		sb.append("\\end{tabular}\n\\end{small}\n\\vspace{-12pt}\n\\end{table*}\n");
		File rankedFeaturesLanguages = new File(tablePath,langTable);
		IOUtil.createAndWrite(rankedFeaturesLanguages, sb.toString());
	}

	private static boolean hasFeature(int ID, String languageName) {
		switch (languageName) {
		case "Python":
			return true;
		case "Perl":
			return perlContains(ID);
		case ".Net":
			return dotNetContains(ID);
		case "JavaScript":
			return javaScriptContains(ID);
		case "Java":
			return javaContains(ID);
		case "POSIX ERE":
			return ereContains(ID);
		case "Ruby":
			return rubyContains(ID);
		case "RE2":
			return re2Contains(ID);
		default:
			throw new RuntimeException("language name not found in switch: " +
				languageName);
		}
	}

	// from: https://re2.googlecode.com/hg/doc/syntax.html
	private static boolean re2Contains(int ID) {
		List<Integer> missingFeatures_re2 = Arrays.asList(FeatureDictionary.I_XTRA_END_SUBJECTLINE, FeatureDictionary.I_LOOK_AHEAD, FeatureDictionary.I_LOOK_AHEAD_NEGATIVE, FeatureDictionary.I_LOOK_BEHIND, FeatureDictionary.I_LOOK_BEHIND_NEGATIVE, FeatureDictionary.I_META_NUMBERED_BACKREFERENCE, FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE);
		return !missingFeatures_re2.contains(ID);
	}

	private static boolean rubyContains(int ID) {
		List<Integer> missingFeatures = Arrays.asList( 
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE);
		return !missingFeatures.contains(ID);
	}

	private static boolean ereContains(int ID) {
		List<Integer> missingFeatures = Arrays.asList(
				FeatureDictionary.I_CC_WHITESPACE,
				FeatureDictionary.I_CC_DECIMAL,
				FeatureDictionary.I_CC_WORD, 
				FeatureDictionary.I_REP_LAZY, 
				FeatureDictionary.I_LOOK_NON_CAPTURE,
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_CC_NWHITESPACE,
				FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
				FeatureDictionary.I_POS_WORD, 
				FeatureDictionary.I_CC_NWORD,
				FeatureDictionary.I_LOOK_AHEAD, 
				FeatureDictionary.I_XTRA_OPTIONS,
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
				FeatureDictionary.I_LOOK_BEHIND,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE,
				FeatureDictionary.I_CC_NDECIMAL,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_POS_NONWORD);
		return !missingFeatures.contains(ID);
	}

	private static boolean javaContains(int ID) {
		List<Integer> missingFeatures_java = Arrays.asList(
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE);
		return !missingFeatures_java.contains(ID);
	}

	private static boolean perlContains(int ID) {
		List<Integer> missingFeatures_prl = Arrays.asList(
				FeatureDictionary.I_XTRA_END_SUBJECTLINE);
		return !missingFeatures_prl.contains(ID);
	}

	private static boolean javaScriptContains(int ID) {
		List<Integer> missingFeatures_js = Arrays.asList(
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_OPTIONS, 
				FeatureDictionary.I_LOOK_BEHIND, 
				FeatureDictionary.I_LOOK_BEHIND_NEGATIVE, 				
				FeatureDictionary.I_XTRA_END_SUBJECTLINE);
		return !missingFeatures_js.contains(ID);
	}

	private static boolean dotNetContains(int ID) {
		List<Integer> missingFeatures_dn = Arrays.asList(
				FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
				FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
				FeatureDictionary.I_XTRA_END_SUBJECTLINE);
		return !missingFeatures_dn.contains(ID);
	}

	static int[] rankedFeatures = { FeatureDictionary.I_REP_ADDITIONAL,
			FeatureDictionary.I_META_CAPTURING_GROUP,
			FeatureDictionary.I_REP_KLEENISH, FeatureDictionary.I_META_CC,
			FeatureDictionary.I_META_DOT_ANY, FeatureDictionary.I_CC_RANGE,
			FeatureDictionary.I_POS_START_ANCHOR,
			FeatureDictionary.I_POS_END_ANCHOR,
			FeatureDictionary.I_META_NCC, FeatureDictionary.I_CC_WHITESPACE,
			FeatureDictionary.I_META_OR, FeatureDictionary.I_CC_DECIMAL,
			FeatureDictionary.I_CC_WORD, FeatureDictionary.I_REP_QUESTIONABLE,
			FeatureDictionary.I_REP_LAZY, FeatureDictionary.I_LOOK_NON_CAPTURE,
			FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
			FeatureDictionary.I_REP_SINGLEEXACTLY,
			FeatureDictionary.I_CC_NWHITESPACE,
			FeatureDictionary.I_REP_DOUBLEBOUNDED,
			FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
			FeatureDictionary.I_POS_WORD, FeatureDictionary.I_CC_NWORD,
			FeatureDictionary.I_REP_LOWERBOUNDED,
			FeatureDictionary.I_LOOK_AHEAD, FeatureDictionary.I_XTRA_OPTIONS,
			FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
			FeatureDictionary.I_LOOK_BEHIND,
			FeatureDictionary.I_XTRA_END_SUBJECTLINE,
			FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
			FeatureDictionary.I_CC_NDECIMAL,
			FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
			FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE,
			FeatureDictionary.I_POS_NONWORD

	};

}
