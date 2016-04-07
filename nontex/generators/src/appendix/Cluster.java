package appendix;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import build_corpus.RegexProjectSet;

@SuppressWarnings("serial")
public class Cluster extends TreeSet<RegexProjectSet> implements Comparable<Cluster>{
	private static int nextClusterID = 0;
	public final int thisClusterID = nextClusterID++;
	private TreeSet<Integer> allProjectIDs;
	private RegexProjectSet shortest = null;

	public Cluster() {
		allProjectIDs = new TreeSet<Integer>();
	}
	
	public String getPatternDump(){
		String categoryHeader = "\\begin{multicols}{1}\n\\begin{description}[noitemsep,topsep=0pt]\n";
		String categoryFooter = "\\end{description}\n\\end{multicols}\n\n\n\n";
		StringBuilder sb = new StringBuilder();
		sb.append("total projects:"+this.getNProjects()+"\n");
		sb.append("total patterns:"+this.getNPatterns()+"\n");
		sb.append(categoryHeader);
		for(RegexProjectSet member : this){
			sb.append(itemMaker(member,"["+member.getRankableValue()+"] "));
			sb.append("\n");
		}
		sb.append(categoryFooter);
		return sb.toString();
	}
	
	public String getItemLineLatex(HashMap<String, Integer> patternIndexMap){
		StringBuilder sb = new StringBuilder();
		
//		RegexProjectSet shorty = getHeaviest();
		RegexProjectSet shorty = getShorty();

		Integer index = patternIndexMap.get(shorty.getContent());
		sb.append(", "+index+" ");
		sb.append(itemMaker(shorty,getDescription()));
		sb.append("\n");
		return sb.toString();
	}
	
	private String itemMaker(RegexProjectSet regex, String description){
		StringBuilder sb = new StringBuilder();
		sb.append("\\item ");
		sb.append("["+description+"] ");
		sb.append(AppendixHelper.wrap(regex));
		return sb.toString();
	}

	private String getDescription() {
		return allProjectIDs.size() + " \\<"+this.size()+"\\>";
	}

	@Override
	public boolean add(RegexProjectSet x) {
		boolean addSuccess = super.add(x);
		allProjectIDs.addAll(x.getProjectIDSet());
		return addSuccess;
	}

	public TreeSet<Integer> getAllProjectIDs() {
		return allProjectIDs;
	}
	
	public int getNProjects(){
		return allProjectIDs.size();
	}

	public RegexProjectSet getHeaviest() {
		return this.first();
	}
	
	public RegexProjectSet getShorty() {
		if(shortest!=null){
			return shortest;
		}else{
			RegexProjectSet current = null;
			Iterator<RegexProjectSet> it = this.iterator();
			String smallestUnescapedPattern = null;
			if (it.hasNext()) {

				// should always get here - no empty clusters
				current = it.next();
				shortest = current;
				smallestUnescapedPattern = current.getUnescapedPattern();
			}
			while (it.hasNext()) {
				current = it.next();
				String smaller = current.getUnescapedPattern();
				if (smaller.length() < smallestUnescapedPattern.length()) {
					smallestUnescapedPattern = smaller;
					shortest = current;
				}
			}
			return shortest;
		}
	}

	@Override
	public int compareTo(Cluster other) {
		if (other.getClass() != this.getClass()) {
			System.err.println("class mismatch");
			return 1;
		}
		Cluster cOther = (Cluster) other;
		int nProjectsThis = this.getNProjects();
		int nProjectsOther = cOther.getNProjects();
		// higher weight is earlier
		if (nProjectsThis > nProjectsOther) {
			return -1;
		} else if (nProjectsThis < nProjectsOther) {
			return 1;
		} else {
			if(this.size() > cOther.size()){
				return -1;
			}else if(this.size() < cOther.size()){
				return 1;
			}else{
				Iterator<RegexProjectSet> it1 = this.iterator();
				Iterator<RegexProjectSet> it2 = cOther.iterator();
				while(it1.hasNext()){
					RegexProjectSet wrr1 = it1.next();
					RegexProjectSet wrr2 = it2.next();
					int ct = wrr1.compareTo(wrr2);
					if(ct!=0){
						return ct;
					}
				}
				return 0;
			}
		}
	}

	public int getNPatterns() {
		return size();
	}
}
