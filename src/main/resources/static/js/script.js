document.addEventListener("DOMContentLoaded", () => {
    addRow();
    document.getElementById("order_date").valueAsDate = new Date();
});

// 切換分頁
function switchView(viewName) {
    document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-btn').forEach(el => el.classList.remove('active'));

    if (viewName === 'sales') {
        document.getElementById('view-sales').classList.add('active');
    } else if (viewName === 'history') {
        document.getElementById('view-history').classList.add('active');
        loadHistory();
    } else if (viewName === 'products') {
        document.getElementById('view-products').classList.add('active');
    }
}

// 匯入 Excel
function uploadExcel() {
    const fileInput = document.getElementById("excelFile");
    const file = fileInput.files[0];
    if (!file) { alert("請先選擇檔案！"); return; }

    const formData = new FormData();
    formData.append("file", file);

    const btn = document.querySelector("#view-products .btn-green");
    btn.innerText = "⏳ 匯入中...";
    btn.disabled = true;

    fetch('/api/products/import', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(result => {
        alert(result);
        btn.innerText = "開始匯入";
        btn.disabled = false;
    })
    .catch(error => {
        console.error('Error:', error);
        alert("匯入失敗: " + error);
        btn.innerText = "開始匯入";
        btn.disabled = false;
    });
}

// 載入歷史
function loadHistory() {
    const tbody = document.getElementById("history-list-body");
    tbody.innerHTML = "<tr><td colspan='5'>⏳ 正在載入資料...</td></tr>";

    fetch('/api/sales')
        .then(response => response.json())
        .then(data => {
            tbody.innerHTML = "";
            if (!data || data.length === 0) {
                tbody.innerHTML = "<tr><td colspan='5'>沒有任何訂單資料</td></tr>";
                return;
            }
            data.forEach(order => {
                let dateStr = order.order_date;
                if (dateStr && dateStr.includes('T')) dateStr = dateStr.split('T')[0];
                
                tbody.innerHTML += `
                    <tr>
                        <td>${order.order_id}</td>
                        <td>${dateStr}</td>
                        <td>${order.customer_id || '-'}</td>
                        <td>${order.customer_address || '-'}</td>
                        <td>${order.creator || '-'}</td>
                    </tr>`;
            });
        })
        .catch(error => {
            console.error(error);
            tbody.innerHTML = `<tr><td colspan='5' style='color:red;'>載入失敗</td></tr>`;
        });
}

// 增加表格行
function addRow() {
    const tbody = document.getElementById("order-items-body");
    const row = document.createElement("tr");
    row.innerHTML = `
        <td><input type="text" name="product_id"></td>
        <td><input type="text" name="car_type"></td>
        <td><input type="text" name="product_name"></td>
        <td><input type="text" name="location"></td>
        <td><input type="number" name="quantity" value="1" style="width: 50px;" onchange="calculateRow(this)"></td>
        <td><input type="number" name="price" value="0" style="width: 80px;" onchange="calculateRow(this)"></td>
        <td><input type="number" name="amount" value="0" style="width: 80px;" readonly></td>
        <td><input type="text" name="note"></td>
        <td><button class="btn-red" onclick="removeRow(this)">刪除</button></td>
    `;
    tbody.appendChild(row);
}

function removeRow(btn) {
    const row = btn.parentNode.parentNode;
    row.parentNode.removeChild(row);
}

function calculateRow(input) {
    const row = input.parentNode.parentNode;
    const qty = row.querySelector('input[name="quantity"]').value;
    const price = row.querySelector('input[name="price"]').value;
    row.querySelector('input[name="amount"]').value = qty * price;
}

// 存檔訂單
function saveOrder() {
    const items = [];
    document.querySelectorAll("#order-items-body tr").forEach(row => {
        const item = {
            product_id: row.querySelector('input[name="product_id"]').value,
            car_type: row.querySelector('input[name="car_type"]').value,
            product_name: row.querySelector('input[name="product_name"]').value,
            location: row.querySelector('input[name="location"]').value,
            quantity: parseInt(row.querySelector('input[name="quantity"]').value) || 0,
            price: parseInt(row.querySelector('input[name="price"]').value) || 0,
            amount: parseInt(row.querySelector('input[name="amount"]').value) || 0,
            note: row.querySelector('input[name="note"]').value
        };
        if(item.product_id || item.product_name) items.push(item);
    });

    const dataToSend = {
        order_date: document.getElementById("order_date").value,
        customer_id: document.getElementById("customer_id").value,
        customer_address: document.getElementById("customer_address").value,
        creator: document.getElementById("creator").value,
        items: items
    };

    fetch('/api/sales', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dataToSend)
    })
    .then(r => r.json())
    .then(d => {
        if (d.success) alert("存檔成功！單號: " + d.orderId);
        else alert("失敗: " + d.message);
    })
    .catch(e => alert("連線錯誤: " + e));
}