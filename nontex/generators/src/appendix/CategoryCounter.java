package appendix;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import build_corpus.RegexProjectSet;
import c.C;
import c.IOUtil;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class CategoryCounter {
	
	private static String genPath = "/Users/carlchapman/git/thesis/nontex/generators/";
	private static String dumpPath = genPath + "categoryFiles/";
	
	public static void main(String[] args) throws IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException{
		HashMap<String, TreeSet<RegexProjectSet>> cats = populateCategories();
		Set<String> y = cats.keySet();
		System.out.println("nCats: "+y.size());
		for(String cat : y){
			TreeSet<RegexProjectSet> miniCorpus = cats.get(cat);
			String categoryData = cat + " nPatterns: " + miniCorpus.size() + " nProjects: " + aggregateProjectIDs(miniCorpus).size();
			System.out.println(categoryData);
			File dumpFile = new File(dumpPath,cat+".txt");
			StringBuilder contents = new StringBuilder();
			contents.append(categoryData+"\n");
			for(RegexProjectSet rps :miniCorpus){
				contents.append(rps.getContent()+"\t"+rps.getProjectsCSV()+"\n");
			}
			IOUtil.createAndWrite(dumpFile, contents.toString());
		}
	}
	
	

	
	public static HashMap<String, TreeSet<RegexProjectSet>> populateCategories() throws IOException, IllegalArgumentException, QuoteRuleException, PythonParsingException{
		TreeSet<RegexProjectSet> corpus = new TreeSet<RegexProjectSet>();
		HashMap<String, TreeSet<RegexProjectSet>> categories = new HashMap<String, TreeSet<RegexProjectSet>>();
		List<String> lines = IOUtil.getLines(genPath + "newCategoriesCorpus.txt");
		
		int counter=0;
		for(String line : lines){
			String[] parts = line.split("\t");
			String categoryName = parts[0];
			String pattern = parts[1];
			String[] IDs = parts[2].split(",");
			TreeSet<Integer> IDSet = new TreeSet<Integer>();
			for(String id : IDs){
				IDSet.add(Integer.parseInt(id));
			}
			TreeSet<RegexProjectSet> miniCorpus = categories.get(categoryName);
			if(miniCorpus == null){
				miniCorpus = new TreeSet<RegexProjectSet>();
			}
			RegexProjectSet rps = new RegexProjectSet(pattern,IDSet);
			miniCorpus.add(rps);
			categories.put(categoryName, miniCorpus);
		}
		return categories;
	}
	
	private static TreeSet<Integer> aggregateProjectIDs(
			TreeSet<RegexProjectSet> regexSet) {
		TreeSet<Integer> allProjectIDs = new TreeSet<Integer>();
		for (RegexProjectSet member : regexSet) {
			allProjectIDs.addAll(member.getProjectIDSet());
		}
		return allProjectIDs;
	}
}




