// === 1. 初始化與同步觸發 ===
document.addEventListener("DOMContentLoaded", async () => {
    await initDB(); // 初始化 IndexedDB
    checkLoginStatus(); // 檢查登入狀態

    // 如果已登入，自動執行增量同步
    if (localStorage.getItem('isLoggedIn') === 'true') {
        syncProducts();
    }
});

// === 2. 增量同步邏輯 (您剛提供的部分) ===
async function syncProducts() {
    const status = document.getElementById('sync-status');
    const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z"; 

    try {
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        const updates = await response.json();

        if (updates.length > 0) {
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            
            // 批次寫入手機 IndexedDB
            updates.forEach(p => store.put(p)); 
            
            localStorage.setItem('lastSyncTime', new Date().toISOString());
            if (status) status.innerText = `✅ 同步成功：更新 ${updates.length} 筆資料`;
        } else {
            if (status) status.innerText = "✅ 本機資料已是最新";
        }
    } catch (e) {
        if (status) status.innerText = "⚠️ 離線查價模式";
    }
}

// === 3. 搜尋邏輯：直接翻手機硬碟 (IndexedDB) ===
async function fetchProductInfo(input) {
    const keyword = input.value.trim().toLowerCase();
    const resultList = document.getElementById('inventory-list');
    if (!keyword || !db) { resultList.innerHTML = ""; return; }

    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const request = store.getAll(); 

    request.onsuccess = () => {
        const allProducts = request.result;
        // 搜尋中文欄位對齊後的變數
        const filtered = allProducts.filter(p => 
            (p.code && p.code.toLowerCase().includes(keyword)) || 
            (p.name && p.name.toLowerCase().includes(keyword)) ||
            (p.carModel && p.carModel.toLowerCase().includes(keyword))
        ).slice(0, 50); // 限制顯示 50 筆，保持流暢

        resultList.innerHTML = filtered.map(p => `
            <div class="product-card">
                <div style="font-weight:bold;">${p.name}</div>
                <div style="font-size:13px; color:gray;">代號: ${p.code} | 車種: ${p.carModel}</div>
                <div style="display:flex; justify-content:space-between; margin-top:10px;">
                    <div style="color:#007AFF;">車行價: $${p.pricePeer}</div>
                    <div style="color:#28a745;">零售價: $${p.priceRetail}</div>
                    <div style="color:gray;">庫存: ${p.stock}</div>
                </div>
            </div>
        `).join('');
    };
}

// === 4. 修正 handleLogin 定義問題 ===
async function handleLogin() {
    // ... 您的登入邏輯 ...
}

// 強制掛載，解決按鈕找不到 handleLogin 的錯誤
window.handleLogin = handleLogin;
window.fetchProductInfo = fetchProductInfo;