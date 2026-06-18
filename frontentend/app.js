// API Gateway URL
const API_BASE = "http://localhost:8080";

// App State
const state = {
    token: localStorage.getItem("token") || null,
    user: JSON.parse(localStorage.getItem("user")) || null,
    products: [],
    inventory: {}, // skuCode -> quantity
    orders: [],
    myOrders: [],
    cart: JSON.parse(localStorage.getItem("cart")) || [],
    activeView: "login",
    charts: {
        stock: null,
        sales: null
    }
};

// Utilities
function saveSession(token, user) {
    state.token = token;
    state.user = user;
    localStorage.setItem("token", token);
    localStorage.setItem("user", JSON.stringify(user));
}

function clearSession() {
    state.token = null;
    state.user = null;
    state.products = [];
    state.inventory = {};
    state.orders = [];
    state.myOrders = [];
    state.cart = [];
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    localStorage.removeItem("cart");
    if (state.charts.stock) state.charts.stock.destroy();
    if (state.charts.sales) state.charts.sales.destroy();
    state.charts.stock = null;
    state.charts.sales = null;
}

function getHeaders() {
    const headers = {
        "Content-Type": "application/json"
    };
    if (state.token) {
        headers["Authorization"] = `Bearer ${state.token}`;
    }
    return headers;
}

// Show Toast Alerts
function showToast(message, type = "success") {
    const container = document.getElementById("toast-container");
    const toast = document.createElement("div");
    toast.className = `p-4 rounded-lg shadow-xl text-white font-medium flex items-center space-x-2 animate-fade-in-up border ${
        type === "success" 
            ? "bg-emerald-950/80 border-emerald-500/30 text-emerald-300" 
            : "bg-red-950/80 border-red-500/30 text-red-300"
    }`;
    toast.innerHTML = `
        <span class="w-2 h-2 rounded-full ${type === "success" ? "bg-emerald-400" : "bg-red-400"}"></span>
        <span>${message}</span>
    `;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(-10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

// Route navigation
function navigateTo(view) {
    state.activeView = view;
    
    // Hide all views
    document.querySelectorAll(".view-section").forEach(sec => sec.classList.add("hidden"));
    
    // Hide auth overlays or main navbar based on status
    if (state.token) {
        document.getElementById("navbar").classList.remove("hidden");
        document.getElementById("auth-container").classList.add("hidden");
        document.getElementById("app-container").classList.remove("hidden");
    } else {
        document.getElementById("navbar").classList.add("hidden");
        document.getElementById("auth-container").classList.remove("hidden");
        document.getElementById("app-container").classList.add("hidden");
        view = state.user ? "login" : "login"; // default
    }

    // Show selected view
    const activeSec = document.getElementById(`view-${view}`);
    if (activeSec) {
        activeSec.classList.remove("hidden");
        activeSec.classList.add("animate-fade-in-up");
    }

    // Render navbar links based on role
    renderNavbar();

    // Trigger load functions for views
    if (state.token) {
        if (view === "admin-dashboard" && state.user.role === "ADMIN") {
            loadAdminDashboard();
        } else if (view === "customer-catalog") {
            loadCatalog();
        } else if (view === "customer-orders") {
            loadCustomerOrders();
        }
    }
}

function renderNavbar() {
    if (!state.user) return;
    document.getElementById("nav-username").innerText = state.user.username;
    document.getElementById("nav-role").innerText = state.user.role;
    
    // Update Cart Badge
    const cartCount = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    const badge = document.getElementById("cart-badge");
    badge.innerText = cartCount;
    if (cartCount > 0) {
        badge.classList.remove("hidden");
    } else {
        badge.classList.add("hidden");
    }

    // Role specific buttons
    const adminLink = document.getElementById("nav-link-admin");
    const catalogLink = document.getElementById("nav-link-catalog");
    const ordersLink = document.getElementById("nav-link-orders");

    if (state.user.role === "ADMIN") {
        adminLink.classList.remove("hidden");
    } else {
        adminLink.classList.add("hidden");
    }
}

// ---------------- AUTHENTICATION APIs ----------------
async function handleRegister(username, email, password, role) {
    try {
        const response = await fetch(`${API_BASE}/api/v1/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, email, password, role })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || "Registration failed");
        }
        showToast("Registration successful! Logging in...");
        await handleLogin(username, password);
    } catch (error) {
        showToast(error.message, "error");
    }
}

async function handleLogin(username, password) {
    try {
        const response = await fetch(`${API_BASE}/api/v1/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password })
        });
        if (!response.ok) {
            throw new Error("Invalid username or password credentials");
        }
        const data = await response.json();
        
        // Decode JWT to extract authorities/role
        const token = data.access_token;
        const payload = JSON.parse(atob(token.split('.')[1]));
        
        // Keycloak roles extract
        let role = "CUSTOMER"; // Default fallback
        if (payload.realm_access && payload.realm_access.roles) {
            if (payload.realm_access.roles.includes("ADMIN")) {
                role = "ADMIN";
            }
        }
        
        const loggedUser = {
            username: username,
            email: payload.email || `${username}@ecommerce.com`,
            role: role
        };

        saveSession(token, loggedUser);
        showToast(`Welcome back, ${username}!`);
        
        if (role === "ADMIN") {
            navigateTo("admin-dashboard");
        } else {
            navigateTo("customer-catalog");
        }
    } catch (error) {
        showToast(error.message, "error");
    }
}

function handleLogout() {
    clearSession();
    showToast("Logged out successfully");
    navigateTo("login");
}

// ---------------- PRODUCTS & CATALOG APIs ----------------
async function loadCatalog() {
    try {
        const searchInput = document.getElementById("catalog-search-input");
        const query = searchInput ? searchInput.value.trim() : "";
        let url = `${API_BASE}/api/v1/products`;
        if (query) {
            url = `${API_BASE}/api/v1/products/search?query=${encodeURIComponent(query)}`;
        }

        const response = await fetch(url, { headers: getHeaders() });
        if (!response.ok) throw new Error("Could not load products catalog");
        state.products = await response.ok ? await response.json() : [];

        // Load inventory for all products to show stock status
        await Promise.all(state.products.map(async p => {
            try {
                const stockRes = await fetch(`${API_BASE}/api/v1/inventory/${p.skuCode}`, { headers: getHeaders() });
                if (stockRes.ok) {
                    const inv = await stockRes.json();
                    state.inventory[p.skuCode] = inv.quantity;
                } else {
                    state.inventory[p.skuCode] = 0;
                }
            } catch {
                state.inventory[p.skuCode] = 0;
            }
        }));

        renderCatalogList();
    } catch (error) {
        showToast(error.message, "error");
    }
}

function renderCatalogList() {
    const grid = document.getElementById("catalog-grid");
    grid.innerHTML = "";
    
    if (state.products.length === 0) {
        grid.innerHTML = `
            <div class="col-span-full py-12 text-center text-gray-500">
                <i data-lucide="package-search" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
                <p class="text-lg">No products found matching your search</p>
            </div>
        `;
        lucide.createIcons();
        return;
    }

    state.products.forEach(p => {
        const qty = state.inventory[p.skuCode] || 0;
        const inStock = qty > 0;
        
        const card = document.createElement("div");
        card.className = "glass-panel rounded-2xl p-6 flex flex-col justify-between";
        card.innerHTML = `
            <div>
                <div class="flex justify-between items-start mb-4">
                    <span class="px-2.5 py-1 text-xs font-semibold uppercase rounded-full ${inStock ? 'bg-emerald-950/40 text-emerald-400 border border-emerald-500/20' : 'bg-red-950/40 text-red-400 border border-red-500/20'}">
                        ${inStock ? `${qty} In Stock` : 'Out of Stock'}
                    </span>
                    <span class="text-sm font-mono text-purple-400">${p.skuCode}</span>
                </div>
                <h3 class="text-xl font-bold mb-2">${p.name}</h3>
                <p class="text-gray-400 text-sm mb-4 line-clamp-2">${p.description}</p>
            </div>
            <div class="mt-4 pt-4 border-t border-white/5 flex items-center justify-between">
                <span class="text-2xl font-bold text-gradient-secondary">$${p.price.toFixed(2)}</span>
                <button onclick="addToCart(${p.id}, '${p.skuCode}', '${p.name}', ${p.price})" 
                    ${!inStock ? 'disabled' : ''}
                    class="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white font-medium transition duration-200 flex items-center space-x-1 disabled:opacity-30 disabled:hover:bg-purple-600">
                    <i data-lucide="shopping-cart" class="w-4 h-4"></i>
                    <span>Add to Cart</span>
                </button>
            </div>
        `;
        grid.appendChild(card);
    });
    lucide.createIcons();
}

// ---------------- CART SYSTEM ----------------
window.addToCart = function(id, skuCode, name, price) {
    const stockQty = state.inventory[skuCode] || 0;
    const existing = state.cart.find(item => item.skuCode === skuCode);
    
    if (existing) {
        if (existing.quantity >= stockQty) {
            showToast(`Cannot add more. Only ${stockQty} items available in stock.`, "error");
            return;
        }
        existing.quantity++;
    } else {
        state.cart.push({ id, skuCode, name, price, quantity: 1 });
    }
    
    localStorage.setItem("cart", JSON.stringify(state.cart));
    renderNavbar();
    showToast(`Added ${name} to your shopping cart!`);
    renderCartDrawer();
}

window.updateCartQuantity = function(skuCode, delta) {
    const item = state.cart.find(item => item.skuCode === skuCode);
    if (!item) return;
    const stockQty = state.inventory[skuCode] || 0;

    if (delta > 0 && item.quantity >= stockQty) {
        showToast(`Cannot add more. Only ${stockQty} items available in stock.`, "error");
        return;
    }
    
    item.quantity += delta;
    if (item.quantity <= 0) {
        state.cart = state.cart.filter(i => i.skuCode !== skuCode);
    }
    
    localStorage.setItem("cart", JSON.stringify(state.cart));
    renderNavbar();
    renderCartDrawer();
}

function renderCartDrawer() {
    const list = document.getElementById("cart-items-list");
    list.innerHTML = "";
    
    let total = 0;
    if (state.cart.length === 0) {
        list.innerHTML = `
            <div class="py-12 text-center text-gray-500">
                <i data-lucide="shopping-bag" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
                <p>Your shopping cart is empty</p>
            </div>
        `;
        document.getElementById("cart-total").innerText = "$0.00";
        lucide.createIcons();
        return;
    }

    state.cart.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        
        const div = document.createElement("div");
        div.className = "flex justify-between items-center bg-white/5 border border-white/5 p-3 rounded-xl";
        div.innerHTML = `
            <div class="flex-1 min-w-0 pr-3">
                <p class="font-bold text-sm truncate">${item.name}</p>
                <p class="text-xs text-purple-400 font-mono">$${item.price.toFixed(2)} each</p>
            </div>
            <div class="flex items-center space-x-2.5">
                <button onclick="updateCartQuantity('${item.skuCode}', -1)" class="w-6 h-6 rounded bg-white/10 hover:bg-white/20 text-white font-bold flex items-center justify-center">-</button>
                <span class="font-mono text-sm">${item.quantity}</span>
                <button onclick="updateCartQuantity('${item.skuCode}', 1)" class="w-6 h-6 rounded bg-white/10 hover:bg-white/20 text-white font-bold flex items-center justify-center">+</button>
                <span class="font-bold text-sm min-w-[50px] text-right">$${itemTotal.toFixed(2)}</span>
            </div>
        `;
        list.appendChild(div);
    });
    
    document.getElementById("cart-total").innerText = `$${total.toFixed(2)}`;
    lucide.createIcons();
}

window.toggleCart = function() {
    const drawer = document.getElementById("cart-drawer");
    drawer.classList.toggle("translate-x-full");
    renderCartDrawer();
}

// ---------------- ORDERS APIs ----------------
async function handleCheckout() {
    if (state.cart.length === 0) {
        showToast("Cart is empty", "error");
        return;
    }
    
    let checkoutSuccess = true;
    const items = [...state.cart];
    
    // Process items in cart sequentially
    for (const item of items) {
        try {
            const response = await fetch(`${API_BASE}/api/v1/orders`, {
                method: "POST",
                headers: getHeaders(),
                body: JSON.stringify({
                    skuCode: item.skuCode,
                    price: item.price,
                    quantity: item.quantity
                })
            });
            if (!response.ok) {
                const err = await response.json();
                throw new Error(err.message || `Failed to checkout item ${item.name}`);
            }
        } catch (error) {
            showToast(error.message, "error");
            checkoutSuccess = false;
            break;
        }
    }

    if (checkoutSuccess) {
        showToast("Order checkout completed successfully! Outbound notifications dispatched.");
        state.cart = [];
        localStorage.removeItem("cart");
        renderNavbar();
        toggleCart();
        navigateTo("customer-orders");
    }
}

async function loadCustomerOrders() {
    try {
        const response = await fetch(`${API_BASE}/api/v1/orders/my-orders`, { headers: getHeaders() });
        if (!response.ok) throw new Error("Could not load customer orders history");
        state.myOrders = await response.json();
        
        const container = document.getElementById("customer-orders-list");
        container.innerHTML = "";
        
        if (state.myOrders.length === 0) {
            container.innerHTML = `
                <div class="col-span-full py-12 text-center text-gray-500">
                    <i data-lucide="clipboard-list" class="w-12 h-12 mx-auto mb-3 opacity-40"></i>
                    <p class="text-lg">You have not placed any orders yet.</p>
                </div>
            `;
            lucide.createIcons();
            return;
        }

        // Sort descending by id
        state.myOrders.sort((a, b) => b.id - a.id);

        state.myOrders.forEach(o => {
            const orderDiv = document.createElement("div");
            orderDiv.className = "glass-panel rounded-2xl p-6 border border-white/5";
            orderDiv.innerHTML = `
                <div class="flex justify-between items-start mb-4 pb-4 border-b border-white/5">
                    <div>
                        <p class="text-xs text-gray-400 font-mono mb-1">Order ID: ${o.id}</p>
                        <h4 class="font-bold text-gradient-primary font-mono text-xs truncate max-w-xs md:max-w-md">${o.orderNumber}</h4>
                    </div>
                    <span class="px-2.5 py-1 text-xs font-semibold uppercase rounded-full ${o.status === 'CANCELLED' ? 'bg-red-950/40 text-red-400 border border-red-500/20' : 'bg-emerald-950/40 text-emerald-400 border border-emerald-500/20'}">
                        ${o.status}
                    </span>
                </div>
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div>
                        <p class="text-xs text-gray-400 mb-1">Product SKU</p>
                        <p class="font-bold font-mono">${o.skuCode}</p>
                    </div>
                    <div>
                        <p class="text-xs text-gray-400 mb-1">Quantity</p>
                        <p class="font-bold">${o.quantity} unit(s)</p>
                    </div>
                    <div>
                        <p class="text-xs text-gray-400 mb-1">Price per unit</p>
                        <p class="font-bold">$${o.price.toFixed(2)}</p>
                    </div>
                    <div>
                        <p class="text-xs text-gray-400 mb-1">Total Paid</p>
                        <p class="font-bold text-gradient-secondary">$${(o.price * o.quantity).toFixed(2)}</p>
                    </div>
                </div>
                ${o.status !== 'CANCELLED' ? `
                    <div class="mt-4 flex justify-end">
                        <button onclick="handleCancelOrder(${o.id})" class="px-3.5 py-1.5 rounded-lg border border-red-500/30 text-red-400 hover:bg-red-950/40 text-xs font-medium transition duration-200">
                            Cancel Order
                        </button>
                    </div>
                ` : ''}
            `;
            container.appendChild(orderDiv);
        });
        lucide.createIcons();
    } catch (error) {
        showToast(error.message, "error");
    }
}

window.handleCancelOrder = async function(id) {
    if (!confirm("Are you sure you want to cancel this order?")) return;
    try {
        const response = await fetch(`${API_BASE}/api/v1/orders/${id}/cancel`, {
            method: "PUT",
            headers: getHeaders()
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || "Failed to cancel order");
        }
        showToast("Order cancelled successfully!");
        if (state.activeView === "admin-dashboard") {
            loadAdminDashboard();
        } else {
            loadCustomerOrders();
        }
    } catch (error) {
        showToast(error.message, "error");
    }
}

// ---------------- ADMIN PANEL APIs & STATS ----------------
async function loadAdminDashboard() {
    try {
        // Fetch all products
        const prodRes = await fetch(`${API_BASE}/api/v1/products`, { headers: getHeaders() });
        if (!prodRes.ok) throw new Error("Could not load products catalog list");
        state.products = await prodRes.json();

        // Fetch all system orders
        const ordRes = await fetch(`${API_BASE}/api/v1/orders`, { headers: getHeaders() });
        if (!ordRes.ok) throw new Error("Could not load system orders logs");
        state.orders = await ordRes.json();

        // Query stock level for each product SKU code
        await Promise.all(state.products.map(async p => {
            try {
                const stockRes = await fetch(`${API_BASE}/api/v1/inventory/${p.skuCode}`, { headers: getHeaders() });
                if (stockRes.ok) {
                    const inv = await stockRes.json();
                    state.inventory[p.skuCode] = inv.quantity;
                } else {
                    state.inventory[p.skuCode] = 0;
                }
            } catch {
                state.inventory[p.skuCode] = 0;
            }
        }));

        // Compute metrics
        const totalProducts = state.products.length;
        const totalOrders = state.orders.length;
        const lowStockItems = state.products.filter(p => (state.inventory[p.skuCode] || 0) <= 10).length;
        
        let cumulativeRevenue = 0;
        state.orders.forEach(o => {
            if (o.status !== "CANCELLED") {
                cumulativeRevenue += o.price * o.quantity;
            }
        });

        // Set telemetry displays
        document.getElementById("stat-products").innerText = totalProducts;
        document.getElementById("stat-orders").innerText = totalOrders;
        document.getElementById("stat-low-stock").innerText = lowStockItems;
        document.getElementById("stat-revenue").innerText = `$${cumulativeRevenue.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}`;

        // Color coding warning logic
        const lowStockCard = document.getElementById("stat-low-stock-card");
        if (lowStockItems > 0) {
            lowStockCard.classList.add("border-red-500/30", "bg-red-950/10");
        } else {
            lowStockCard.classList.remove("border-red-500/30", "bg-red-950/10");
        }

        renderAdminProductsList();
        renderAdminOrdersList();
        renderCharts();

    } catch (error) {
        showToast(error.message, "error");
    }
}

function renderAdminProductsList() {
    const tbody = document.getElementById("admin-products-table-body");
    tbody.innerHTML = "";

    if (state.products.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="py-8 text-center text-gray-500">
                    No products cataloged in the system yet.
                </td>
            </tr>
        `;
        return;
    }

    state.products.forEach(p => {
        const stock = state.inventory[p.skuCode] || 0;
        const tr = document.createElement("tr");
        tr.className = "border-b border-white/5 hover:bg-white/5 transition duration-150";
        tr.innerHTML = `
            <td class="py-4 px-6 text-sm font-bold">${p.name}</td>
            <td class="py-4 px-6 text-sm font-mono text-purple-400">${p.skuCode}</td>
            <td class="py-4 px-6 text-sm font-bold font-mono">$${p.price.toFixed(2)}</td>
            <td class="py-4 px-6 text-sm">
                <span class="px-2 py-0.5 text-xs font-mono font-bold rounded ${stock <= 10 ? 'bg-red-950 text-red-400 border border-red-500/30' : 'bg-emerald-950 text-emerald-400 border border-emerald-500/30'}">
                    ${stock} units
                </span>
            </td>
            <td class="py-4 px-6 text-sm flex items-center space-x-3">
                <button onclick="openRestockModal('${p.skuCode}', ${stock})" class="px-2.5 py-1 bg-white/10 hover:bg-white/20 text-white rounded text-xs font-medium transition duration-150">
                    Restock
                </button>
                <button onclick="handleDeleteProduct(${p.id})" class="px-2.5 py-1 border border-red-500/30 text-red-400 hover:bg-red-950/40 rounded text-xs font-medium transition duration-150">
                    Delete
                </td>
            </tr>
        `;
        tbody.appendChild(tr);
    });
}

function renderAdminOrdersList() {
    const list = document.getElementById("admin-orders-list");
    list.innerHTML = "";

    if (state.orders.length === 0) {
        list.innerHTML = `<div class="py-8 text-center text-gray-500 text-sm">No transactions logged.</div>`;
        return;
    }

    // Sort descending by id
    const sortedOrders = [...state.orders].sort((a, b) => b.id - a.id);

    sortedOrders.forEach(o => {
        const isCancelled = o.status === "CANCELLED";
        const div = document.createElement("div");
        div.className = "flex items-center justify-between p-4 bg-white/5 border border-white/5 rounded-xl text-sm";
        div.innerHTML = `
            <div class="min-w-0 pr-3">
                <div class="flex items-center space-x-2 mb-1">
                    <span class="font-mono text-purple-400 font-bold text-xs">ID: ${o.id}</span>
                    <span class="text-xs text-gray-400 font-mono truncate max-w-[120px]">${o.orderNumber}</span>
                </div>
                <p class="font-medium text-xs">SKU: <span class="font-mono">${o.skuCode}</span> (${o.quantity} units)</p>
            </div>
            <div class="text-right flex items-center space-x-3">
                <div>
                    <p class="font-bold text-gradient-secondary font-mono">$${(o.price * o.quantity).toFixed(2)}</p>
                    <span class="text-[10px] uppercase font-bold tracking-wider ${isCancelled ? 'text-red-400' : 'text-emerald-400'}">${o.status}</span>
                </div>
                ${!isCancelled ? `
                    <button onclick="handleCancelOrder(${o.id})" class="p-1 rounded hover:bg-red-950/30 text-red-400 hover:text-red-300 transition duration-150" title="Cancel order">
                        <i data-lucide="x-circle" class="w-4 h-4"></i>
                    </button>
                ` : ''}
            </div>
        `;
        list.appendChild(div);
    });
    lucide.createIcons();
}

// ---------------- CHART.JS SETUP ----------------
function renderCharts() {
    // 1. Stock levels Bar Chart
    const ctxStock = document.getElementById("chart-stock-levels").getContext("2d");
    if (state.charts.stock) state.charts.stock.destroy();
    
    const labels = state.products.map(p => p.skuCode);
    const stockData = state.products.map(p => state.inventory[p.skuCode] || 0);
    const backgroundColors = stockData.map(qty => qty <= 10 ? 'rgba(239, 68, 68, 0.6)' : 'rgba(139, 92, 246, 0.6)');
    const borderColors = stockData.map(qty => qty <= 10 ? 'rgba(239, 68, 68, 1)' : 'rgba(139, 92, 246, 1)');

    state.charts.stock = new Chart(ctxStock, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Stock Quantity',
                data: stockData,
                backgroundColor: backgroundColors,
                borderColor: borderColors,
                borderWidth: 1.5,
                borderRadius: 6
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: { backgroundColor: '#111827', titleColor: '#a78bfa' }
            },
            scales: {
                y: {
                    grid: { color: 'rgba(255, 255, 255, 0.05)' },
                    ticks: { color: '#9ca3af' }
                },
                x: {
                    grid: { display: false },
                    ticks: { color: '#9ca3af' }
                }
            }
        }
    });

    // 2. Sales Breakdown Doughnut Chart
    const ctxSales = document.getElementById("chart-sales-breakdown").getContext("2d");
    if (state.charts.sales) state.charts.sales.destroy();

    // Sum quantities sold per SKU
    const salesMap = {};
    state.products.forEach(p => { salesMap[p.skuCode] = 0; });
    state.orders.forEach(o => {
        if (o.status !== "CANCELLED" && salesMap[o.skuCode] !== undefined) {
            salesMap[o.skuCode] += o.quantity;
        }
    });

    const salesLabels = Object.keys(salesMap);
    const salesData = Object.values(salesMap);
    const totalUnitsSold = salesData.reduce((sum, val) => sum + val, 0);

    if (totalUnitsSold === 0) {
        // Render empty state in chart
        state.charts.sales = new Chart(ctxSales, {
            type: 'doughnut',
            data: {
                labels: ['No Sales Data'],
                datasets: [{
                    data: [1],
                    backgroundColor: ['rgba(255, 255, 255, 0.05)'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: '#9ca3af' } },
                    tooltip: { enabled: false }
                }
            }
        });
    } else {
        state.charts.sales = new Chart(ctxSales, {
            type: 'doughnut',
            data: {
                labels: salesLabels,
                datasets: [{
                    data: salesData,
                    backgroundColor: [
                        'rgba(139, 92, 246, 0.6)',
                        'rgba(16, 185, 129, 0.6)',
                        'rgba(245, 158, 11, 0.6)',
                        'rgba(59, 130, 246, 0.6)',
                        'rgba(236, 72, 153, 0.6)'
                    ],
                    borderColor: [
                        '#8b5cf6',
                        '#10b981',
                        '#f59e0b',
                        '#3b82f6',
                        '#ec4899'
                    ],
                    borderWidth: 1.5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { 
                        position: 'right',
                        labels: { color: '#9ca3af', font: { size: 10 } }
                    },
                    tooltip: { backgroundColor: '#111827' }
                }
            }
        });
    }
}

// ---------------- DIALOG MODALS ----------------
window.openCreateProductModal = function() {
    document.getElementById("create-product-modal").classList.remove("hidden");
    document.getElementById("create-product-modal").classList.add("flex");
}

window.closeCreateProductModal = function() {
    document.getElementById("create-product-modal").classList.add("hidden");
    document.getElementById("create-product-modal").classList.remove("flex");
}

window.handleCreateProductSubmit = async function(event) {
    event.preventDefault();
    const name = document.getElementById("prod-name").value.trim();
    const description = document.getElementById("prod-desc").value.trim();
    const price = parseFloat(document.getElementById("prod-price").value);
    const skuCode = document.getElementById("prod-sku").value.trim().toUpperCase();

    try {
        const response = await fetch(`${API_BASE}/api/v1/products`, {
            method: "POST",
            headers: getHeaders(),
            body: JSON.stringify({ name, description, price, skuCode })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || "Failed to create product");
        }
        showToast("Product successfully created!");
        closeCreateProductModal();
        event.target.reset();
        await loadAdminDashboard();
    } catch (error) {
        showToast(error.message, "error");
    }
}

window.handleDeleteProduct = async function(id) {
    if (!confirm("Are you sure you want to delete this product?")) return;
    try {
        const response = await fetch(`${API_BASE}/api/v1/products/${id}`, {
            method: "DELETE",
            headers: getHeaders()
        });
        if (!response.ok) throw new Error("Could not delete product");
        showToast("Product catalog entry deleted");
        await loadAdminDashboard();
    } catch (error) {
        showToast(error.message, "error");
    }
}

window.openRestockModal = function(sku, currentQty) {
    document.getElementById("restock-sku").value = sku;
    document.getElementById("restock-qty").value = currentQty;
    document.getElementById("restock-modal").classList.remove("hidden");
    document.getElementById("restock-modal").classList.add("flex");
}

window.closeRestockModal = function() {
    document.getElementById("restock-modal").classList.add("hidden");
    document.getElementById("restock-modal").classList.remove("flex");
}

window.handleRestockSubmit = async function(event) {
    event.preventDefault();
    const skuCode = document.getElementById("restock-sku").value;
    const quantity = parseInt(document.getElementById("restock-qty").value);

    try {
        const response = await fetch(`${API_BASE}/api/v1/inventory/update`, {
            method: "PUT",
            headers: getHeaders(),
            body: JSON.stringify({ skuCode, quantity })
        });
        if (!response.ok) {
            const err = await response.json();
            throw new Error(err.message || "Failed to update inventory stock");
        }
        showToast("Stock updated successfully!");
        closeRestockModal();
        await loadAdminDashboard();
    } catch (error) {
        showToast(error.message, "error");
    }
}

// ---------------- ENTRY INITIALIZATION ----------------
window.addEventListener("DOMContentLoaded", () => {
    // Nav Click Handlers
    document.getElementById("nav-link-admin").addEventListener("click", () => navigateTo("admin-dashboard"));
    document.getElementById("nav-link-catalog").addEventListener("click", () => navigateTo("customer-catalog"));
    document.getElementById("nav-link-orders").addEventListener("click", () => navigateTo("customer-orders"));
    document.getElementById("nav-logout").addEventListener("click", handleLogout);
    
    // Auth Forms Handlers
    document.getElementById("login-form").addEventListener("submit", (e) => {
        e.preventDefault();
        const u = document.getElementById("login-username").value.trim();
        const p = document.getElementById("login-password").value;
        handleLogin(u, p);
    });

    document.getElementById("register-form").addEventListener("submit", (e) => {
        e.preventDefault();
        const u = document.getElementById("reg-username").value.trim();
        const em = document.getElementById("reg-email").value.trim();
        const p = document.getElementById("reg-password").value;
        const r = document.getElementById("reg-role").value;
        handleRegister(u, em, p, r);
    });

    // Toggle forms
    document.getElementById("show-register").addEventListener("click", () => {
        document.getElementById("login-box").classList.add("hidden");
        document.getElementById("register-box").classList.remove("hidden");
    });
    document.getElementById("show-login").addEventListener("click", () => {
        document.getElementById("register-box").classList.add("hidden");
        document.getElementById("login-box").classList.remove("hidden");
    });

    // Catalog search
    document.getElementById("catalog-search-btn").addEventListener("click", loadCatalog);
    document.getElementById("catalog-search-input").addEventListener("keyup", (e) => {
        if (e.key === "Enter") loadCatalog();
    });

    // Checkout
    document.getElementById("btn-checkout").addEventListener("click", handleCheckout);

    // Initial View routing
    if (state.token && state.user) {
        if (state.user.role === "ADMIN") {
            navigateTo("admin-dashboard");
        } else {
            navigateTo("customer-catalog");
        }
    } else {
        navigateTo("login");
    }
});
