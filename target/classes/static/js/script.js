// === 全局變數 ===
const API_BASE = ""; 
let db;

// === 1. 初始化：網頁載入完成後執行 ===
document.addEventListener("DOMContentLoaded", async () => {
    // 初始化 IndexedDB 本機資料庫
    await initDB();
    
    // 檢查登入狀態並切換畫面
    checkLoginStatus();

    // 如果已登入，執行「增量同步」
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

// === 2. 本地資料庫邏輯 (IndexedDB) ===
function initDB() {
    return new Promise((resolve, reject) => {
        // 版本號設為 3 以確保支援新欄位
        const request = indexedDB.open("XiangYiDB", 3); 
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            // 使用 id 作為 KeyPath
            const store = database.createObjectStore("products", { keyPath: "id" });
            // 建立索引加速搜尋
            store.createIndex("code", "code", { unique: false });
            store.createIndex("name", "name", { unique: false });
            store.createIndex("carModel", "carModel", { unique: false });
        };
        request.onsuccess = (e) => {
            db = e.target.result;
            resolve();
        };
        request.onerror = (e) => reject("DB Error");
    });
}

// === 3. 增量同步邏輯 (只抓更改項) ===
async function syncProducts() {
    const status = document.getElementById('sync-status');
    // 取得上次同步時間，若無則從 1970 年開始 (即全量下載)
    const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z"; 

    try {
        if (status) {
            status.style.display = 'block';
            status.innerText = "⏳ 正在檢查雲端更新...";
        }

        // 帶上時間戳記，只索取變動項目
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        if (!response.ok) throw new Error("同步失敗");

        const updates = await response.json();

        if (updates && updates.length > 0) {
            if (status) status.innerText = `🔄 偵測到 ${updates.length} 項變動，同步中...`;
            
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            
            // put 會自動處理：ID 存在則更新，不存在則新增
            updates.forEach(p => store.put(p)); 
            
            // 更新本機的同步標記時間
            localStorage.setItem('lastSyncTime', new Date().toISOString());
            if (status) status.innerText = `✅ 已同步 ${updates.length} 筆變更`;
        } else {
            if (status) status.innerText = "✅ 本機資料已是最新";
        }
        setTimeout(() => { if(status) status.style.display = 'none'; }, 3000);
    } catch (e) {
        console.error("同步錯誤:", e);
        if (status) {
            status.innerText = "⚠️ 離線模式：目前使用本機快取";
            status.style.background = "#fee2e2";
        }
    }
}

// === 4. 登入功能 ===
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
            checkLoginStatus();
            syncProducts();
        } else {
            alert("❌ " + (result.message || "登入失敗"));
            btn.innerText = "登入系統";
            btn.disabled = false;
        }
    } catch (e) {
        alert("⚠️ 伺服器連線失敗");
        btn.innerText = "登入系統";
        btn.disabled = false;
    }
}

// === 5. 搜尋功能 (完全讀取本地 IndexedDB) ===
async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }
    
    // 直接在本地資料庫查詢，不經過網路
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const request = store.getAll(); 

    request.onsuccess = () => {
        const allProducts = request.result;
        const filtered = allProducts.filter(p => 
            (p.code && p.code.toLowerCase().includes(keyword)) || 
            (p.name && p.name.toLowerCase().includes(keyword)) ||
            (p.carModel && p.carModel.toLowerCase().includes(keyword))
        ).slice(0, 50); // 限制顯示數量以保持流暢

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

// 介面與登出邏輯
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

function logout() {
    if (confirm("確定要登出嗎？")) {
        localStorage.clear();
        location.reload();
    }
}