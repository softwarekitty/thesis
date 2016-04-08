package appendix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CategoryOracles {

	public static String[] categoryNames = { "Web And XML Related",
			"Path And File Related", "Code Related", "Labels",
			"Non-Free Ordinary Strings", "Bracket Capturing", "Messages",
			"Identifiers", "Tuples And Punctuation", "Numbers And Dates",
			"Space", "Vanilla Characters", "Uncategorized" };

	// "Web And XML Related" https, &#0xff;, etc, doctype
	public static ArrayList<Integer> list0 = new ArrayList<Integer>(Arrays.asList(447, 4050, 4270, 9767));

	// "Path And File Related" /usr/bin/.*\.py.
	public static ArrayList<Integer> list1 = new ArrayList<Integer>(Arrays.asList(447, 4050));

	// "Code Or Shell Related" include, a=(b) (keywords divert here over
	// message)
	public static ArrayList<Integer> list2 = new ArrayList<Integer>(Arrays.asList(11588, 11676, 7918, 6471, 3505, 2883, 9105, 8789, 12301, 322, 3138, 1392, 1347, 10124, 11766, 2979));// fake
																																														// 4

	// "Labels"
	public static ArrayList<Integer> list3 = new ArrayList<Integer>(Arrays.asList(12563, 12556, 12558, 12562, 12565, 12555, 12564, 13299, 13135, 13507, 12291, 12554, 13241, 13242, 13258, 12557, 12144, 13173, 13061, 13303, 13250, 13136, 13137, 12807, 13251, 5653));

	// "Non-Free Ordinary Strings" a+, [aeiou]
	public static ArrayList<Integer> list4 = new ArrayList<Integer>(Arrays.asList(6039, 10778, 13514, 8692));

	// "Bracket Capturing" (not lone bracket delimieters)
	public static ArrayList<Integer> list5 = new ArrayList<Integer>(Arrays.asList(3948, 4015, 8771, 8971, 8869, 11778, 13553));

	// "Messages"
	public static ArrayList<Integer> list6 = new ArrayList<Integer>(Arrays.asList(7777, 2626, 13440));

	// "Identifiers" - may have a delmiter, but focus is on a semi-free string
	// following rules
	public static ArrayList<Integer> list7 = new ArrayList<Integer>(Arrays.asList(2572, 6579, 6575, 450, 11018, 13346));

	// "Delimiters" - for Tuples And Punctuation, even \n, rest of content is
	// free, often captured
	public static ArrayList<Integer> list8 = new ArrayList<Integer>(Arrays.asList(1573, 9145, 6204, 6879, 8070, 9090, 6381, 1580, 6462, 3286, 9425, 2508, 7935, 7947, 3832, 7759, 1908, 2987, 1019, 4550, 119, 7940, 12015, 497, 6304, 178, 3916, 3903, 412, 60, 4520, 7414, 3067, 8341, 3037, 6517, 3918, 49, 8731, 447, 4050, 4270, 9767));

	// "Numbers And Dates"
	public static ArrayList<Integer> list9 = new ArrayList<Integer>(Arrays.asList(418, 7665, 11055, 710));

	// "Space"
	public static ArrayList<Integer> list10 = new ArrayList<Integer>(Arrays.asList(8233, 7950, 9647, 6287, 2560, 11146, 45, 1585, 11121, 7958));

	// "Vanilla Characters"
	public static ArrayList<Integer> list11 = new ArrayList<Integer>(Arrays.asList(6302, 7250, 6252, 6369, 11491, 7234, 7209, 1867, 10833, 8355, 150, 7256));
	// public static LinkedList<List<Integer>> categoryOracle = new
	// LinkedList<List<Integer>>(Arrays.asList(list3));

	public static LinkedList<List<Integer>> categoryOracle = new LinkedList<List<Integer>>(Arrays.asList(list0, list1, list2, list3, list4, list5, list6, list7, list8, list9, list10, list11));

}
