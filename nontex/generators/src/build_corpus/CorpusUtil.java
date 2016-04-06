package build_corpus;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import appendix.BehavioralCategories;
import c.C;
import c.IOUtil;
import exceptions.AlienFeatureException;
import exceptions.PythonParsingException;
import exceptions.QuoteRuleException;

public class CorpusUtil {

	public static final String connectionString = "jdbc:sqlite:/Users/carlchapman/Documents/SoftwareProjects/tour_de_source/tools/merged/merged_report.db";

	public static TreeSet<RegexProjectSet> initializeCorpus(
			String connectionString) throws ClassNotFoundException,
			SQLException, IllegalArgumentException, QuoteRuleException,
			PythonParsingException {

		HashMap<PatternEscapedPair, TreeSet<Integer>> patternProjectMM = new HashMap<PatternEscapedPair, TreeSet<Integer>>();
		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// unlike the previous version, we will now do the group by in memory,
		// to be able to finally get an accurate count of projects per unquoted
		// pattern
		String query = "select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		TreeSet<String> errorPatternSet = new TreeSet<String>();
		TreeSet<String> alienPatternSet = new TreeSet<String>();
		TreeSet<String> unicodePatternSet = new TreeSet<String>();
		TreeSet<String> corpusPatternSet = new TreeSet<String>();
		while (rs.next()) {
			String pattern = rs.getString("pattern");
			int projectID = rs.getInt("uniqueSourceID");
			try {

				// the important thing to know about patternEscapedPair is that
				// it compares
				// and hashes to others using ONLY the unescaped version
				PatternEscapedPair patternEscapedPair = new PatternEscapedPair(pattern);
				if (patternEscapedPair.getPattern().equals("")) {
					System.out.println("found empty: " + pattern);
				} else {
					TreeSet<Integer> projectIDs = patternProjectMM.get(patternEscapedPair);
					if (projectIDs == null) {
						projectIDs = new TreeSet<Integer>();
					}
					projectIDs.add(projectID);
					patternProjectMM.put(patternEscapedPair, projectIDs);
				}
			} catch (QuoteRuleException e) {
				// System.out.println("problem unquoting pattern: " + pattern);
				errorPatternSet.add(pattern);
			}
		}
		// allPatterns[0] = patternProjectMM.size();

		rs.close();
		stmt.close();
		c.close();

		// sort so that we always get the same order (sets do not guarantee an
		// ordering)
		LinkedList<SortableEntry> entryList = new LinkedList<SortableEntry>();
		for (Entry<PatternEscapedPair, TreeSet<Integer>> entry : patternProjectMM.entrySet()) {
			entryList.add(new SortableEntry(entry.getKey(), entry.getValue()));
		}
		Collections.sort(entryList);
		TreeSet<RegexProjectSet> corpus = new TreeSet<RegexProjectSet>();
		for (SortableEntry entry : entryList) {
			String pattern = entry.getKey().getPattern();
			try {
				RegexProjectSet r = new RegexProjectSet(pattern, entry.getValue());
				corpusPatternSet.add(pattern);
				if (!corpus.add(r)) {
					throw new RuntimeException("Failure to add pattern " +
						pattern + " - every RegexProjectSet must be unique!!!");
				}
			} catch (AlienFeatureException e) {
				String alienMessage = e.getMessage();
				if (alienMessage != null && !alienMessage.equals("")) {
					String token = e.getTokenName();
					if ("<invalid>".equals(token) &&
						(pattern.startsWith("u") || pattern.contains("(?u"))) {
						unicodePatternSet.add(pattern);
					} else {
						alienPatternSet.add(pattern);
					}
				}
				// System.out.println(e.getMessage());
			} catch (IllegalArgumentException e) {
				System.out.println("initializeCorpus: Cannot parse " + pattern +
					" because: " + e.toString());
				errorPatternSet.add(pattern);
				// e.printStackTrace();
			} catch (QuoteRuleException e) {
				errorPatternSet.add(pattern);
			} catch (PythonParsingException e) {
				errorPatternSet.add(pattern);
			}
		}
		return corpus;
	}

	public static TreeSet<RegexProjectSet> reloadCorpus() throws IOException,
			IllegalArgumentException, QuoteRuleException,
			PythonParsingException {
		TreeSet<RegexProjectSet> corpus = new TreeSet<RegexProjectSet>();
		List<String> lines = IOUtil.getLines(C.dataPath + C.CORPUS +
			"serializedCorpus.txt");
		for (String line : lines) {
			String[] parts = line.split("\t");
			String[] IDs = parts[1].split(",");
			TreeSet<Integer> IDSet = new TreeSet<Integer>();
			for (String id : IDs) {
				IDSet.add(Integer.parseInt(id));
			}
			corpus.add(new RegexProjectSet(parts[0], IDSet));
		}
		return corpus;
	}

	public static HashMap<Integer, TreeSet<RegexProjectSet>> reloadProjectPatternMM(TreeSet<RegexProjectSet> corpus)
			throws IOException, IllegalArgumentException, QuoteRuleException,
			PythonParsingException {
		HashMap<Integer, TreeSet<RegexProjectSet>> reloadedProjectPatternMM = new HashMap<Integer, TreeSet<RegexProjectSet>>();
		HashMap<String, Integer> patternIndexMap = BehavioralCategories.getPatternIndexMap();
		HashMap<Integer, RegexProjectSet> lookup = BehavioralCategories.getLookup(BehavioralCategories.filtered_corpus_path, corpus, patternIndexMap);

		File dumpWithIndices = new File(BehavioralCategories.homePath, "projectIDPatternIDMultiMap.txt");
		String serializedProjectPatternMM = FileUtils.readFileToString(dumpWithIndices, "UTF-8");
		Pattern finder = Pattern.compile("(\\d+)\\t(.*)");
		Matcher pairMatcher = finder.matcher(serializedProjectPatternMM);
		while (pairMatcher.find()) {
			String projectID = pairMatcher.group(1);
			String patternIDList = pairMatcher.group(2);
			List<String> patternIDs = Arrays.asList(patternIDList.split(","));
			TreeSet<RegexProjectSet> regexesInAProject = new TreeSet<RegexProjectSet>();
			for (String IDString : patternIDs) {
				Integer ID = Integer.parseInt(IDString);
				regexesInAProject.add(lookup.get(ID));
			}
			reloadedProjectPatternMM.put(Integer.parseInt(projectID), regexesInAProject);

		}
		return reloadedProjectPatternMM;
	}

	public static HashMap<Integer, TreeSet<RegexProjectSet>> initializeProjectPatternMM(
			String connectionString,HashMap<String, Integer> patternIndexMap) throws IOException,
			IllegalArgumentException, QuoteRuleException,
			PythonParsingException, SQLException, ClassNotFoundException {
		HashMap<Integer, TreeSet<RegexProjectSet>> initialProjectPatternMM = new HashMap<Integer, TreeSet<RegexProjectSet>>();

		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();


		TreeSet<RegexProjectSet> corpus = CorpusUtil.reloadCorpus();
		HashMap<Integer, RegexProjectSet> lookup = BehavioralCategories.getLookup(BehavioralCategories.filtered_corpus_path, corpus, patternIndexMap);

		String query = "select pattern, uniqueSourceID from RegexCitationMerged where (flags=0 or flags like 'arg%' or flags=128 or flags='re.DEBUG') and pattern!='arg1';";

		// these are all the distinct patterns with weight
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			int projectID = rs.getInt("uniqueSourceID");
			String pattern = rs.getString("pattern");
			Integer patternID = patternIndexMap.get(pattern);
			if (patternID == null) {
				continue;
			} else {
				RegexProjectSet regex = lookup.get(patternID);
				TreeSet<RegexProjectSet> regexesInAPattern = initialProjectPatternMM.get(projectID);
				if (regexesInAPattern == null) {
					regexesInAPattern = new TreeSet<RegexProjectSet>();
				}
				regexesInAPattern.add(regex);
				initialProjectPatternMM.put(projectID, regexesInAPattern);
			}
		}

		rs.close();
		stmt.close();
		c.close();
		return initialProjectPatternMM;
	}

//	public static void main(String[] args) throws ClassNotFoundException,
//			IllegalArgumentException, SQLException, QuoteRuleException,
//			PythonParsingException, IOException {
//		HashMap<String, Integer> patternIndexMap = BehavioralCategories.getPatternIndexMap();
//		TreeSet<RegexProjectSet> corpus = reloadCorpus();
//
//		// now building a reloadable file that maps projectIDs to their
//		// patterns' javaIDs
//		StringBuilder sb = new StringBuilder();
//		File dumpWithIndices = new File(BehavioralCategories.homePath, "projectIDPatternIDMultiMap.txt");
//		HashMap<Integer, TreeSet<RegexProjectSet>> initial = initializeProjectPatternMM(connectionString,patternIndexMap);
//
//		StringBuilder contents = new StringBuilder();
//		for (Entry<Integer, TreeSet<RegexProjectSet>> e : initial.entrySet()) {
//			contents.append(e.getKey().toString() + "\t" +
//				getCSV(e.getValue(),patternIndexMap) + "\n");
//		}
//		IOUtil.createAndWrite(dumpWithIndices, contents.toString());
//		HashMap<Integer, TreeSet<RegexProjectSet>> reloaded = reloadProjectPatternMM(corpus);
//		System.out.println(reloaded.equals(initial));
//	}

	private static String getCSV(TreeSet<RegexProjectSet> value,HashMap<String, Integer> patternIndexMap) {
		StringBuilder sb = new StringBuilder();
		for(RegexProjectSet y : value){
			sb.append(patternIndexMap.get(y.getContent()));
			sb.append(",");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		return sb.toString();
	}

	// public static void main(String[] args) throws ClassNotFoundException,
	// IllegalArgumentException, SQLException, QuoteRuleException,
	// PythonParsingException, IOException {
	//
	// // now we are dumping the corpus with the java indices, for behavioral
	// clustering
	// StringBuilder sb = new StringBuilder();
	// File dumpWithIndices = new File(BehavioralCategories.behavioralPath,
	// "indexedOrderedCorpusDump.txt");
	// HashMap<String, Integer> patternIndexMap =
	// BehavioralCategories.getPatternIndexMap();
	// TreeSet<RegexProjectSet> loadedC = reloadCorpus();
	// for(RegexProjectSet rps : loadedC){
	// String original = rps.getContent();
	// Integer index = patternIndexMap.get(original);
	// sb.append(index+"\t"+rps.getRankableValue()+"\t"+original+"\n");
	// }
	//
	// IOUtil.createAndWrite(dumpWithIndices, sb.toString());
	// }

	// public static void main(String[] args) throws ClassNotFoundException,
	// IllegalArgumentException, SQLException, QuoteRuleException,
	// PythonParsingException, IOException{
	// //here we serialize the corpus, to avoid lag in development waiting for
	// corpus to build again
	// File corpusFile = new File(IOUtil.dataPath +
	// IOUtil.CORPUS,"serializedCorpus.txt");
	// File loadedFile = new File(IOUtil.dataPath +
	// IOUtil.CORPUS,"loadedCorpus.txt");
	// TreeSet<RegexProjectSet> corpus =
	// initializeCorpus(Step1_CreateCandidateFiles.connectionString);
	// StringBuilder contents = new StringBuilder();
	// for(RegexProjectSet rps :corpus){
	// contents.append(rps.getContent()+"\t"+rps.getProjectsCSV()+"\n");
	// }
	// IOUtil.createAndWrite(corpusFile,contents.toString());
	// TreeSet<RegexProjectSet> loadedC = reloadCorpus();
	// StringBuilder contents2 = new StringBuilder();
	// for(RegexProjectSet rps :loadedC){
	// contents2.append(rps.getContent()+"\t"+rps.getProjectsCSV()+"\n");
	// }
	// IOUtil.createAndWrite(loadedFile,contents2.toString());
	// System.out.println(corpus.equals(loadedC));
	// }

}
