import tkinter as tk
from tkinter import messagebox

from atm import get_customer, setup_database, update_balance


class ATMApp(tk.Tk):
    def __init__(self):
        super().__init__()
        setup_database()

        self.title("Bank: Money Transaction")
        self.geometry("520x420")
        self.minsize(480, 380)
        self.configure(bg="#f4f7fb")

        self.customer = None
        self.card_inserted = tk.BooleanVar(value=False)
        self.account_number = tk.StringVar()
        self.amount = tk.StringVar()
        self.pin = tk.StringVar()
        self.status = tk.StringVar(value="Welcome to SBI ATM, please insert your debit card")

        self._build_layout()

    def _build_layout(self):
        header = tk.Frame(self, bg="#164f9f", padx=20, pady=18)
        header.pack(fill="x")

        tk.Label(
            header,
            text="SBI ATM",
            bg="#164f9f",
            fg="white",
            font=("Segoe UI", 22, "bold"),
        ).pack(anchor="w")
        tk.Label(
            header,
            text="Bank: Money Transaction",
            bg="#164f9f",
            fg="#dbeafe",
            font=("Segoe UI", 11),
        ).pack(anchor="w")

        body = tk.Frame(self, bg="#f4f7fb", padx=24, pady=22)
        body.pack(fill="both", expand=True)

        tk.Label(
            body,
            textvariable=self.status,
            bg="#f4f7fb",
            fg="#172033",
            wraplength=440,
            justify="left",
            font=("Segoe UI", 13, "bold"),
        ).pack(anchor="w", pady=(0, 16))

        self.card_frame = tk.Frame(body, bg="#f4f7fb")
        self.card_frame.pack(fill="x", pady=(0, 16))

        tk.Checkbutton(
            self.card_frame,
            text="I inserted my debit card",
            variable=self.card_inserted,
            command=self._toggle_card,
            bg="#f4f7fb",
            fg="#172033",
            activebackground="#f4f7fb",
            font=("Segoe UI", 11),
        ).pack(anchor="w")

        self.account_frame = tk.Frame(body, bg="#f4f7fb")
        self.account_frame.pack(fill="x", pady=(0, 16))

        tk.Label(
            self.account_frame,
            text="Account Number",
            bg="#f4f7fb",
            fg="#3b465c",
            font=("Segoe UI", 10, "bold"),
        ).pack(anchor="w")
        tk.Entry(
            self.account_frame,
            textvariable=self.account_number,
            font=("Segoe UI", 12),
            width=18,
        ).pack(anchor="w", pady=(4, 8))
        tk.Button(
            self.account_frame,
            text="Continue",
            command=self._login,
            bg="#164f9f",
            fg="white",
            activebackground="#0f3d7a",
            activeforeground="white",
            font=("Segoe UI", 10, "bold"),
            padx=16,
            pady=6,
        ).pack(anchor="w")

        self.transaction_frame = tk.Frame(body, bg="#f4f7fb")

        self.balance_label = tk.Label(
            self.transaction_frame,
            text="",
            bg="#f4f7fb",
            fg="#172033",
            font=("Segoe UI", 12),
        )
        self.balance_label.pack(anchor="w", pady=(0, 14))

        amount_row = tk.Frame(self.transaction_frame, bg="#f4f7fb")
        amount_row.pack(fill="x", pady=(0, 10))
        tk.Label(
            amount_row,
            text="Amount",
            bg="#f4f7fb",
            fg="#3b465c",
            font=("Segoe UI", 10, "bold"),
        ).pack(anchor="w")
        tk.Entry(amount_row, textvariable=self.amount, font=("Segoe UI", 12), width=18).pack(
            anchor="w", pady=(4, 0)
        )

        pin_row = tk.Frame(self.transaction_frame, bg="#f4f7fb")
        pin_row.pack(fill="x", pady=(0, 16))
        tk.Label(
            pin_row,
            text="4 Digit PIN",
            bg="#f4f7fb",
            fg="#3b465c",
            font=("Segoe UI", 10, "bold"),
        ).pack(anchor="w")
        tk.Entry(
            pin_row,
            textvariable=self.pin,
            show="*",
            font=("Segoe UI", 12),
            width=18,
        ).pack(anchor="w", pady=(4, 0))

        buttons = tk.Frame(self.transaction_frame, bg="#f4f7fb")
        buttons.pack(fill="x")

        self._action_button(buttons, "Deposit", self._deposit, "#0f766e").pack(
            side="left", padx=(0, 8)
        )
        self._action_button(buttons, "Withdraw", self._withdraw, "#164f9f").pack(
            side="left", padx=(0, 8)
        )
        self._action_button(buttons, "Exit", self._exit_transaction, "#6b7280").pack(
            side="left"
        )

        self.account_frame.pack_forget()

    def _action_button(self, parent, text, command, color):
        return tk.Button(
            parent,
            text=text,
            command=command,
            bg=color,
            fg="white",
            activebackground=color,
            activeforeground="white",
            font=("Segoe UI", 10, "bold"),
            padx=14,
            pady=7,
        )

    def _toggle_card(self):
        if self.card_inserted.get():
            self.status.set("Debit card inserted. Please enter your account number.")
            self.account_frame.pack(fill="x", pady=(0, 16), before=self.transaction_frame)
        else:
            self._reset_transaction()
            self.status.set("Transaction cancelled. Please insert your debit card to continue.")
            self.account_frame.pack_forget()

    def _login(self):
        account_number = self.account_number.get().strip()
        if len(account_number) != 6 or not account_number.isdigit():
            messagebox.showerror("Invalid Account", "Please enter a valid 6 digit account number.")
            return

        customer = get_customer(account_number)
        if customer is None:
            messagebox.showerror("Account Not Found", "Account number does not exist.")
            self._exit_transaction()
            return

        self.customer = customer
        self.status.set(f"Welcome {customer['account_holder_name']}. Select a transaction.")
        self.account_frame.pack_forget()
        self.transaction_frame.pack(fill="x", pady=(0, 16))
        self._refresh_balance()

    def _read_amount(self):
        try:
            value = float(self.amount.get().strip())
        except ValueError:
            messagebox.showerror("Invalid Amount", "Please enter a valid amount.")
            return None

        if value <= 0:
            messagebox.showerror("Invalid Amount", "Amount must be greater than 0.")
            return None

        return value

    def _refresh_customer(self):
        if self.customer is not None:
            self.customer = get_customer(self.customer["account_number"])

    def _refresh_balance(self):
        self._refresh_customer()
        if self.customer is not None:
            self.balance_label.config(text=f"Available balance: {self.customer['balance']:.2f}")

    def _deposit(self):
        if self.customer is None:
            return

        amount = self._read_amount()
        if amount is None:
            return

        new_balance = self.customer["balance"] + amount
        update_balance(self.customer["account_number"], new_balance)
        self.amount.set("")
        self.pin.set("")
        self.status.set("Money is credited")
        self._refresh_balance()

    def _withdraw(self):
        if self.customer is None:
            return

        amount = self._read_amount()
        if amount is None:
            return

        if amount > self.customer["balance"]:
            messagebox.showerror("Insufficient Balance", "Insufficient balance")
            return

        if self.pin.get().strip() != self.customer["pin"]:
            messagebox.showerror("Wrong PIN", "Wrong pin number")
            return

        new_balance = self.customer["balance"] - amount
        update_balance(self.customer["account_number"], new_balance)
        self.amount.set("")
        self.pin.set("")
        self.status.set("Money is debited")
        self._refresh_balance()

    def _reset_transaction(self):
        self.customer = None
        self.account_number.set("")
        self.amount.set("")
        self.pin.set("")
        self.transaction_frame.pack_forget()

    def _exit_transaction(self):
        self._reset_transaction()
        self.card_inserted.set(False)
        self.account_frame.pack_forget()
        self.status.set("Transaction ended. Thank you for using SBI ATM.")


if __name__ == "__main__":
    app = ATMApp()
    app.mainloop()
