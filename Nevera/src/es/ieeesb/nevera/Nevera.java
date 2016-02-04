package es.ieeesb.nevera;

import es.ieeesb.nevera.utils.DBManager;
import es.ieeesb.nevera.utils.Item;
import es.ieeesb.nevera.utils.PropertiesManager;
import es.ieeesb.nevera.utils.User;

/**
 * The Class Nevera.
 */
public class Nevera {
	
	private static final int COMPRA = 1;
	private static final int CANCELACION = 2;
	
	/** The min balance. */
	private static double MIN_BALANCE;
	
	/** The close session command. */
	private static String CLOSE_SESSION_COMMAND;
	
	/** The cancel command. */
	private static String CANCEL_COMMAND;
	
	/** The user. */
	private User user;
	
	/** The session started. */
	private boolean sessionStarted;
	
	/** The session id **/
	private int sessionId;
	
	/** The time out of session.**/
	private static final long TIME_OUT =30000;
	
	//private Monitor monitor ;
	private long TIME_START;
	//private Timer mTimer;
	
	/**
	 * Instantiates a new nevera.
	 */
	public Nevera() {
		MIN_BALANCE = Double.parseDouble(
				PropertiesManager.properties.getProperty(
						"MIN_BALANCE", "-3.0") );

		CLOSE_SESSION_COMMAND =
				PropertiesManager.properties.getProperty(
						"CLOSE_SESSION_COMMAND", "endSession");
		
		CANCEL_COMMAND =
				PropertiesManager.properties.getProperty(
						"CANCEL_COMMAND", "cancel");
		//mTimer = new Timer();
	}
	
	/**
	 * Message recieved.
	 *
	 * @param messageReaded the message readed
	 */
	public void messageRecieved( String messageReaded ) {
		//Si la sesion esta iniciada solo pueden llegar codigos de producto
		// o el comando para finalizar la sesison
		if(sessionStarted) {
			if( CLOSE_SESSION_COMMAND.equals(messageReaded) ) {
				endSession();
			} else if( CANCEL_COMMAND.equals(messageReaded) ) {
				cancel();
			} else {
				buy( messageReaded );
			}
		} else {
			//Si la sesion no esta iniciada el mensaje recibido debe ser la
			// autenticacion de un usuario
			startSession( messageReaded );
		}
	}
	
	/**
	 * Start session.
	 *
	 * @param uid the uid
	 */
	private void startSession( String uid ) {
		
		DBManager.connect();
		int id = DBManager.userExists( uid );
		if( id != -1 ) {
			TIME_START=System.currentTimeMillis();
			Monitor monitor = new Monitor(TIME_START);
			Thread thmonitor = new Thread((Runnable) monitor);
			thmonitor.start();
			
			//starCountdown();
			
			sessionStarted = true;
			sessionId = DBManager.logStartSession(id);
			user = getUserInfo( id );
			System.out.println(String.format(
					"Bienvenido %s,  tu saldo es de %.2f€",
					user.name, user.credit));
		} else {
			System.out.println("ERROR: usuario no encontrado");
			DBManager.disconnect();
		}
	}
	
	/**
	 * End session.
	 */
	private void endSession( ) {
		if(!sessionStarted) return;
		DBManager.logEndSession(sessionId);
		//TODO: comprobar que el usuario en la db sea igual que user
		System.out.println(String.format("Tu saldo final es de %.2f€", user.credit));
		System.out.println("Gracias por consumir");
		sessionStarted = false;
		user = null;
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		System.out.println("Esperando identificación");
	}
	
	/**
	 * Buy.
	 *
	 * @param itemId the item id
	 */
	private void buy( String itemId ) {
		if(!sessionStarted) return;
		Item item = DBManager.getItem( itemId );
		if( item != null ){
			if( hasCredit( item.cost ) ) {
				DBManager.updateStock( item.id, -1 );
				DBManager.updateUserCredit( user.id, -item.cost );
				user.credit -= item.cost;
				System.out.println(String.format("Has consumido un %s por %.2f€ \n Su saldo actual es: %.2f€", item.name, item.cost, user.credit));
				DBManager.log(sessionId, user.id, item.id, COMPRA, -item.cost);
			} else {
				System.out.println("Tu saldo es insuficiente");
			}
		} else {
			System.out.println("Producto no encontrado");
		}
		
	}
	
	/**
	 * Checks for credit.
	 *
	 * @param cost the cost
	 * @return true, if successful
	 */
	private boolean hasCredit(double cost) {
		return user.credit - cost >= MIN_BALANCE;
	}

	/**
	 * Gets the user info.
	 *
	 * @param id the id
	 * @return the user info
	 */
	private User getUserInfo( int id ) {
		if(!sessionStarted) return null;
		return DBManager.getUser( id );
	}
	
	/**
	 * Cancel.
	 */
	private void cancel( ) {
		if(!sessionStarted) return;
		Item item = DBManager.deleteLastBought( user.id );
		if(item!=null){
			DBManager.updateUserCredit(user.id, item.cost);
			user.credit += item.cost;
			System.out.println(String.format("Eliminada la compra de %s, por %.2f€", item.name, item.cost));
			DBManager.log(sessionId, user.id, item.id, CANCELACION, item.cost);
		} else {
			System.out.println("No se encontró ninguna compra en los ultimos dos dias");
		}
	}
	
	private class Monitor implements Runnable {
		private long num;
		public Monitor (long n){
			this.num=n;
		}
		public void run() {
			try {
				Thread.sleep(TIME_OUT);
				if(num==TIME_START)
					endSession();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
		}
	}
	
//	private void starCountdown(){
//		cancelCountdown();
//		
//		TimerTask mEndSessionTTask = new TimerTask(){
//			@Override
//			public void run() {
//				endSession();
//			}
//		};
//		
//		mTimer.schedule(mEndSessionTTask, TIME_OUT);
//	}
//	
//	private void cancelCountdown(){
//		mTimer.cancel();
//		mTimer.purge();
//	}
}
