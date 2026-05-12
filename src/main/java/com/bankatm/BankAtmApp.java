package com.bankatm;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class BankAtmApp extends JFrame {
    private static final String CARD_INSERT = "cardInsert";
    private static final String ACCOUNT = "account";
    private static final String MENU = "menu";
    private static final String DEPOSIT = "deposit";
    private static final String WITHDRAW_AMOUNT = "withdrawAmount";
    private static final String PIN = "pin";
    private static final String RESULT = "result";

    private final Database database = new Database();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel pages = new JPanel(cardLayout);

    private Customer currentCustomer;
    private double pendingWithdrawAmount;

    private final JTextField accountField = new JTextField();
    private final JTextField depositAmountField = new JTextField();
    private final JTextField withdrawAmountField = new JTextField();
    private final JPasswordField pinField = new JPasswordField();
    private final JLabel menuTitle = new JLabel();
    private final JLabel balanceLabel = new JLabel();
    private final JLabel resultTitle = new JLabel();
    private final JLabel resultMessage = new JLabel();

    public BankAtmApp() {
        database.setup();

        setTitle("Bank: Money Transaction");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(620, 460));
        setLocationRelativeTo(null);

        pages.add(createCardInsertPage(), CARD_INSERT);
        pages.add(createAccountPage(), ACCOUNT);
        pages.add(createMenuPage(), MENU);
        pages.add(createDepositPage(), DEPOSIT);
        pages.add(createWithdrawAmountPage(), WITHDRAW_AMOUNT);
        pages.add(createPinPage(), PIN);
        pages.add(createResultPage(), RESULT);

        add(pages);
        showPage(CARD_INSERT);
    }

    private JPanel createPage(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(new Color(244, 247, 251));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(new Color(22, 79, 159));
        header.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setForeground(new Color(219, 234, 254));
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitleLabel);
        page.add(header, BorderLayout.NORTH);

        return page;
    }

    private JPanel createContentPanel() {
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(new Color(244, 247, 251));
        content.setBorder(BorderFactory.createEmptyBorder(28, 34, 28, 34));
        return content;
    }

    private GridBagConstraints constraints(int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 14, 0);
        return gbc;
    }

    private JLabel text(String value, int size, int style) {
        JLabel label = new JLabel(value);
        label.setForeground(new Color(23, 32, 51));
        label.setFont(new Font("Segoe UI", style, size));
        return label;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return button;
    }

    private JPanel createCardInsertPage() {
        JPanel page = createPage("SBI ATM", "Welcome to SBI ATM, please insert your debit card");
        JPanel content = createContentPanel();

        content.add(text("Did you insert your debit card?", 18, Font.BOLD), constraints(0));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton yesButton = button("Yes", new Color(22, 79, 159));
        yesButton.addActionListener(event -> {
            accountField.setText("");
            showPage(ACCOUNT);
        });

        JButton noButton = button("No", new Color(107, 114, 128));
        noButton.addActionListener(event -> showResult(
            "Transaction Cancelled",
            "Please insert your debit card to continue."
        ));

        actions.add(yesButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(noButton);
        content.add(actions, constraints(1));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createAccountPage() {
        JPanel page = createPage("Account Verification", "Enter your 6 digit account number");
        JPanel content = createContentPanel();

        accountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        content.add(text("Account Number", 14, Font.BOLD), constraints(0));
        content.add(accountField, constraints(1));

        JButton continueButton = button("Continue", new Color(22, 79, 159));
        continueButton.addActionListener(event -> verifyAccount());
        content.add(continueButton, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createMenuPage() {
        JPanel page = createPage("Transaction Menu", "Choose the transaction you want to perform");
        JPanel content = createContentPanel();

        menuTitle.setForeground(new Color(23, 32, 51));
        menuTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        content.add(menuTitle, constraints(0));

        balanceLabel.setForeground(new Color(59, 70, 92));
        balanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        content.add(balanceLabel, constraints(1));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton depositButton = button("Deposit", new Color(15, 118, 110));
        depositButton.addActionListener(event -> {
            depositAmountField.setText("");
            showPage(DEPOSIT);
        });

        JButton withdrawButton = button("Withdraw", new Color(22, 79, 159));
        withdrawButton.addActionListener(event -> {
            withdrawAmountField.setText("");
            pinField.setText("");
            showPage(WITHDRAW_AMOUNT);
        });

        JButton exitButton = button("Exit", new Color(107, 114, 128));
        exitButton.addActionListener(event -> exitTransaction());

        actions.add(depositButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(withdrawButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(exitButton);
        content.add(actions, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createDepositPage() {
        JPanel page = createPage("Deposit", "Enter deposit amount");
        JPanel content = createContentPanel();

        depositAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        content.add(text("Deposit Amount", 14, Font.BOLD), constraints(0));
        content.add(depositAmountField, constraints(1));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton depositButton = button("Deposit Money", new Color(15, 118, 110));
        depositButton.addActionListener(event -> depositMoney());

        JButton backButton = button("Back", new Color(107, 114, 128));
        backButton.addActionListener(event -> showMenu());

        actions.add(depositButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(backButton);
        content.add(actions, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createWithdrawAmountPage() {
        JPanel page = createPage("Withdraw", "Enter withdraw amount");
        JPanel content = createContentPanel();

        withdrawAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        content.add(text("Withdraw Amount", 14, Font.BOLD), constraints(0));
        content.add(withdrawAmountField, constraints(1));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton continueButton = button("Continue", new Color(22, 79, 159));
        continueButton.addActionListener(event -> verifyWithdrawAmount());

        JButton backButton = button("Back", new Color(107, 114, 128));
        backButton.addActionListener(event -> showMenu());

        actions.add(continueButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(backButton);
        content.add(actions, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createPinPage() {
        JPanel page = createPage("PIN Verification", "Enter your 4 digit pin number");
        JPanel content = createContentPanel();

        pinField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        content.add(text("PIN Number", 14, Font.BOLD), constraints(0));
        content.add(pinField, constraints(1));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton withdrawButton = button("Withdraw Money", new Color(22, 79, 159));
        withdrawButton.addActionListener(event -> withdrawMoney());

        JButton backButton = button("Back", new Color(107, 114, 128));
        backButton.addActionListener(event -> showPage(WITHDRAW_AMOUNT));

        actions.add(withdrawButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(backButton);
        content.add(actions, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel createResultPage() {
        JPanel page = createPage("Transaction Status", "Review the transaction result");
        JPanel content = createContentPanel();

        resultTitle.setForeground(new Color(23, 32, 51));
        resultTitle.setFont(new Font("Segoe UI", Font.BOLD, 19));
        content.add(resultTitle, constraints(0));

        resultMessage.setForeground(new Color(59, 70, 92));
        resultMessage.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        content.add(resultMessage, constraints(1));

        JPanel actions = new JPanel();
        actions.setBackground(new Color(244, 247, 251));
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        JButton menuButton = button("Main Menu", new Color(22, 79, 159));
        menuButton.addActionListener(event -> {
            if (currentCustomer == null) {
                showPage(CARD_INSERT);
            } else {
                showMenu();
            }
        });

        JButton exitButton = button("Exit Transaction", new Color(107, 114, 128));
        exitButton.addActionListener(event -> exitTransaction());

        actions.add(menuButton);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(exitButton);
        content.add(actions, constraints(2));

        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private void verifyAccount() {
        String accountNumber = accountField.getText().trim();
        if (!accountNumber.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter a valid 6 digit account number.",
                "Invalid Account Number",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Customer customer = database.findCustomer(accountNumber);
        if (customer == null) {
            currentCustomer = null;
            showResult("Account Not Found", "Account number does not exist. Transaction cancelled.");
            return;
        }

        currentCustomer = customer;
        showMenu();
    }

    private void showMenu() {
        refreshCustomer();
        menuTitle.setText("Welcome " + currentCustomer.name());
        balanceLabel.setText(String.format("Available balance: %.2f", currentCustomer.balance()));
        showPage(MENU);
    }

    private void depositMoney() {
        Double amount = readAmount(depositAmountField);
        if (amount == null) {
            return;
        }

        double newBalance = currentCustomer.balance() + amount;
        database.updateBalance(currentCustomer.accountNumber(), newBalance);
        refreshCustomer();
        showResult("Money is credited", String.format("Available balance: %.2f", newBalance));
    }

    private void verifyWithdrawAmount() {
        Double amount = readAmount(withdrawAmountField);
        if (amount == null) {
            return;
        }

        refreshCustomer();
        if (amount > currentCustomer.balance()) {
            JOptionPane.showMessageDialog(
                this,
                "Insufficient balance",
                "Insufficient Balance",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        pendingWithdrawAmount = amount;
        pinField.setText("");
        showPage(PIN);
    }

    private void withdrawMoney() {
        String pin = new String(pinField.getPassword()).trim();
        if (!pin.equals(currentCustomer.pin())) {
            JOptionPane.showMessageDialog(
                this,
                "Wrong pin number",
                "Wrong PIN",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        double newBalance = currentCustomer.balance() - pendingWithdrawAmount;
        database.updateBalance(currentCustomer.accountNumber(), newBalance);
        refreshCustomer();
        showResult("Money is debited", String.format("Available balance: %.2f", newBalance));
    }

    private Double readAmount(JTextField field) {
        try {
            double amount = Double.parseDouble(field.getText().trim());
            if (amount <= 0) {
                throw new NumberFormatException();
            }
            return amount;
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(
                this,
                "Please enter an amount greater than 0.",
                "Invalid Amount",
                JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    private void refreshCustomer() {
        if (currentCustomer != null) {
            currentCustomer = database.findCustomer(currentCustomer.accountNumber());
        }
    }

    private void showResult(String title, String message) {
        resultTitle.setText(title);
        resultMessage.setText(message);
        showPage(RESULT);
    }

    private void exitTransaction() {
        currentCustomer = null;
        pendingWithdrawAmount = 0;
        accountField.setText("");
        depositAmountField.setText("");
        withdrawAmountField.setText("");
        pinField.setText("");
        showResult("Transaction Ended", "Thank you for using SBI ATM.");
    }

    private void showPage(String pageName) {
        cardLayout.show(pages, pageName);
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--check-db".equals(args[0])) {
            new Database().setup();
            System.out.println("Database setup completed.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            BankAtmApp app = new BankAtmApp();
            app.setVisible(true);
        });
    }

    private record Customer(String accountNumber, String pin, double balance, String name) {
    }

    private static class Database {
        private static final String DB_URL = "jdbc:sqlite:bank.db";

        void setup() {
            try (Connection connection = connect(); Statement statement = connection.createStatement()) {
                statement.execute("""
                    CREATE TABLE IF NOT EXISTS customers (
                        account_number TEXT PRIMARY KEY CHECK(length(account_number) = 6),
                        pin TEXT NOT NULL CHECK(length(pin) = 4),
                        balance REAL NOT NULL CHECK(balance >= 0),
                        account_holder_name TEXT NOT NULL
                    )
                    """);

                seedCustomer("123456", "2354", 50000.0, "John, Doe");
                seedCustomer("214365", "5243", 10000.0, "Praneetha, Badepally");
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to setup database.", exception);
            }
        }

        Customer findCustomer(String accountNumber) {
            String sql = """
                SELECT account_number, pin, balance, account_holder_name
                FROM customers
                WHERE account_number = ?
                """;

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, accountNumber);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }

                    return new Customer(
                        resultSet.getString("account_number"),
                        resultSet.getString("pin"),
                        resultSet.getDouble("balance"),
                        resultSet.getString("account_holder_name")
                    );
                }
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to find customer.", exception);
            }
        }

        void updateBalance(String accountNumber, double balance) {
            String sql = "UPDATE customers SET balance = ? WHERE account_number = ?";

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, balance);
                statement.setString(2, accountNumber);
                statement.executeUpdate();
            } catch (SQLException exception) {
                throw new IllegalStateException("Unable to update balance.", exception);
            }
        }

        private void seedCustomer(String accountNumber, String pin, double balance, String name)
            throws SQLException {
            String sql = """
                INSERT OR IGNORE INTO customers
                    (account_number, pin, balance, account_holder_name)
                VALUES (?, ?, ?, ?)
                """;

            try (Connection connection = connect();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, accountNumber);
                statement.setString(2, pin);
                statement.setDouble(3, balance);
                statement.setString(4, name);
                statement.executeUpdate();
            }
        }

        private Connection connect() throws SQLException {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException exception) {
                throw new SQLException("SQLite JDBC driver was not found.", exception);
            }
            return DriverManager.getConnection(DB_URL);
        }
    }
}
