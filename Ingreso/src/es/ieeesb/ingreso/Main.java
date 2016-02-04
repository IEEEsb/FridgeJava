package es.ieeesb.ingreso;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

import es.ieeesb.nevera.utils.PropertiesManager;

public class Main {

	private static final int INGRESO = 3;
	public static void main(String args[]) throws Exception {

		PropertiesManager.loadProperties();
		
		Scanner scan = new Scanner(System.in);
		System.out.println("Introduzca código autorizado");
		String dni = scan.nextLine();
		
		Class.forName("com.mysql.jdbc.Connection");
		Connection connection = DriverManager.getConnection("jdbc:mysql://"+
				PropertiesManager.properties.getProperty( "HOST" ),
				PropertiesManager.properties.getProperty( "USER" ),
				PropertiesManager.properties.getProperty( "PASS" ));
		
		ResultSet rs = connection.createStatement().executeQuery("SELECT autorized FROM users WHERE dni = '"+dni+"'");
		if(!rs.next() || rs.getInt(1) == 0){
			System.err.println("Usuario no autorizado");
			scan.close();
			connection.close();
			System.exit(-1);
		}
		rs.close();
		System.out.print("Introduce el dni del usuario que va a ingresar dinero: ");
		dni = scan.nextLine();
		System.out.print("Cantidad de dinero a ingresar: ");
		double credit = Double.parseDouble(scan.nextLine());
		
		PreparedStatement ps = connection.prepareStatement("UPDATE users SET credit = credit + ? WHERE dni = ?");
		ps.setDouble(1, credit);
		ps.setString(2, dni);
		ps.execute();
		ps.close();
		rs = connection.createStatement().executeQuery("SELECT credit, id FROM users WHERE dni = '"+dni+"'");
		rs.next();
		System.out.println(String.format("Credito actual %.2f€", rs.getDouble(1)));
		
		ps = connection.prepareStatement("INSERT INTO log (session_id, user_id, item_id, type, money) VALUES (?,?,?,?,?)");
		ps.setInt(1, -1);
		ps.setInt(2, rs.getInt(2));
		ps.setInt(3, -1);
		ps.setInt(4, INGRESO);
		ps.setDouble(5, credit);
		ps.execute();
		ps.close();
		rs.close();
		connection.close();
		scan.close();
	}
}
