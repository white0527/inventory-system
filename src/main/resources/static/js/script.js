// === 全局變數 ===
const API_BASE = ""; 
let db;

// === 1. 初始化：網頁載入完成後執行 ===
document.addEventListener("DOMContentLoaded", async () => {
    // A. 檢查是否登入
    checkLoginStatus();

    // B. 初始化本地資料庫
    await initDB();

    // C. 如果已登入，執行背景同步
    if (localStorage.getItem('isLoggedIn')) {
        syncProducts();
    }

    // D. 設定 UI 初始狀態
    const today = new Date().toISOString().split('T')[0];
    if(document.getElementById('sys-date')) document.getElementById('sys-date').innerText = today;
    if(document.getElementById('sales-list')) addSalesRow();
});

// === 2. 登入功能 (修正 405 錯誤) ===
async function handleLogin() {
    const userEl = document.getElementById('username');
    const passEl = document.getElementById('password');
    if(!userEl || !passEl) return;

    const loginData = {
        username: userEl.value,
        password: passEl.value
    };

    try {
        // 確保網址開頭有 /api，對應 LoginController
        const response = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(loginData)
        });

        const result = await response.json();

        if (response.ok && result.success) {
            localStorage.setItem('isLoggedIn', 'true');
            localStorage.setItem('userRole', result.role);
            localStorage.setItem('username', result.username);
            location.reload(); // 重新整理進入系統
        } else {
            alert(result.message || "登入失敗");
        }
    } catch (e) {
        console.error("登入請求出錯:", e);
        alert("伺服器連線失敗，請稍後再試");
    }
}

function checkLoginStatus() {
    const loginSection = document.getElementById('login-section'); // 請確認 index.html 裡登入區塊的 ID
    const mainSection = document.getElementById('main-system');   // 請確認主系統區塊的 ID
    
    if (localStorage.getItem('isLoggedIn')) {
        if(loginSection) loginSection.style.display = 'none';
        if(mainSection) mainSection.style.display = 'block';
    } else {
        if(loginSection) loginSection.style.display = 'block';
        if(mainSection) mainSection.style.display = 'none';
    }
}

function logout() {
    localStorage.clear();
    location.reload();
}

// === 3. 離線資料庫與同步邏輯 ===
function initDB() {
    return new Promise((resolve) => {
        const request = indexedDB.open("XiangYiDB", 1);
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (!database.objectStoreNames.contains("products")) {
                const store = database.createObjectStore("products", { keyPath: "id" });
                store.createIndex("code", "code", { unique: false });
            }
        };
        request.onsuccess = (e) => {
            db = e.target.result;
            resolve();
        };
    });
}

async function syncProducts() {
    const lastSync = localStorage.getItem('lastSyncTime') || "";
    try {
        // 使用我們新寫的增量下載接口
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        if (response.ok) {
            const updates = await response.json();
            if (updates.length > 0) {
                const tx = db.transaction("products", "readwrite");
                const store = tx.objectStore("products");
                updates.forEach(p => store.put(p)); 
                localStorage.setItem('lastSyncTime', new Date().toISOString());
                console.log(`同步完成：更新 ${updates.length} 筆`);
            }
            // 隱藏斷線紅條
            const banner = document.querySelector('.offline-banner');
            if(banner) banner.style.display = 'none';
        }
    } catch (e) {
        console.warn("離線中，使用本地庫存資料");
    }
}

// === 4. 查價邏輯 (秒出結果) ===
async function fetchProductInfo(input) {
    const code = input.value.trim();
    if (!code || !db) return;
    
    const row = input.parentElement.parentElement;
    const nameSpan = row.querySelector('.p-name');
    const priceInput = row.querySelector('.p-price');

    // 優先搜尋本地 IndexedDB
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const index = store.index("code");
    const request = index.get(code);

    request.onsuccess = () => {
        const p = request.result;
        if (p) {
            nameSpan.innerText = p.name;
            priceInput.value = p.pricePeer; // 載入車行價
            calcRow(input);
            row.querySelector('.p-qty').focus();
        } else {
            nameSpan.innerText = "❌ 查無此代號";
        }
    };
}

// ... 其餘 addSalesRow, calcRow, calcTotal 邏輯維持原樣 ...