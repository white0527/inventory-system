// === 瀏覽式查詢模組 ===

// 模擬查詢功能
function mockSearch() {
    const masterBody = document.getElementById('browse-master-list');
    if(!masterBody) return;

    // 模擬資料 (未來這裡會換成真正的 fetch API)
    let html = '';
    for(let i=1; i<=5; i++) {
        html += `
            <tr onclick="showDetail(${i}, this)">
                <td>${i}</td>
                <td>S202602040${i}</td>
                <td>2026-02-04</td>
                <td>CUST-00${i}</td>
                <td style="color:blue; text-align:right;">$1,500</td>
            </tr>`;
    }
    masterBody.innerHTML = html;
    
    // 更新右上角統計
    const amountEl = document.querySelector('.stats-amount');
    if(amountEl) amountEl.innerText = "$7,500";
}

// 點擊左側單據，顯示右側明細
function showDetail(id, trElement) {
    // 處理 Highlight (選中變色)
    document.querySelectorAll('.browse-table tr').forEach(r => r.classList.remove('selected'));
    trElement.classList.add('selected');

    const detailBody = document.getElementById('browse-detail-list');
    
    // 假明細資料
    detailBody.innerHTML = `
        <tr><td>1</td><td>OIL-001</td><td>機油</td><td>2</td><td>300</td><td>600</td></tr>
        <tr><td>2</td><td>TIRE-09</td><td>正新輪胎</td><td>1</td><td>900</td><td>900</td></tr>
    `;
}