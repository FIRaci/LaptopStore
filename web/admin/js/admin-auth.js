// Core Admin Auth Logic
let adminUser = null;

async function initAdminAuth() {
  const token = localStorage.getItem("auth_token");
  if (!token) {
    window.location.href = "/";
    return;
  }
  
  try {
    const res = await fetch("/api/auth/me", {
      headers: { "Authorization": `Bearer ${token}` }
    });
    if (!res.ok) throw new Error("Invalid token");
    
    const data = await res.json();
    adminUser = data.user;
    
    if (adminUser.role !== "ADMIN") {
      window.location.href = "/";
      return;
    }

    document.getElementById("adminUserName").textContent = adminUser.name;
    document.body.style.display = 'flex'; // show content if auth passes (hidden by default)
  } catch (error) {
    localStorage.removeItem("auth_token");
    window.location.href = "/";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  // Hide body initially to prevent flash of content
  document.body.style.display = 'none';
  initAdminAuth();
  
  document.getElementById("adminLogoutBtn")?.addEventListener("click", () => {
    localStorage.removeItem("auth_token");
    window.location.href = "/";
  });
});

// Toast Helper
export function showToast(message) {
  let toastContainer = document.getElementById("toastContainer");
  if (!toastContainer) {
    toastContainer = document.createElement("div");
    toastContainer.id = "toastContainer";
    toastContainer.className = "toast-container";
    document.body.appendChild(toastContainer);
  }
  const toast = document.createElement("div");
  toast.className = "toast";
  toast.innerHTML = `
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2" style="color: var(--accent);">
      <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
    </svg>
    <span>${message}</span>
  `;
  toastContainer.appendChild(toast);
  requestAnimationFrame(() => toast.classList.add("show"));
  setTimeout(() => {
    toast.classList.remove("show");
    setTimeout(() => toast.remove(), 400);
  }, 3000);
}
