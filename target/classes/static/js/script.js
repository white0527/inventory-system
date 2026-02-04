// === 全局變數 ===
const API_BASE = ""; // Render 環境通常留空即可

// === 初始化：網頁載入完成後執行 ===
document.addEventListener("DOMContentLoaded", () => {
    // 1. 設定今天日期
    const today = new Date().toISOString().split('T')[0];
    if(document.getElementById('sys-date')) document.getElementById('sys-date').innerText = today;
    if(document.getElementById('sales-date')) document.getElementById('sales-date').value = today;
    if(document.getElementById('purchase-date')) document.getElementById('purchase-date').value = today;

    // 2. 自動幫銷貨單「新增第一行」，不然會空空的
    if(document.getElementById('sales-list')) {
        addSalesRow();
    }
});

// === 1. 分頁切換功能 ===
function switchTab(tabName) {
    // 移除所有 active 狀態
    document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
    document.querySelectorAll('.page-section').forEach(p => p.classList.remove('active'));
    
    // 加上新的 active 狀態
    const navBtn = event.currentTarget; // 點擊的那個按鈕
    if(navBtn) navBtn.classList.add('active');
    
    const targetTab = document.getElementById('tab-' + tabName);
    if(targetTab) targetTab.classList.add('active');

    // 更新標題
    const titles = {
        'sales': '門市銷貨作業',
        'purchase': '進貨入庫作業',
        'inventory': '庫存查詢系統',
        'customer': '客戶資料維護',
        'settings': '系統參數設定'
    };
    if(document.getElementById('page-title')) {
        document.getElementById('page-title').innerText = titles[tabName] || '作業系統';
    }
}

// === 2. 銷貨功能 (核心) ===

// 新增一行 (解決您按不出來的問題)
function addSalesRow() {
    const tbody = document.getElementById("sales-list");
    if(!tbody) return; // 如果找不到表格就停止

    const tr = document.createElement("tr");
    const index = tbody.children.length + 1; // 算出序號

    tr.innerHTML = `
        <td style="text-align:center; color:#64748b;">${index}</td>
        <td><input type="text" class="p-code" onchange="fetchProductInfo(this)" placeholder="掃描或輸入代號" style="width:100%"></td>
        <td><span class="p-name" style="color:#334155; font-size:14px;"></span></td>
        <td><input type="number" class="p-qty" value="1" onchange="calcRow(this)" style="width:60px; text-align:center;"></td>
        <td><input type="number" class="p-price" value="0" onchange="calcRow(this)" style="width:80px"></td>
        <td><span class="p-total" style="font-weight:bold; color:#3b82f6">0</span></td>
        <td style="text-align:center">
            <button onclick="deleteRow(this)" style="color:#ef4444; background:none; border:none; cursor:pointer; font-size:16px;">
                <i class="fas fa-times-circle"></i>
            </button>
        </td>
    `;
    tbody.appendChild(tr);
    
    // 自動聚焦到新格子的輸入框
    setTimeout(() => tr.querySelector('.p-code').focus(), 100);
}

// 刪除一行
function deleteRow(btn) {
    const row = btn.parentElement.parentElement;
    row.remove();
    calcTotal();
}

// 查詢商品 (掃描後自動帶出資料)
async function fetchProductInfo(input) {
    const code = input.value.trim();
    if (!code) return;
    
    const row = input.parentElement.parentElement;
    const nameSpan = row.querySelector('.p-name');
    const priceInput = row.querySelector('.p-price');

    // 顯示載入中...
    nameSpan.innerText = "🔍 查詢中...";

    try {
        const response = await fetch(`${API_BASE}/api/products/${code}`);
        if (response.ok) {
            const product = await response.json();
            nameSpan.innerText = product.name; 
            priceInput.value = product.price;  
            calcRow(input); // 算錢
            
            // 查到後，自動跳去輸入數量
            row.querySelector('.p-qty').focus();
            row.querySelector('.p-qty').select();
        } else {
            nameSpan.innerText = "❌ 查無商品";
            nameSpan.style.color = "#ef4444";
            priceInput.value = 0;
        }
    } catch (e) {
        nameSpan.innerText = "⚠️ 連線失敗";
        console.error(e);
    }
}

// 計算單行小計
function calcRow(ele) {
    const row = ele.parentElement.parentElement;
    const qty = parseFloat(row.querySelector('.p-qty').value) || 0;
    const price = parseFloat(row.querySelector('.p-price').value) || 0;
    const total = Math.round(qty * price);
    
    row.querySelector('.p-total').innerText = total;
    calcTotal();
}

// 計算總金額 (更新右上角大數字)
function calcTotal() {
    let grandTotal = 0;
    document.querySelectorAll('.p-total').forEach(span => {
        grandTotal += parseFloat(span.innerText) || 0;
    });
    
    const displayElement = document.getElementById('sales-total');
    if(displayElement) {
        // 加上千分位符號 (ex: $1,200)
        displayElement.innerText = '$' + grandTotal.toLocaleString();
    }
}

// 存檔 (送出訂單)
async function saveSales(printMode) {
    const customerCode = document.getElementById("sales-cust").value || "GUEST";
    const date = document.getElementById("sales-date").value;
    const items = [];

    document.querySelectorAll("#sales-list tr").forEach(row => {
        const code = row.querySelector(".p-code").value;
        if (code) {
            items.push({
                productCode: code,
                quantity: parseInt(row.querySelector(".p-qty").value) || 0,
                price: parseFloat(row.querySelector(".p-price").value) || 0,
                amount: parseFloat(row.querySelector(".p-total").innerText) || 0
            });
        }
    });

    if (items.length === 0) {
        alert("⚠️ 請至少輸入一項商品！");
        return;
    }

    const orderData = { customerCode, date, items };

    try {
        const response = await fetch(`${API_BASE}/api/sales`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            const result = await response.json();
            alert("✅ 存檔成功！單號：" + (result.orderNumber || "New"));
            if (printMode) window.print();
            location.reload();
        } else {
            alert("❌ 存檔失敗");
        }
    } catch (e) {
        alert("連線錯誤: " + e);
    }
}

// === 3. 庫存查詢 ===
async function searchInventory() {
    const keyword = document.getElementById('inv-search').value.trim();
    const tbody = document.getElementById('inventory-list');
    
    if(!keyword) {
        alert("請輸入商品代號！");
        return;
    }

    tbody.innerHTML = "<tr><td colspan='6' style='text-align:center;'>查詢中...</td></tr>";

    try {
        const response = await fetch(`${API_BASE}/api/products/${keyword}`);
        if (response.ok) {
            const p = await response.json();
            tbody.innerHTML = `
                <tr>
                    <td style="font-weight:bold;">${p.code}</td>
                    <td>${p.name}</td>
                    <td>${p.carType || '-'}</td>
                    <td style="color:${p.stock < 5 ? 'red' : 'green'}">${p.stock}</td>
                    <td>$${p.price}</td>
                    <td>${p.location || 'A-01'}</td>
                </tr>`;
        } else {
            tbody.innerHTML = "<tr><td colspan='6' style='text-align:center; color:red;'>查無資料</td></tr>";
        }
    } catch (e) {
        tbody.innerHTML = "<tr><td colspan='6' style='text-align:center; color:red;'>連線失敗</td></tr>";
    }
}