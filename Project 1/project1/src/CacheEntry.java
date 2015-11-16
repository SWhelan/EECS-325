/**
 * 
 * A container object to store ip address and TTL in cache.
 * 
 * @author Sarah Whelan
 *
 */
public class CacheEntry {
	private String ip;
	private float timeCreated;
	
	// Variables for converting TTL
	private static final double SEC_TO_NANO = 1 * 1000 * 1000 * 1000d;
	private static final int TTL_SEC = 30;
	
	public CacheEntry(String ip){
		this.ip = ip;
		timeCreated = System.nanoTime();
	}
	
	public String getIp() {
		return ip;
	}
	
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public float getTimeCreated() {
		return timeCreated;
	}
	
	public void setTimeCreated(float timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	/**
	 * Determines if this entry is not expired
	 * @return true for not expired false for expired
	 */
	public boolean lessThan30Sec(){
		return System.nanoTime() - timeCreated < TTL_SEC * SEC_TO_NANO;
	}
}
