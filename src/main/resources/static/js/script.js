const API_BASE = ""; 
let db;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. 強制初始化資料庫
    await initDB();
    // 2. 登入檢查與同步
    if (localStorage.getItem('isLoggedIn')) {
        syncProducts();
    }
    // 3. UI 初始化
    const today = new Date().toISOString().split('T')[0];
    if(document.getElementById('sys-date')) document.getElementById('sys-date').innerText = today;
});

function initDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("XiangYiDB", 2); // 升級版本號強制更新
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            // 砍掉舊的重新建立，確保索引生效
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            const store = database.createObjectStore("products", { keyPath: "id" });
            store.createIndex("code", "code", { unique: false });
            store.createIndex("name", "name", { unique: false }); // 加入名稱索引
        };
        request.onsuccess = (e) => {
            db = e.target.result;
            resolve();
        };
        request.onerror = (e) => reject("DB Error");
    });
}

// 增量同步優化版
async function syncProducts() {
    const status = document.getElementById('sync-status');
    const lastSync = localStorage.getItem('lastSyncTime') || "";
    try {
        status.innerText = "⏳ 正在從雲端下載三萬筆資料...";
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
        status.innerText = "⚠️ 離線模式：無法連線伺服器";
        status.style.background = "#fee2e2";
    }
}

// 搜尋功能：支援關鍵字「模糊搜尋」
async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }
    
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const request = store.getAll(); // 模糊搜尋需取得所有資料比對

    request.onsuccess = () => {
        const allProducts = request.result;
        // 搜尋代號、名稱或車種
        const filtered = allProducts.filter(p => 
            (p.code && p.code.toLowerCase().includes(keyword)) || 
            (p.name && p.name.toLowerCase().includes(keyword)) ||
            (p.carModel && p.carModel.toLowerCase().includes(keyword))
        ).slice(0, 50); // 最多顯示 50 筆避免卡頓

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