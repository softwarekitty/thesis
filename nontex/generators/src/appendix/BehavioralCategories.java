package appendix;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import build_corpus.CorpusUtil;
import build_corpus.RegexProjectSet;
import c.IOUtil;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class BehavioralCategories {
	public static final String homePath = "/Users/carlchapman/Documents/SoftwareProjects/tour_de_source/";
	public static final String behavioralPath = homePath +
		"analysis/behavioral_clustering/";
	public static final String analysisPath = homePath +
			"analysis/";
	public static String filtered_corpus_path = homePath + "csharp/filteredCorpus.txt";

	// after getting the behavioral graph in csharp,
	// output a human-readable dump of clusters found.
	public static void main(String[] args) throws ClassNotFoundException,
			SQLException, IOException, InterruptedException,
			IllegalArgumentException, QuoteRuleException,
			PythonParsingException {

		String fullInputFilePath = behavioralPath +
			"behavioralSimilarityGraph.abc";
		File finalGraphFile = new File(fullInputFilePath);

		DecimalFormat df = new DecimalFormat("0.00");
		double i_value = 1.8;
		double p_value = 0.75;
		double k_value = 83;

		String suffix = "_i" + df.format(i_value) + "_p" + df.format(p_value) +
			"_k" + k_value + "_";
		String fullOutputFilePath = behavioralPath +
			"behavioralSimilarityClusters" + suffix + ".txt";
		String newOptions = " -tf gq(" + df.format(p_value) + ") -tf #knn(" +
			k_value + ")";
		String mclInput = fullInputFilePath + " -I " + df.format(i_value) +
			newOptions + " --abc -o " + fullOutputFilePath;


		HashMap<String, Integer> patternIndexMap = getPatternIndexMap();
		// HashMap<Integer, Integer> javaCSIndexMap = getJavaCSIndexMap();
		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
		HashMap<Integer, RegexProjectSet> lookup = getLookup(filtered_corpus_path, corpus, patternIndexMap);
		TreeSet<Cluster> behavioralClusters = getClustersFromFile(fullInputFilePath, fullOutputFilePath, mclInput, lookup);
		dumpCategories(behavioralClusters, "clusterCategoryDump.tex", patternIndexMap,corpus);
	}

	// this is the result of rushed design - needing to map one type of index
	// to another to get back to an original state
	// this is also quite fragile...modifying this text file will
	// break the mapping, which relies on line number!
	private static HashMap<Integer, Integer> getJavaCSIndexMap()
			throws IOException {

		HashMap<Integer, Integer> javaCSIndexMap = new HashMap<Integer, Integer>();
		int lineNumber = 0;

		String filteredContent = FileUtils.readFileToString(new File(homePath +
			"csharp/filteredCorpus.txt"), "UTF-8");
		Pattern finder = Pattern.compile("(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(filteredContent);
		while (pairMatcher.find()) {
			String indexString = pairMatcher.group(1);
			javaCSIndexMap.put(Integer.parseInt(indexString), lineNumber);
			// if (lineNumber == 16) {
			//
			// /*
			// * testing the mapping using the rex strings generated, which
			// * for index 16 (on line 17 in a text editor) we have `failed:
			// * (.*)', which requires the long string `failed', and so is
			// * easy to recognize.
			// */
			// System.out.println("javaIndex: " + indexString +
			// " csharpIndex: " + lineNumber + "pattern: " +
			// pairMatcher.group(2));
			// System.exit(0);
			// }
			lineNumber++;
		}

		return javaCSIndexMap;
	}

	private static TreeSet<Cluster> getClustersFromFile(
			String fullInputFilePath, String fullOutputFilePath,
			String mclInput, HashMap<Integer, RegexProjectSet> lookup)
			throws InterruptedException, IOException {

		// run the mcl script
		List<String> cmds = new ArrayList<String>(Arrays.asList(mclInput.split(" ")));
		System.out.println("mcl input: " + mclInput);
		cmds.add(0, "/usr/local/bin/mcl");
		ProcessBuilder pb = new ProcessBuilder(cmds);
		Process p = pb.start();
		int x = p.waitFor();
		System.out.println("process int: " + x);

		// parse mcl output
		TreeSet<Cluster> clusters = new TreeSet<Cluster>();
		File outputFile = new File(fullOutputFilePath);

		// this outputFile is where mcl wrote its output
		List<String> lines = FileUtils.readLines(outputFile, "UTF-8");
		int lineNumber = 0;
		int m = 0;
		// TreeSet<Integer> missingSet = new TreeSet<Integer>();

		// each line of the output is a cluster
		for (String line : lines) {
			String[] indices = line.split("\t");
			Cluster cluster = new Cluster();

			// each tab-separated index is one regex
			for (String index : indices) {
				int indexValue = Integer.parseInt(index);

				RegexProjectSet rps = lookup.get(indexValue);
				if (rps == null) {
					// happens when the abc file has some index not in
					// the lookup - 232 values from 13597 to 13910
					// these are unique patterns were added back
					// with only edges to themselves to help
					// to understand a missing 2.5% of projects,
					// as documented in the git comments on page:
					// https://github.com/softwarekitty/tour_de_source/commit/020651fca048452df4569e636aebc8e42f9a6153
					// System.out.println("missing rps at: " + indexValue +
					// " in cluster: " + cluster.thisClusterID);
					// missingSet.add(indexValue);
					m++;

				} else {
					boolean added = cluster.add(rps);
					if (!added) {
						System.out.println("indexValue: " + indexValue +
							" failure to add: " + rps.dump(0, 1) +
							" problem with: " + Arrays.toString(indices) +
							"on line: " + lineNumber);
						// System.out.println("cluster: "+cluster.getContent());
						// waitNsecsOrContinue(12);
					}
				}
			}
			if (cluster.isEmpty()) {
				// these clusters are dummy clusters of size 1 to deal with
				// an accounting issue - pay them no mind
				// System.out.println("missing cluster "+m+" on: "+ lineNumber);
			} else {
				clusters.add(cluster);
			}
			lineNumber++;
		}
		// System.out.println("nMissing: " + missingSet.size());
		// System.out.println("missing set: " + missingSet.toString());
		// System.exit(0);
		return clusters;
	}

	public static HashMap<Integer, RegexProjectSet> getLookup(
			String filtered_corpus_path, TreeSet<RegexProjectSet> corpus,
			HashMap<String, Integer> patternIndexMap) {
		HashMap<Integer, RegexProjectSet> lookup = new HashMap<Integer, RegexProjectSet>();

		for (RegexProjectSet regex : corpus) {
			String originalPattern = regex.getContent();
			Integer javaIndex = patternIndexMap.get(originalPattern);
			// Integer csharpIndex = javaCSIndexMap.get(javaIndex);
			lookup.put(javaIndex, regex);
		}
		return lookup;
	}

	public static HashMap<String, Integer> getPatternIndexMap()
			throws IOException {
		HashMap<String, Integer> patternIndexMap = new HashMap<String, Integer>();
		String content = FileUtils.readFileToString(new File(homePath +
			"analysis/analysis_output/exportedCorpusRaw.txt"), "UTF-8");
		Pattern finder = Pattern.compile("(\\d+)\\t(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(content);
		while (pairMatcher.find()) {
			String indexString = pairMatcher.group(1);
			String originalPattern = pairMatcher.group(3);
			Integer existsAlready = patternIndexMap.get(originalPattern);
			if (existsAlready != null) {
				System.out.println("duplicate original pattern: " +
					originalPattern);
				System.exit(1);
			}
			patternIndexMap.put(originalPattern, Integer.parseInt(indexString));
		}
		// System.out.println(patternIndexMap.size());
		return patternIndexMap;
	}

	static String[] categoryNames = { "Web And XML Related",
			"Path And File Related", "Code Related", "Labels",
			"Non-Free Ordinary Strings", "Bracket Capturing", "Messages",
			"Identifiers", "Tuples And Punctuation", "Numbers And Dates",
			"Space", "Vanilla Characters", "Uncategorized" };
	
	// "Web And XML Related" https, &#0xff;, etc, doctype
	static ArrayList<Integer> list0 = new ArrayList<Integer>(Arrays.asList(447,4050));
	
	// "Path And File Related" /usr/bin/.*\.py.  
	static ArrayList<Integer> list1 = new ArrayList<Integer>(Arrays.asList(8896));
	
	// "Code Or Shell Related"  include, a=(b)
	static ArrayList<Integer> list2 = new ArrayList<Integer>(Arrays.asList(11588,11676));
	
	// "Labels"
	static ArrayList<Integer> list3 = new ArrayList<Integer>(Arrays.asList(12563,12556));
	
	// "Non-Free Ordinary Strings" a+, [aeiou]
	static ArrayList<Integer> list4 = new ArrayList<Integer>(Arrays.asList(6039));
	
	// "Bracket Capturing"
	static ArrayList<Integer> list5 = new ArrayList<Integer>(Arrays.asList(3948,4015));
	
	// "Messages"
	static ArrayList<Integer> list6 = new ArrayList<Integer>(Arrays.asList(7777));
	
	// "Identifiers" - may have a delmiter, but focus is on a semi-free string following rules
	static ArrayList<Integer> list7 = new ArrayList<Integer>(Arrays.asList(2572));
	
	// "Tuples And Punctuation" - focus is on a delimiter, rest of content is free, often captured
	static ArrayList<Integer> list8 = new ArrayList<Integer>(Arrays.asList(1573));
	
	// "Numbers And Dates"
	static ArrayList<Integer> list9 = new ArrayList<Integer>(Arrays.asList(418,7665,11055,710));
	
	// "Space"
	static ArrayList<Integer> list10 = new ArrayList<Integer>(Arrays.asList(8233));
	
	// "Vanilla Characters"
	static ArrayList<Integer> list11 = new ArrayList<Integer>(Arrays.asList(6302,7250));
	//static LinkedList<List<Integer>> categoryOracle = new LinkedList<List<Integer>>(Arrays.asList(list3));

	static LinkedList<List<Integer>> categoryOracle = new LinkedList<List<Integer>>(Arrays.asList(list0, list1, list2, list3, list4, list5, list6, list7, list8, list9, list10, list11));

	private static boolean addClusterToCategoryClusters(Cluster cluster,
			List<Category> categories, HashMap<String, Integer> patternIndexMap) {
		Integer javaIndex = patternIndexMap.get(cluster.getHeaviest().getContent());
		int i = 0;
		for (; i < categoryOracle.size(); i++) {
			List<Integer> categoryMembers = categoryOracle.get(i);
			if (categoryMembers.contains(javaIndex)) {
				categories.get(i).add(cluster);
				return true;
			}
		}
		categories.get(i).add(cluster);
		return false;
	}

	private static void dumpCategories(TreeSet<Cluster> behavioralClusters,
			String outFilename, HashMap<String, Integer> patternIndexMap, TreeSet<RegexProjectSet> corpus)
			throws ClassNotFoundException, SQLException, IllegalArgumentException, IOException, QuoteRuleException, PythonParsingException {
		HashMap<Integer, TreeSet<RegexProjectSet>> projectPatternMM = CorpusUtil.reloadProjectPatternMM(corpus);
		
		StringBuilder sb = new StringBuilder();

		List<Category> categoryClusters = new LinkedList<Category>();
		for (int catID = 0; catID <= categoryOracle.size(); catID++) {
			categoryClusters.add(new Category());
		}

		int touchedPatterns = 0;
		int categorizedClusters = 0;
		TreeSet<Integer> allProjectIDs = new TreeSet<Integer>();
		Cluster allCategorizedRegexes = new Cluster();
		Category nonCategorizedClusters = new Category();
		
		//main loop
		for (Cluster cluster : behavioralClusters) {
			touchedPatterns += cluster.size();
			allProjectIDs.addAll(cluster.getAllProjectIDs());
			if (addClusterToCategoryClusters(cluster, categoryClusters, patternIndexMap)) {
				categorizedClusters++;
				allCategorizedRegexes.addAll(cluster);
			} else {
				nonCategorizedClusters.add(cluster);
			}
		}
		
		//measuring coverage
		int totallyCoveredProjects = 0;
		int partiallyCoveredProjects = 0;
		int nProjectsLoaded = projectPatternMM.size();
		for(Entry<Integer, TreeSet<RegexProjectSet>> entry : projectPatternMM.entrySet()){
			TreeSet<RegexProjectSet> regexes = entry.getValue();
			TreeSet<RegexProjectSet> regexesCopy = new TreeSet<RegexProjectSet>();
			regexesCopy.addAll(regexes);
			regexesCopy.removeAll(allCategorizedRegexes);
			if(regexesCopy.isEmpty()){
				totallyCoveredProjects++;
			}else if(regexes.size() - regexesCopy.size() >0){
				partiallyCoveredProjects++;
			}
		}
		int untouchedProjects = nProjectsLoaded-(partiallyCoveredProjects+totallyCoveredProjects);
		
		
		//print a summary report
		sb.append("Cluster stats:\n\ntotalClusters: " +behavioralClusters.size() + 
			"\nCategorizedClusters: " + categorizedClusters + 
			"\nTotalPatterns: " + corpus.size() + " (in the corpus)" +
			"\nTouchedPatterns: " + touchedPatterns + " (by some cluster)" +
			"\nnTotalProjects: " + nProjectsLoaded + " (containing a corpus regex)" +
			"\nnProjectsTouched: " + allProjectIDs.size() +" (by some cluster)" +
			"\ntotallyCoveredProjects: " + totallyCoveredProjects +" (by categorized regexes)" +
			"\npartiallyCoveredProjects: " + partiallyCoveredProjects +" (by categorized regexes)" +
			"\nuntouchedProjects: " + untouchedProjects + " (no category touches these)" +
			"\n\n");
		
		//get the bulk of data
		sb.append(getCategoryProjectInfo(categoryClusters, patternIndexMap) +
			"\n\n\n");
		File output = new File(behavioralPath, outFilename);
		IOUtil.createAndWrite(output, sb.toString());
	}

	// this builds the giant string printed in the cluster dump
	// by printing all clusters in each category
	private static String getCategoryProjectInfo(List<Category> categories,
			HashMap<String, Integer> patternIndexMap)
			throws ClassNotFoundException, SQLException {
		StringBuilder sb = new StringBuilder();
		String categoryHeader = "\\begin{multicols}{2}\n\\begin{description}[noitemsep,topsep=0pt]\n";
		String categoryFooter = "\\end{description}\n\\end{multicols}\n\n\n\n";
		int i = 1;
		for (Category category : categories) {
			sb.append(categoryHeader);
			int nClusters = category.size();
			int nPatternsTotal = category.getCombinedClusters().getNPatterns();
			int nProjectsTotal = category.getCombinedClusters().getNProjects();
			sb.append("categoryCluster " +
				i +
				" stats:\nname: " +
				categoryNames[i - 1] +
				"\nnClusters: " +
				nClusters +
				"\nnPatternsTotal: " +
				nPatternsTotal +
				"\nnProjectsTotal: " +
				nProjectsTotal +
				"\nshortest: " +
				AppendixHelper.wrap(category.getCombinedClusters().getShorty()) +
				"\n\n");
			i++;
			for (Cluster currentCluster : category) {
				sb.append(currentCluster.getItemLineLatex(patternIndexMap));
			}
			sb.append(categoryFooter);
		}
		return sb.toString();
	}
}
