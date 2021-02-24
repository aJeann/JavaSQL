import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Axel Jeansson
 * Date: 2021-02-19
 * Time: 23:58
 * Project: JavaSQL
 * Copyright: MIT
 */
public class Main {
    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        Repository jdbc = new Repository();
        jdbc.openShop();
    }


}
