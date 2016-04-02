package appendix;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeSet;

import build_corpus.CorpusUtil;
import build_corpus.RegexProjectSet;
import c.IOUtil;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class AppendixHelper {
	
	private static String thesisPath = "/Users/carlchapman/git/thesis/";
	private static String nonTexPath = thesisPath + "nontex/";

	public static void main(String[] args) throws IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException {
		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
		StringBuilder sb = new StringBuilder();
		HashMap<Integer, TreeSet<RegexProjectSet>> bucketSetMap = new HashMap<Integer,TreeSet<RegexProjectSet>>();
		for(RegexProjectSet regex : corpus){
			if(regex.getRankableValue()>1 && regex.isRexCompatible()){
				int key = regex.getUnescapedPattern().length();
				if(key==0){
					continue;
				}
				TreeSet<RegexProjectSet> bucket = bucketSetMap.get(key);
				if(bucket==null){
					bucket = new TreeSet<RegexProjectSet>();
				}
				bucket.add(regex);
				bucketSetMap.put(key,bucket);
			}
		}
		LinkedList<Integer> keylist = new LinkedList<Integer>();
		keylist.addAll(bucketSetMap.keySet());
		Collections.sort(keylist);
		for(Integer key : keylist){
			TreeSet<RegexProjectSet> bucket = bucketSetMap.get(key);
			for(RegexProjectSet regex : bucket){
				sb.append("\\item[] " + wrap(regex)+"\n");
			}
		}
		
		
		File corpusLatexOut = new File(nonTexPath,"top200Shorts.txt");
		IOUtil.createAndWrite(corpusLatexOut, sb.toString());

	}
	
	public static String wrap(RegexProjectSet regex){
		char[] charsToUse = { '!', '@', '|', ':'};
		String un = regex.getUnescapedPattern();
		for(int i=0;i<charsToUse.length;i++){
			char c = charsToUse[i];
			if(un.indexOf(c)==-1){
				return "\\cverb"+c+un+c;
			}
		}
		return "\\cverb•"+un+"•";
	}

}
