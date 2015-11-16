import java.util.HashMap;

/**
 * 
 * A simple DNS cache
 * 
 * @author Sarah Whelan
 *
 */
public class Cache {
	// A map to store cache entries
	private static HashMap<String, CacheEntry> cache = new HashMap<String, CacheEntry>();

	/**
	 * 
	 * Adds an entry to the cache if there isn't already an entry for that hostname 
	 * 
	 * @param host the key is the hostname
	 * @param ip the value for the map
	 */
	public static void add(String host, String ip){
		if(!cache.containsKey(host)){
			cache.put(host, new CacheEntry(ip));
		}
	}
	
	/**
	 * 
	 * Returns the caches ip address if it exists otherwise returns the hostname
	 * 
	 * @param host the hostname to check
	 * @return cached ip or not found hostname
	 */
	public static String getHostNameOrIp(String host) {
		CacheEntry entry = cache.get(host);
		if(entry != null && entry.lessThan30Sec()){
			return entry.getIp();
		} else {
			if(entry != null){
				// Also removes from the cache if the record is more than 30 seconds old
				cache.remove(host);
			}
			return host;
		}
	}
	
}
