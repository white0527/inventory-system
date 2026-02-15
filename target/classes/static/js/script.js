// === 全局變數 ===
const API_BASE = ""; 
let db;

// === 1. 初始化：載入即準備本地資料庫 ===
document.addEventListener("DOMContentLoaded", async () => {
    // A. 初始化本地資料庫 (IndexedDB)
    await initDB();
    
    // B. 秒登入檢查：優先切換畫面
    checkLoginStatus();

    // C. 執行同步策略：判斷是「全量安裝」還是「增量更新」
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

// === 2. 本地資料庫邏輯 (IndexedDB) ===
function initDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("XiangYiDB", 3); 
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            const store = database.createObjectStore("products", { keyPath: "id" });
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

// === 3. 核心同步邏輯：全量下載 vs 增量更新 ===
async function syncProducts() {
    const status = document.getElementById('sync-status');
    if (!status) return;

    // 先檢查本地已經存了多少筆資料
    const tx = db.transaction("products", "readonly");
    const countRequest = tx.objectStore("products").count();

    countRequest.onsuccess = async () => {
        const localCount = countRequest.result;
        const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z";

        // 策略 A：本地沒資料 (視為剛下載 App) -> 執行「全量同步」
        if (localCount < 30000) {
            status.style.display = 'block';
            status.innerText = "🚀 首次安裝：正在搬運三萬筆零件資料至本機...";
            try {
                // 呼叫您剛寫好的 /api/sync/full-sync
                const response = await fetch(`${API_BASE}/api/sync/full-sync`);
                const allData = await response.json();
                
                const writeTx = db.transaction("products", "readwrite");
                const store = writeTx.objectStore("products");
                allData.forEach(p => store.put(p)); 
                
                localStorage.setItem('lastSyncTime', new Date().toISOString());
                status.innerText = "✨ 三萬筆資料已成功內建，搜尋秒開！";
                setTimeout(() => status.style.display = 'none', 3000);
            } catch (e) {
                status.innerText = "⚠️ 初始化失敗，請檢查網路後重新登入";
            }
            return;
        }

        // 策略 B：本地已有資料 -> 執行「增量同步」(只抓更改項)
        status.innerText = "⏳ 正在檢查雲端更新...";
        try {
            const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
            const updates = await response.json();
            
            if (updates.length > 0) {
                status.innerText = `🔄 偵測到 ${updates.length} 項變動，同步中...`;
                const writeTx = db.transaction("products", "readwrite");
                const store = writeTx.objectStore("products");
                updates.forEach(p => store.put(p)); // 覆蓋更改過的項目
                localStorage.setItem('lastSyncTime', new Date().toISOString());
                status.innerText = `✅ 已更新 ${updates.length} 筆最新資料`;
            } else {
                status.innerText = "✅ 本機資料已是最新";
            }
            setTimeout(() => status.style.display = 'none', 2000);
        } catch (e) {
            status.innerText = "⚠️ 離線模式：目前使用本機快取";
            status.style.background = "#fee2e2";
        }
    };
}

// === 4. 登入與搜尋邏輯 (維持穩定版) ===
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
            body: JSON.stringify({ username: userEl.value, password: passEl.value })
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
        alert("⚠️ 伺服器連線超時");
        btn.innerText = "登入系統";
        btn.disabled = false;
    }
}

async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }
    
    // 直接在手機本地資料庫查詢，不經過網路
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