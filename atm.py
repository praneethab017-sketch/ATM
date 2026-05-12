import sqlite3
from pathlib import Path


DB_PATH = Path(__file__).with_name("bank.db")


CUSTOMERS = [
    ("123456", "2354", 50000.0, "John, Doe"),
    ("214365", "5243", 10000.0, "Praneetha, Badepally"),
]


def connect_db():
    return sqlite3.connect(DB_PATH)


def setup_database():
    with connect_db() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS customers (
                account_number TEXT PRIMARY KEY CHECK(length(account_number) = 6),
                pin TEXT NOT NULL CHECK(length(pin) = 4),
                balance REAL NOT NULL CHECK(balance >= 0),
                account_holder_name TEXT NOT NULL
            )
            """
        )
        conn.executemany(
            """
            INSERT OR IGNORE INTO customers
                (account_number, pin, balance, account_holder_name)
            VALUES (?, ?, ?, ?)
            """,
            CUSTOMERS,
        )


def get_customer(account_number):
    with connect_db() as conn:
        conn.row_factory = sqlite3.Row
        return conn.execute(
            """
            SELECT account_number, pin, balance, account_holder_name
            FROM customers
            WHERE account_number = ?
            """,
            (account_number,),
        ).fetchone()


def update_balance(account_number, balance):
    with connect_db() as conn:
        conn.execute(
            "UPDATE customers SET balance = ? WHERE account_number = ?",
            (balance, account_number),
        )


def read_amount(prompt):
    try:
        amount = float(input(prompt).strip())
    except ValueError:
        print("Invalid amount. Please enter a number.")
        return None

    if amount <= 0:
        print("Amount must be greater than 0.")
        return None

    return amount


def verify_pin(customer):
    pin = input("Enter your 4 digit pin number: ").strip()
    if pin != customer["pin"]:
        print("Wrong pin number")
        return False
    return True


def withdraw(customer):
    amount = read_amount("Enter withdraw amount: ")
    if amount is None:
        return

    balance = customer["balance"]
    if amount > balance:
        print("Insufficient balance")
        return

    if not verify_pin(customer):
        return

    new_balance = balance - amount
    update_balance(customer["account_number"], new_balance)
    print("Money is debited")
    print(f"Available balance: {new_balance:.2f}")


def deposit(customer):
    amount = read_amount("Enter deposit amount: ")
    if amount is None:
        return

    new_balance = customer["balance"] + amount
    update_balance(customer["account_number"], new_balance)
    print("Money is credited")
    print(f"Available balance: {new_balance:.2f}")


def show_transaction_menu(customer):
    while True:
        latest_customer = get_customer(customer["account_number"])

        print("\nPlease select an option:")
        print("1. Deposit")
        print("2. Withdraw")
        print("3. Exit")

        choice = input("Enter your choice: ").strip()

        if choice == "1":
            deposit(latest_customer)
        elif choice == "2":
            withdraw(latest_customer)
        elif choice == "3":
            print("Transaction ended. Thank you for using SBI ATM.")
            break
        else:
            print("Invalid choice. Please select 1, 2, or 3.")


def run_atm():
    setup_database()

    print("Welcome to SBI ATM, please insert your debit card")
    inserted_card = input("Did you insert card? Enter Yes or No: ").strip().lower()

    if inserted_card != "yes":
        print("Transaction cancelled. Please insert your debit card to continue.")
        return

    account_number = input("Enter your 6 digit account number: ").strip()
    customer = get_customer(account_number)

    if customer is None:
        print("Account number does not exist. Transaction cancelled.")
        return

    print(f"Welcome {customer['account_holder_name']}")
    show_transaction_menu(customer)


if __name__ == "__main__":
    run_atm()
