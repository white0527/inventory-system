/**
 * main.js - 全域共用邏輯 (包含補貨統計功能)
 */

const API_BASE = "";

function switchMode(mode, element, titleName) {
    
    // 1. 處理按鈕變色
    if (element && element.classList.contains('nav-item')) {
        document.querySelectorAll('.sidebar .nav-item').forEach(item => item.classList.remove('active'));
        element.classList.add('active');
    }
    if (element && element.classList.contains('top-pill')) {
        document.querySelectorAll('.top-pill').forEach(item => item.classList.remove('active'));
        element.classList.add('active');
    }

    // 2. 隱藏所有畫面 (加入 view-replenishment)
    const views = ['view-sales-entry', 'view-sales-browse', 'view-import', 'view-construction', 'view-sales-stats', 'view-sales-analysis', 'view-daily-stats', 'view-replenishment'];
    views.forEach(id => {
        const el = document.getElementById(id);
        if(el) el.style.display = 'none';
    });

    // 3. 顯示對應畫面
    const titleEl = document.getElementById('current-mode-title');
    let displayTitle = titleName || "功能作業"; 

    if (mode === 'entry') {
        const view = document.getElementById('view-sales-entry');
        if(view) {
            view.style.display = 'flex';
            setTimeout(() => document.getElementById('sales-date')?.focus(), 100);
        }
    } 
    else if (mode === 'browse') {
        const view = document.getElementById('view-sales-browse');
        if(view) {
            view.style.display = 'flex';
            if (typeof loadSalesData === 'function') loadSalesData();
        }
    } 
    else if (mode === 'stats') {
        const view = document.getElementById('view-sales-stats');
        if(view) view.style.display = 'flex';
        initDateRange('stats-date-start', 'stats-date-end');
        document.getElementById('stats-result-panel').style.display = 'none';
        displayTitle = "銷貨統計作業";
    }
    else if (mode === 'analysis') {
        const view = document.getElementById('view-sales-analysis');
        if(view) view.style.display = 'flex';
        initDateRange('ana-date-start', 'ana-date-end');
        document.getElementById('analysis-result-panel').style.display = 'none';
        displayTitle = "銷貨分析作業";
    }
    else if (mode === 'daily-stats') {
        const view = document.getElementById('view-daily-stats');
        if(view) view.style.display = 'flex';
        const today = new Date();
        document.getElementById('daily-stats-month').value = today.toISOString().slice(0, 7);
        document.getElementById('daily-stats-list').innerHTML = '<tr><td colspan="7" style="text-align:center; color:#999; padding:20px;">請選擇月份並按 [開始計算]</td></tr>';
        displayTitle = "每日銷貨統計";
    }
    else if (mode === 'replenishment') {
        // [模式 8] 補貨統計
        const view = document.getElementById('view-replenishment');
        if(view) view.style.display = 'flex';
        initDateRange('rep-date-start', 'rep-date-end');
        document.getElementById('replenishment-result-panel').style.display = 'none';
        displayTitle = "補貨統計作業";
    }
    else if (mode === 'import') {
        const view = document.getElementById('view-import');
        if(view) view.style.display = 'flex';
        displayTitle = "商品資料匯入";
    } 
    else if (mode === 'construction') {
        const view = document.getElementById('view-construction');
        if(view) view.style.display = 'flex';
        const constTitle = document.getElementById('construction-title');
        if(constTitle) constTitle.innerText = titleName || "功能建置中";
        displayTitle = titleName || "功能開發中";
    }

    // 4. 更新標題
    if(titleEl) titleEl.innerText = displayTitle;
}

// 初始化日期範圍
function initDateRange(startId, endId) {
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    const s = document.getElementById(startId);
    const e = document.getElementById(endId);
    if(s && e) {
        s.valueAsDate = firstDay;
        e.valueAsDate = today;
    }
}

// --- 補貨統計功能 ---
function runReplenishment() {
    const outputType = document.querySelector('input[name="rep-output"]:checked').value;
    if (outputType !== 'S') { alert("模擬功能僅支援螢幕顯示"); return; }

    const resultPanel = document.getElementById('replenishment-result-panel');
    const tbody = document.getElementById('replenishment-result-list');
    
    resultPanel.style.display = 'flex';
    tbody.innerHTML = "";
    
    // 模擬補貨資料
    for(let i=1; i<=6; i++) {
        const currentStock = Math.floor(Math.random() * 5); // 庫存很低
        const safeStock = 10;
        const suggestQty = safeStock - currentStock + 5; // 建議補到安全量+5
        const price = Math.floor(Math.random() * 1000) + 100;

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>P-2026-${100+i}</td>
            <td>機車煞車皮 (模擬)</td>
            <td>廠商 ${String.fromCharCode(64+i)}</td>
            <td style="text-align:right; color:red; font-weight:bold;">${currentStock}</td>
            <td style="text-align:right;">${safeStock}</td>
            <td style="text-align:right; color:blue; font-weight:bold;">${suggestQty}</td>
            <td style="text-align:right;">$${price}</td>
        `;
        tbody.appendChild(tr);
    }
    
    resultPanel.scrollIntoView({ behavior: 'smooth' });
}

// (其他舊函式：runStats, runDailyStats, runAnalysis... 保持原樣)
function runDailyStats() {
    const monthInput = document.getElementById('daily-stats-month').value;
    if (!monthInput) { alert("請選擇月份"); return; }
    const tbody = document.getElementById('daily-stats-list');
    tbody.innerHTML = ""; 
    document.getElementById('daily-stats-info').innerText = `統計區間: ${monthInput}`;
    const [year, month] = monthInput.split('-');
    const daysInMonth = new Date(year, month, 0).getDate();
    let sumSales = 0, sumCount1 = 0, sumReturn = 0, sumCount2 = 0, sumNet = 0, sumProfit = 0;
    for (let day = 1; day <= daysInMonth; day++) {
        const hasSales = Math.random() > 0.3; 
        const salesAmt = hasSales ? Math.floor(Math.random() * 50000) + 1000 : 0;
        const salesCount = hasSales ? Math.floor(Math.random() * 10) + 1 : 0;
        const hasReturn = Math.random() > 0.8; 
        const returnAmt = hasReturn ? Math.floor(Math.random() * 2000) : 0;
        const returnCount = hasReturn ? 1 : 0;
        const net = salesAmt - returnAmt;
        const profit = Math.floor(net * 0.3); 
        sumSales += salesAmt; sumCount1 += salesCount; sumReturn += returnAmt; sumCount2 += returnCount; sumNet += net; sumProfit += profit;
        const tr = document.createElement('tr');
        tr.innerHTML = `<td style="text-align:center;">${day}</td><td style="text-align:right;">${salesAmt===0?'':salesAmt.toLocaleString()}</td><td style="text-align:right;">${salesCount===0?'':salesCount}</td><td style="text-align:right;color:red;">${returnAmt===0?'':returnAmt.toLocaleString()}</td><td style="text-align:right;">${returnCount===0?'':returnCount}</td><td style="text-align:right;font-weight:bold;color:blue;">${net.toLocaleString()}</td><td style="text-align:right;color:green;">${profit.toLocaleString()}</td>`;
        tbody.appendChild(tr);
    }
    document.getElementById('d-total-sales').innerText = sumSales.toLocaleString();
    document.getElementById('d-total-count1').innerText = sumCount1;
    document.getElementById('d-total-return').innerText = sumReturn.toLocaleString();
    document.getElementById('d-total-count2').innerText = sumCount2;
    document.getElementById('d-total-net').innerText = sumNet.toLocaleString();
    document.getElementById('d-total-profit').innerText = sumProfit.toLocaleString();
}

function runStats() {
    const outputType = document.querySelector('input[name="stats-output"]:checked').value;
    if (outputType !== 'S') { alert("模擬功能僅支援螢幕顯示"); return; }
    const resultPanel = document.getElementById('stats-result-panel');
    const tbody = document.getElementById('stats-result-list');
    const totalDisplay = document.getElementById('stats-total-display');
    resultPanel.style.display = 'flex';
    tbody.innerHTML = "";
    let total = 0;
    for(let i=1; i<=5; i++) {
        const qty = Math.floor(Math.random() * 50) + 1;
        const price = Math.floor(Math.random() * 5000) + 1000;
        const subtotal = qty * price;
        total += subtotal;
        tbody.innerHTML += `<tr><td>2026/02/0${i}</td><td style="text-align:right">${qty}</td><td style="text-align:right">$${price.toLocaleString()}</td><td style="text-align:right">$${Math.round(subtotal*0.05).toLocaleString()}</td><td style="text-align:right;color:blue;font-weight:bold">$${subtotal.toLocaleString()}</td></tr>`;
    }
    totalDisplay.innerText = "$" + total.toLocaleString();
    resultPanel.scrollIntoView({ behavior: 'smooth' });
}

function runAnalysis() {
    const outputType = document.querySelector('input[name="ana-output"]:checked').value;
    if (outputType !== 'S') { alert("模擬功能僅支援螢幕顯示"); return; }
    const resultPanel = document.getElementById('analysis-result-panel');
    const tbody = document.getElementById('analysis-result-list');
    resultPanel.style.display = 'flex';
    tbody.innerHTML = "";
    for(let i=1; i<=8; i++) {
        const qty = Math.floor(Math.random() * 10) + 1;
        const price = Math.floor(Math.random() * 2000) + 100;
        const amt = qty * price;
        tbody.innerHTML += `<tr><td>2026/02/0${i}</td><td>測試客戶 ${String.fromCharCode(64+i)}</td><td>機車零件 - ${i}</td><td>規格A</td><td style="text-align:right">${qty}</td><td style="text-align:right">$${price}</td><td style="text-align:right;color:blue;">$${amt.toLocaleString()}</td><td>一般出貨</td></tr>`;
    }
    resultPanel.scrollIntoView({ behavior: 'smooth' });
}

async function uploadExcel() {
    const fileInput = document.getElementById('excel-file');
    const resultDiv = document.getElementById('import-result');
    if (!fileInput || !fileInput.files[0]) { alert("請先選擇 Excel 檔案！"); return; }
    const formData = new FormData(); formData.append("file", fileInput.files[0]);
    if(resultDiv) { resultDiv.innerText = "⏳ 上傳處理中..."; resultDiv.style.color = "blue"; }
    try {
        const response = await fetch(`${API_BASE}/api/products/import`, { method: 'POST', body: formData });
        const text = await response.text();
        if (response.ok) {
            if(resultDiv) { resultDiv.innerText = "✅ " + text; resultDiv.style.color = "green"; }
            fileInput.value = "";
        } else {
            if(resultDiv) { resultDiv.innerText = "❌ 匯入失敗：" + text; resultDiv.style.color = "red"; }
        }
    } catch (e) {
        console.error(e);
        if(resultDiv) { resultDiv.innerText = "❌ 連線錯誤"; resultDiv.style.color = "red"; }
    }
}

function toggleDropdown(element) {
    document.querySelectorAll('.dropdown-wrapper.active').forEach(item => { if (item !== element) item.classList.remove('active'); });
    element.classList.toggle('active');
}
document.addEventListener('click', function(e) {
    if (!e.target.closest('.dropdown-wrapper')) {
        document.querySelectorAll('.dropdown-wrapper.active').forEach(item => item.classList.remove('active'));
    }
});
// 側邊欄控制
function toggleSidebar() { document.querySelector('.sidebar').classList.toggle('open'); }
document.querySelector('.main-content').addEventListener('click', function(e) {
    const sidebar = document.querySelector('.sidebar');
    if (!e.target.closest('.mobile-menu-btn') && sidebar.classList.contains('open')) { sidebar.classList.remove('open'); }
});

function exitSystem() {
    if(confirm("確定要退出系統嗎？")) window.close();
}
document.addEventListener("DOMContentLoaded", () => {
    const dateInput = document.getElementById('sales-date');
    if(dateInput) dateInput.valueAsDate = new Date();
});