const API_BASE = ""; 
let db;

document.addEventListener("DOMContentLoaded", async () => {
    await initDB();
    checkLoginStatus();
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

function initDB() {
    return new Promise((resolve, reject) => {
        const request = indexedDB.open("XiangYiDB", 4); // 版本更新以支援新邏輯
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (database.objectStoreNames.contains("products")) {
                database.deleteObjectStore("products");
            }
            const store = database.createObjectStore("products", { keyPath: "id" });
            store.createIndex("code", "code", { unique: false });
            store.createIndex("name", "name", { unique: false });
        };
        request.onsuccess = (e) => { db = e.target.result; resolve(); };
        request.onerror = () => reject("DB Error");
    });
}

async function syncProducts() {
    const status = document.getElementById('sync-status');
    const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z";

    try {
        if (status) status.innerText = "⏳ 正在同步最新零件資料...";
        // 增量同步請求
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        const updates = await response.json();

        if (updates.length > 0) {
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            
            updates.forEach(p => {
                if (p.isDeleted) {
                    store.delete(p.id); // 處理軟刪除
                } else {
                    store.put(p); // 存入本機硬碟
                }
            });
            
            localStorage.setItem('lastSyncTime', new Date().toISOString());
            if (status) status.innerText = `✅ 已同步 ${updates.length} 筆變更`;
        } else {
            if (status) status.innerText = "✅ 資料已是最新";
        }
        setTimeout(() => { if(status) status.style.display='none'; }, 3000);
    } catch (e) {
        if (status) status.innerText = "⚠️ 離線模式：目前使用本機資料";
    }
}

async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }
    
    // 完全脫離網路，秒級搜尋本地資料庫
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const request = store.getAll(); 

    request.onsuccess = () => {
        const filtered = request.result.filter(p => 
            (p.code && p.code.toLowerCase().includes(keyword)) || 
            (p.name && p.name.toLowerCase().includes(keyword)) ||
            (p.carModel && p.carModel.toLowerCase().includes(keyword))
        ).slice(0, 50); 

        resultList.innerHTML = filtered.map(p => `
            <div class="product-card" style="background:white; padding:15px; border-radius:10px; margin-bottom:10px; box-shadow:0 2px 5px rgba(0,0,0,0.1);">
                <div style="font-weight:bold;">${p.name}</div>
                <div style="font-size:13px; color:gray;">代號: ${p.code} | 車種: ${p.carModel}</div>
                <div style="display:flex; justify-content:space-between; margin-top:10px; border-top:1px solid #eee; padding-top:10px;">
                    <div style="color:#007AFF;">車行價: $${p.pricePeer}</div>
                    <div style="color:#28a745;">零售價: $${p.priceRetail}</div>
                </div>
            </div>
        `).join('');
    };
}

// 登入、登出與切換畫面邏輯... (維持不變)