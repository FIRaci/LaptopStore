import random
from datetime import datetime, timedelta
import decimal

# Constants
PAYMENT_METHODS = ["Cash", "Credit Cash", "Bank Transfer", "Other"]
PAYMENT_STATUS = ["Pending", "Paid", "Failed", "Refunded", "Cancelled"]

def generate_random_date():
    start_date = datetime(2024, 1, 1)
    end_date = datetime(2025, 6, 2)
    time_between_dates = end_date - start_date
    days_between_dates = time_between_dates.days
    random_days = random.randrange(days_between_dates)
    random_date = start_date + timedelta(days=random_days)
    random_hour = random.randint(8, 20)
    random_minute = random.randint(0, 59)
    return random_date.replace(hour=random_hour, minute=random_minute)

def generate_random_amount():
    return round(decimal.Decimal(random.uniform(50, 5000)), 2)

def generate_note(payment_id):
    return f"Order {payment_id} payment transaction"

def generate_payments(num_records):
    sql_statements = []
    batch_size = 100
    current_batch = []

    for i in range(1, num_records + 1):
        payment = (
            i,  # payment_id
            random.randint(1, 60),  # employee_id
            generate_random_date().strftime("%Y-%m-%d %H:%M:%S"),  # payment_date
            random.choice(PAYMENT_METHODS),  # payment_method
            generate_random_amount(),  # total_amount
            random.choice(PAYMENT_STATUS),  # status
            generate_note(i)  # notes
        )
        
        current_batch.append(f"({payment[0]}, {payment[1]}, '{payment[2]}', '{payment[3]}', {payment[4]}, '{payment[5]}', '{payment[6]}')")
        
        if len(current_batch) == batch_size or i == num_records:
            sql_statements.append(
                "INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES\n" +
                ",\n".join(current_batch) + ";"
            )
            current_batch = []

    with open('payments.sql', 'w', encoding='utf-8') as f:
        f.write("-- Generated Payments Data\n")
        for statement in sql_statements:
            f.write("\n" + statement + "\n")

if __name__ == "__main__":
    generate_payments(7164)
    print("Generated 7164 payment records in payments.sql")
