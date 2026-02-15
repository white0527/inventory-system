// === 全局變數 ===
const API_BASE = ""; 
let db;

// === 1. 初始化：資料庫與網頁載入 ===
document.addEventListener("DOMContentLoaded", async () => {
    // A. 初始化本地資料庫 (IndexedDB)
    await initDB();

    // B. 自動執行一次同步 (背景執行，不影響使用)
    syncProducts();

    // C. 設定日期與銷貨單
    const today = new Date().toISOString().split('T')[0];
    if(document.getElementById('sys-date')) document.getElementById('sys-date').innerText = today;
    if(document.getElementById('sales-list')) addSalesRow();
});

// 初始化 IndexedDB
function initDB() {
    return new Promise((resolve) => {
        const request = indexedDB.open("XiangYiDB", 1);
        request.onupgradeneeded = (e) => {
            const database = e.target.result;
            if (!database.objectStoreNames.contains("products")) {
                database.createObjectStore("products", { keyPath: "id" });
                // 建立索引方便搜尋
                const store = database.transaction.objectStore("products");
                store.createIndex("code", "code", { unique: false });
            }
        };
        request.onsuccess = (e) => {
            db = e.target.result;
            resolve();
        };
    });
}

// === 2. 核心：增量同步功能 ===
async function syncProducts() {
    const lastSync = localStorage.getItem('lastSyncTime') || "";
    try {
        // 呼叫我們新寫的增量下載 API
        const response = await fetch(`${API_BASE}/api/sync/download?lastSyncTime=${lastSync}`);
        if (response.ok) {
            const updates = await response.json();
            if (updates.length > 0) {
                const tx = db.transaction("products", "readwrite");
                const store = tx.objectStore("products");
                updates.forEach(p => store.put(p)); // 存入手機
                localStorage.setItem('lastSyncTime', new Date().toISOString());
                console.log(`成功同步 ${updates.length} 筆資料`);
            }
            // 隱藏紅條警告
            if(document.querySelector('.offline-banner')) document.querySelector('.offline-banner').style.display = 'none';
        }
    } catch (e) {
        console.log("目前處於離線模式，使用本地資料。");
    }
}

// === 3. 查詢功能 (改為查手機本地，秒出結果) ===
async function fetchProductInfo(input) {
    const code = input.value.trim();
    if (!code) return;
    
    const row = input.parentElement.parentElement;
    const nameSpan = row.querySelector('.p-name');
    const priceInput = row.querySelector('.p-price');

    // 直接從 IndexedDB 找
    const tx = db.transaction("products", "readonly");
    const store = tx.objectStore("products");
    const index = store.index("code");
    const request = index.get(code);

    request.onsuccess = () => {
        const product = request.result;
        if (product) {
            nameSpan.innerText = product.name;
            priceInput.value = product.pricePeer; // 使用車行價
            calcRow(input);
            row.querySelector('.p-qty').focus();
        } else {
            nameSpan.innerText = "❌ 查無此代號";
        }
    };
}

// === 4. 銷貨與計算邏輯 (維持不變但優化) ===
function addSalesRow() {
    const tbody = document.getElementById("sales-list");
    if(!tbody) return;
    const tr = document.createElement("tr");
    const index = tbody.children.length + 1;
    tr.innerHTML = `
        <td style="text-align:center;">${index}</td>
        <td><input type="text" class="p-code" onchange="fetchProductInfo(this)" placeholder="輸入代號"></td>
        <td><span class="p-name"></span></td>
        <td><input type="number" class="p-qty" value="1" onchange="calcRow(this)"></td>
        <td><input type="number" class="p-price" value="0" onchange="calcRow(this)"></td>
        <td><span class="p-total">0</span></td>
        <td><button onclick="deleteRow(this)">❌</button></td>
    `;
    tbody.appendChild(tr);
}

function calcRow(ele) {
    const row = ele.parentElement.parentElement;
    const qty = parseFloat(row.querySelector('.p-qty').value) || 0;
    const price = parseFloat(row.querySelector('.p-price').value) || 0;
    row.querySelector('.p-total').innerText = Math.round(qty * price);
    calcTotal();
}

function calcTotal() {
    let grandTotal = 0;
    document.querySelectorAll('.p-total').forEach(span => grandTotal += parseFloat(span.innerText) || 0);
    if(document.getElementById('sales-total')) document.getElementById('sales-total').innerText = '$' + grandTotal.toLocaleString();
}