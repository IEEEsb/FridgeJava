import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import es.ieeesb.nevera.utils.PropertiesManager;



public class Main {
	private static Connection connection;
	private static ScriptEngineManager mgr = new ScriptEngineManager();
    private static ScriptEngine engine = mgr.getEngineByName("JavaScript");
	private static boolean cPvpVenta=false;
	private static boolean cPvpCompra=false;
	
	public static void main (String[] args) throws Exception{

//		PreparedStatement ps;
//		ScriptEngineManager mgr = new ScriptEngineManager();
//	    ScriptEngine engine = mgr.getEngineByName("JavaScript");
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Introduzca codigo autorizado");
		String dni = scan.nextLine();
		PropertiesManager.loadProperties();
		Class.forName("com.mysql.jdbc.Connection");
		connection = DriverManager.getConnection("jdbc:mysql://"+
				PropertiesManager.properties.getProperty( "HOST" ),
				PropertiesManager.properties.getProperty( "USER" ),
				PropertiesManager.properties.getProperty( "PASS" ));
		
		ResultSet rs = connection.createStatement().executeQuery("SELECT autorized FROM users WHERE dni = '"+dni+"'");
		if(!rs.next() && rs.getInt(1) == 0){
			System.out.println("Usuario no autorizado");
			scan.close();
			return;
		}
		System.out.println("Autorización correcta\n");
		
		wantDeleteAllStore(connection, scan);
		
		System.out.println("Introduzca producto\n");
		String code=scan.nextLine();
		
		while(!code.equals("end")&&!code.equals("endSession")){
			rs = connection.createStatement().executeQuery("SELECT id, name, buy_price, sell_price, stock, category FROM inventory WHERE barcode = '"+code+"'");
			
			if (rs.next()){
				Item item = new Item(rs.getInt(1),rs.getString(2),rs.getDouble(3),rs.getDouble(4),rs.getInt(5),rs.getInt(6));		
				wantRebootUds(connection, scan, item);
				getDataRestockItem(connection, scan, rs, code, item);
				showInfo(item);
				System.out.println("¿Es correcta esta informacion? (Y/n)\n");
				if (!scan.nextLine().startsWith("n")){
					restockItem(item, connection);
				}
				
			}else{
				Item item = getDataAddNewItem(connection, scan, rs, code);
				showInfo(item);
				System.out.println("¿Esta seguro de que desea añadir este producto? (Y/n)\n");
				if (!scan.nextLine().startsWith("n")){
					addNewItem(item,connection);
				}
			}
			System.out.println("Introduzca producto\n");
			code=scan.nextLine();
		}
		scan.close();
		System.out.println("todo ha ido bien");
	}


	private static void restockItem(Item item, Connection connection) throws SQLException {
		
        PreparedStatement ps = connection.prepareStatement("UPDATE inventory SET buy_price=?, sell_price=?, init_stock=?, stock=? WHERE id = ?");
        ps.setDouble(1, Double.valueOf(item.buy_price));
        ps.setDouble(2, Double.valueOf(item.sell_price).doubleValue());
        ps.setInt(3, item.stock);
        ps.setInt(4, item.stock);
        ps.setInt(5, item.id);
        ps.executeUpdate();
        ps.close();
        
        ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,7,?,NOW())" );
        ps.setInt(1, item.id);
		ps.setDouble(2, item.stock);
		ps.execute();
		ps.close();
		
		if (cPvpCompra){
			ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,6,?,NOW())" );
			ps.setInt(1, item.id);
			ps.setDouble(2, item.buy_price);
			ps.execute();
			ps.close();
		}
		
		if (cPvpVenta){
			ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,5,?,NOW())" );
			ps.setInt(1, item.id);
			ps.setDouble(2, item.sell_price);
			ps.execute();
			ps.close();
		}
	}


	private static void getDataRestockItem(Connection connection,
			Scanner scan, ResultSet rs, String code, Item item) throws ScriptException {
		System.out.println("¿Cuantas unidades vas a añadir?\n");
		item.stock += Integer.valueOf(scan.nextLine());
		
		System.out.println("Precio de venta actual: "+item.sell_price+" €\n¿Desea conservarlo?(Y/n)\n");
		if (scan.nextLine().startsWith("n")){
			System.out.println("¿Precio de venta?\n");
			double pvp_venta =(Double)engine.eval(scan.nextLine());
			item.sell_price=pvp_venta;
			cPvpVenta=true;
		}
		
		System.out.println("Precio de compra actual: "+item.buy_price+" €\n¿Desea conservarlo? (Y/n)\n");
		if (scan.nextLine().startsWith("n")){
			System.out.println("¿Precio de compra?\n");
			double pvp_compra = (Double)engine.eval(scan.nextLine());
			item.buy_price=pvp_compra;
			cPvpCompra=true;
		}

	}


	private static void wantRebootUds(Connection connection, Scanner scan, Item item) throws SQLException {
		System.out.println("¿Desea reiniciar las unidades del producto? (N,y)\n");
		if(scan.nextLine().startsWith("y")){
			PreparedStatement ps = connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,7,?,NOW())" );
			ps.setInt(1, item.id);
			ps.setDouble(2, 0);
			ps.execute();
			ps.close();
			item.stock=0;
			System.out.println("Las unidades de: "+item.name+"han sido reiniciadas.\n");
		}
		
		
	}


	private static void addNewItem(Item item, Connection connection) throws SQLException {
		PreparedStatement ps =connection.prepareStatement("INSERT INTO inventory (name, barcode, buy_price, sell_price, init_stock, stock, category) VALUES (?,?,?,?,?,?,?)");
		ps.setString(1, item.name);
        ps.setString(2, item.barcode);
        ps.setDouble(3, Double.valueOf(item.buy_price).doubleValue());
        ps.setDouble(4, Double.valueOf(item.sell_price).doubleValue());
        ps.setInt(5, Integer.valueOf(item.stock).intValue());
        ps.setInt(6, Integer.valueOf(item.init_stock).intValue());
        ps.setInt(7, Integer.valueOf(item.category).intValue());
        ps.execute();
        ps.close();
        ResultSet rs = connection.createStatement().executeQuery("SELECT id FROM inventory WHERE barcode = '"+item.barcode+"'");
        rs.next();   
        int id = rs.getInt(1);
        rs.close();
        ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,7,?,NOW())" );
		ps.setInt(1, id);
		ps.setDouble(2, Integer.valueOf(item.stock).intValue());
		ps.execute();
		ps.close();
		ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,6,?,NOW())" );
		ps.setInt(1, id);
		ps.setDouble(2, Double.valueOf(item.buy_price).doubleValue());
		ps.execute();
		ps.close();
		ps=connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,?,5,?,NOW())" );
		ps.setInt(1, id);
		ps.setDouble(2, Double.valueOf(item.sell_price).doubleValue());
		ps.execute();
		ps.close();
	}


	private static void showInfo(Item item) {
		System.out.println("Elemento a añadir:\n" +
				"Nombre: "+item.name+"\n"+
				"Unidades: "+item.stock+"\n"+
				"Precio venta: "+item.sell_price+"\n"+
				"Precio compra: "+item.buy_price+"\n"+
				"Categoria: "+item.category); /**TODO añadir el nombre de la categoria**/
		
	}


	private static Item getDataAddNewItem(Connection connection2, Scanner scan,
			ResultSet rs, String code) throws ScriptException {
		System.out.println("Producto nuevo.\n");
		
		System.out.println("Introduzca nombre de producto.\n");
		String name = scan.nextLine();
		
		System.out.println("¿Cuantas unidades?\n");
		int uds = Integer.valueOf(scan.nextLine());
		
		System.out.println("¿Precio de venta?\n");
		double pvp_venta =(Double)engine.eval(scan.nextLine());
		
		System.out.println("¿Precio de compra?\n");
		double pvp_compra = (Double)engine.eval(scan.nextLine());
		
		System.out.println("Elija la categoria\n"); /**TODO añadir listado de todas**/
		int category = Integer.valueOf(scan.nextLine());
		
		return new Item(name, code, pvp_compra, pvp_venta, uds, category);
		
	}



	private static void wantDeleteAllStore(Connection connection,Scanner scan) throws SQLException {
		System.out.println("¿Desea borrar todas las existencias de la nevera? (N,y)\n");
		boolean borrado = scan.nextLine().startsWith("y");
		if(borrado){
			connection.createStatement().execute("UPDATE inventory SET stock=0, init_stock=0");

			connection.createStatement().execute("INSERT INTO log (session_id, user_id, item_id, type, money, date) VALUES (-1,-1,-1,8,0,NOW())" );
			System.out.println("Todas las existencias han sido borradas.");
		}
	}
}
