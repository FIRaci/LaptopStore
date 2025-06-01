-- Initial PAYMENTS Data
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(1, 2, '2024-03-01 10:00:00', 'Credit Card', 1958.99, 'Completed', 'Payment for Order #1, Customer John Doe'),
(2, 4, '2024-03-02 11:15:00', 'PayPal', 1131.97, 'Completed', 'Payment for Order #2, Customer Jane Smith'),
(3, 6, '2024-03-03 14:30:00', 'Debit Card', 1700.00, 'Completed', 'Payment for Order #3'),
(4, 8, '2024-03-04 09:00:00', 'Credit Card', 102.99, 'Pending', 'Awaiting confirmation for Order #4'),
(5, 10, '2024-03-05 16:00:00', 'Bank Transfer', 1500.00, 'Completed', 'Order #5'),
(6, 2, '2024-03-06 10:30:00', 'PayPal', 772.49, 'Completed', 'Order #6'),
(7, 4, '2024-03-07 12:00:00', 'Credit Card', 3099.99, 'Failed', 'Payment declined for Order #7'),
(8, 6, '2024-03-08 17:45:00', 'Debit Card', 597.59, 'Completed', 'Order #8'),
(9, 8, '2024-03-09 08:20:00', 'Credit Card', 1647.99, 'Completed', 'Order #9'),
(10, 1, '2024-03-10 13:00:00', 'Cash', 1028.99, 'Completed', 'Payment for Order #10 at store');

-- Additional PAYMENTS Data (Payments 11-20)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(11, 3, '2024-03-16 10:00:00', 'Credit Card', 2749.99, 'Completed', 'Order #11 payment'),
(12, 5, '2024-03-16 11:30:00', 'Bank Transfer', 2374.89, 'Completed', 'Order #12 payment'),
(13, 7, '2024-03-17 09:15:00', 'PayPal', 274.99, 'Completed', 'Order #13 software purchase'),
(14, 9, '2024-03-17 14:00:00', 'Credit Card', 434.48, 'Pending', 'Order #14 accessories, verify stock'),
(15, 11, '2024-03-18 16:45:00', 'Debit Card', 768.90, 'Completed', 'Order #15 refurbished unit'),
(16, 16, '2024-03-20 10:30:00', 'Credit Card', 2419.99, 'Completed', 'Order #16, Alienware laptop'),
(17, 18, '2024-03-20 12:00:00', 'PayPal', 1263.89, 'Completed', 'Order #17, HP Chromebook + Powerbank'),
(18, 20, '2024-03-21 09:45:00', 'Debit Card', 658.90, 'Completed', 'Order #18, Mac mini'),
(19, 1, '2024-03-21 15:15:00', 'Credit Card', 406.97, 'Processing', 'Order #19, PC Peripherals'),
(20, 3, '2024-03-22 11:00:00', 'Bank Transfer', 1429.99, 'Completed', 'Order #20, Samsung G9 Monitor');

-- Additional PAYMENTS Data (Payments 21-30)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(21, 5, '2024-03-23 10:00:00', 'Credit Card', 163.90, 'Completed', 'Order #21, Windows 11 Pro Key'),
(22, 7, '2024-03-23 14:30:00', 'PayPal', 2264.85, 'Completed', 'Order #22, MacBook Pro M3 + Sleeve'),
(23, 9, '2024-03-24 09:00:00', 'Debit Card', 823.90, 'Completed', 'Order #23, Refurbished EliteBook'),
(24, 17, '2024-03-24 16:20:00', 'Credit Card', 516.98, 'Pending', 'Order #24, Monitor + RAM, stock check'),
(25, 19, '2024-03-25 10:50:00', 'Bank Transfer', 186.99, 'Completed', 'Order #25, Belkin Dock'),
(26, 21, '2024-03-26 10:00:00', 'Credit Card', 4289.99, 'Completed', 'Order #26, High-end ASUS Gaming Laptop'),
(27, 23, '2024-03-26 11:45:00', 'Bank Transfer', 3299.98, 'Completed', 'Order #27, Surface Studio 2 + MX Combo'),
(28, 25, '2024-03-27 09:30:00', 'PayPal', 1428.90, 'Completed', 'Order #28, iMac M3'),
(29, 2, '2024-03-27 14:20:00', 'Credit Card', 1132.80, 'Processing', 'Order #29, Software and accessories, fraud check'),
(30, 4, '2024-03-28 10:10:00', 'Debit Card', 1978.90, 'Completed', 'Order #30, Used MacBook Pro 16');

-- Additional PAYMENTS Data (Payments 31-40)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(31, 6, '2024-03-29 11:00:00', 'Credit Card', 604.99, 'Completed', 'Order #31, BenQ Designer Monitor'),
(32, 8, '2024-03-29 15:00:00', 'PayPal', 1186.89, 'Completed', 'Order #32, Lenovo Yoga + OWC Hub'),
(33, 20, '2024-03-30 09:00:00', 'Debit Card', 274.99, 'Completed', 'Order #33, Razer Keyboard'),
(34, 22, '2024-03-30 16:30:00', 'Credit Card', 1120.89, 'Pending', 'Order #34, ThinkBook + SSD, partial shipment possible'),
(35, 24, '2024-03-31 10:00:00', 'Bank Transfer', 38.49, 'Completed', 'Order #35, Dell Sleeve'),
(36, 31, '2024-04-01 09:15:00', 'Credit Card', 1098.90, 'Completed', 'Order #36, Framework Laptop DIY'),
(37, 33, '2024-04-01 10:50:00', 'PayPal', 2032.79, 'Completed', 'Order #37, LG Gram + Sony Headphones'),
(38, 35, '2024-04-02 11:30:00', 'Debit Card', 164.99, 'Completed', 'Order #38, Stream Deck'),
(39, 10, '2024-04-02 14:00:00', 'Credit Card', 318.93, 'Processing', 'Order #39, PC Components, high value'),
(40, 12, '2024-04-03 09:00:00', 'Bank Transfer', 988.90, 'Completed', 'Order #40, Used ThinkPad T14');

-- Additional PAYMENTS Data (Payments 41-50)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(41, 14, '2024-04-04 10:20:00', 'Credit Card', 142.95, 'Completed', 'Order #41, Ergonomic Desktop Set'),
(42, 16, '2024-04-04 15:00:00', 'PayPal', 274.98, 'Completed', 'Order #42, Razer Mouse + Satechi Hub'),
(43, 36, '2024-04-05 09:00:00', 'Credit Card', 1539.99, 'Completed', 'Order #43, ASUS Zenbook S 13 OLED'),
(44, 38, '2024-04-05 10:30:00', 'Bank Transfer', 3331.89, 'Completed', 'Order #44, Dell XPS 17 + Bose Headphones'),
(45, 40, '2024-04-06 11:15:00', 'PayPal', 439.99, 'Completed', 'Order #45, GoPro HERO12 Black'),
(46, 15, '2024-04-06 14:45:00', 'Credit Card', 889.85, 'Processing', 'Order #46, High-performance CPU and Cooler'),
(47, 17, '2024-04-07 09:30:00', 'Debit Card', 878.90, 'Completed', 'Order #47, Refurbished MacBook Air M1'),
(48, 19, '2024-04-08 10:00:00', 'Credit Card', 208.99, 'Completed', 'Order #48, SteelSeries Gaming Keyboard'),
(49, 21, '2024-04-08 15:20:00', 'PayPal', 504.89, 'Completed', 'Order #49, Logitech Mouse + Anker Dock'),
(50, 41, '2024-04-09 09:00:00', 'Credit Card', 5499.99, 'Completed', 'Order #50, HP Spectre Foldable PC, High Value');

-- Additional PAYMENTS Data (Payments 51-60)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(51, 43, '2024-04-09 10:45:00', 'Bank Transfer', 2913.89, 'Completed', 'Order #51, Samsung Galaxy Book3 Ultra + Shure Mic'),
(52, 45, '2024-04-10 11:30:00', 'PayPal', 329.99, 'Completed', 'Order #52, WD My Passport SSD 4TB'),
(53, 20, '2024-04-10 14:15:00', 'Credit Card', 2527.80, 'Processing', 'Order #53, RTX 4090 + Ryzen 7950X3D, Verify Stock'),
(54, 22, '2024-04-11 09:45:00', 'Debit Card', 834.90, 'Completed', 'Order #54, Refurbished Surface Pro 8'),
(55, 24, '2024-04-12 10:30:00', 'Credit Card', 164.99, 'Completed', 'Order #55, Everki Atlas Backpack'),
(56, 26, '2024-04-12 15:00:00', 'PayPal', 614.89, 'Completed', 'Order #56, Corsair K100 Air + Plugable Dock'),
(57, 46, '2024-04-13 09:15:00', 'Credit Card', 1869.99, 'Completed', 'Order #57, Lenovo Legion Slim 7i'),
(58, 48, '2024-04-13 10:50:00', 'Bank Transfer', 5146.85, 'Completed', 'Order #58, MacBook Pro 16 M3 Max + Sennheiser Headphones, High Value'),
(59, 50, '2024-04-14 11:30:00', 'PayPal', 438.90, 'Completed', 'Order #59, DJI Osmo Action 4 Combo'),
(60, 25, '2024-04-14 14:00:00', 'Credit Card', 2406.80, 'Processing', 'Order #60, High-end PC Components, Address Verification');

-- Additional PAYMENTS Data (Payments 61-70)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(61, 27, '2024-04-15 09:00:00', 'Debit Card', 933.90, 'Completed', 'Order #61, Refurbished Dell Latitude 5430'),
(62, 29, '2024-04-16 10:20:00', 'Credit Card', 153.95, 'Completed', 'Order #62, NuPhy Air75 V2 Keyboard'),
(63, 31, '2024-04-16 15:00:00', 'PayPal', 450.98, 'Completed', 'Order #63, Logitech Mouse + Kensington Dock'),
(69, 51, '2024-04-20 09:30:00', 'Credit Card', 5498.90, 'Completed', 'Order #69, MSI Titan GT77 HX, Expedited Shipping'),
(70, 53, '2024-04-20 11:00:00', 'Bank Transfer', 5443.89, 'Completed', 'Order #70, Dell Precision Workstation + Headset'),
(71, 55, '2024-04-21 10:15:00', 'PayPal', 494.99, 'Completed', 'Order #71, Insta360 X3 Action Camera'),
(72, 30, '2024-04-21 14:45:00', 'Credit Card', 989.98, 'Processing', 'Order #72, Synology NAS + WD SSD, Verify Address'),
(73, 32, '2024-04-22 09:00:00', 'Debit Card', 1208.90, 'Completed', 'Order #73, Refurbished Lenovo Yoga 9i'),
(74, 34, '2024-04-23 10:30:00', 'Credit Card', 988.90, 'Completed', 'Order #74, Herman Miller Aeron Chair'),
(75, 36, '2024-04-23 15:15:00', 'PayPal', 1373.89, 'Completed', 'Order #75, AMD GPU + CalDigit Hub');

-- Final PAYMENTS Data (Payments 76-82)
INSERT INTO PAYMENTS (payment_id, employee_id, payment_date, payment_method, total_amount, status, notes) VALUES
(76, 56, '2024-04-24 09:00:00', 'Credit Card', 879.99, 'Completed', 'Order #76, ASUS ROG Ally X Handheld'),
(77, 58, '2024-04-24 10:30:00', 'Bank Transfer', 1758.89, 'Completed', 'Order #77, Framework Laptop 16 + DualSense Edge'),
(78, 60, '2024-04-25 11:15:00', 'PayPal', 3848.90, 'Completed', 'Order #78, Apple Vision Pro, High Value Item'),
(79, 35, '2024-04-25 14:00:00', 'Credit Card', 1209.98, 'Processing', 'Order #79, Dual NAS units, confirm use case'),
(80, 37, '2024-04-26 09:45:00', 'Debit Card', 1428.90, 'Completed', 'Order #80, Refurbished HP Spectre x360 16'),
(81, 39, '2024-04-27 10:30:00', 'Credit Card', 218.90, 'Completed', 'Order #81, Das Keyboard 6 Professional'),
(82, 41, '2024-04-27 15:00:00', 'PayPal', 999.89, 'Completed', 'Order #82, NVIDIA GPU + Anker Charger');
