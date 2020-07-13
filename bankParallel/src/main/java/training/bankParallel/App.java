package training.bankParallel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class App {
	static Connection con;
	static Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		try {

			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost:3306/parallpro?serverTimezone=UTC", "root", "");

		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Trouble Connecting to the database");
		}

		while (true) {

			System.out.println("Welcome to XYZ Bank. Choose from an option below");
			System.out.println("1 - Create new Account");
			System.out.println("2 - View Balance");
			System.out.println("3 - Deposit Money");
			System.out.println("4 - Withdraw Money");
			System.out.println("5 - Transfer Funds");
			System.out.println("6 - Print transaction statement");

			int option = Integer.parseInt(sc.nextLine());
			switch (option) {
			case 1:
				new App().createAccount();
				break;
			case 2:
				new App().viewBalance();
				break;
			case 3:
				new App().depositHelper();
				break;
			case 4:
				new App().withdrawHelper();
				break;
			case 5:
				new App().transferFunds();
				break;
			case 6:
				new App().printStatement();
				break;
			default:
				System.out.println("Select a valid option!");
			}
			System.out.println("Do you want to continue (y/n) ?");
			String res = sc.nextLine();
			if (res.equals("n")) {
				System.out.println("Thankyou for using our service!");
				break;
			}
		}
	}
	
	void printStatement() {
		System.out.println("Enter Account Number!");
		int ac = Integer.parseInt(sc.nextLine());
		System.out.println("Enter password");
		int pass = Integer.parseInt(sc.nextLine());

		if (checkDetails(ac, pass)) {
			try {
				PreparedStatement st = con.prepareStatement("select * from transactions where accountNumber=?");
				st.setInt(1, ac);

				ResultSet rs = st.executeQuery();
				java.sql.ResultSetMetaData rsmd = rs.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= columnsNumber; i++) {
						if (i > 1)
							System.out.print(",  ");
						String columnValue = rs.getString(i);
						System.out.print(rsmd.getColumnName(i) + " : " + columnValue);
					}
					System.out.println("");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Failed to connect with the database." + e);
			}
		}
	}

	void updateTransaction(int acn, String type, int amt) {
		try {
			PreparedStatement st = con.prepareStatement(
					"insert into transactions(accountNumber, transactionType, transactionAmount) values (?,?,?)");
			st.setInt(1, acn);
			st.setString(2, type);
			st.setInt(3, amt);

			st.executeUpdate();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("failed to update transaction details! " + e);
		}
	}
	
	void transferFunds()
	{
		System.out.println("Enter your account Number: ");
		int ac1 = Integer.parseInt(sc.nextLine());
		System.out.println("Enter password");
		int pass = Integer.parseInt(sc.nextLine());
		System.out.println("Enter amount to be transferred:");
		int tr = Integer.parseInt(sc.nextLine());
		System.out.println("Enter receiver's account number :");
		int ac2 = Integer.parseInt(sc.nextLine());
		
		int i = transfer(ac1, pass, tr, ac2);
		if(i != 0)
		{
			System.out.println("Transfer successful");
			updateTransaction(ac1, "withdraw by transfer", tr);
			updateTransaction(ac2, "deposit by transfer", tr);
		}
		else 
			System.out.println("Transfer fail");
	}
	
	int transfer(int ac1, int pass, int tr, int ac2)
	{
		if(checkDetails(ac1, pass))
		{
			try {
				PreparedStatement st = con.prepareStatement("select * from accountDetails where accountNumber=?");
				st.setInt(1, ac2);
				
				ResultSet rs = st.executeQuery();
				if(rs.next())
				{
					int i = withdraw(ac1, pass, tr);
					int j = deposit(ac2 , tr);
					if(i==1 && j==1)
						return i+j;
					else return 0;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Error connecting with the database."+e);;
			}
		}
		return 0;
	}
	
	void withdrawHelper() {
		System.out.println("Enter account Number: ");
		int ac = Integer.parseInt(sc.nextLine());
		System.out.println("Enter password");
		int pass = Integer.parseInt(sc.nextLine());
		System.out.println("Enter amount to be withdrawn:");
		int wt = Integer.parseInt(sc.nextLine());

		int i = withdraw(ac, pass, wt);
		if (i != 0) 
		{
			System.out.println("Withdrawal successful");
			updateTransaction(ac, "withdraw", wt);
		}
		else
			System.out.println("Withdrawal failed");
	}
	int withdraw(int ac, int pass, int wt)
	{
		if(checkDetails(ac,pass))
		{
			try {
				PreparedStatement st = con.prepareStatement("select accountBalance from accountDetails where accountNumber=?");
				st.setInt(1,ac);
				
				ResultSet rs = st.executeQuery();
				rs.next();
				if(rs.getInt("accountBalance")>wt)
				{	
				st = con.prepareStatement("update accountDetails set accountBalance=accountBalance-? where accountNumber=?");
				st.setInt(1, wt);
				st.setInt(2, ac);
				
				int i = st.executeUpdate();
				if (i>0)
					return 1;
				else return 0;
				}
				else
				{
					System.out.println("Insfficient balance");
					return 0;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Error connecting to database");
			}
		}
		else
		{
			System.out.println("Enter correct Details and try again!");
			return 0;
		}
		return 0;
}

	void depositHelper() {
		System.out.println("Enter account Number: ");
		int ac = Integer.parseInt(sc.nextLine());
		System.out.println("Enter password");
		int pass = Integer.parseInt(sc.nextLine());
		System.out.println("Enter amount to be deposited:");
		int dp = Integer.parseInt(sc.nextLine());

		int i = deposit(ac, pass, dp);
		if (i != 0) 
		{
			System.out.println("Deposit successful");
			updateTransaction(ac, "deposit", dp);
		}
		else
			System.out.println("Deposit failed");
	}

	int deposit(int ac, int pass, int dp) {

		if (checkDetails(ac, pass)) {
			try {

				PreparedStatement st = con.prepareStatement("update accountDetails set accountBalance= (accountBalance+?) where accountNumber =?");
				st.setInt(1, dp);
				st.setInt(2, ac);

				int i = st.executeUpdate();
				if (i > 0)
					return 1;
				else
					return 0;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Error in depositing the amount" + e);
			}
		} else {
			System.out.println("Try Again with correct details");
			return 0;
		}
		return 0;
	}

	int deposit(int ac, int dp) {
			try {

				PreparedStatement st = con.prepareStatement("update accountDetails set accountBalance= (accountBalance+?) where accountNumber =?");
				st.setInt(1, dp);
				st.setInt(2, ac);

				int i = st.executeUpdate();
				if (i > 0)
					return 1;
				else
					return 0;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Error in depositing the amount" + e);
			}

			return 0;
		} 
	
	boolean checkDetails(int ac, int pass) {
		try {
			PreparedStatement st = con.prepareStatement("select * from accountDetails where accountNumber=?");
			st.setInt(1, ac);

			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				if (rs.getInt("accountPassword") != pass) {
					System.out.println("Wrong Password");
					return false;
				}
			} else {
				System.out.println("Invalid Account Number");
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in retrieving your details. Try again later!" + e);
		}
		return true;
	}

	void viewBalance() {
		System.out.println("Enter account Number: ");
		int ac = Integer.parseInt(sc.nextLine());
		System.out.println("Enter password");
		int pass = Integer.parseInt(sc.nextLine());
		try {
			PreparedStatement st = con.prepareStatement("select * from accountDetails where accountNumber=?");
			st.setInt(1, ac);

			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				if (rs.getInt("accountPassword") != pass)
					System.out.println("Wrong Password");
				else
					System.out.println("Your balance is " + rs.getInt("accountBalance"));
			} else
				System.out.println("Invalid Account Number");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in retrieving your details. Try again later!" + e);
		}
	}

	void createAccount() {
		System.out.println("Enter User Name :");
		String uname = sc.nextLine();
		int pass;
		int rpass;
		while (true) {
			System.out.println("Enter Password :");
			pass = Integer.parseInt(sc.nextLine());
			System.out.println("Re-enter Password :");
			rpass = Integer.parseInt(sc.nextLine());
			if (!(pass == rpass))
				System.out.println("Both Passwords should match");
			else
				break;
		}

		try {
			PreparedStatement st = con.prepareStatement(
					"insert into accountDetails(accountHolderName,accountPassword) values (?,?)",
					PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, uname);
			st.setInt(2, rpass);
			int i = st.executeUpdate();
			if (i > 0)
				System.out.println("Account created successfully");
			
			ResultSet rs = st.getGeneratedKeys();
			if (rs.next()) {
				int ac = rs.getInt(1);
				System.out.println("Your account number is: " + ac);
				updateTransaction(ac, "account created", 0);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error Creating a new Account!" + e);
		}
	}
}
