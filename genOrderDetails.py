import random

def generate_order_details():
    sql_statements = []
    batch_size = 100
    current_batch = []
    current_id = 1

    # For each order, generate 1-4 order details
    for order_id in range(1, 13512):
        num_details = random.randint(1, 4)
        
        for _ in range(num_details):
            order_detail = (
                current_id,  # od_id
                order_id,  # order_id
                random.randint(1, 120),  # product_id
                random.randint(1, 5),  # quantity
                round(random.uniform(100, 3000), 2)  # unit_price
            )
            
            current_batch.append(
                f"({order_detail[0]}, {order_detail[1]}, {order_detail[2]}, {order_detail[3]}, {order_detail[4]})"
            )
            current_id += 1
            
            if len(current_batch) >= batch_size:
                sql_statements.append(
                    "INSERT INTO ORDER_DETAILS (od_id, order_id, product_id, quantity, unit_price) VALUES\n" +
                    ",\n".join(current_batch) + ";"
                )
                current_batch = []

    # Add any remaining records in the last batch
    if current_batch:
        sql_statements.append(
            "INSERT INTO ORDER_DETAILS (od_id, order_id, product_id, quantity, unit_price) VALUES\n" +
            ",\n".join(current_batch) + ";"
        )

    # Write to file
    with open('order_details.sql', 'w', encoding='utf-8') as f:
        f.write("-- Generated Order Details Data\n")
        for statement in sql_statements:
            f.write("\n" + statement + "\n")

    return current_id - 1  # Return total number of records generated

if __name__ == "__main__":
    total_records = generate_order_details()
    print(f"Generated {total_records} order detail records in order_details.sql")
