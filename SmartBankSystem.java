import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// ---------------------------------------------------------
// 1. MAIN CLASS (The only PUBLIC class)
// ---------------------------------------------------------
public class SmartBankSystem {
    public static void main(String[] args) {
        // Create dummy objects (In reality, you'd fetch these from DB)
        // Note: We use the subclass (SavingsAccount) but hold it in parent reference (Account) -> Polymorphism
        Account akhil = new SavingsAccount(101, "Akhil", 5000);
        Account john = new SavingsAccount(102, "John", 1000);

        BankService bank = new BankService();

        System.out.println("--- Initial State ---");
        System.out.println("Akhil: " + akhil.getBalance());
        System.out.println("John:  " + john.getBalance());

        System.out.println("\n--- Transferring 1000 from Akhil to John ---");
        bank.transferMoney(akhil, john, 1000);

        System.out.println("\n--- Final State ---");
        System.out.println("Akhil: " + akhil.getBalance());
        System.out.println("John:  " + john.getBalance());
    }
}

// ---------------------------------------------------------
// 2. DATABASE CONNECTION (Singleton Pattern)
// ---------------------------------------------------------
class DatabaseConnection {
    // -----------------------------------------------------------
    // COPY PASTE FROM TIDB DASHBOARD HERE:
    // -----------------------------------------------------------
    
    // 1. Look for 'Host' (It looks like: gateway01.us-west-2...)
    private static final String HOST = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com"; 
    
    // 2. Look for 'Port' (Usually 4000)
    private static final String PORT = "4000"; 
    
    // 3. Look for 'User' (It looks weird like: 2G9E82.root)
    private static final String USER = "21G3kYGyQ1hioUk.root"; 
    
    // 4. Your Password (If you lost it, click 'Reset Password' in TiDB)
    private static final String PASS = "jLWrHQD5pKiHD0fy"; 

    // 5. The Database Name (It is 'test' unless you created a new one)
    private static final String DB_NAME = "test"; 
    // -----------------------------------------------------------


    // This weird string combines all the above into a valid URL
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + "?useSSL=true&requireSSL=true&serverTimezone=UTC";

    private static Connection connection = null;

    public static Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASS);
        }
        return connection;
    }
}

// ---------------------------------------------------------
// 3. MODELS (OOP: Abstraction & Inheritance)
// ---------------------------------------------------------
abstract class Account {
    protected int accountNumber;
    protected String owner;
    protected double balance;

    public Account(int accountNumber, String owner, double balance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.balance = balance;
    }

    // Abstract method: Forces subclasses to define rules
    public abstract void withdraw(double amount) throws Exception;

    public void deposit(double amount) {
        this.balance += amount;
    }

    public double getBalance() { return balance; }
    public int getAccountNumber() { return accountNumber; }
}

class SavingsAccount extends Account {
    private static final double MIN_BALANCE = 500;

    public SavingsAccount(int accNum, String owner, double balance) {
        super(accNum, owner, balance);
    }

    @Override
    public void withdraw(double amount) throws Exception {
        if (balance - amount < MIN_BALANCE) {
            throw new Exception("Low Balance: Minimum 500 required.");
        }
        this.balance -= amount;
    }
}

// ---------------------------------------------------------
// 4. SERVICE LAYER (JDBC Transactions)
// ---------------------------------------------------------
class BankService {

    public void transferMoney(Account fromAccount, Account toAccount, double amount) {
        Connection conn = null;
        PreparedStatement withdrawStmt = null;
        PreparedStatement depositStmt = null;

        String withdrawSQL = "UPDATE accounts SET balance = ? WHERE acc_id = ?";
        String depositSQL = "UPDATE accounts SET balance = ? WHERE acc_id = ?";

        try {
            conn = DatabaseConnection.getInstance();
            
            // ACID PROPERTY: Atomicity
            conn.setAutoCommit(false); // Start Transaction

            // 1. Java Logic Check
            fromAccount.withdraw(amount);
            toAccount.deposit(amount);

            // 2. Database Update
            withdrawStmt = conn.prepareStatement(withdrawSQL);
            withdrawStmt.setDouble(1, fromAccount.getBalance());
            withdrawStmt.setInt(2, fromAccount.getAccountNumber());
            
            depositStmt = conn.prepareStatement(depositSQL);
            depositStmt.setDouble(1, toAccount.getBalance());
            depositStmt.setInt(2, toAccount.getAccountNumber());

            int rows1 = withdrawStmt.executeUpdate();
            int rows2 = depositStmt.executeUpdate();

            if (rows1 == 1 && rows2 == 1) {
                conn.commit(); // Save
                System.out.println("✅ Transaction Success!");
            } else {
                conn.rollback(); // Undo
                System.out.println("❌ Database Error. Rolling back.");
            }

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException se) {}
            System.out.println("❌ Failed: " + e.getMessage());
        } finally {
            // Cleanup
            try {
                if (withdrawStmt != null) withdrawStmt.close();
                if (depositStmt != null) depositStmt.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
}