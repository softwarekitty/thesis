package appendix;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import analysisUtil.build_corpus.CorpusUtil;
import metric.FeatureCount;
import metric.FeatureDictionary;
import metric.FeatureSetClass;
import build_corpus.RegexProjectSet;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class ToolSupport {

	static int[] bricsMissingFeatures = { FeatureDictionary.I_REP_LAZY,
			FeatureDictionary.I_CC_DECIMAL,
			FeatureDictionary.I_CC_NDECIMAL,
			FeatureDictionary.I_CC_WHITESPACE,
			FeatureDictionary.I_CC_NWHITESPACE,
			FeatureDictionary.I_CC_WORD, FeatureDictionary.I_CC_NWORD,
			FeatureDictionary.I_LOOK_AHEAD,
			FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
			FeatureDictionary.I_LOOK_BEHIND,
			FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
			FeatureDictionary.I_LOOK_NON_CAPTURE,
			FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
			FeatureDictionary.I_POS_END_ANCHOR,
			FeatureDictionary.I_POS_NONWORD,
			FeatureDictionary.I_POS_START_ANCHOR,
			FeatureDictionary.I_POS_WORD,
			FeatureDictionary.I_XTRA_END_SUBJECTLINE,
			FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
			FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
			FeatureDictionary.I_XTRA_OPTIONS,
			FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE };
	
	// by using Rex, these are from PaperWriter
	static int[] rexMissingFeatures = { FeatureDictionary.I_REP_LAZY,
			FeatureDictionary.I_LOOK_AHEAD,
			FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
			FeatureDictionary.I_LOOK_BEHIND,
			FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
			FeatureDictionary.I_LOOK_NON_CAPTURE,
			FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
			FeatureDictionary.I_XTRA_NAMED_BACKREFERENCE,
			FeatureDictionary.I_POS_NONWORD, FeatureDictionary.I_POS_WORD,
			FeatureDictionary.I_XTRA_NAMED_GROUP_PYTHON,
			FeatureDictionary.I_XTRA_OPTIONS,
			FeatureDictionary.I_XTRA_END_SUBJECTLINE };
	
	static int[] hampiMissingFeatures = { FeatureDictionary.I_LOOK_AHEAD,
			FeatureDictionary.I_LOOK_AHEAD_NEGATIVE,
			FeatureDictionary.I_LOOK_BEHIND,
			FeatureDictionary.I_LOOK_BEHIND_NEGATIVE,
			FeatureDictionary.I_META_NUMBERED_BACKREFERENCE,
			FeatureDictionary.I_POS_NONWORD, FeatureDictionary.I_POS_WORD,
			FeatureDictionary.I_XTRA_END_SUBJECTLINE,
			FeatureDictionary.I_XTRA_VERTICAL_WHITESPACE };
	
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException {
		List<Integer> bricsMissingList = new ArrayList<Integer>();
		List<Integer> rexMissingList = new ArrayList<Integer>();
		List<Integer> hampiMissingList = new ArrayList<Integer>();
		for(int bm : bricsMissingFeatures){
			bricsMissingList.add(bm);
		}
		for(int rm : rexMissingFeatures){
			rexMissingList.add(rm);
		}
		for(int hm : hampiMissingFeatures){
			hampiMissingList.add(hm);
		}
		FeatureDictionary fd = new FeatureDictionary();
		HashMap<Integer, Integer> bricsDummy = new HashMap<Integer,Integer>();
		HashMap<Integer, Integer> rexDummy = new HashMap<Integer,Integer>();
		HashMap<Integer, Integer> hampiDummy = new HashMap<Integer,Integer>();

		for(Integer featureID : fd.intToNameMap.keySet()){
			if(!bricsMissingList.contains(featureID)){
				bricsDummy.put(featureID, 1);
			}
			if(!rexMissingList.contains(featureID)){
				rexDummy.put(featureID, 1);
			}
			if(!hampiMissingList.contains(featureID)){
				hampiDummy.put(featureID, 1);
			}
		}
		FeatureSetClass bricsFeatures = new FeatureSetClass(new FeatureCount(bricsDummy));
		FeatureSetClass rexFeatures = new FeatureSetClass(new FeatureCount(rexDummy));
		FeatureSetClass hampiFeatures = new FeatureSetClass(new FeatureCount(hampiDummy));

		
		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();

		
		
		
		int bricsOK = 0;
		int rexOK = 0;
		int hampiOK = 0;
		int has2 = 0;
		
		for(RegexProjectSet rps : corpus){
			if(rexFeatures.subsumes(new FeatureSetClass(rps.getFeatures())) && rps.getRankableValue() > 1){
				rexOK++;
			}
			if(bricsFeatures.subsumes(new FeatureSetClass(rps.getFeatures())) && rps.getRankableValue()>1){
				bricsOK++;
			}
			if(hampiFeatures.subsumes(new FeatureSetClass(rps.getFeatures())) && rps.getRankableValue()>1){
				hampiOK++;
			}
			if(rps.getRankableValue()>1){
				has2++;
			}
		}
		
		System.out.println("bricsOK: "+bricsOK + " rexOK: "+rexOK+ " hampiOK: "+hampiOK + " has2: "+has2);

	}

}
