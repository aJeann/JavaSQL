import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * Created by Axel Jeansson
 * Date: 2021-02-24
 * Time: 23:27
 * Project: JavaSQL
 * Copyright: MIT
 */
public class Repository {

    private Properties p = new Properties();

    public Repository() throws ClassNotFoundException, IOException {
        p.load(new FileInputStream("C:\\Users\\GODofTWERK\\Desktop\\JavaSQL\\DBProperties.properties"));
        Class.forName("com.mysql.cj.jdbc.Driver");
    }


    public void openShop() throws SQLException {

        Scanner s = new Scanner(System.in);
        System.out.println("Användarnamn:");
        String userID = s.nextLine();
        System.out.println("Lösen:");
        int userpw = s.nextInt();
        confirmLogin(userID, userpw);
    }

    public void confirmLogin(String userID, int userpw) throws SQLException {

        try (Connection con = DriverManager.getConnection(p.getProperty("connectionString"),
                        p.getProperty("name"),
                        p.getProperty("password"));
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Customer_ID, Customer_Name, User_PW from Customer");
        ) {
            while (rs.next()) {
                System.out.println("1");
                int id = rs.getInt("Customer_ID");
                String name = rs.getString("Customer_Name");
                int pw = rs.getInt("User_PW");

                if (name.equals(userID) && pw == userpw) {
                    shop(userID, id);
                }
            }
            openShop();



        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ;

    }

    public void shop(String userID, int id) {
        Scanner s = new Scanner(System.in);
        int shoe_ID = 0;
        System.out.println("Du har loggat in som användare: " + userID);
        System.out.println("\nProdukter i lager:" + "\n");
        try (Connection con = DriverManager.getConnection(p.getProperty("connectionString"),
                p.getProperty("name"),
                p.getProperty("password"));
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Name, Stock.Shoe_ID, Stock.amount from shoes inner join Stock on Shoes.Shoe_ID = stock.Shoe_ID;");
        ) {
            while (rs.next()) {

                String shoe_name = rs.getString("Name");
                int amount = rs.getInt("amount");
                shoe_ID = rs.getInt("Shoe_ID");

                if (amount > 0)
                    System.out.println(shoe_ID + "\nShoe: " + shoe_name + "\nAmount in stock:" + amount + "\n");

            }

            String[] userChoice = {"Köpa nya skor", "Kolla ordrar"};

            int input = JOptionPane.showOptionDialog(null, "Vill du köpa nya skor eller kolla tidigare ordrar?", null, JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, userChoice, userChoice[0]);

            if (input == 0) {
                int choice = 0;
                while (choice < 1 || choice > shoe_ID) {
                    System.out.println("Vilken sko vill du köpa?");
                    choice = s.nextInt();
                }
                purchaseShoe(userID, choice, id);
            } else if(input == 1){
                checkOrders(userID, id);
            }
            else
                System.exit(0);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        ;


    }

    public void purchaseShoe(String userID, int choice, int id) throws SQLException {
        Random r = new Random();

        int orderID = r.nextInt(200);

        try (Connection con = DriverManager.getConnection(p.getProperty("connectionString"),
                p.getProperty("name"),
                p.getProperty("password"));
             CallableStatement stmt = con.prepareCall("call AddToCart(?, ?, ?, ?)");
        ) {
            stmt.setInt(1, orderID);
            stmt.setInt(2, id);
            stmt.setInt(3, choice);
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.execute();
            String status = stmt.getString(4);
            System.out.println(status);
            int input = JOptionPane.showConfirmDialog(null, "Vill du se din beställning?");
            if (input == 0) {
                showOrder(orderID);
            } else {
                System.out.println("Tack för att du handlat hos oss!");
                System.exit(0);
            }

        }
    }

    public void showOrder(int orderID) throws SQLException {

        try (Connection con = DriverManager.getConnection(p.getProperty("connectionString"),
                p.getProperty("name"),
                p.getProperty("password"));
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Customer_Name, Orders.Order_ID, Orders.datum, Shoes.Name from Customer inner join Orders on orders.Customer = Customer.Customer_ID inner join order_contents on order_contents.orderID=Orders.order_ID inner join shoes on shoes.Shoe_ID = Order_contents.shoe where order_contents.orderID=" + orderID);
        ) {
            System.out.println("-------------------------------");
            System.out.println("Order: " + orderID);
            System.out.println("Produkter i ordern: ");
            while (rs.next()) {
                String shoe_name = rs.getString("Name");
                System.out.println(shoe_name);
            }
            System.out.println("-------------------------------");
        }

        System.out.println("Tack för att du handlat hos oss");
        System.exit(0);

    }

    public void checkOrders(String userID, int id) {
        Scanner s = new Scanner(System.in);

        List<Integer> list = new ArrayList<Integer>();

        try (Connection con = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/shoe_shop?serverTimezone=UTC&useSSL=false",
                "root",
                "axjea001");
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Customer_Name, Orders.Order_ID, Orders.datum from Customer inner join Orders on Orders.customer = Customer.customer_ID where customer_ID="+id);
        ) {
            System.out.println("Ordrar för kund: " +userID);
            while (rs.next()) {
                int order_ID = rs.getInt("Order_ID");
                String datum = rs.getString("datum");

                list.add(order_ID);

                System.out.println("-----------------");
                System.out.println("Order-ID: " +order_ID);
                System.out.println("Datum: " +datum);
                System.out.println("-----------------");
            }

            while(true){
                System.out.println("Vilken order vill du kolla på?");
                int orderChoice = s.nextInt();

                for (int order:list) {
                    if (order == orderChoice) {
                        openOrder(orderChoice);
                        break;
                    }
                }
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void openOrder(int orderChoice) {
        try (Connection con = DriverManager.getConnection(p.getProperty("connectionString"),
                p.getProperty("name"),
                p.getProperty("password"));
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT Customer_Name, Orders.Order_ID, Orders.datum, Shoes.Name from Customer inner join Orders on orders.Customer = Customer.Customer_ID" +
                     " inner join order_contents on order_contents.orderID=Orders.order_ID inner join shoes on shoes.Shoe_ID = Order_contents.shoe where order_contents.orderID ="+orderChoice);
        ) {
            System.out.println("Ordern innehåller: ");
            while (rs.next()) {
                String shoeName = rs.getString("Name");
                System.out.println("- " + shoeName);
            }
            System.out.println("Programmet avslutas");
            System.exit(0);


        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
