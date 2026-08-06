/* ============================================================
   Payment Processing Platform - js/dashboard.js
   ============================================================ */

function resolveApiBase() {
  const queryApiBase = new URLSearchParams(window.location.search).get('apiBase');
  if (queryApiBase) {
    return queryApiBase.replace(/\/$/, '');
  }

  const { protocol, hostname, port } = window.location;
  const isLocalHost = hostname === 'localhost' || hostname === '127.0.0.1';
  const isBackendPort = port === '8080';

  if (protocol === 'file:' || (isLocalHost && !isBackendPort)) {
    return 'http://localhost:8080/api';
  }

  return '/api';
}

const API_BASE = resolveApiBase();
const SENDER_CURRENCY = 'INR';
const PLATFORM_SENDER_ACCOUNT = 'PLATFORM-INR-0001';
const LOCAL_AUDIT_KEY = 'payment_platform_audit_logs';
const LOCAL_PAYMENTS_KEY = 'payment_platform_local_payments';

const state = {
  vendors: [],
  invoices: [],
  payments: loadLocalPayments(),
  auditLogs: loadLocalAuditLogs(),
  activePayInvoiceId: null,
  activePayContext: null,
  currentPaymentFilter: 'ALL',
  fxRates: { USD: 0.012, GBP: 0.0094, EUR: 0.011, INR: 1.0 }
};

document.addEventListener('DOMContentLoaded', async () => {
  await loadDashboardData();
});

function loadLocalAuditLogs() {
  try {
    const data = localStorage.getItem(LOCAL_AUDIT_KEY);
    return data ? JSON.parse(data) : [];
  } catch (_) { return []; }
}

function saveLocalAuditLogs() {
  try { localStorage.setItem(LOCAL_AUDIT_KEY, JSON.stringify(state.auditLogs)); } catch (_) {}
}

function loadLocalPayments() {
  try {
    const data = localStorage.getItem(LOCAL_PAYMENTS_KEY);
    return data ? JSON.parse(data) : [];
  } catch (_) { return []; }
}

function saveLocalPayments() {
  try { localStorage.setItem(LOCAL_PAYMENTS_KEY, JSON.stringify(state.payments)); } catch (_) {}
}

async function apiRequest(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {})
    }
  });

  const rawBody = response.status === 204 ? '' : await response.text();
  let parsedBody = null;
  if (rawBody) {
    try { parsedBody = JSON.parse(rawBody); } catch (_) { parsedBody = null; }
  }

  if (!response.ok) {
    let errorText = `HTTP ${response.status}`;
    if (parsedBody && typeof parsedBody === 'object') {
      errorText = parsedBody.message || parsedBody.error || JSON.stringify(parsedBody);
    } else if (rawBody) {
      const looksLikeHtml = /^\s*</.test(rawBody);
      errorText = looksLikeHtml ? `Endpoint not found (HTTP ${response.status})` : rawBody;
    }
    throw new Error(errorText);
  }

  if (response.status === 204) return null;
  return parsedBody !== null ? parsedBody : rawBody;
}

function mapVendor(vendor) {
  return {
    id: vendor.id,
    displayId: `VND-${String(vendor.id).padStart(3, '0')}`,
    name: vendor.name,
    email: vendor.email,
    acc: vendor.bankAccount,
    country: (vendor.country || 'INR').toUpperCase(),
    curr: (vendor.country || 'INR').toUpperCase()
  };
}

function mapInvoice(invoice, statusByInvoiceId) {
  const vendor = state.vendors.find(v => Number(v.id) === Number(invoice.vendor?.id));
  const curr = (invoice.currency || vendor?.curr || 'INR').toUpperCase();
  const vendorCurrAmount = Number(invoice.invoiceAmount || 0);
  const rate = state.fxRates[curr] || 1;
  const baseAmountInr = curr === 'INR' ? vendorCurrAmount : Math.round(vendorCurrAmount / rate);

  return {
    id: invoice.id,
    displayId: invoice.invoiceNumber || `INV-${invoice.id}`,
    createdAt: formatDateTime(invoice.createdAt),
    vendorId: invoice.vendor?.id,
    vendorName: invoice.vendor?.name || vendor?.name || '-',
    vendorAcc: invoice.vendor?.bankAccount || vendor?.acc || '',
    baseAmountInr: baseAmountInr,
    vendorCurr: curr,
    convertedAmount: vendorCurrAmount.toFixed(2),
    status: statusByInvoiceId.get(invoice.id) || 'UNPAID'
  };
}

function formatDateTime(value) {
  if (!value) return '-';
  const dt = new Date(value);
  if (Number.isNaN(dt.getTime())) return value;
  return dt.toLocaleString('en-IN', { year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' });
}

function buildStatusByInvoice(payments) {
  const priority = { COMPLETED: 4, SENT: 3, VALIDATED: 2, CREATED: 1, FAILED: 0 };
  const latest = new Map();

  const allStatusSources = [...payments];
  state.auditLogs.forEach(log => {
    if (log.status === 'FAILED') {
      const invIdNum = parseInt(log.invId.replace(/[^0-9]/g, ''), 10);
      allStatusSources.push({
        invoice: { id: invIdNum, invoiceNumber: log.invId },
        status: 'FAILED',
        createdAt: log.timestamp
      });
    }
  });

  allStatusSources.forEach(payment => {
    const invoiceId = payment.invoice?.id;
    if (!invoiceId) return;

    const current = latest.get(invoiceId);
    if (!current) {
      latest.set(invoiceId, payment);
      return;
    }

    const currentTime = Date.parse(current.createdAt || '') || 0;
    const nextTime = Date.parse(payment.createdAt || '') || 0;
    const currentPrio = priority[(current.status || '').toUpperCase()] ?? -1;
    const nextPrio = priority[(payment.status || '').toUpperCase()] ?? -1;

    if (nextTime > currentTime || (nextTime === currentTime && nextPrio >= currentPrio)) {
      latest.set(invoiceId, payment);
    }
  });

  const mapped = new Map();
  latest.forEach((payment, invoiceId) => {
    const statusUpper = (payment.status || '').toUpperCase();
    if (statusUpper === 'COMPLETED') {
      mapped.set(invoiceId, 'PAID');
    } else if (['CREATED', 'VALIDATED', 'SENT'].includes(statusUpper)) {
      mapped.set(invoiceId, 'PROCESSING');
    } else if (statusUpper === 'FAILED') {
      mapped.set(invoiceId, 'FAILED');
    }
  });

  return mapped;
}

async function loadDashboardData() {
  try {
    const [vendorsRaw, invoicesRaw, paymentsRaw] = await Promise.all([
      apiRequest('/vendors'),
      apiRequest('/invoices'),
      apiRequest('/payments')
    ]);

    state.vendors = vendorsRaw.map(mapVendor);
    const remotePayments = Array.isArray(paymentsRaw) ? paymentsRaw : [];

    remotePayments.forEach(rp => {
      const idx = state.payments.findIndex(lp => String(lp.id) === String(rp.id));
      if (idx >= 0) {
        state.payments[idx] = { ...state.payments[idx], ...rp };
      } else {
        state.payments.unshift(rp);
      }
    });

    saveLocalPayments();

    state.payments.forEach(payment => {
      const pId = `#PAY-${payment.id}`;
      const pStatus = (payment.status || 'CREATED').toUpperCase();
      const exists = state.auditLogs.some(log => log.payId === pId && log.status === pStatus);

      if (!exists) {
        state.auditLogs.push({
          timestamp: (payment.createdAt || '').replace('T', ' ').substring(0, 19) || '-',
          payId: pId,
          invId: payment.invoice?.invoiceNumber || `INV-${payment.invoice?.id ?? 'NA'}`,
          vendor: payment.invoice?.vendor?.name || '-',
          senderAccount: payment.senderAccount || PLATFORM_SENDER_ACCOUNT,
          receiverAccount: payment.receiverAccount || payment.invoice?.vendor?.bankAccount || '-',
          settled: `${payment.amount || 0} ${payment.currency || 'INR'}`,
          status: pStatus
        });
      }
    });

    saveLocalAuditLogs();

    const statusByInvoiceId = buildStatusByInvoice(state.payments);
    state.invoices = invoicesRaw.map(invoice => mapInvoice(invoice, statusByInvoiceId));

    renderVendors();
    renderInvoices();
    renderPayments();
    renderAudit();
    renderOverviewRecent();
    updateKPIs();
  } catch (error) {
    showToast('danger', 'Unable to load dashboard data', error.message);
  }
}

function recordPaymentLifecycleState(payId, inv, status, amount, currency, receiverAcc) {
  const pStatus = status.toUpperCase();
  const timestamp = new Date().toISOString();

  const existingIdx = state.payments.findIndex(p => String(p.id) === String(payId));
  const paymentObj = {
    id: payId,
    invoice: {
      id: inv.id,
      invoiceNumber: inv.displayId,
      vendor: { name: inv.vendorName, bankAccount: receiverAcc }
    },
    amount: amount,
    currency: currency,
    senderAccount: PLATFORM_SENDER_ACCOUNT,
    receiverAccount: receiverAcc || '-',
    status: pStatus,
    createdAt: timestamp
  };

  if (existingIdx >= 0) {
    state.payments[existingIdx].status = pStatus;
  } else {
    state.payments.unshift(paymentObj);
  }

  saveLocalPayments();
  addAuditEntry(payId, inv.displayId, inv.vendorName, amount, currency, pStatus, receiverAcc);

  if (pStatus === 'COMPLETED') {
    inv.status = 'PAID';
  } else if (['CREATED', 'VALIDATED', 'SENT'].includes(pStatus)) {
    inv.status = 'PROCESSING';
  } else if (pStatus === 'FAILED') {
    inv.status = 'FAILED';
  }

  renderPayments();
  renderInvoices();
  renderOverviewRecent();
  updateKPIs();
}

function addAuditEntry(payId, invId, vendor, amount, currency, status, receiverAcc) {
  const now = new Date();
  const timestamp = now.toLocaleString('en-IN', {
    year: 'numeric', month: 'short', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  });

  const entry = {
    timestamp,
    payId: `#PAY-${payId}`,
    invId,
    vendor,
    senderAccount: PLATFORM_SENDER_ACCOUNT,
    receiverAccount: receiverAcc || '-',
    settled: `${amount} ${currency}`,
    status: status.toUpperCase()
  };

  state.auditLogs.unshift(entry);
  saveLocalAuditLogs();
  renderAudit();
}

function switchTab(tabId, el) {
  document.querySelectorAll('.tab-view').forEach(t => t.classList.remove('active'));
  document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
  document.getElementById(tabId).classList.add('active');
  if (el) el.classList.add('active');
}

function populateVendorDropdown() {
  const select = document.getElementById('invVendorSelect');
  if (!select) return;

  if (state.vendors.length === 0) {
    select.innerHTML = '<option value="">No vendors available — please add a vendor first</option>';
    return;
  }

  select.innerHTML = state.vendors.map(v =>
    `<option value="${v.id}">${v.name} (${v.curr})</option>`
  ).join('');
}

function openModal(id) {
  const modal = document.getElementById(id);
  if (!modal) {
    console.error("Modal element not found:", id);
    return;
  }

  if (id === 'invoiceModal') {
    populateVendorDropdown();
    updateInvoiceFXPreview();
  }

  modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
  if (id === 'payModal') { resetPaymentSteps(); }
}

function toggleFailureReasonSelect() {
  const isChecked = document.getElementById('simFailure').checked;
  const failureGroup = document.getElementById('failureReasonGroup');
  if (isChecked) { failureGroup.classList.remove('hidden'); } else { failureGroup.classList.add('hidden'); }
}

function showToast(type, title, message) {
  let container = document.getElementById('toastContainer');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toastContainer';
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `
    <div class="toast-body">
      <span class="toast-icon">${type === 'success' ? '✅' : '❌'}</span>
      <div class="toast-text">
        <div class="toast-title">${title}</div>
        <div class="toast-msg">${message}</div>
      </div>
    </div>
    <button class="toast-dismiss" onclick="this.parentElement.remove()">✕</button>
  `;

  container.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('toast-out');
    setTimeout(() => toast.remove(), 250);
  }, 5000);
}

function renderOverviewRecent() {
  const tbody = document.getElementById('overviewRecentBody');
  if (!tbody) return;

  const recent = state.auditLogs.slice(0, 5);
  if (recent.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="6">No recent transactions recorded.</td></tr>`;
    return;
  }

  tbody.innerHTML = recent.map(a => `
    <tr>
      <td>${a.timestamp}</td>
      <td><code>${a.payId}</code></td>
      <td>${a.invId}</td>
      <td>${a.vendor}</td>
      <td>${a.settled}</td>
      <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
    </tr>
  `).join('');
}

function renderVendors() {
  const tbody = document.getElementById('vendorTableBody');
  if (!tbody) return;

  if (state.vendors.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="5">No vendors registered yet.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.vendors.map(v => `
    <tr>
      <td>${v.displayId}</td>
      <td><b>${v.name}</b></td>
      <td>${v.email || '-'}</td>
      <td><code>${v.acc}</code></td>
      <td><b>${v.country}</b></td>
    </tr>
  `).join('');
}

function renderInvoices() {
  const tbody = document.getElementById('invoiceTableBody');
  if (!tbody) return;

  if (state.invoices.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8">No invoices found.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.invoices.map(inv => {
    let badge = `<span class="badge badge-unpaid">UNPAID</span>`;
    let btn = `<button class="btn btn-primary" onclick="initiatePayment('${inv.id}')">Pay Now</button>`;

    if (inv.status === 'PAID') {
      badge = `<span class="badge badge-paid">PAID</span>`;
      btn = `<button class="btn btn-secondary" disabled>Paid</button>`;
    } else if (inv.status === 'PROCESSING') {
      badge = `<span class="badge badge-processing">LOCKED</span>`;
      btn = `<button class="btn btn-secondary" disabled>Processing...</button>`;
    } else if (inv.status === 'FAILED') {
      badge = `<span class="badge badge-failed">FAILED</span>`;
      btn = `<button class="btn btn-primary" onclick="initiatePayment('${inv.id}')">Retry Payment</button>`;
    }

    return `
      <tr>
        <td><b>${inv.displayId}</b></td>
        <td>${inv.createdAt}</td>
        <td>${inv.vendorName}</td>
        <td>₹${inv.baseAmountInr.toLocaleString('en-IN')}</td>
        <td><b>${inv.vendorCurr}</b></td>
        <td>${inv.convertedAmount} ${inv.vendorCurr}</td>
        <td>${badge}</td>
        <td>${btn}</td>
      </tr>
    `;
  }).join('');
}

function renderPayments() {
  const tbody = document.getElementById('paymentTableBody');
  if (!tbody) return;

  const searchQuery = (document.getElementById('paymentSearch')?.value || '').toLowerCase();

  const filtered = state.payments.filter(payment => {
    const pStatus = (payment.status || '').toUpperCase();
    const pId = `#PAY-${payment.id}`.toLowerCase();
    const invNum = (payment.invoice?.invoiceNumber || `INV-${payment.invoice?.id}`).toLowerCase();

    const matchesFilter = state.currentPaymentFilter === 'ALL' || pStatus === state.currentPaymentFilter;
    const matchesSearch = !searchQuery || pId.includes(searchQuery) || invNum.includes(searchQuery);

    return matchesFilter && matchesSearch;
  });

  if (filtered.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="10">No payments found matching filter criteria.</td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map(p => {
    const pStatus = (p.status || 'CREATED').toUpperCase();
    const badgeClass = `badge-${pStatus.toLowerCase()}`;

    return `
      <tr>
        <td><code>#PAY-${p.id}</code></td>
        <td><b>${p.invoice?.invoiceNumber || `INV-${p.invoice?.id ?? '-'}`}</b></td>
        <td>${p.invoice?.vendor?.name || '-'}</td>
        <td>${p.amount}</td>
        <td><b>${p.currency}</b></td>
        <td><code>${p.senderAccount || PLATFORM_SENDER_ACCOUNT}</code></td>
        <td><code>${p.receiverAccount || p.invoice?.vendor?.bankAccount || '-'}</code></td>
        <td><span class="badge ${badgeClass}">${pStatus}</span></td>
        <td>${formatDateTime(p.createdAt)}</td>
        <td>
          <button class="btn btn-secondary" style="padding: 4px 10px; font-size: 11px;" onclick="viewPaymentDetails('${p.id}')">View</button>
        </td>
      </tr>
    `;
  }).join('');
}

function filterPayments(statusFilter, btnEl) {
  if (statusFilter) {
    state.currentPaymentFilter = statusFilter;
    if (btnEl) {
      document.querySelectorAll('#paymentFilterTabs .filter-btn').forEach(b => b.classList.remove('active'));
      btnEl.classList.add('active');
    }
  }
  renderPayments();
}

function renderAudit() {
  const tbody = document.getElementById('auditTableBody');
  if (!tbody) return;

  if (state.auditLogs.length === 0) {
    tbody.innerHTML = `<tr class="empty-row"><td colspan="8">No audit logs available.</td></tr>`;
    return;
  }

  tbody.innerHTML = state.auditLogs.map(a => `
    <tr>
      <td>${a.timestamp}</td>
      <td><code>${a.payId}</code></td>
      <td>${a.invId}</td>
      <td>${a.vendor}</td>
      <td><code>${a.senderAccount}</code></td>
      <td><code>${a.receiverAccount}</code></td>
      <td>${a.settled}</td>
      <td><span class="badge badge-${a.status.toLowerCase()}">${a.status}</span></td>
    </tr>
  `).join('');
}

function updateKPIs() {
  const totalDisbursed = state.invoices.filter(i => i.status === 'PAID').reduce((sum, i) => sum + i.baseAmountInr, 0);
  const pendingCount = state.invoices.filter(i => i.status === 'UNPAID').length;
  const failedCount = state.auditLogs.filter(a => (a.status || '').toUpperCase() === 'FAILED').length;

  document.getElementById('kpiDisbursed').innerText = `₹${totalDisbursed.toLocaleString('en-IN')}`;
  document.getElementById('kpiPending').innerText = pendingCount;
  document.getElementById('kpiVendors').innerText = state.vendors.length;
  document.getElementById('kpiFailed').innerText = failedCount;
}

async function saveVendor() {
  const name = document.getElementById('vName').value.trim();
  const acc = document.getElementById('vAcc').value.trim();
  const email = document.getElementById('vEmail').value.trim();
  const country = document.getElementById('vCountry').value.trim().toUpperCase();

  if (!name || !acc || !email || !country) {
    alert('Please fill out all vendor fields including currency code.');
    return;
  }

  try {
    await apiRequest('/vendors', {
      method: 'POST',
      body: JSON.stringify({ name, email, bankAccount: acc, country })
    });

    showToast('success', 'Vendor Saved', `Vendor ${name} (${country}) created successfully.`);
    await loadDashboardData();
    closeModal('vendorModal');
  } catch (error) {
    showToast('danger', 'Vendor Creation Failed', error.message);
  }
}

function updateInvoiceFXPreview() {
  const select = document.getElementById('invVendorSelect');
  if (!select || state.vendors.length === 0) return;

  const vId = select.value;
  const invoiceAmount = parseFloat(document.getElementById('invAmount').value) || 0;
  const vendor = state.vendors.find(v => String(v.id) === String(vId));
  if (!vendor) return;

  const rate = state.fxRates[vendor.curr] || 1;
  const inrAmount = vendor.curr === 'INR' ? invoiceAmount : (invoiceAmount / rate);
  const inrPerUnit = vendor.curr === 'INR' ? 1 : (1 / rate);

  document.getElementById('invAmountLabel').innerText = `Invoice Amount (${vendor.curr})`;
  document.getElementById('invVendorCurrPreview').innerText = vendor.curr;
  document.getElementById('invFxRatePreview').innerText = `1 ${vendor.curr} = ${inrPerUnit.toFixed(2)} INR`;
  document.getElementById('invConvertedPreview').innerText = `~₹${inrAmount.toLocaleString('en-IN', { maximumFractionDigits: 2 })}`;
}

async function saveInvoice() {
  const vId = document.getElementById('invVendorSelect').value;
  const invoiceAmount = parseFloat(document.getElementById('invAmount').value);

  if (!vId) {
    alert('Please select a recipient vendor.');
    return;
  }

  if (!invoiceAmount || invoiceAmount <= 0) {
    alert('Please enter a valid positive invoice amount.');
    return;
  }

  const vendor = state.vendors.find(v => String(v.id) === String(vId));
  if (!vendor) return;

  const invoiceNumber = `INV-${Date.now()}`;

  try {
    await apiRequest('/invoices', {
      method: 'POST',
      body: JSON.stringify({
        invoiceNumber,
        invoiceAmount,
        vendorId: Number(vId),
        currency: vendor.curr
      })
    });

    showToast('success', 'Invoice Created', `Invoice ${invoiceNumber} created.`);
    await loadDashboardData();
    closeModal('invoiceModal');
  } catch (error) {
    showToast('danger', 'Invoice Creation Failed', error.message);
  }
}

async function initiatePayment(invId) {
  state.activePayInvoiceId = invId;
  const inv = state.invoices.find(i => String(i.id) === String(invId));
  if (!inv) return;

  try {
    const quote = await apiRequest(`/payments/quote?invoiceId=${inv.id}&currency=${SENDER_CURRENCY}`);
    state.activePayContext = {
      quote,
      receiverAccount: inv.vendorAcc,
      vendorName: inv.vendorName
    };

    document.getElementById('payInvId').innerText = inv.displayId;
    document.getElementById('payVendorName').innerText = inv.vendorName;
    document.getElementById('payVendorAcc').innerText = inv.vendorAcc || '-';
    document.getElementById('payTargetCurr').innerText = quote.invoiceCurrency;
    document.getElementById('payBaseInr').innerText = `${quote.requiredPaymentAmount} ${quote.paymentCurrency}`;
    document.getElementById('payForexFee').innerText = `${quote.fxFeeAmount} ${quote.invoiceCurrency}`;
    document.getElementById('payFinalAmount').innerText = `${quote.convertedAmount} ${quote.invoiceCurrency}`;
    document.getElementById('payIdempotency').value = crypto.randomUUID();

    openModal('payModal');
  } catch (error) {
    showToast('danger', 'Quote Request Failed', error.message);
  }
}

function updatePaymentProgress(stepName, status, statusText) {
  const progressDiv = document.getElementById('paymentProgress');
  const statusDiv = document.getElementById('paymentProgressStatus');
  progressDiv.classList.remove('hidden');

  const stepEl = document.getElementById(`step-${stepName.toLowerCase()}`);
  if (stepEl) { stepEl.className = `payment-step step-${status}`; }

  if (statusDiv) {
    statusDiv.className = `payment-progress-status ${status === 'error' ? 'status-error' : (status === 'done' ? 'status-done' : '')}`;
    statusDiv.innerText = statusText;
  }
}

function resetPaymentSteps() {
  document.getElementById('paymentProgress')?.classList.add('hidden');
  ['created', 'validated', 'sent', 'completed'].forEach(s => {
    const el = document.getElementById(`step-${s}`);
    if (el) el.className = 'payment-step step-pending';
  });
  const statusDiv = document.getElementById('paymentProgressStatus');
  if (statusDiv) statusDiv.innerText = '';
}

async function processPayment() {
  const inv = state.invoices.find(i => String(i.id) === String(state.activePayInvoiceId));
  const shouldFail = document.getElementById('simFailure').checked;
  const specificReason = document.getElementById('simReason').value;
  const btnConfirm = document.getElementById('btnConfirmPayment');

  if (!inv || !state.activePayContext) return;

  btnConfirm.disabled = true;
  const reqAmount = state.activePayContext.quote.requiredPaymentAmount;
  const receiverAcc = state.activePayContext.receiverAccount || inv.vendorAcc;

  if (shouldFail) {
    const simPayId = `SIM-${Math.floor(1000 + Math.random() * 9000)}`;

    updatePaymentProgress('created', 'active', 'Step 1/4: Payment initialized...');
    recordPaymentLifecycleState(simPayId, inv, 'CREATED', reqAmount, SENDER_CURRENCY, receiverAcc);
    await new Promise(r => setTimeout(r, 400));

    updatePaymentProgress('created', 'error', `Payment Blocked: ${specificReason}`);
    recordPaymentLifecycleState(simPayId, inv, 'FAILED', reqAmount, SENDER_CURRENCY, receiverAcc);

    showToast('danger', 'Payment Failed', `Simulation active: ${specificReason}`);
    btnConfirm.disabled = false;
    return;
  }

  let payment = null;
  try {
    updatePaymentProgress('created', 'active', 'Step 1/4: Payment initialized...');
    payment = await apiRequest('/payments', {
      method: 'POST',
      headers: {
        'X-Idempotency-Key': document.getElementById('payIdempotency').value
      },
      body: JSON.stringify({
        invoiceId: inv.id,
        amount: Number(reqAmount),
        currency: SENDER_CURRENCY,
        senderAccount: PLATFORM_SENDER_ACCOUNT,
        receiverAccount: receiverAcc
      })
    });
    updatePaymentProgress('created', 'done', 'Payment created.');
    recordPaymentLifecycleState(payment.id, inv, 'CREATED', reqAmount, SENDER_CURRENCY, receiverAcc);

    updatePaymentProgress('validated', 'active', 'Step 2/4: Validating accounts...');
    await new Promise(r => setTimeout(r, 600));
    await apiRequest(`/payments/${payment.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: 'VALIDATED' })
    });
    updatePaymentProgress('validated', 'done', 'Accounts validated.');
    recordPaymentLifecycleState(payment.id, inv, 'VALIDATED', reqAmount, SENDER_CURRENCY, receiverAcc);

    updatePaymentProgress('sent', 'active', 'Step 3/4: Dispatched to bank gateway...');
    await new Promise(r => setTimeout(r, 600));
    await apiRequest(`/payments/${payment.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: 'SENT' })
    });
    updatePaymentProgress('sent', 'done', 'Payment sent.');
    recordPaymentLifecycleState(payment.id, inv, 'SENT', reqAmount, SENDER_CURRENCY, receiverAcc);

    updatePaymentProgress('completed', 'active', 'Step 4/4: Settling payment...');
    await new Promise(r => setTimeout(r, 600));
    await apiRequest(`/payments/${payment.id}/status`, {
      method: 'PUT',
      body: JSON.stringify({ status: 'COMPLETED' })
    });
    updatePaymentProgress('completed', 'done', 'Payment settled!');
    recordPaymentLifecycleState(payment.id, inv, 'COMPLETED', reqAmount, SENDER_CURRENCY, receiverAcc);

    showToast('success', 'Payment Successful', `Payment for invoice ${inv.displayId} has been completed.`);

    setTimeout(() => {
      closeModal('payModal');
      btnConfirm.disabled = false;
    }, 1000);

  } catch (error) {
    const activePayId = payment?.id || `ERR-${Math.floor(1000 + Math.random() * 9000)}`;
    updatePaymentProgress('created', 'error', `Payment processing failed: ${error.message}`);
    recordPaymentLifecycleState(activePayId, inv, 'FAILED', reqAmount, SENDER_CURRENCY, receiverAcc);

    showToast('danger', 'Payment Processing Failed', error.message);
    btnConfirm.disabled = false;
  }
}

function viewPaymentDetails(payId) {
  const payment = state.payments.find(p => String(p.id) === String(payId));
  if (!payment) return;

  const content = document.getElementById('paymentDetailContent');
  const pStatus = (payment.status || 'CREATED').toUpperCase();

  const titleEl = document.getElementById('pdTitle');
  const subtitleEl = document.getElementById('pdSubtitle');
  if (titleEl) titleEl.innerText = `Payment Details: #PAY-${payment.id}`;
  if (subtitleEl) subtitleEl.innerText = `Invoice ${payment.invoice?.invoiceNumber || payment.invoice?.id}`;

  content.innerHTML = `
    <div class="pd-status-header pd-status-${pStatus.toLowerCase()}">
      <span class="pd-status-icon">${pStatus === 'COMPLETED' ? '✅' : (pStatus === 'FAILED' ? '❌' : 'ℹ️')}</span>
      Status: ${pStatus}
    </div>
    <div class="pd-grid">
      <div class="pd-section">
        <div class="pd-section-title">Transaction Information</div>
        <div class="pd-row"><span class="pd-label">Payment ID:</span><span class="pd-value"><code>#PAY-${payment.id}</code></span></div>
        <div class="pd-row"><span class="pd-label">Invoice ID:</span><span class="pd-value"><b>${payment.invoice?.invoiceNumber || '-'}</b></span></div>
        <div class="pd-row"><span class="pd-label">Amount:</span><span class="pd-value">${payment.amount} ${payment.currency}</span></div>
        <div class="pd-row"><span class="pd-label">Created At:</span><span class="pd-value">${formatDateTime(payment.createdAt)}</span></div>
      </div>
      <div class="pd-section">
        <div class="pd-section-title">Account Details</div>
        <div class="pd-row"><span class="pd-label">Vendor:</span><span class="pd-value">${payment.invoice?.vendor?.name || '-'}</span></div>
        <div class="pd-row"><span class="pd-label">Sender Acc:</span><span class="pd-value"><code>${payment.senderAccount || PLATFORM_SENDER_ACCOUNT}</code></span></div>
        <div class="pd-row"><span class="pd-label">Receiver Acc:</span><span class="pd-value"><code>${payment.receiverAccount || payment.invoice?.vendor?.bankAccount || '-'}</code></span></div>
      </div>
    </div>
  `;

  openModal('paymentDetailModal');
}