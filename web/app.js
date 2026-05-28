const state = {
  products: [],
  filtered: [],
  cart: loadCart(),
  activeType: "ALL"
};

const currency = new Intl.NumberFormat("en-US", {
  style: "currency",
  currency: "USD"
});

const productGrid = document.getElementById("productGrid");
const cartToggle = document.getElementById("cartToggle");
const cartPanel = document.getElementById("cartPanel");
const cartOverlay = document.getElementById("cartOverlay");
const cartClose = document.getElementById("cartClose");
const cartItems = document.getElementById("cartItems");
const cartCount = document.getElementById("cartCount");
const cartTotal = document.getElementById("cartTotal");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const heroShop = document.getElementById("heroShop");
const checkoutForm = document.getElementById("checkoutForm");
const checkoutStatus = document.getElementById("checkoutStatus");
const toastContainer = document.getElementById("toastContainer");
const productModal = document.getElementById("productModal");
const modalGrid = document.getElementById("modalGrid");
const modalClose = document.getElementById("modalClose");
const checkoutBtn = document.getElementById("checkoutBtn");

function renderSkeletons(count = 8) {
  productGrid.innerHTML = Array(count).fill(`
    <div class="product-card">
      <div class="skeleton sk-img"></div>
      <div class="product-info">
        <div class="skeleton sk-title"></div>
        <div class="skeleton sk-desc"></div>
        <div class="product-bottom">
          <div class="skeleton sk-title" style="width: 60px;"></div>
          <div class="skeleton sk-btn"></div>
        </div>
      </div>
    </div>
  `).join("");
}

async function loadProducts() {
  renderSkeletons();
  
  const params = new URLSearchParams();
  params.set("sort", sortSelect.value);
  if (state.activeType !== "ALL") {
    params.set("type", state.activeType);
  }
  if (searchInput.value.trim()) {
    params.set("q", searchInput.value.trim());
  }

  try {
    const response = await fetch(`/api/products?${params.toString()}`);
    if (!response.ok) throw new Error("Failed to load products");
    const data = await response.json();
    state.products = data;
    state.filtered = data;
    
    // Slight delay to show off skeletons on fast local networks
    setTimeout(() => {
      renderProducts();
    }, 400);
  } catch (error) {
    productGrid.innerHTML = `
      <div class="empty-state">
        <h3>Oops, connection lost!</h3>
        <p>Could not fetch products. Is the server running?</p>
      </div>
    `;
  }
}

function renderProducts() {
  if (state.filtered.length === 0) {
    productGrid.innerHTML = `
      <div class="empty-state">
        <h3>No products found</h3>
        <p>Try adjusting your search or filters.</p>
      </div>
    `;
    return;
  }

  productGrid.innerHTML = state.filtered
    .map((product) => {
      const img = product.imageUrl || 'https://via.placeholder.com/400?text=No+Image';
      return `
        <div class="product-card" data-card-id="${product.id}">
          <div class="product-type">${product.type}</div>
          <div class="product-image">
            <img src="${img}" alt="${product.name}" loading="lazy" />
          </div>
          <div class="product-info">
            <div class="product-brand">${product.brand} • SKU: ${product.sku.split('-').pop()}</div>
            <h3 class="product-title">${product.name}</h3>
            <p class="product-desc">${product.description || ""}</p>
            <div class="product-bottom">
              <div class="product-price">${currency.format(product.price)}</div>
              <button class="btn btn-primary" data-id="${product.id}">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
                </svg>
                Add
              </button>
            </div>
          </div>
        </div>
      `;
    })
    .join("");

  // Bind Add to Cart
  productGrid.querySelectorAll("button[data-id]").forEach((button) => {
    button.addEventListener("click", (e) => {
      e.stopPropagation(); // Prevent opening modal
      addToCart(Number(button.dataset.id));
    });
  });

  // Bind Product Modal opening
  productGrid.querySelectorAll(".product-card").forEach((card) => {
    card.addEventListener("click", () => {
      openProductModal(Number(card.dataset.cardId));
    });
  });
}

function showToast(message) {
  const toast = document.createElement("div");
  toast.className = "toast";
  toast.innerHTML = `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" style="color: var(--accent);">
      <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
    <span>${message}</span>
  `;
  toastContainer.appendChild(toast);
  
  // Trigger animation
  requestAnimationFrame(() => {
    toast.classList.add("show");
  });

  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 400); // Wait for transition
  }, 3000);
}

function addToCart(productId) {
  const product = state.products.find(p => p.id === productId);
  if (!product) return;

  const existing = state.cart.find((item) => item.productId === productId);
  if (existing) {
    existing.quantity += 1;
  } else {
    state.cart.push({ productId, quantity: 1, _img: product.imageUrl });
  }
  
  persistCart();
  renderCart();
  showToast(`Added <strong>${product.name}</strong> to cart`);

  // Badge animation
  cartCount.classList.remove("bouncing");
  void cartCount.offsetWidth; // trigger reflow
  cartCount.classList.add("bouncing");
}

function updateCartItem(productId, delta) {
  const item = state.cart.find((entry) => entry.productId === productId);
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    state.cart = state.cart.filter((entry) => entry.productId !== productId);
  }
  persistCart();
  renderCart();
}

function renderCart() {
  const map = new Map(state.products.map((p) => [p.id, p]));
  let total = 0;

  if (state.cart.length === 0) {
    cartItems.innerHTML = `
      <div class="empty-state" style="padding: 40px 0;">
        <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5" style="color: var(--border); margin-bottom: 16px;">
          <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
        </svg>
        <h3>Your cart is empty</h3>
        <p>Looks like you haven't added anything yet.</p>
      </div>
    `;
  } else {
    cartItems.innerHTML = state.cart
      .map((item) => {
        const product = map.get(item.productId);
        if (!product) return "";
        const lineTotal = product.price * item.quantity;
        total += lineTotal;
        const img = product.imageUrl || item._img || 'https://via.placeholder.com/80';
        return `
          <div class="cart-item">
            <img src="${img}" class="cart-item-img" alt="${product.name}" />
            <div class="cart-item-details">
              <h4>${product.name}</h4>
              <p>${currency.format(product.price)} x ${item.quantity}</p>
              <div class="cart-item-actions">
                <button class="btn btn-ghost" data-action="dec" data-id="${product.id}">-</button>
                <button class="btn btn-ghost" data-action="inc" data-id="${product.id}">+</button>
              </div>
            </div>
          </div>
        `;
      })
      .join("");
  }

  cartItems.querySelectorAll("button[data-action]").forEach((button) => {
    const id = Number(button.dataset.id);
    const delta = button.dataset.action === "inc" ? 1 : -1;
    button.addEventListener("click", () => updateCartItem(id, delta));
  });

  cartTotal.textContent = currency.format(total);
  cartCount.textContent = state.cart.reduce((sum, item) => sum + item.quantity, 0);
}

function openCart() {
  cartPanel.classList.add("open");
  cartOverlay.classList.add("open");
}

function closeCart() {
  cartPanel.classList.remove("open");
  cartOverlay.classList.remove("open");
}

function bindFilters() {
  document.querySelectorAll(".chip").forEach((chip) => {
    chip.addEventListener("click", () => {
      document.querySelectorAll(".chip").forEach((c) => c.classList.remove("active"));
      chip.classList.add("active");
      state.activeType = chip.dataset.type;
      loadProducts();
    });
  });
}

// Debounce search
let searchTimeout;
searchInput.addEventListener("input", () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    loadProducts();
  }, 400);
});

checkoutForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  checkoutStatus.style.color = "var(--accent)";
  checkoutStatus.textContent = "Processing order...";

  if (state.cart.length === 0) {
    checkoutStatus.style.color = "#ff4d4f";
    checkoutStatus.textContent = "Your cart is empty.";
    return;
  }

  const formData = new FormData(checkoutForm);
  const payload = {
    customer: {
      firstName: formData.get("firstName"),
      lastName: formData.get("lastName"),
      email: formData.get("email"),
      phone: formData.get("phone"),
      address: formData.get("address")
    },
    items: state.cart.map((item) => ({
      productId: item.productId,
      quantity: item.quantity
    }))
  };

  try {
    checkoutBtn.disabled = true;
    checkoutBtn.querySelector('.btn-text').style.opacity = '0';
    checkoutBtn.querySelector('.btn-loader').style.display = 'inline-flex';

    const orderResponse = await fetch("/api/orders", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!orderResponse.ok) {
      const data = await orderResponse.json();
      throw new Error(data.error || "Failed to place order");
    }

    const order = await orderResponse.json();
    const method = formData.get("method");

    const paymentResponse = await fetch("/api/payments", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        orderId: order.id,
        amount: order.totalAmount,
        method,
        status: "PAID"
      })
    });

    if (!paymentResponse.ok) {
      throw new Error("Order created, but payment failed.");
    }

    state.cart = [];
    persistCart();
    renderCart();
    checkoutForm.reset();
    showToast(`Success! Order #${order.id} placed.`);
    closeCart();
    checkoutStatus.textContent = "";
  } catch (error) {
    checkoutStatus.style.color = "#ff4d4f";
    checkoutStatus.textContent = error.message;
  } finally {
    checkoutBtn.disabled = false;
    checkoutBtn.querySelector('.btn-text').style.opacity = '1';
    checkoutBtn.querySelector('.btn-loader').style.display = 'none';
  }
});

function persistCart() {
  localStorage.setItem("ls_cart", JSON.stringify(state.cart));
}

function loadCart() {
  const stored = localStorage.getItem("ls_cart");
  return stored ? JSON.parse(stored) : [];
}

cartToggle.addEventListener("click", openCart);
cartClose.addEventListener("click", closeCart);
cartOverlay.addEventListener("click", closeCart);

// Modal logic
function openProductModal(id) {
  const product = state.products.find(p => p.id === id);
  if (!product) return;
  const img = product.imageUrl || 'https://via.placeholder.com/600';
  
  modalGrid.innerHTML = `
    <div class="modal-img-col">
      <img src="${img}" alt="${product.name}" />
    </div>
    <div class="modal-info-col">
      <h2>${product.name}</h2>
      <div class="price">${currency.format(product.price)}</div>
      <div class="desc">
        <p><strong>Brand:</strong> ${product.brand}</p>
        <p><strong>SKU:</strong> ${product.sku}</p>
        <br/>
        <p>${product.description || 'No detailed description available.'}</p>
      </div>
      <div class="modal-actions">
        <button class="btn btn-primary" id="modalAddBtn">Add to Cart</button>
      </div>
    </div>
  `;
  
  document.getElementById("modalAddBtn").addEventListener("click", () => {
    addToCart(product.id);
    closeProductModal();
  });
  
  productModal.classList.add("open");
}

function closeProductModal() {
  productModal.classList.remove("open");
}

modalClose.addEventListener("click", closeProductModal);
productModal.addEventListener("click", (e) => {
  if (e.target === productModal) closeProductModal();
});

sortSelect.addEventListener("change", () => loadProducts());
heroShop.addEventListener("click", () => {
  document.getElementById("productGrid").scrollIntoView({ behavior: "smooth" });
});

// Extra Interaction Handlers for Phase 7
const heroBundles = document.getElementById("heroBundles");
if (heroBundles) {
  heroBundles.addEventListener("click", () => {
    document.getElementById("productGrid").scrollIntoView({ behavior: "smooth" });
    // Simulate clicking the ALL/Bundles chip if we had a specific one. For now just show ALL
    const allChip = document.querySelector('.chip[data-type="ALL"]');
    if (allChip) allChip.click();
  });
}

// Auth & User State
let currentUser = null;
const authModal = document.getElementById("authModal");
const accountBtn = document.getElementById("accountBtn");
const accountBtnText = document.getElementById("accountBtnText");
const authModalClose = document.getElementById("authModalClose");
const authForm = document.getElementById("authForm");
const authSwitch = document.getElementById("authSwitch");
const authSwitchText = document.getElementById("authSwitchText");
const registerFields = document.getElementById("registerFields");
const authSubmitBtn = document.getElementById("authSubmitBtn");
const authError = document.getElementById("authError");
const adminBtn = document.getElementById("adminBtn");

const userProfileModal = document.getElementById("userProfileModal");
const userModalClose = document.getElementById("userModalClose");
const userProfileForm = document.getElementById("userProfileForm");
const logoutBtn = document.getElementById("logoutBtn");

// UI Tabs logic
document.querySelectorAll(".tabs").forEach(tabContainer => {
  const buttons = tabContainer.querySelectorAll(".tab-btn");
  buttons.forEach(btn => {
    btn.addEventListener("click", () => {
      // Deactivate all inside this tab container
      buttons.forEach(b => b.classList.remove("active"));
      // Hide all contents in the parent
      const parent = tabContainer.parentElement;
      parent.querySelectorAll(".tab-content").forEach(content => {
        content.classList.remove("active");
        content.style.display = "none";
      });
      // Activate clicked
      btn.classList.add("active");
      const targetId = btn.getAttribute("data-target");
      const target = document.getElementById(targetId);
      if (target) {
        target.classList.add("active");
        target.style.display = "block";
      }
    });
  });
});

async function checkAuth() {
  const token = localStorage.getItem("auth_token");
  if (!token) return;
  try {
    const res = await fetch("/api/auth/me", {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (res.ok) {
      const data = await res.json();
      currentUser = data.user;
      updateAuthUI();
    } else {
      localStorage.removeItem("auth_token");
    }
  } catch (error) {
    console.error(error);
  }
}

function updateAuthUI() {
  if (currentUser) {
    accountBtnText.textContent = currentUser.name;
    if (currentUser.role === "ADMIN") {
      adminBtn.style.display = "inline-flex";
    } else {
      adminBtn.style.display = "none";
    }
  } else {
    accountBtnText.textContent = "Sign In";
    adminBtn.style.display = "none";
  }
}

if (accountBtn && authModal) {
  accountBtn.addEventListener("click", () => {
    if (currentUser) {
      // Open User Profile Modal
      userProfileModal.classList.add("open");
      loadUserProfile();
      loadUserOrders();
    } else {
      authModal.classList.add("open");
    }
  });
  authModalClose.addEventListener("click", () => authModal.classList.remove("open"));
  authModal.addEventListener("click", (e) => {
    if (e.target === authModal) authModal.classList.remove("open");
  });
}

if (logoutBtn) {
  logoutBtn.addEventListener("click", () => {
    currentUser = null;
    localStorage.removeItem("auth_token");
    updateAuthUI();
    userProfileModal.classList.remove("open");
    showToast("Logged out successfully");
  });
}

if (userModalClose) {
  userModalClose.addEventListener("click", () => userProfileModal.classList.remove("open"));
  userProfileModal.addEventListener("click", (e) => {
    if (e.target === userProfileModal) userProfileModal.classList.remove("open");
  });
}

if (authSwitch) {
  authSwitch.addEventListener("click", (e) => {
    e.preventDefault();
    const isLogin = authForm.getAttribute("data-mode") === "login";
    if (isLogin) {
      authForm.setAttribute("data-mode", "register");
      registerFields.style.display = "flex";
      authSubmitBtn.textContent = "Create Account";
      authSwitchText.textContent = "Already have an account?";
      authSwitch.textContent = "Sign in";
      authError.textContent = "";
    } else {
      authForm.setAttribute("data-mode", "login");
      registerFields.style.display = "none";
      authSubmitBtn.textContent = "Sign In";
      authSwitchText.textContent = "Don't have an account?";
      authSwitch.textContent = "Create one";
      authError.textContent = "";
    }
  });
}

if (authForm) {
  authForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    authError.textContent = "";
    const mode = authForm.getAttribute("data-mode");
    const email = document.getElementById("authEmail").value;
    const password = document.getElementById("authPassword").value;
    
    let payload = { email, password };
    let endpoint = "/api/auth/login";
    
    if (mode === "register") {
      endpoint = "/api/auth/register";
      payload.firstName = document.getElementById("authFirstName").value;
      payload.lastName = document.getElementById("authLastName").value;
    }

    try {
      const res = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      const data = await res.json();
      if (!res.ok) {
        throw new Error(data.error || "Authentication failed");
      }
      localStorage.setItem("auth_token", data.token);
      currentUser = data.user;
      updateAuthUI();
      authModal.classList.remove("open");
      showToast(`Welcome back, ${currentUser.name}!`);
      authForm.reset();
    } catch (err) {
      authError.textContent = err.message;
    }
  });
}

checkAuth();

// Newsletter Form
const newsletterForm = document.getElementById("newsletterForm");
if (newsletterForm) {
  newsletterForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const email = newsletterForm.querySelector('input[type="email"]').value;
    showToast(`Thanks for subscribing with <strong>${email}</strong>!`);
    newsletterForm.reset();
  });
}

// Footer Links Handlers
const footerShopLinks = document.querySelectorAll("#footerShopLinks a");
footerShopLinks.forEach(link => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const tag = link.getAttribute("data-tag");
    const targetChip = document.querySelector(`.chip[data-type="${tag}"]`);
    if (targetChip) {
      targetChip.click();
      document.getElementById("productGrid").scrollIntoView({ behavior: "smooth" });
    }
  });
});

const footerCompanyLinks = document.querySelectorAll("#footerCompanyLinks a");
footerCompanyLinks.forEach(link => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    showToast(`Redirecting to <strong>${link.textContent}</strong>...`);
  });
});

bindFilters();
loadProducts();
renderCart();
