
package connection;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author acer
 */
public class Db_connection {

    private static Connection conn = null;

    public static Connection getConnection() {
    try {
        // 1. Nạp Driver (Bỏ .newInstance() đi vì nó thừa và deprecated)
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // 2. Cấu hình đúng: Port 3307 và Pass là rootpassword
        String url = "jdbc:mysql://localhost:3307/DB_QuanLyChungCu?useUnicode=true&characterEncoding=utf-8";
        String user = "root";
        String password = "root"; // <-- Phải điền pass vào đây
        
        conn = DriverManager.getConnection(url, user, password);
        
        System.out.println("Kết nối thành công!");
    } catch (Exception er) {
        System.out.println("Kết nối thất bại!");
        er.printStackTrace();
    }
    return conn;
}

  
}
