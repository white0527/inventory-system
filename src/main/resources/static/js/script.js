const API_BASE = ""; 
let db;

// 1. 初始化
document.addEventListener("DOMContentLoaded", async () => {
    await initDB();
    checkLoginStatus(); // 修正找不到定義的問題
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

function initDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("XiangYiDB", 4);
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            database.createObjectStore("products", { keyPath: "id" });
        };
        request.onsuccess = (e) => { db = e.target.result; resolve(); };
        request.onerror = () => reject();
    });
}

// 2. 登入功能
async function handleLogin() {
    const user = document.getElementById('username').value;
    const pass = document.getElementById('password').value;
    
    try {
        const res = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        });
        const result = await res.json();
        if (result.success) {
            localStorage.setItem('isLoggedIn', 'true');
            checkLoginStatus();
            syncProducts();
        } else { alert("登入失敗"); }
    } catch (e) { alert("連線錯誤"); }
}

function checkLoginStatus() {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    document.getElementById('login-section').style.display = isLoggedIn ? 'none' : 'flex';
    document.getElementById('main-system').style.display = isLoggedIn ? 'flex' : 'none';
}

async function syncProducts() {
    const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z";
    try {
        const res = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        const updates = await res.json();
        if (updates.length > 0) {
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            updates.forEach(p => p.isDeleted ? store.delete(p.id) : store.put(p));
            localStorage.setItem('lastSyncTime', new Date().toISOString());
        }
    } catch (e) { console.log("離線模式"); }
}

async function fetchProductInfo(input) {
    const kw = input.value.trim().toLowerCase();
    if (!kw || !db) return;
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    store.getAll().onsuccess = (e) => {
        const results = e.target.result.filter(p => 
            p.name.toLowerCase().includes(kw) || p.code.toLowerCase().includes(kw)
        ).slice(0, 50);
        document.getElementById('inventory-list').innerHTML = results.map(p => `
            <div class="product-card">
                <b>${p.name}</b><br>代號: ${p.code} | 價格: $${p.pricePeer}
            </div>
        `).join('');
    };
}

function logout() {
    localStorage.clear();
    location.reload();
}