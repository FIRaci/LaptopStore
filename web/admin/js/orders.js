import { showToast } from './admin-auth.js';

const tbody = document.querySelector("#ordersTable tbody");
const searchInput = document.getElementById("orderSearch");
let orders = [];

const currency = new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" });

async function fetchOrders() {
  const token = localStorage.getItem("auth_token");
  try {
    const res = await fetch("/api/orders", { headers: { "Authorization": `Bearer ${token}` } });
    if (!res.ok) throw new Error("Failed to fetch");
    orders = await res.json();
    renderTable();
  } catch(e) { showToast("Error loading orders"); }
}

function renderTable() {
  const query = searchInput.value.toLowerCase();
  const filtered = orders.filter(o => o.user.email.toLowerCase().includes(query));

  tbody.innerHTML = filtered.map(o => `
    <tr>
      <td>#${o.id}</td>
      <td>${o.user.email}</td>
      <td>${currency.format(o.totalAmount)}</td>
      <td>${new Date(o.createdAt).toLocaleDateString()}</td>
      <td>
        <select class="inline-input order-status-select" data-id="${o.id}" style="width: auto;">
          <option value="PENDING" ${o.status==='PENDING'?'selected':''}>PENDING</option>
          <option value="SHIPPED" ${o.status==='SHIPPED'?'selected':''}>SHIPPED</option>
          <option value="DELIVERED" ${o.status==='DELIVERED'?'selected':''}>DELIVERED</option>
          <option value="CANCELED" ${o.status==='CANCELED'?'selected':''}>CANCELED</option>
        </select>
      </td>
      <td>
        <button class="btn btn-ghost update-order-btn" data-id="${o.id}" style="padding:4px 8px; color:var(--accent);">Update</button>
      </td>
    </tr>
  `).join("");

  bindRowActions();
}

function bindRowActions() {
  document.querySelectorAll(".update-order-btn").forEach(btn => {
    btn.addEventListener("click", async () => {
      const id = btn.dataset.id;
      const select = document.querySelector(`.order-status-select[data-id="${id}"]`);
      const newStatus = select.value;
      const token = localStorage.getItem("auth_token");
      try {
        const res = await fetch(`/api/orders/${id}/status`, {
          method: "PUT",
          headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
          body: JSON.stringify({ status: newStatus })
        });
        if(!res.ok) throw new Error("Update failed");
        showToast(`Order #${id} status updated to ${newStatus}`);
        fetchOrders();
      } catch(e) { showToast(e.message); }
    });
  });
}

searchInput.addEventListener("input", renderTable);
document.addEventListener("DOMContentLoaded", fetchOrders);
