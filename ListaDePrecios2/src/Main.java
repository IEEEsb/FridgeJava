import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import es.ieeesb.nevera.utils.PropertiesManager;


public class Main {

	public static void main(String[] args) throws Exception{
		PropertiesManager.loadProperties();
		Class.forName("com.mysql.jdbc.Connection");
		Connection connection = DriverManager.getConnection("jdbc:mysql://"+
				PropertiesManager.properties.getProperty( "HOST" ),
				PropertiesManager.properties.getProperty( "USER" ),
				PropertiesManager.properties.getProperty( "PASS" ));
		ResultSet rs = connection.createStatement().executeQuery("SELECT id, name, barcode, sell_price FROM inventory ORDER BY type, name");
		
		while(rs.next()){
			System.out.println(rs.getString(2)+"---"+rs.getString(4)+"€---"+rs.getString(3));
			
		}
		System.out.println("todo bien :)");
	}

}
