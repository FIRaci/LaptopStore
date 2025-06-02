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
    
    # Make list of all payment IDs
    payment_ids = list(range(1, 7165))  # All payment IDs from 1 to 7164
    random.shuffle(payment_ids)  # Shuffle to distribute randomly
    
    sql_statements = []
    batch_size = 100
    current_batch = []
    used_order_ids = set()  # Track used order IDs
    order_id = 1  # Start with order_id 1
    
    # Generate orders ensuring unique order_ids
    for i in range(num_records):
        while order_id in used_order_ids:
            order_id += 1
        
        used_order_ids.add(order_id)
        
        net_amount = generate_random_amount()
        tax = round(net_amount * decimal.Decimal('0.1'), 2)
        total = net_amount + tax
        
        # First 7164 orders get unique payment_ids, rest get 80% existing payment or NULL
        payment_id = payment_ids[i] if i < len(payment_ids) else (
            'NULL' if random.random() < 0.2 else str(random.choice(payment_ids))
        )
        
        order = (
            order_id,  # Guaranteed unique order_id
            random.choice(active_customers),  # customer_id
            payment_id,  # payment_id based on logic above
            generate_random_date(),
            random.choice(ORDER_STATUS),
            net_amount,
            tax,
            total,
            f"Shipping Address {order_id}",
            f"Order {order_id} notes"
        )
        
        current_batch.append(
            f"({order[0]}, {order[1]}, {order[2]}, '{order[3]}', '{order[4]}', {order[5]}, {order[6]}, {order[7]}, '{order[8]}', '{order[9]}')"
        )
        
        if len(current_batch) == batch_size:
            sql_statements.append(
                "INSERT INTO ORDERS (order_id, customer_id, payment_id, order_date, status, net_amount, tax, total_amount, shipping_address, notes) VALUES\n" +
                ",\n".join(current_batch) + ";"
            )
            current_batch = []

    # Handle any remaining orders in the last batch
    if current_batch:
        sql_statements.append(
            "INSERT INTO ORDERS (order_id, customer_id, payment_id, order_date, status, net_amount, tax, total_amount, shipping_address, notes) VALUES\n" +
            ",\n".join(current_batch) + ";"
        )

    with open('orders.sql', 'w', encoding='utf-8') as f:
        f.write("-- Generated Orders Data\n")
        f.write(f"-- Note: {len(inactive_customers)} customers have no orders\n")
        f.write(f"-- Inactive customer IDs: {sorted(list(inactive_customers))}\n\n")
        for statement in sql_statements:
            f.write("\n" + statement + "\n")

if __name__ == "__main__":
    generate_orders(13511)
    print("Generated 13511 order records in orders.sql")
