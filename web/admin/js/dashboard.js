import './admin-auth.js';

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD"
});

async function loadDashboard() {
  const token = localStorage.getItem("auth_token");
  if (!token) return;

  try {
    const resOrders = await fetch("/api/orders", { headers: { "Authorization": `Bearer ${token}` } });
    if (resOrders.ok) {
      const orders = await resOrders.json();
      
      const paidOrders = orders.filter(o => o.status !== "CANCELLED");
      document.getElementById("revTotalOrders").textContent = orders.length;
      document.getElementById("revTotalAmount").textContent = currency.format(paidOrders.reduce((sum, o) => sum + o.totalAmount, 0));
    }
  } catch(e) { console.error(e) }
}

document.addEventListener("DOMContentLoaded", loadDashboard);
