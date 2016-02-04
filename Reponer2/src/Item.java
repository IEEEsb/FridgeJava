
public class Item {
	
	int id;
	String name;
	String barcode;
	double buy_price;
	double sell_price;
	int init_stock;
	int stock;
	int category;

	
	public Item(String name, String barcode, double buy_price, double sell_price, int stock, int category){
		this.name=name;
		this.barcode=barcode;
		this.buy_price=buy_price;
		this.sell_price=sell_price;
		this.init_stock=stock;
		this.stock=stock;
		this.category=category;
	}
	
	public Item(int id, String name, double buy_price, double sell_price, int stock, int category){
		this.id=id;
		this.name=name;
		this.buy_price=buy_price;
		this.sell_price=sell_price;
		this.stock=stock;
		this.category=category;
	}
	


}
