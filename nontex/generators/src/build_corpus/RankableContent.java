package build_corpus;

public interface RankableContent extends Comparable<RankableContent>{
	
	public int getRankableValue();
	public String getContent();
	public int compareTo(RankableContent other);

}
