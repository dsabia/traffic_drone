package poc;

import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import com.github.davidmoten.rtree.geometry.Point;

import rx.Observable;

/* Not an unit test but only a PoC of the library used for the R-tree.
 */
public class PoCRTree {

	public static void main(String[] args) {
		RTree<String, Point> tree = RTree.maxChildren(5).create();
		tree = tree.add("DAVE", Geometries.point(.010, .020))
		           .add("FRED", Geometries.point(.012, .025))
		           .add("MARY", Geometries.point(.097, .125));
		 
		Observable<Entry<String, Point>> entries =
		    tree.search(Geometries.rectangle(.008, .015, .030, .035));
		
		entries.toBlocking().toIterable().forEach( x -> {
			System.out.println(x);
		});
	}
}
