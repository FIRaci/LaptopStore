import random
from datetime import datetime, timedelta
import decimal

# Constants
ORDER_STATUS = ["Pending", "Processing", "Shipped", "Delivered", "Cancelled", "Returned"]

def generate_random_date():
    start_date = datetime(2024, 1, 1).date()
    end_date = datetime(2025, 6, 2).date()
    days_between = (end_date - start_date).days
    random_days = random.randint(0, days_between)
    return start_date + timedelta(days=random_days)

def get_inactive_customers(total_customers=1013, inactive_count=267):
    all_customers = list(range(1, total_customers + 1))
    return set(random.sample(all_customers, inactive_count))

def generate_random_amount():
    return round(decimal.Decimal(random.uniform(100, 10000)), 2)

def generate_orders(num_records):
    inactive_customers = get_inactive_customers()
    active_customers = [c for c in range(1, 1014) if c not in inactive_customers]
    payment_range = list(range(1, 7165)) + [None] * 100  # Adding None values for null payments

    sql_statements = []
    batch_size = 100
    current_batch = []

    for i in range(1, num_records + 1):
        net_amount = generate_random_amount()
        tax = round(net_amount * decimal.Decimal('0.1'), 2)  # 10% tax
        total = net_amount + tax

        order = (
            i,  # order_id
            random.choice(active_customers),  # customer_id
            random.choice(payment_range),  # payment_id (can be NULL)
            generate_random_date(),  # order_date
            random.choice(ORDER_STATUS),  # status
            net_amount,  # net_amount
            tax,  # tax
            total,  # total_amount
            f"Shipping Address {i}",  # shipping_address
            f"Order {i} notes"  # notes
        )
        
        payment_value = 'NULL' if order[2] is None else str(order[2])
        current_batch.append(
            f"({order[0]}, {order[1]}, {payment_value}, '{order[3]}', '{order[4]}', {order[5]}, {order[6]}, {order[7]}, '{order[8]}', '{order[9]}')"
        )
        
        if len(current_batch) == batch_size or i == num_records:
            sql_statements.append(
                "INSERT INTO ORDERS (order_id, customer_id, payment_id, order_date, status, net_amount, tax, total_amount, shipping_address, notes) VALUES\n" +
                ",\n".join(current_batch) + ";"
            )
            current_batch = []

    with open('orders.sql', 'w', encoding='utf-8') as f:
        f.write("-- Generated Orders Data\n")
        f.write(f"-- Note: {len(inactive_customers)} customers have no orders\n")
        f.write(f"-- Inactive customer IDs: {sorted(list(inactive_customers))}\n\n")
        for statement in sql_statements:
            f.write("\n" + statement + "\n")

if __name__ == "__main__":
    generate_orders(13511)
    print("Generated 13511 order records in orders.sql")
