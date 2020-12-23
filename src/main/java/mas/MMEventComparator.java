package mas;

import java.util.Comparator;

public class MMEventComparator implements Comparator<MMEvent> {

	@Override
	public int compare(MMEvent a, MMEvent b) {
		if (a==null) return -1;
		if (b==null) return 1;
		long t1=((MMEvent)a).getTime();
		long t2=((MMEvent)b).getTime();
		if (t1<t2) return -1;
		if (t1>t2) return 1;
		return 0;
	}

}
