// src/main/resources/static/js/sales.js

// === 銷貨登記模組 ===

// 初始化：預設加 8 行
document.addEventListener("DOMContentLoaded", () => {
    if(document.getElementById('sales-list')) {
        for(let i=0; i<8; i++) addSalesRow();
    }
});

// 新增一行 (含所有欄位)
function addSalesRow() {
    const tbody = document.getElementById("sales-list");
    if(!tbody) return;
    
    const tr = document.createElement("tr");
    const index = tbody.children.length + 1;

    tr.innerHTML = `
        <td style="text-align:center; background:#f8fafc;">${index}</td>
        <td><input type="text" class="p-code" onchange="fetchProductInfo(this)"></td>
        <td><input type="text" class="p-car"></td>
        <td><input type="text" class="p-name"></td>
        <td><input type="number" class="p-qty" value="" onchange="calcRow(this)" style="text-align:center;"></td>
        <td><input type="number" class="p-price" value="" onchange="calcRow(this)" style="text-align:right;"></td>
        <td class="price-col"><input type="text" class="p-total" readonly style="font-weight:bold; color:#059669; text-align:right;"></td>
        <td><input type="text" class="p-note"></td>
        <td><input type="text" class="p-loc" style="text-align:center;"></td>
    `;
    tbody.appendChild(tr);
}

// 查詢商品 API
async function fetchProductInfo(input) {
    const code = input.value.trim();
    if (!code) return;
    const row = input.parentElement.parentElement;
    
    try {
        row.querySelector('.p-name').value = "查詢中...";
        const res = await fetch(`${API_BASE}/api/products/${code}`);
        if(res.ok) {
            const p = await res.json();
            row.querySelector('.p-name').value = p.name || '';
            row.querySelector('.p-car').value = p.carType || '';
            row.querySelector('.p-loc').value = p.location || '';
            row.querySelector('.p-price').value = p.price || 0;
            
            const qty = row.querySelector('.p-qty');
            if(!qty.value) qty.value = 1;
            
            calcRow(input);
            qty.focus(); 
            qty.select();
        } else {
             row.querySelector('.p-name').value = "無此商品";
        }
    } catch(e) { console.error(e); }
}

// 計算單行金額 (並觸發總金額計算)
function calcRow(ele) {
    const row = ele.parentElement.parentElement;
    const qty = parseFloat(row.querySelector('.p-qty').value) || 0;
    const price = parseFloat(row.querySelector('.p-price').value) || 0;
    const total = Math.round(qty * price);
    
    // 顯示小計 (如果是 0 就不顯示，保持畫面乾淨)
    row.querySelector('.p-total').value = total > 0 ? total : '';

    // ★ 關鍵：每次算完單行，都要重新算一次整張單的總金額
    calcTotal();
}

// ★ 新增：計算整張單總金額 (更新右上角大數字)
function calcTotal() {
    let grandTotal = 0;
    document.querySelectorAll('.p-total').forEach(input => {
        grandTotal += parseFloat(input.value) || 0; // 如果是空字串就當 0
    });
    
    // 更新右上角的大數字顯示
    const displayEl = document.getElementById('sales-total-display');
    if(displayEl) {
        // 使用 toLocaleString() 加上千分位逗號 (例如 $1,200)
        displayEl.innerText = '$' + grandTotal.toLocaleString();
    }
}

function changePriceType(select) {
    alert("已切換為 [" + select.options[select.selectedIndex].text + "] 模式");
}

// 存檔 (整合新欄位)
async function saveSales(printMode) {
    const items = [];
    document.querySelectorAll("#sales-list tr").forEach(row => {
        const code = row.querySelector(".p-code").value;
        const name = row.querySelector(".p-name").value;
        
        // 只要有代號或名稱就算有效
        if (code || name) {
            items.push({
                productCode: code || "MANUAL",
                productName: name,
                carType: row.querySelector(".p-car").value,
                quantity: parseInt(row.querySelector(".p-qty").value) || 0,
                price: parseFloat(row.querySelector(".p-price").value) || 0,
                amount: parseFloat(row.querySelector(".p-total").value) || 0,
                note: row.querySelector(".p-note").value,
                location: row.querySelector(".p-loc").value
            });
        }
    });

    if (items.length === 0) { alert("無資料可存檔"); return; }

    // 收集單頭資料
    const orderData = {
        date: document.getElementById("sales-date").value,
        billingMonth: document.getElementById("billing-month").value,
        customerCode: document.getElementById("sales-cust").value,
        customerName: document.getElementById("sales-cust-name").value,
        customerAddress: document.getElementById("sales-addr").value,
        creator: "管理員", 
        items: items
        // 注意：底部的備註、稅金等欄位，目前後端還沒準備好接收
        // 等之後後端更新後，再把 footer-note 等欄位加進來
    };

    try {
        const res = await fetch(`${API_BASE}/api/sales`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(orderData)
        });

        if(res.ok) {
            const ret = await res.json();
            alert("✅ 存檔成功！\n單號：" + ret.orderNumber);
            if(printMode) window.print();
            location.reload();
        } else {
            alert("❌ 存檔失敗，請檢查後端連線");
        }
    } catch(e) { 
        console.error(e);
        alert("⚠️ 連線錯誤：" + e); 
    }
}

function clearOrder() {
    if(confirm("確定要刪除整張單據並清空嗎？")) location.reload();
}