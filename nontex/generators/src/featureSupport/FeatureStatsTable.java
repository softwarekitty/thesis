package featureSupport;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import build_corpus.RegexProjectSet;
import metric.FeatureDictionary;
import c.IOUtil;

public class FeatureStatsTable {

	public static String featureStats(TreeSet<RegexProjectSet> corpus,
			String connectionString) throws ClassNotFoundException,
			SQLException {

		FeatureDictionary fd = new FeatureDictionary();
		int[] totalNProjects = { 0 };
		int[] nProjectsPerFeature = getProjectsPerFeature(corpus, fd, totalNProjects, connectionString);

		int[] totalNFiles = { 0 };
		int[] filesWithFeature = getFilesPerFeature(corpus, fd, totalNFiles, connectionString);

		int nFeatures = fd.getSize();
		int nPatterns = corpus.size();
		int literalTokens = 0;
		int literalPresent = 0;
		int[] presentCounter = new int[nFeatures];
		int[] tokensCounter = new int[nFeatures];
		int[] max = new int[nFeatures];
		double totalWeight = 0;
		int[] featureWeight = new int[nFeatures];

		for (RegexProjectSet wrr : corpus) {
			int[] featureCount = wrr.getFeatures().getFeatureCountArray();
			for (int i = 0; i < nFeatures; i++) {
				int count = featureCount[i];
				if (count > 0) {
					featureWeight[i] += wrr.getRankableValue();

					if (max[i] < count) {
						max[i] = count;
						if (i != FeatureDictionary.I_META_LITERAL &&
							count > 100) {
							System.out.println("100? INSPECT THIS (" +
								fd.getCode(i) + "): " + wrr.getContent());
							// IOUtil.waitNsecsOrContinue(20);
						}
					}
					tokensCounter[i] += count;
					presentCounter[i]++;
					if (i == FeatureDictionary.I_META_LITERAL) {
						literalTokens += count;
						literalPresent++;
					}
				}
			}
			totalWeight += wrr.getRankableValue();
		}
		DecimalFormat df = new DecimalFormat("00.0");

		int totalTokens = 0;
		for (int tokens : tokensCounter) {
			totalTokens += tokens;
		}
		int adjustedTokens = totalTokens - literalTokens;

		StringBuilder sb = new StringBuilder();
		String between = " & ";
		sb.append("\\begin{table*}\n\\begin{center}\n\\begin{footnotesize}\n"
			+ "\\caption{Frequency of feature appearance in Projects, Files and Patterns, with number of tokens observed and the maximum number of tokens observed in a single pattern.}\n"
			+ "\\label{table:featureStatsOnly}\n"
			+ "\\begin{tabular}\n{lllcccc  cc}\n");
		sb.append("rank & code & example & \\% projects & nProjects & nFiles & nPatterns & nTokens & maxTokens. \\\\ \n\\toprule[0.16em]\n");
		TreeSet<FeatureDetail> sortedFeatures = new TreeSet<FeatureDetail>();
		for (int i = 0; i < nFeatures; i++) {
			if (i == FeatureDictionary.I_META_LITERAL || presentCounter[i] == 0) {
				continue;
			}
			// int featureID, int nFiles, int nPresent, int nProjects, int max,
			// int nTokens)
			sortedFeatures.add(new FeatureDetail(i, filesWithFeature[i], presentCounter[i], nProjectsPerFeature[i], max[i], tokensCounter[i]));
		}

		int rankIndex = 1;
		for (FeatureDetail featureDetail : sortedFeatures) {
			int ID = featureDetail.getID();
			String featureCode = fd.getCode(ID);
			String description = fd.getDescription(ID);
			String verbatimBlock = fd.getVerbatim(ID);

			String nPresent = commafy(presentCounter[ID]);
			String percentPresent = percentify(presentCounter[ID], nPatterns);

			String nTokens = commafy(tokensCounter[ID]);
			String percentTokens = percentify(tokensCounter[ID], adjustedTokens);

			String maxOccurances = commafy(max[ID]);

			String weightInt = commafy(featureDetail.getRankableValue());
			String weightPercent = df.format(100 * (featureDetail.getRankableValue() / totalWeight));

			// System.out.println("filesWithFeature[ID]: "+filesWithFeature[ID]+" totalNFiles[0]: "+totalNFiles[0]+" nProjectsPerFeature[ID]: "+nProjectsPerFeature[ID]+" totalNProjects[0]: "+totalNProjects[0]);

			String nFiles = commafy(filesWithFeature[ID]);
			String percentFiles = percentify(filesWithFeature[ID], totalNFiles[0]);

			String nProjects = commafy(nProjectsPerFeature[ID]);
			String percentProjects = percentify(nProjectsPerFeature[ID], totalNProjects[0]);

			sb.append("" + rankIndex);
			sb.append(between);
			sb.append(featureCode);
			// sb.append(between);
			// sb.append(description);
			sb.append(between);
			sb.append(verbatimBlock);
			sb.append(between);
			sb.append(percentProjects);
			sb.append(between);			
			sb.append(nProjects);
			sb.append(between);
			sb.append(nFiles);
			sb.append(between);
//			sb.append(percentPresent);
//			sb.append(between);
			sb.append(nPresent);
			sb.append(between);
			sb.append(nTokens);
			sb.append(between);
			// sb.append(percentTokens);
			// sb.append(between);

			sb.append(maxOccurances);

			// sb.append(weightInt);
			// sb.append(between);
			// sb.append(weightPercent);
			// sb.append(between);

			// sb.append(percentFiles);
			// sb.append(between);
			// sb.append(between);
			// sb.append(percentProjects);

			if (rankIndex == 8 || rankIndex == 27) {
				sb.append(" \\\\ \n\\midrule[0.12em]\n");
			} else if (rankIndex < sortedFeatures.size()) {
				sb.append(" \\\\ \n\\midrule\n");
			}
			rankIndex++;
		}
		sb.append(" \\\\ \n\\bottomrule[0.13em]\n\\end{tabular}\n"
			+ "\\end{footnotesize}\n\\end{center}\n\\end{table*}\n");
		return sb.toString();
	}

	private static int[] getFilesPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNFiles, String connectionString)
			throws ClassNotFoundException, SQLException {
		String filePatternQuery = "select uniqueSourceID || filePath as key, pattern from RegexCitationMerged;";
		return getElementsPerFeature(corpus, fd, totalNFiles, filePatternQuery, connectionString);
	}

	private static int[] getProjectsPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNProjects, String connectionString)
			throws ClassNotFoundException, SQLException {
		String projectPatternQuery = "select uniqueSourceID || 'X' as key, pattern from RegexCitationMerged;";
		return getElementsPerFeature(corpus, fd, totalNProjects, projectPatternQuery, connectionString);
	}

	private static int[] getElementsPerFeature(TreeSet<RegexProjectSet> corpus,
			FeatureDictionary fd, int[] totalNElements, String elementQuery,
			String connectionString) throws ClassNotFoundException,
			SQLException {
		HashMap<String, ArrayList<Integer>> corpusMap = getCorpusMap(corpus);
		HashMap<Integer, ArrayList<String>> projectIndexListMap = getIndexListMap(elementQuery, connectionString);
		int nIndices = projectIndexListMap.size();
		totalNElements[0] = nIndices;
		int nFeatures = fd.getSize();
		int[][] matrix = new int[nIndices][nFeatures];
		Collection<Integer> indices = projectIndexListMap.keySet();

		// does not matter if these are iterated in order, but the keys MUST
		// be sequential so that fileMatrix is full, all indices are in bounds
		for (Integer index : indices) {
			ArrayList<String> patternList = projectIndexListMap.get(index);
			// System.out.println("index: "+index +
			// " patternList: "+patternList);
			for (String pattern : patternList) {

				// note how important the iteration order in this list is!
				ArrayList<Integer> featureCount = corpusMap.get(pattern);

				// some patterns are in the database, but not in the corpus -
				// we have to ignore these because they were excluded because
				// they have features that PCRE parser cannot parse, so we have
				// no featureCount for some feature they use (this is rare)
				if (featureCount != null) {
					for (int featureIndex = 0; featureIndex < nFeatures; featureIndex++) {
						// System.out.println("featureIndex: "+featureIndex +
						// " featureCount: "+featureCount);

						int fCount = featureCount.get(featureIndex);
						matrix[index][featureIndex] += fCount;
					}
				} else {
					// System.out.println("pattern not in corpus: " + pattern);
				}

			}
		}
		int[] elementsPerFeature = new int[nFeatures];
		for (int i = 0; i < nIndices; i++) {
			for (int j = 0; j < nFeatures; j++) {
				if (matrix[i][j] != 0) {
					elementsPerFeature[j]++;
				}
			}
		}
		return elementsPerFeature;
	}

	private static HashMap<String, ArrayList<Integer>> getCorpusMap(
			TreeSet<RegexProjectSet> corpus) {
		HashMap<String, ArrayList<Integer>> corpusMap = new HashMap<String, ArrayList<Integer>>();
		for (RegexProjectSet wrr : corpus) {
			int[] fc = wrr.getFeatures().getFeatureCountArray();
			ArrayList<Integer> featureCounts = new ArrayList<Integer>(fc.length);
			for (int fCount : fc) {
				featureCounts.add(fCount);
			}
			corpusMap.put(wrr.getContent(), featureCounts);
		}
		return corpusMap;
	}

	// important note: Integer keys must be sequential from zero - one for each
	// element
	private static HashMap<Integer, ArrayList<String>> getIndexListMap(
			String query, String connectionString)
			throws ClassNotFoundException, SQLException {
		HashMap<String, ArrayList<String>> keyListMap = new HashMap<String, ArrayList<String>>();

		// this is not a necessity, but guarantees identical results across runs
		TreeSet<String> sortedKeys = new TreeSet<String>();

		// prepare sql
		Connection c = null;
		Statement stmt = null;
		Class.forName("org.sqlite.JDBC");
		c = DriverManager.getConnection(connectionString);
		c.setAutoCommit(false);
		stmt = c.createStatement();

		// the query needs to return a relation,
		// the first string is a key, second the pattern
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			String key = rs.getString("key");
			String pattern = rs.getString("pattern");
			sortedKeys.add(key);
			ArrayList<String> patternList = keyListMap.get(key);
			if (patternList == null) {
				patternList = new ArrayList<String>();
			}
			patternList.add(pattern);
			keyListMap.put(key, patternList);
		}

		// wind down sql
		rs.close();
		stmt.close();
		c.close();

		HashMap<Integer, ArrayList<String>> indexListMap = new HashMap<Integer, ArrayList<String>>();
		int sequentialIndex = 0;
		for (String elementKey : sortedKeys) {
			ArrayList<String> finalPatternList = keyListMap.get(elementKey);
			indexListMap.put(sequentialIndex++, finalPatternList);
		}
		return indexListMap;
	}

	public static String percentify(double d, double sum) {
		DecimalFormat df = new DecimalFormat("##0.#");
		return df.format(100 * round(d / sum, 3));
	}

	public static String commafy(int n) {
		return NumberFormat.getNumberInstance(Locale.US).format(n);
	}

	public static int intify(String s) {
		int dotIndex = s.indexOf('.');
		String intString = s;
		if (dotIndex > -1) {
			intString = s.substring(0, dotIndex);
		}
		return Integer.parseInt(intString.replaceAll(",", ""));
	}

	private static String quote(String s) {
		return "\"" + s + "\"";
	}

	private static String[] stringifyNumbers(int[] data) {
		String[] dataS = new String[data.length];
		for (int i = 0; i < data.length; i++) {
			dataS[i] = "" + data[i];
		}
		return dataS;
	}

	private static String commaSeparate(String[] items) {
		if (items == null || items.length == 0) {
			return "";
		} else if (items.length == 1) {
			return items[0];
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(items[0]);
			for (int i = 1; i < items.length; i++) {
				sb.append(",");
				sb.append(items[i]);
			}
			return sb.toString();
		}
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
