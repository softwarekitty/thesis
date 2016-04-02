package appendix;

import java.util.TreeSet;

public class Category extends TreeSet<Cluster>{
	private static final long serialVersionUID = -217224086597240194L;
	Cluster combinedClusters;
	
	public Category(){
		super();
		combinedClusters = new Cluster();
	}
	
	@Override
	public boolean add(Cluster c){
		boolean added = super.add(c);
		combinedClusters.addAll(c);
		return added;
	}
	
	public Cluster getCombinedClusters(){
		return combinedClusters;
	}
}
