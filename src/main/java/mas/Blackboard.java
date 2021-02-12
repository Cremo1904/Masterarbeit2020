package mas;

import java.util.HashMap;
import java.util.Map;

/**
 * Instance for centralised storage and access to information
 * @author Christian Hinrichs
 * @author Jörg Bremer
 *
 * modified by
 * @author Lukas Cremers
 */
public class Blackboard {
	private static Map<String, Object> memory=new HashMap<String, Object>();
	private static int delay = 0;
	
	public static void clear() {
		memory.clear();
	}
	
	public static Object get(String key) {
		return memory.get(key);
	}
	
	public static void put(final String key, final Object value) {
		if (delay==0) memory.put(key, value);
		else {
			new Thread() {
				public void run() {
					try {
						Thread.sleep(delay);
					} catch (Exception e) {
						e.printStackTrace();
					}
					memory.put(key, value);
				}
			}.start();
		}
	}

	public static void simMessageDelay(int del) {
		delay=del;		
	}

	
}
