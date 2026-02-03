// src/main/resources/static/script.js

let orderItems = [];

// 初始化：自動插入第一行
document.addEventListener("DOMContentLoaded", () => {
    addNewRow();
});

// 新增一行
function addNewRow() {
    const tableBody = document.getElementById("order-list");
    const row = document.createElement("tr");
    
    // 序號 (自動計算)
    const index = tableBody.children.length + 1;

    row.innerHTML = `
        <td style="text-align:center;">${index}</td>
        <td><input type="text" class="p-code" onchange="searchProduct(this)" placeholder="掃描或輸入"></td>
        <td><span class="p-car"></span></td>
        <td><span class="p-name"></span></td>
        <td><input type="number" class="p-qty" value="1" onchange="calculateRow(this)" style="width:50px;"></td>
        <td><input type="number" class="p-price" value="0" onchange="calculateRow(this)" style="width:70px;"></td>
        <td><span class="p-total">0</span></td>
        <td><input type="text" class="p-note"></td>
        <td><button onclick="removeRow(this)" style="color:red;">X</button></td>
    `;
    tableBody.appendChild(row);
    
    // 自動聚焦到新行的商品代號
    row.querySelector(".p-code").focus();
}

// 移除行
function removeRow(btn) {
    const row = btn.parentElement.parentElement;
    row.remove();
    calculateTotal(); // 重新計算總額
}

// 搜尋商品 (模擬) - 這裡對接後端
async function searchProduct(inputElement) {
    const code = inputElement.value;
    const row = inputElement.parentElement.parentElement;

    if (!code) return;

    try {
        // 呼叫後端 API
        const response = await fetch(`/api/products/${code}`);
        if (response.ok) {
            const product = await response.json();
            row.querySelector(".p-car").innerText = product.carType || ""; // 車種
            row.querySelector(".p-name").innerText = product.name;         // 品名
            row.querySelector(".p-price").value = product.price;           // 單價
            calculateRow(inputElement); // 計算金額
        } else {
            alert("查無此商品！");
            inputElement.value = "";
        }
    } catch (e) {
        console.error("連線錯誤", e);
    }
}

// 計算單行金額
function calculateRow(element) {
    const row = element.parentElement.parentElement;
    const qty = parseFloat(row.querySelector(".p-qty").value) || 0;
    const price = parseFloat(row.querySelector(".p-price").value) || 0;
    const total = qty * price;
    
    row.querySelector(".p-total").innerText = total;
    calculateTotal();
}

// 計算整張單總金額
function calculateTotal() {
    let grandTotal = 0;
    document.querySelectorAll(".p-total").forEach(span => {
        grandTotal += parseFloat(span.innerText) || 0;
    });
    
    // 更新右上方那個大大的藍色數字
    document.getElementById("big-total-display").innerText = grandTotal;
}

// 存檔
async function saveOrder(print) {
    const customerCode = document.getElementById("customer-code").value;
    const date = document.getElementById("order-date").value;
    const items = [];

    document.querySelectorAll("#order-list tr").forEach(row => {
        const code = row.querySelector(".p-code").value;
        if (code) {
            items.push({
                productCode: code,
                quantity: parseInt(row.querySelector(".p-qty").value),
                price: parseFloat(row.querySelector(".p-price").value),
                amount: parseFloat(row.querySelector(".p-total").innerText)
            });
        }
    });

    if (items.length === 0) {
        alert("請至少輸入一項商品！");
        return;
    }

    const orderData = {
        customerCode: customerCode,
        date: date,
        items: items
    };

    try {
        const response = await fetch('/api/sales', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(orderData)
        });

        if (response.ok) {
            alert("✅ 存檔成功！");
            if (print) {
                window.print(); // 簡單呼叫瀏覽器列印
            }
            location.reload(); // 重新整理頁面
        } else {
            alert("❌ 存檔失敗！");
        }
    } catch (e) {
        alert("連線錯誤：" + e);
    }
}