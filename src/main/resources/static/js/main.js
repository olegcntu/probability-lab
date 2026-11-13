/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Точка входу: маршрутизація між розділами, тема, масштаб для проєктора, гарячі клавіші.
import { initLab, run, reset, toggleExplain } from './lab.js';
import { initPractice, showPractice } from './practice.js';
import { initTasks } from './tasks.js';
import { initLessons } from './lessons.js';
import { getJson } from './api.js';
import { esc } from './format.js';

const VIEWS = ['lab', 'practice', 'tasks', 'lessons', 'guide'];
const params = new URLSearchParams(location.search);
const $ = sel => document.querySelector(sel);
// режим підготовки ілюстрацій: смуга прокрутки не повинна змінювати ширину макета
if (params.get('rects') === '1') document.documentElement.style.scrollbarWidth = 'none';

function store(key, value) {
  try {
    if (value === undefined) return localStorage.getItem(key);
    localStorage.setItem(key, value);
  } catch (e) {
    return null;
  }
  return null;
}

let toastTimer;
const app = {
  params,
  toast(message) {
    const t = $('#toast');
    t.textContent = message;
    t.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { t.hidden = true; }, 4000);
  },
};

// ------------------------------------------------------------------ тема і масштаб
function applyTheme(theme) {
  if (theme === 'light' || theme === 'dark') document.documentElement.dataset.theme = theme;
  else delete document.documentElement.dataset.theme;
}
function currentTheme() {
  const t = document.documentElement.dataset.theme;
  if (t) return t;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}
let scale = 1;
function applyScale(s) {
  scale = Math.max(0.85, Math.min(1.8, Math.round(s * 100) / 100));
  document.documentElement.style.setProperty('--scale', scale);
}

applyTheme(params.get('theme') || store('problab-theme'));
applyScale(+(params.get('scale') || store('problab-scale') || 1));

$('#theme-toggle').addEventListener('click', () => {
  const next = currentTheme() === 'dark' ? 'light' : 'dark';
  applyTheme(next);
  store('problab-theme', next);
});
$('#font-up').addEventListener('click', () => { applyScale(scale + 0.1); store('problab-scale', String(scale)); });
$('#font-down').addEventListener('click', () => { applyScale(scale - 0.1); store('problab-scale', String(scale)); });
$('#fullscreen').addEventListener('click', toggleFullscreen);

function toggleFullscreen() {
  try {
    if (document.fullscreenElement) document.exitFullscreen();
    else document.documentElement.requestFullscreen().catch(() => app.toast('Повноекранний режим недоступний у цьому браузері.'));
  } catch (e) {
    app.toast('Повноекранний режим недоступний у цьому браузері.');
  }
}

// ------------------------------------------------------------------ маршрутизація
function route() {
  const v = location.hash.replace('#', '');
  const view = VIEWS.includes(v) ? v : 'lab';
  for (const name of VIEWS) $('#view-' + name).hidden = name !== view;
  document.querySelectorAll('.nav a').forEach(a => {
    if (a.dataset.view === view) a.setAttribute('aria-current', 'page');
    else a.removeAttribute('aria-current');
  });
  if (view === 'practice') showPractice();
}
window.addEventListener('hashchange', route);

// ------------------------------------------------------------------ гарячі клавіші
document.addEventListener('keydown', e => {
  if (e.ctrlKey || e.metaKey || e.altKey) return;
  const tag = (e.target.tagName || '').toLowerCase();
  if (['input', 'select', 'textarea', 'button', 'summary'].includes(tag)) return;
  const inLab = !$('#view-lab').hidden;
  if (e.key === '+' || e.key === '=') { applyScale(scale + 0.1); store('problab-scale', String(scale)); }
  else if (e.key === '-') { applyScale(scale - 0.1); store('problab-scale', String(scale)); }
  else if (e.key === 'f' || e.key === 'F' || e.key === 'а' || e.key === 'А') toggleFullscreen();
  else if (inLab && e.key === ' ') { e.preventDefault(); run('new'); }
  else if (inLab && (e.key === 'r' || e.key === 'R' || e.key === 'к' || e.key === 'К')) reset();
  else if (inLab && (e.key === 'e' || e.key === 'E' || e.key === 'у' || e.key === 'У')) toggleExplain();
});

// ------------------------------------------------------------------ довідка: адреса для учнів
async function fillGuide() {
  $('#guide-url').textContent = location.origin + location.pathname.replace(/index\.html$/, '');
  const host = location.hostname;
  const local = ['localhost', '127.0.0.1', '[::1]'].includes(host);
  const lan = /^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[01])\.)/.test(host) || host.endsWith('.local');
  if (!local && !lan) {
    // програма розміщена в інтернеті: одна адреса для вчителя й учнів
    $('#guide-intro').textContent = "Програма працює в інтернеті. Учитель і учні відкривають одну й ту саму адресу в браузері телефона, планшета чи комп'ютера:";
    $('#guide-note').innerHTML = "Встановлювати нічого не потрібно. Безкоштовний сервер «засинає» після простою, тому перше відкриття може тривати до хвилини: відкрийте сторінку заздалегідь, до початку уроку. Без інтернету програму можна запустити на комп'ютері вчителя (файл <b>run.bat</b>).";
    return;
  }
  try {
    const info = await getJson('api/info');
    const local = ['localhost', '127.0.0.1', '[::1]'].includes(location.hostname);
    if (local && info.lanUrls && info.lanUrls.length) {
      $('#guide-url').textContent = info.lanUrls[0];
      $('#guide-lan').innerHTML = info.lanUrls.length > 1
        ? `<p class="small">Інші адреси цього комп'ютера: ${info.lanUrls.slice(1).map(u => `<b>${esc(u)}</b>`).join(', ')}</p>` : '';
    }
  } catch (e) {
    // не критично
  }
}

// ------------------------------------------------------------------ старт
(async function start() {
  initPractice(app);
  initTasks(app);
  route();
  fillGuide();
  initLessons(app);
  try {
    await initLab(app);
  } catch (e) {
    const box = $('#server-error');
    box.textContent = e.message;
    box.hidden = false;
  }
  if (params.get('rects') === '1') setTimeout(dumpRects, 1500);
  if (params.has('scroll')) {
    setTimeout(() => {
      const el = document.querySelector(params.get('scroll'));
      if (el) el.scrollIntoView({ block: 'start' });
    }, 1200);
  }
})();

/** Службовий режим для підготовки ілюстрацій: координати основних блоків сторінки. */
function dumpRects() {
  const selectors = ['.topbar', '#view-lab .settings', '#view-lab .results', '#view-lab .readouts', '#explain',
    '#view-lab .chart-card', '#dist-chart', '.strip-card', '#question', '#view-practice .score', '#tasks-form',
    '#sheets .sheet', '#lessons details[open]', '#view-guide .guide', '.view:not([hidden]) .view-head'];
  const out = {};
  for (const s of selectors) {
    const el = document.querySelector(s);
    if (!el || el.closest('[hidden]')) continue;
    const r = el.getBoundingClientRect();
    out[s] = [r.left + scrollX, r.top + scrollY, r.width, r.height];
  }
  const distCard = document.querySelector('#dist-chart');
  if (distCard) {
    const r = distCard.closest('.panel').getBoundingClientRect();
    out['dist-card'] = [r.left + scrollX, r.top + scrollY, r.width, r.height];
  }
  const pre = document.createElement('script');
  pre.type = 'application/json';
  pre.id = '__rects';
  pre.textContent = JSON.stringify(out);
  document.body.appendChild(pre);
}
