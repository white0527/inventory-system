// === 全局變數 ===
const API_BASE = ""; 
let db;

// === 1. 初始化：網頁載入完成後執行 ===
document.addEventListener("DOMContentLoaded", async () => {
    // A. 初始化本地資料庫
    await initDB();
    
    // B. 檢查登入狀態並切換畫面
    checkLoginStatus();

    // C. 如果已登入，執行背景同步
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

// === 2. 本地資料庫邏輯 (IndexedDB) ===
function initDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("XiangYiDB", 2); // 升級版本號強制更新索引
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            const store = database.createObjectStore("products", { keyPath: "id" });
            store.createIndex("code", "code", { unique: false });
            store.createIndex("name", "name", { unique: false });
        };
        request.onsuccess = (e) => {
            db = e.target.result;
            resolve();
        };
        request.onerror = (e) => reject("DB Error");
    });
}

// === 3. 登入功能 (修正 is not defined 錯誤) ===
async function handleLogin() {
    const userEl = document.getElementById('username');
    const passEl = document.getElementById('password');
    const btn = document.querySelector('.login-btn');

    if (!userEl.value || !passEl.value) {
        alert("請輸入帳號和密碼");
        return;
    }

    btn.innerText = "驗證中...";
    btn.disabled = true;

    try {
        // 呼叫 LoginController.java
        const response = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                username: userEl.value,
                password: passEl.value
            })
        });

        const result = await response.json();

        if (response.ok && result.success) {
            localStorage.setItem('isLoggedIn', 'true');
            localStorage.setItem('userRole', result.role);
            
            // 登入成功，切換畫面並開始同步
            checkLoginStatus();
            syncProducts();
        } else {
            alert("❌ " + (result.message || "登入失敗"));
            btn.innerText = "登入系統";
            btn.disabled = false;
        }
    } catch (e) {
        console.error("登入錯誤:", e);
        alert("⚠️ 伺服器連線失敗，請檢查網路或 Render 狀態");
        btn.innerText = "登入系統";
        btn.disabled = false;
    }
}

// 畫面切換邏輯
function checkLoginStatus() {
    const loginSection = document.getElementById('login-section');
    const mainSystem = document.getElementById('main-system');
    
    if (localStorage.getItem('isLoggedIn') === 'true') {
        if(loginSection) loginSection.style.display = 'none';
        if(mainSystem) mainSystem.style.display = 'flex';
    } else {
        if(loginSection) loginSection.style.display = 'flex';
        if(mainSystem) mainSystem.style.display = 'none';
    }
}

// 登出功能
function logout() {
    if (confirm("確定要登出嗎？")) {
        localStorage.clear();
        location.reload();
    }
}

// === 4. 資料同步邏輯 (大數據下載) ===
async function syncProducts() {
    const status = document.getElementById('sync-status');
    const lastSync = localStorage.getItem('lastSyncTime') || "";
    try {
        status.innerText = "⏳ 正在從雲端更新三萬筆零件資料...";
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        if (!response.ok) throw new Error("網路連線不穩");

        const updates = await response.json();
        if (updates.length > 0) {
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            updates.forEach(p => store.put(p)); 
            localStorage.setItem('lastSyncTime', new Date().toISOString());
            status.innerText = `✅ 同步完成 (共 ${updates.length} 筆)`;
            setTimeout(() => status.style.display = 'none', 3000);
        } else {
            status.innerText = "✅ 資料已是最新";
            setTimeout(() => status.style.display = 'none', 2000);
        }
    } catch (e) {
        status.innerText = "⚠️ 離線模式：目前無法更新資料";
        status.style.background = "#fee2e2";
    }
}

// === 5. 搜尋功能 (支援關鍵字模糊搜尋) ===
async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }
    
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const request = store.getAll(); 

    request.onsuccess = () => {
        const allProducts = request.result;
        const filtered = allProducts.filter(p => 
            (p.code && p.code.toLowerCase().includes(keyword)) || 
            (p.name && p.name.toLowerCase().includes(keyword)) ||
            (p.carModel && p.carModel.toLowerCase().includes(keyword))
        ).slice(0, 50); 

        resultList.innerHTML = filtered.map(p => `
            <div style="background:white; padding:15px; border-radius:10px; margin-bottom:10px; box-shadow:0 2px 5px rgba(0,0,0,0.1);">
                <div style="font-weight:bold; color:#1e293b;">${p.name}</div>
                <div style="font-size:13px; color:#64748b;">代號: ${p.code} | 車種: ${p.carModel || '通用'}</div>
                <div style="display:flex; justify-content:space-between; margin-top:10px; border-top:1px solid #eee; padding-top:10px;">
                    <div style="color:#007AFF;">車行價: $${p.pricePeer}</div>
                    <div style="color:#28a745;">零售價: $${p.priceRetail}</div>
                    <div style="color:${p.stock < 5 ? 'red' : 'gray'};">庫存: ${p.stock}</div>
                </div>
            </div>
        `).join('');
    };
}