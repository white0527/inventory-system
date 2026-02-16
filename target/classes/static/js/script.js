// ...前面的 DB 初始化代碼保持不變

async function syncProducts() {
    const status = document.getElementById('sync-status');
    const lastSync = localStorage.getItem('lastSyncTime') || "1970-01-01T00:00:00Z"; 

    try {
        // 向伺服器索取增量資料
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        const updates = await response.json();

        if (updates.length > 0) {
            const tx = db.transaction("products", "readwrite");
            const store = tx.objectStore("products");
            
            // 存入手機本機資料庫
            updates.forEach(p => store.put(p)); 
            
            localStorage.setItem('lastSyncTime', new Date().toISOString());
            status.innerText = `✅ 同步成功：更新 ${updates.length} 筆資料`;
        } else {
            status.innerText = "✅ 本機資料已是最新";
        }
    } catch (e) {
        status.innerText = "⚠️ 離線查價模式";
    }
}