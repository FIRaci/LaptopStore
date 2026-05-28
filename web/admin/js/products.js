import { showToast } from './admin-auth.js';

const tbody = document.querySelector("#productsTable tbody");
const searchInput = document.getElementById("productSearch");
let products = [];
let editingId = null;

async function fetchProducts() {
  try {
    const res = await fetch("/api/products?sort=newest");
    if (!res.ok) throw new Error("Failed to fetch");
    products = await res.json();
    renderTable();
  } catch(e) {
    showToast("Error loading products");
  }
}

function renderTable() {
  const query = searchInput.value.toLowerCase();
  const filtered = products.filter(p => 
    p.name.toLowerCase().includes(query) || p.sku.toLowerCase().includes(query)
  );

  tbody.innerHTML = filtered.map(p => {
    const isEditing = editingId === p.id;
    if (isEditing) {
      return `
        <tr>
          <td>${p.id}</td>
          <td><input type="text" class="inline-input" id="edit-sku-${p.id}" value="${p.sku}" /></td>
          <td><input type="text" class="inline-input" id="edit-name-${p.id}" value="${p.name}" /></td>
          <td>
            <select class="inline-input" id="edit-type-${p.id}">
              <option value="LAPTOP" ${p.type === 'LAPTOP' ? 'selected' : ''}>LAPTOP</option>
              <option value="GEAR" ${p.type === 'GEAR' ? 'selected' : ''}>GEAR</option>
              <option value="COMPONENT" ${p.type === 'COMPONENT' ? 'selected' : ''}>COMPONENT</option>
            </select>
          </td>
          <td><input type="number" step="0.01" class="inline-input" id="edit-price-${p.id}" value="${p.price}" style="width: 80px;" /></td>
          <td><input type="number" class="inline-input" id="edit-stock-${p.id}" value="${p.stock}" style="width: 60px;" /></td>
          <td>
            <button class="btn btn-ghost save-btn" data-id="${p.id}" style="padding:4px 8px; color:var(--accent);">Save</button>
            <button class="btn btn-ghost cancel-btn" data-id="${p.id}" style="padding:4px 8px;">Cancel</button>
          </td>
        </tr>
      `;
    }

    return `
      <tr ondblclick="window.startEdit(${p.id})">
        <td>${p.id}</td>
        <td>${p.sku}</td>
        <td>${p.name}</td>
        <td>${p.type}</td>
        <td>$${Number(p.price).toFixed(2)}</td>
        <td>${p.stock}</td>
        <td>
          <button class="btn btn-ghost edit-btn" data-id="${p.id}" style="padding:4px 8px;">Edit</button>
          <button class="btn btn-ghost delete-btn" data-id="${p.id}" style="padding:4px 8px; color:#ff4d4f;">Del</button>
        </td>
      </tr>
    `;
  }).join("");

  bindRowActions();
}

// Expose startEdit for double click
window.startEdit = (id) => {
  editingId = id;
  renderTable();
};

function bindRowActions() {
  document.querySelectorAll(".edit-btn").forEach(b => {
    b.addEventListener("click", () => {
      editingId = Number(b.dataset.id);
      renderTable();
    });
  });

  document.querySelectorAll(".cancel-btn").forEach(b => {
    b.addEventListener("click", () => {
      editingId = null;
      renderTable();
    });
  });

  document.querySelectorAll(".delete-btn").forEach(b => {
    b.addEventListener("click", async () => {
      if(!confirm("Delete this product?")) return;
      const id = Number(b.dataset.id);
      const token = localStorage.getItem("auth_token");
      try {
        const res = await fetch(`/api/products/${id}`, {
          method: "DELETE",
          headers: { "Authorization": `Bearer ${token}` }
        });
        if (!res.ok) throw new Error("Delete failed");
        showToast("Product deleted");
        await fetchProducts();
      } catch(e) { showToast(e.message); }
    });
  });

  document.querySelectorAll(".save-btn").forEach(b => {
    b.addEventListener("click", async () => {
      const id = Number(b.dataset.id);
      const token = localStorage.getItem("auth_token");
      const p = products.find(x => x.id === id);
      const payload = {
        name: document.getElementById(`edit-name-${id}`).value,
        sku: document.getElementById(`edit-sku-${id}`).value,
        type: document.getElementById(`edit-type-${id}`).value,
        price: Number(document.getElementById(`edit-price-${id}`).value),
        stock: Number(document.getElementById(`edit-stock-${id}`).value),
        // keep old fields
        brand: p.brand,
        description: p.description,
        imageUrl: p.imageUrl
      };

      try {
        const res = await fetch(`/api/products/${id}`, {
          method: "PUT",
          headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
          body: JSON.stringify(payload)
        });
        if (!res.ok) {
           const err = await res.json();
           throw new Error(err.error || "Save failed");
        }
        showToast("Product updated inline!");
        editingId = null;
        await fetchProducts();
      } catch(e) { showToast(e.message); }
    });
  });
}

searchInput.addEventListener("input", renderTable);

// Add Modal Logic
const addModal = document.getElementById("addModal");
const addModalClose = document.getElementById("addModalClose");
const addForm = document.getElementById("addForm");

document.getElementById("addProductBtn").addEventListener("click", () => addModal.classList.add("open"));
addModalClose.addEventListener("click", () => addModal.classList.remove("open"));
addModal.addEventListener("click", (e) => { if (e.target === addModal) addModal.classList.remove("open"); });

addForm.addEventListener("submit", async (e) => {
  e.preventDefault();
  const token = localStorage.getItem("auth_token");
  const payload = {
    name: document.getElementById("addName").value,
    sku: document.getElementById("addSku").value,
    brand: document.getElementById("addBrand").value,
    type: document.getElementById("addType").value,
    price: Number(document.getElementById("addPrice").value),
    stock: Number(document.getElementById("addStock").value),
    description: document.getElementById("addDesc").value,
    imageUrl: document.getElementById("addImage").value
  };

  try {
    const res = await fetch("/api/products", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Authorization": `Bearer ${token}` },
      body: JSON.stringify(payload)
    });
    if (!res.ok) throw new Error("Add failed");
    showToast("Product added!");
    addForm.reset();
    addModal.classList.remove("open");
    fetchProducts();
  } catch(e) { showToast(e.message); }
});

document.addEventListener("DOMContentLoaded", fetchProducts);
