package es.ieeesb.nevera.utils;

/**
 * The Class Item.
 */
public class Item {
	
	/** The id. */
	public int id;
	
	/** The name. */
	public String name;
	
	/** The cost. */
	public double cost;
	
	/**
	 * Instantiates a new item.
	 *
	 * @param id the id
	 * @param name the name
	 * @param cost the cost
	 */
	public Item(int id, String name, double cost) {
		this.id = id;
		this.name = name;
		this.cost = cost;
	}
}
