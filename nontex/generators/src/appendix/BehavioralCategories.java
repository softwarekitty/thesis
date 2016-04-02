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
	private static final String behavioralPath = homePath +
		"analysis/behavioral_clustering/";

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
		String filtered_corpus_path = homePath + "csharp/filteredCorpus.txt";

		HashMap<String, Integer> patternIndexMap = getPatternIndexMap();
		// HashMap<Integer, Integer> javaCSIndexMap = getJavaCSIndexMap();
		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
		HashMap<Integer, RegexProjectSet> lookup = getLookup(filtered_corpus_path, corpus, patternIndexMap);
		TreeSet<Cluster> behavioralClusters = getClustersFromFile(fullInputFilePath, fullOutputFilePath, mclInput, lookup);
		dumpCategories(behavioralClusters, "clusterCategoryDump.tex");
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

	private static HashMap<Integer, RegexProjectSet> getLookup(
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

	private static HashMap<String, Integer> getPatternIndexMap()
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

	static String[] categoryNames = { "Code Search and Variable Capturing",
			"Content Of Brackets and Parenthesis", "Anchored Patterns",
			"Requiring Two or More Characters In Sequence",
			"Requiring a Specific Character to Match",
			"Multiple Matching Alternatives", "Uncategorized" };

	// `^https?://'(23), `&#(x?[0-9A-Fa-f]+)[^0-9A-Fa-f]'(18),
	// `<base\s+href\s*=\s*[\'"]?([^\'">]+)'(17),
	// `SECRET|PASSWORD|PROFANITIES_LIST'(13),
	// `^([a-zA-Z0-9_]+)=(.*)'(12), `\$\{([\w\-]+)\}'(11), `https?://'(9),
	// `http://'(9), `(.+)=(.+)'(9), `var'(9), `HTML'(9), `Xorg'(9),
	// `Websafe'(9),
	// `cc_(.*)$'(9), `lightlink'(9)
	static ArrayList<String> list0 = new ArrayList<String>(Arrays.asList(
			"^(http|ftp):\\/\\/[\\w.\\-]+\\/(\\S*)", "^https?://", "&#(x?[0-9A-Fa-f]+)[^0-9A-Fa-f]", 
			"<base\\s+href\\s*=\\s*[\\'\"]?([^\\'\">]+)", "\\nmd5_data = {\\n([^}]+)}", "SECRET|PASSWORD|PROFANITIES_LIST", 
			"^([a-zA-Z0-9_]+)=(.*)", "\\$\\{([\\w\\-]+)\\}", "https?://", "http://", "(.+)=(.+)", "var", "HTML", "Xorg", 
			"Websafe", "cc_(.*)$", "lightlink", "^(?P<remote_ip>[^ ]+) (?P<http_user>[^ ]+) (?P<http_user2>[^ ]+) (?P<req_date>[^ ]+) (?P<timezone>[^ ]+) \"(?P<request>[^ ]+) (?P<url>[^ ]+) (?P<http_protocol>[^ ]+) (?P<init_retcode>[^ ]+)",
			"(?P<year>\\d{4})-(?P<month>\\d{1,2})-(?P<day>\\d{1,2})[T ](?P<hour>\\d{1,2}):(?P<minute>\\d{1,2})(?::(?P<second>\\d{1,2})(?:\\.(?P<microsecond>\\d{1,6})\\d{0,6})?)?(?P<tzinfo>Z|[+-]\\d{2}(?::?\\d{2})?)?$",
			"(?P<year>[0-9]{4})([-/.])(?P<month>[0-9]{2})\2(?P<day>[0-9]{2})$",
			"<textarea name=\"text\"[\\W]*rows=\"25\"[\\W]id=\"text\"",
			"^(\\s*)#\\s*begin\\s+wxGlade:\\s*([a-zA-Z_][\\w:]*?)::(\\w+)\\s*$",
			" failures today: (\\w+)",
			"QtGui\\.QApplication.translate\\(.*?, (.*?), None, QtGui\\.QApplication\\.UnicodeUTF8\\)",
			"^(?:nfs://)?(?P<host>([a-zA-Z][\\w\\.^-]*|\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}))(?::(?P<port>\\d*))?(?P<dir>/[\\w/]*)?$",
			"^(.*\\n)*def\\s+step_init\\s*\\(\\s*\\)\\s*:"
			
			));

	// `\(.*\)'(29), `{[^}]*}'(27), `".*"'(25), `<(.+)>'(23), `\[.*\]'(22),
	// `<(.*)>'(21), `\([^)]+\)'(10), `'.''(9), `/.+/$'(9), `'.*'$'(9)
	static ArrayList<String> list1 = new ArrayList<String>(Arrays.asList("\\(.*\\)", "{[^}]*}", "\".*\"", "<(.+)>", "\\[.*\\]", "<(.*)>", "\\([^)]+\\)", "'.'", "/.+/$", "'.*'$"));

	// `(\w+)$'(35), `^[-\w/]+$'(30), `^[a-zA-Z]'(17), `^-?\d+$'(17),
	// `^\s'(16), `:\d+$'(16), `^\s*\#'(15), `^[\w_]+$'(14), `^(\d+)'(14),
	// '^\w'(13), `^/'(13), `^\W+'(11), `^[\w.@+-]+$'(10), `(\d+)$'(10),
	// `^[a-zA-Z][a-zA-Z0-9\-_]*$'(10), `^[ -~]*$'(10), `.*\!$'(10)
	static ArrayList<String> list2 = new ArrayList<String>(Arrays.asList("^\\s*$","(\\w+)$", "^[-\\w/]+$", "^[a-zA-Z]", "^-?\\d+$", "^\\s", ":\\d+$", "^\\s*\\#", "^[\\w_]+$", "^(\\d+)", "^\\w", "^/", "^\\W+", "^[\\w.@+-]+$", "(\\d+)$", "^[a-zA-Z][a-zA-Z0-9\\-_]*$", "^[ -~]*$", ".*\\!$", "#.*$", "\\.\\d+$"));

	// `\d+\.\d+'(30), ` '(17), `//'(16), `(\S)\s+(\S)'(16), `(\033|~{)'(14),
	// `([a-z\d])([A-Z])'(13), `%([0-9A-Fa-f]{2})'(13), `\.\d+$'(12),
	// `\\"'(11), `\$\$.*'(11), `([A-Z][a-z]+[A-Z][^ ]+)'(11), `[ \t][
	// \t]+'(10),
	// `\\[A-Za-z]+'(9), `[A-Z][a-z]+'(9), `\*/'(9), `@[a-z]+'(9),
	// `\$[()]'(9), `v[0-9]+.*'(9)
	static ArrayList<String> list3 = new ArrayList<String>(Arrays.asList("[a-zA-Z][-_.:a-zA-Z0-9]*", "failures today: (\\w+)", "\\d+\\.\\d+", "  ", "//", "(\\S)\\s+(\\S)", "(\\033|~{)", "([a-z\\d])([A-Z])", "%([0-9A-Fa-f]{2})", "\\\\\"", "\\$\\$.*", "([A-Z][a-z]+[A-Z][^ ]+)", "[ \\t][ \\t]+", "[A-Za-z]+", "[A-Z][a-z]+", "\\*/", "@[a-z]+", "\\$[()]", "v[0-9]+.*"));

	// `\n\s*'(42), `/+'(39), `:+'(31), `\.+'(24),
	// `( +)'(24), `%'(22), `{'(21), `\|'(19), `\-'(17), `@'(17),
	// `#.*$'(16), `\['(14), `}'(14), `\('(12), `a+'(10),
	// `\t+'(10), `\)'(9), `\]'(9)
	static ArrayList<String> list4 = new ArrayList<String>(Arrays.asList("\\s*,\\s*","\\n","\\n\\s*", "/+", ":+", "\\.+", "( +)", "%", "{", "\\|", "\\-", "@", "\\[", "}", "\\(", "a+", "\\t+", "\\)", "\\]"));

	// `..'(95), `(\W)'(89), `(\s)'(89), `\S+'(74), `\d'(58),
	// `"|\\'(35), `[\000-\037]'(31), ,`[\\/]'(31) `[\({\[\]}\)\n]'(21),
	// `[^!-~]'(19), `[ ,]'(19), `[\\"]|[^ -~]'(19), `{|}'(18),
	// `,|;'(18), `[<>&]'(16), `[-.]'(12), `[()]'(11),
	// `(["\\`])'(10), `\s*=.*'(9), `[@{} ]'(9), `["\'/]'(9)
	static ArrayList<String> list5 = new ArrayList<String>(Arrays.asList("(\\s+)","([\\{-\\~\\[-\\` -\\&\\(-\\+\\:-\\@\\/])","[\\x00-\\x1f\\\\\"\\b\\f\\n\\r\\t]","\\W","[\\x80-\\xff]","\\s","\\s+", "..", "(\\W)", "(\\s)", "\\S+", "\\d", "\"|\\\\", "[\\000-\\037]", "[\\\\/]", "[\\({\\[\\]}\\)\\n]", "[^!-~]", "[ ,]", "[\\\\\"]|[^ -~]", "{|}", ",|;", "[<>&]", "[-.]", "[()]", "([\"\\\\`])", "\\s*=.*", "[@{} ]", "[\"\\'/]"));
	static LinkedList<List<String>> categoryOracle = new LinkedList<List<String>>(Arrays.asList(list0, list1, list2, list3, list4, list5));

	private static boolean addClusterToCategoryClusters(Cluster cluster,
			List<Category> categories) {
		String shortest = cluster.getShortest().getUnescapedPattern();

		int i = 0;
		for (; i < categoryOracle.size(); i++) {
			List<String> categoryMembers = categoryOracle.get(i);
			if (categoryMembers.contains(shortest)) {
				categories.get(i).add(cluster);
				return true;
			}
		}
		categories.get(i).add(cluster);
		return false;
	}

	private static void dumpCategories(TreeSet<Cluster> behavioralClusters,
			String outFilename) throws ClassNotFoundException, SQLException {
		StringBuilder sb = new StringBuilder();

		List<Category> categoryClusters = new LinkedList<Category>();
		for (int catID = 0; catID <= categoryOracle.size(); catID++) {
			categoryClusters.add(new Category());
		}

		int totalPatterns = 0;
		int categorizedClusters = 0;
		TreeSet<Integer> allProjectIDs = new TreeSet<Integer>();
		Category nonCategorizedClusters = new Category();
		for (Cluster cluster : behavioralClusters) {
			int nPatterns = cluster.size();
			totalPatterns += nPatterns;
			allProjectIDs.addAll(cluster.getAllProjectIDs());
			if (addClusterToCategoryClusters(cluster, categoryClusters)) {
				categorizedClusters++;
			} else {
				nonCategorizedClusters.add(cluster);
			}
		}

		sb.append("Cluster stats:\n\ntotalClusters: " +
			behavioralClusters.size() + "\nCategorizedClusters: " +
			categorizedClusters + "\nTotalPatterns: " + totalPatterns +
			"\ntotalnProjects: " + allProjectIDs.size() + "\n\n");
		sb.append(getCategoryProjectInfo(categoryClusters) + "\n\n\n");
		File output = new File(behavioralPath, outFilename);
		IOUtil.createAndWrite(output, sb.toString());
	}

	// this builds the giant string printed in the cluster dump
	// by printing all clusters in each category
	private static String getCategoryProjectInfo(List<Category> categories)
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
				AppendixHelper.wrap(category.getCombinedClusters().getShortest()) +
				"\n\n");
			i++;
			for (Cluster currentCluster : category) {
				sb.append(currentCluster.getItemLineLatex());
			}
			sb.append(categoryFooter);
		}
		return sb.toString();
	}
}
