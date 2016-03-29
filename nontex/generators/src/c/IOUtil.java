package c;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class IOUtil {

//	public static String paperPath = "/Users/carlchapman/git/regex_readability_study/paper/";
	public static String dataPath = "/Users/carlchapman/git/regex_readability_study/data/";
	public static String ORIGINAL = "original/";
	public static String COMPOSITION = "composition/";
	public static String NODES = "nodes/";
	public static String CLEAN_NODES = "manuallyReviewedNodes/";
	public static String IN = "Rinput/";
	public static String OUT = "Routput/";
	public static String TMP = "temp/";
	public static String SIMPLE = "simplified/";
	public static String P2_PATH = "P2/";
	public static String P3_PATH = "P3/";
	public static String T_PATH = "T/";
	public static final String R_SCRIPTNAME = "scRipt.r";
	public static final String M_PATH = "M/";
	public static final String CORPUS = "corpus/";
	public static final String TABLE = "table/";
	public static final String G_PATH = "G/";
	public static final String E_PATH = "E/";
	
	

	public static void createAndWrite(File file, String content) {
		FileWriter fw;
		try {
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> getLines(String filePathString)
			throws IOException {
		Path filePath = new File(filePathString).toPath();
		Charset charset = Charset.defaultCharset();
		return Files.readAllLines(filePath, charset);
	}


//	public static HashMap<String, Pattern> getCodeMap(List<String> lines,
//			String delim) {
//		HashMap<String,Pattern> codeMap = new HashMap<String,Pattern>();
//
//		for(String line : lines){
//			//System.out.println(line);
//			String[] tokens = line.split(delim);
//			Pattern p = Pattern.compile(tokens[1].substring(0,tokens[1].length()-1));
//			Pattern y = codeMap.put(tokens[0], p);
//			if (y!=null){
//				System.err.println("duplicate key");
//			}
//		}
//		return codeMap;
//	}
//
//	public static HashMap<String, List<String>> getAnswerMap(
//			List<String> lines, String delim) {
//		HashMap<String,List<String>> answerMap = new HashMap<String,List<String>>();
//
//		for(String line : lines){
//			String[] tokens = line.split(delim);
//			String tempCode = "";
//			boolean isCode = true;
//			for(String token : tokens){
//				if(isCode){
//					tempCode = token;
//				}else{
//					List<String> answers = answerMap.get(tempCode);
//					if(answers==null){
//						answers = new LinkedList<String>();
//					}
//					answers.add(token);
//					answerMap.put(tempCode, answers);
//				}
//				isCode=!isCode;
//			}
//		}
//		return answerMap;
//	}

}
