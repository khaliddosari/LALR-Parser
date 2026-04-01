// ==================== INIT ====================
AOS.init({ duration: 700, easing: 'ease-out', once: true, offset: 80 });

// ==================== NAVBAR ====================
const navbar = document.getElementById('navbar');
window.addEventListener('scroll', () => {
    navbar.classList.toggle('scrolled', window.scrollY > 50);
});

const navToggle = document.getElementById('navToggle');
const navLinks = document.getElementById('navLinks');

navToggle.addEventListener('click', () => {
    navToggle.classList.toggle('active');
    navLinks.classList.toggle('open');
});

navLinks.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
        navToggle.classList.remove('active');
        navLinks.classList.remove('open');
    });
});

// Active nav link highlighting
const sections = document.querySelectorAll('.section, .hero');
const navItems = navLinks.querySelectorAll('a');

function setActiveLink() {
    let current = '';
    sections.forEach(s => {
        if (window.scrollY >= s.offsetTop - 100) current = s.getAttribute('id');
    });
    navItems.forEach(link => {
        link.classList.toggle('active', link.getAttribute('href') === '#' + current);
    });
}

window.addEventListener('scroll', setActiveLink);
setActiveLink();

// ==================== PRESETS ====================
const presets = {
    simple: { start: 'S', prods: 'S -> A a\nS -> b\nA -> c', input: 'c a' },
    expr:   { start: 'E', prods: 'E -> E + T\nE -> T\nT -> id', input: 'id + id' },
    arith:  { start: 'E', prods: 'E -> E + T\nE -> T\nT -> T * F\nT -> F\nF -> ( E )\nF -> id', input: 'id + id * id' },
    paren:  { start: 'S', prods: 'S -> ( S )\nS -> a', input: '( ( a ) )' }
};

function loadPreset(name) {
    const p = presets[name];
    document.getElementById('startSymbol').value = p.start;
    document.getElementById('productions').value = p.prods;
    document.getElementById('input').value = p.input;
    clearResults();
}

function clearResults() {
    document.getElementById('results').classList.add('hidden');
    document.getElementById('errorBox').classList.add('hidden');
}

// ==================== PARSE ====================
async function doParse() {
    const btn = document.getElementById('parseBtn');
    const btnText = document.getElementById('btnText');
    const spinner = document.getElementById('btnSpinner');

    btn.disabled = true;
    btnText.textContent = 'Parsing...';
    spinner.classList.remove('hidden');
    clearResults();

    const startSymbol = document.getElementById('startSymbol').value.trim();
    const productions = document.getElementById('productions').value
        .split('\n').map(l => l.trim()).filter(l => l.length > 0);
    const input = document.getElementById('input').value;

    try {
        const res = await fetch('/api/parse', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ startSymbol, productions, input })
        });

        const data = await res.json();

        if (!res.ok) {
            showError(data.error || 'Server error');
            return;
        }

        renderResults(data);
    } catch (err) {
        showError('Failed to connect to server: ' + err.message);
    } finally {
        btn.disabled = false;
        btnText.textContent = 'Parse';
        spinner.classList.add('hidden');
    }
}

function showError(msg) {
    const box = document.getElementById('errorBox');
    box.textContent = msg;
    box.classList.remove('hidden');
}

// ==================== RENDER ====================
function renderResults(data) {
    const lr = data.lalrResult;
    const pr = data.parseResult;

    // Badges
    document.getElementById('resultBadge').className = 'badge ' + (pr.accepted ? 'badge-accept' : 'badge-reject');
    document.getElementById('resultBadge').textContent = pr.accepted ? 'Accepted' : 'Rejected';

    document.getElementById('lalrBadge').className = 'badge badge-lalr';
    document.getElementById('lalrBadge').textContent = lr.lalr ? 'LALR(1)' : 'Not LALR';

    // Stats
    document.getElementById('canonicalCount').textContent = lr.canonicalStateCount;
    document.getElementById('lalrCount').textContent = lr.lalrStateCount;
    document.getElementById('stepCount').textContent = pr.steps.length;
    document.getElementById('conflictCount').textContent = lr.conflicts.length;

    // Parse trace
    const tbody = document.getElementById('traceBody');
    tbody.innerHTML = '';
    pr.steps.forEach((step, i) => {
        const tr = document.createElement('tr');
        const isLast = i === pr.steps.length - 1;
        if (isLast) tr.classList.add('trace-step-highlight');

        const cls = step.action.startsWith('s') ? 'action-shift'
            : step.action.startsWith('r') ? 'action-reduce'
            : step.action === 'accept' ? 'action-accept'
            : step.action.startsWith('ERROR') ? 'action-error' : '';

        tr.innerHTML = `<td>${i + 1}</td><td>${esc(step.stack)}</td><td>${esc(step.remainingInput)}</td><td class="${cls}">${esc(step.action)}</td>`;
        tbody.appendChild(tr);
    });

    renderActionTable(lr.tableData);
    renderGotoTable(lr.tableData);

    // Merge log
    const mergeCard = document.getElementById('mergeCard');
    const mergeList = document.getElementById('mergeList');
    if (lr.mergeLog && lr.mergeLog.length > 0) {
        mergeList.innerHTML = lr.mergeLog.map(m => `<li>${esc(m)}</li>`).join('');
        mergeCard.classList.remove('hidden');
    } else {
        mergeCard.classList.add('hidden');
    }

    document.getElementById('results').classList.remove('hidden');
    setTimeout(() => {
        document.getElementById('results').scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 100);
}

function renderActionTable(td) {
    const terms = td.terminals;
    const maxState = Math.max(...Object.keys(td.actionTable).map(Number));
    let html = '<table><thead><tr><th>State</th>';
    terms.forEach(t => html += `<th>${esc(t)}</th>`);
    html += '</tr></thead><tbody>';

    for (let s = 0; s <= maxState; s++) {
        html += `<tr><td>${s}</td>`;
        const row = td.actionTable[s] || {};
        terms.forEach(t => {
            const val = row[t] || '';
            const cls = val.startsWith('s') ? 'action-shift'
                : val.startsWith('r') ? 'action-reduce'
                : val === 'accept' ? 'action-accept' : '';
            html += `<td class="${cls}">${esc(val)}</td>`;
        });
        html += '</tr>';
    }
    html += '</tbody></table>';
    document.getElementById('actionTableWrapper').innerHTML = html;
}

function renderGotoTable(td) {
    const nts = td.nonterminals;
    const allStates = new Set([
        ...Object.keys(td.actionTable || {}).map(Number),
        ...Object.keys(td.gotoTable || {}).map(Number)
    ]);
    const maxState = Math.max(...allStates, 0);

    let html = '<table><thead><tr><th>State</th>';
    nts.forEach(nt => html += `<th>${esc(nt)}</th>`);
    html += '</tr></thead><tbody>';

    for (let s = 0; s <= maxState; s++) {
        html += `<tr><td>${s}</td>`;
        const row = td.gotoTable[s] || {};
        nts.forEach(nt => {
            const val = row[nt];
            html += `<td>${val !== undefined ? val : ''}</td>`;
        });
        html += '</tr>';
    }
    html += '</tbody></table>';
    document.getElementById('gotoTableWrapper').innerHTML = html;
}

function esc(str) {
    const d = document.createElement('div');
    d.textContent = str;
    return d.innerHTML;
}

// ==================== KEYBOARD ====================
document.addEventListener('keydown', e => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') doParse();
});
