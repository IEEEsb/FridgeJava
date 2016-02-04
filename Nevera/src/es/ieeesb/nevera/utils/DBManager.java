package es.ieeesb.nevera.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



/**
 * The Class DBManager.
 */
public class DBManager {

	/** The Constant COLUMN_ID. */
	private static final String COLUMN_ID = "id";
	
	/** The Constant COLUMN_NAME. */
	private static final String COLUMN_NAME = "name";
	
	/** The Constant COLUMN_CREDIT. */
	private static final String COLUMN_CREDIT = "credit";
	
	/** The Constant COLUMN_COST. */
	private static final String COLUMN_COST = "sell_price";
	
	/** The Constant SQL_USER_EXISTS. */
	private static final String SQL_USER_EXISTS = "SELECT id FROM users WHERE ? = dni OR ? = rfid LIMIT 1";
	
	/** The Constant SQL_USER_INFO. */
	private static final String SQL_USER_INFO = "SELECT * FROM users WHERE ? = id LIMIT 1";
	
	/** The Constant SQL_ITEM_INFO. */
	private static final String SQL_ITEM_INFO = "SELECT * FROM inventory WHERE ? = barcode LIMIT 1";
	
	/** The Constant SQL_ITEM_STOCK. */
	private static final String SQL_ITEM_STOCK = "UPDATE inventory SET stock = stock + ? WHERE id = ?";
	
	/** The Constant SQL_USER_CREDIT. */
	private static final String SQL_USER_CREDIT = "UPDATE users SET credit = credit + ? WHERE id = ?";
	private static final String SQL_SESSION_START = "INSERT INTO sessions (user_id, start_session) VALUES (?, NOW())";
	private static final String SQL_SESSION_END = "UPDATE sessions SET end_session=NOW() WHERE id = ?";
	private static final String SQL_LOG = "INSERT INTO log (session_id, user_id, item_id, type, money) VALUES (?,?,?,?,?)";
	private static final String SQL_CANCEL_BOUGHT = "UPDATE log SET type=4 WHERE type=1 AND user_id = ? ORDER BY date DESC LIMIT 1";
	private static final String SQL_GET_LAST_BOUGHT = "SELECT * FROM inventory WHERE id = ( SELECT item_id FROM log WHERE date > DATE_SUB(NOW() , INTERVAL 2 DAY) AND type=1 AND user_id = ? ORDER BY date DESC LIMIT 1)";
	/** The host. */
	private static String host;
	
	/** The user. */
	private static String user;
	
	/** The password. */
	private static String password;

	/** The connection. */
	public static Connection connection;

	/**
	 * Obtain connection properties.
	 */
	public static void obtainConnectionProperties() {
		if( !PropertiesManager.properties.containsKey("HOST") ||
			!PropertiesManager.properties.containsKey("USER") ||
			!PropertiesManager.properties.containsKey("PASS")){
			System.err.println("Datos de conexion erroneos");
			System.exit( -1 );
		}

		host = PropertiesManager.properties.getProperty( "HOST" );
		user = PropertiesManager.properties.getProperty( "USER" );
		password = PropertiesManager.properties.getProperty( "PASS" );
	}

	/**
	 * Connect.
	 */
	public static void connect() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://"+host, user, password);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			System.err.println("No se pudo conectar con la base de datos");
			System.exit( -1 );
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			System.err.println("No se pudo conectar con la base de datos");
			System.exit( -1 );
		}
	}

	/**
	 * User exists.
	 *
	 * @param uid the uid
	 * @return the int
	 */
	public static int userExists(String uid) {
		int id = -1;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_USER_EXISTS);

			preparedStatement.setString(1, uid);
			preparedStatement.setString(2, uid);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				id = resultSet.getInt( COLUMN_ID );
			}
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("userExists: No se pudo consultar a la base de datos");
		}
		return id;
	}

	/**
	 * Disconnect.
	 */
	public static void disconnect() {
		try {
            connection.commit();
			connection.close();
		} catch (SQLException e) {
			System.err.println("Error cerrando la conexion");
		}
	}

	/**
	 * Gets the item.
	 *
	 * @param itemId the item id
	 * @return the item
	 */
	public static Item getItem(String itemId) {
		Item item = null;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_ITEM_INFO);

			preparedStatement.setString(1, itemId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				item = new Item(
						resultSet.getInt(COLUMN_ID),
						resultSet.getString(COLUMN_NAME),
						resultSet.getDouble(COLUMN_COST));
			}
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("getItem: No se pudo consultar a la base de datos");
		}
		return item;
	}
	
	/**
	 * Gets the user.
	 *
	 * @param userId the user id
	 * @return the user
	 */
	public static User getUser(int userId) {
		User user = null;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_USER_INFO);

			preparedStatement.setInt(1, userId);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				user = new User(
						resultSet.getInt(COLUMN_ID),
						resultSet.getString(COLUMN_NAME),
						resultSet.getDouble(COLUMN_CREDIT));
			}
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("getUser: No se pudo consultar a la base de datos");
		}
		return user;
	}

	/**
	 * Update stock.
	 *
	 * @param itemId the item id
	 * @param ammount the ammount
	 */
	public static void updateStock(int itemId, int ammount) {
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_ITEM_STOCK);

			preparedStatement.setInt(1, ammount);
			preparedStatement.setInt(2, itemId);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("updateStock: No se pudo consultar a la base de datos");
		}
	}

	/**
	 * Update user credit.
	 *
	 * @param id the id
	 * @param cost the cost
	 */
	public static void updateUserCredit(int id, double cost) {
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_USER_CREDIT);

			preparedStatement.setDouble(1, cost);
			preparedStatement.setInt(2, id);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("updateUserCredit: No se pudo consultar a la base de datos");
		}
	}

	public static int logStartSession(int id) {
		int sessionId = -1;
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_SESSION_START, Statement.RETURN_GENERATED_KEYS);

			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if(rs.next())
            {
            	sessionId = rs.getInt(1);
            }
            preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("logStartSession: No se pudo consultar a la base de datos");
		}
		return sessionId;
	}

	public static void logEndSession(int sessionId) {
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_SESSION_END);

			preparedStatement.setInt(1, sessionId);
			preparedStatement.executeUpdate();
            preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("logEndSession: No se pudo consultar a la base de datos");
		}
	}

	public static void log(int sessionId, int userId, int itemId, int type, double cost) {
		try {
			PreparedStatement preparedStatement = 
					connection.prepareStatement(SQL_LOG);

			preparedStatement.setInt(1, sessionId);
			preparedStatement.setInt(2, userId);
			preparedStatement.setInt(3, itemId);
			preparedStatement.setInt(4, type);
			preparedStatement.setDouble(5, cost);
			preparedStatement.execute();
            preparedStatement.close();
		} catch (SQLException e) {
			System.err.println("logBought: No se pudo consultar a la base de datos");
		}
		
	}

	public static Item deleteLastBought(int id) {
		Item item = null;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_LAST_BOUGHT);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()) {
				item = new Item(
						resultSet.getInt(COLUMN_ID),
						resultSet.getString(COLUMN_NAME),
						resultSet.getDouble(COLUMN_COST));
			}
			preparedStatement.close();
			if(item!=null){
			preparedStatement = connection.prepareStatement(SQL_CANCEL_BOUGHT);
			preparedStatement.setInt(1, id);
			preparedStatement.execute();
			preparedStatement.close();
			}
		} catch (SQLException e) {
			System.err.println("deleteLastBought: No se pudo consultar a la base de datos");
		}
		return item;
	}

}
