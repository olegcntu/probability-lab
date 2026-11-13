/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Лабораторія: налаштування експерименту, теоретична ймовірність, моделювання, графіки.
import { api, getJson } from './api.js';
import { dec, int, esc, tex, fracHtml, plural } from './format.js';
import { drawConvergence, drawDistribution } from './charts.js';

const $ = sel => document.querySelector(sel);

const DEFAULTS = {
  COINS: { type: 'COINS', count: 3, headsProbability: '1/2', event: 'HEADS_EXACTLY', value: 2 },
  DICE: { type: 'DICE', count: 2, event: 'SUM_EQ', value: 7 },
  URN: { type: 'URN', red: 5, blue: 3, draw: 3, replacement: false, event: 'RED_EXACTLY', value: 2 },
};
const MAX_SERIES = 6;
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

const state = {
  catalog: null,
  presets: [],
  config: { ...DEFAULTS.COINS },
  trials: 1000,
  theory: null,
  series: [],
  hideTheory: false,
  anim: !reducedMotion,
  log: true,
  busy: false,
  explainOpen: false,
  token: 0,
  seed: null,
  timer: null,
};

let ui;

// ------------------------------------------------------------------ ініціалізація

export async function initLab(app) {
  ui = app;
  state.catalog = await getJson('api/catalog');
  try {
    const data = await getJson('data/presets.json');
    state.presets = data.presets || [];
  } catch (e) {
    state.presets = [];
  }
  const sel = $('#preset');
  for (const p of state.presets) {
    const o = document.createElement('option');
    o.value = p.id;
    o.textContent = p.title;
    sel.appendChild(o);
  }
  bindStatic();
  applyUrlParams(app.params);
  renderAll();
  await requestTheory();
  const p = app.params;
  if (p.get('explain') === '1') setExplain(true);
  if (p.get('run') === '1') {
    const runs = Math.max(1, Math.min(MAX_SERIES, +(p.get('runs') || 1)));
    for (let i = 0; i < runs; i++) await run(i === 0 ? 'new' : 'again');
  }
}

function bindStatic() {
  document.querySelectorAll('#exp-type button').forEach(b => b.addEventListener('click', () => {
    if (state.config.type === b.dataset.type) return;
    state.config = { ...DEFAULTS[b.dataset.type] };
    clearSeries();
    renderAll();
    requestTheory();
  }));

  $('#preset').addEventListener('change', e => {
    const p = state.presets.find(x => x.id === e.target.value);
    if (p) openLab({ config: p.config, trials: p.trials, run: false });
  });

  $('#event').addEventListener('change', e => {
    state.config.event = e.target.value;
    if (state.config.event === 'HEADS_AFTER_STREAK') state.config.count = 1;
    fixValue(true);
    clearSeries();
    renderParams();
    renderEvent();
    requestTheory();
  });

  const ev = $('#event-value');
  ev.addEventListener('change', () => {
    state.config.value = clampInt(ev.value, ...(valueBounds() || [0, 0]));
    ev.value = state.config.value;
    clearSeries();
    requestTheory();
  });
  bindStepper($('#event-value-field'));

  document.querySelectorAll('#trial-chips .chip').forEach(c => c.addEventListener('click', () => {
    setTrials(+c.dataset.n);
  }));
  $('#trials').addEventListener('change', e => setTrials(e.target.value));

  $('#run').addEventListener('click', () => run('new'));
  $('#again').addEventListener('click', () => run('again'));
  $('#once').addEventListener('click', () => run('once'));
  $('#reset').addEventListener('click', () => reset());
  $('#anim').addEventListener('change', e => { state.anim = e.target.checked; });
  $('#log').addEventListener('change', e => { state.log = e.target.checked; renderCharts(); });
  $('#hide-theory').addEventListener('change', e => { state.hideTheory = e.target.checked; renderTheory(); renderCharts(); });
  $('#reveal-btn').addEventListener('click', () => {
    state.hideTheory = false;
    $('#hide-theory').checked = false;
    renderTheory();
    renderCharts();
  });
  $('#explain-btn').addEventListener('click', () => setExplain(!state.explainOpen));
}

function applyUrlParams(p) {
  if (p.has('preset')) {
    const pr = state.presets.find(x => x.id === p.get('preset'));
    if (pr) {
      state.config = normalize(pr.config);
      state.trials = pr.trials || state.trials;
      $('#preset').value = pr.id;
    }
  }
  if (p.has('type')) {
    const type = p.get('type').toUpperCase();
    if (DEFAULTS[type]) {
      const cfg = { ...DEFAULTS[type] };
      for (const k of ['count', 'red', 'blue', 'draw', 'value']) if (p.has(k)) cfg[k] = +p.get(k);
      if (p.has('p')) cfg.headsProbability = p.get('p');
      if (p.has('repl')) cfg.replacement = p.get('repl') === '1';
      if (p.has('event')) cfg.event = p.get('event').toUpperCase();
      state.config = cfg;
    }
  }
  if (p.has('trials')) state.trials = clampInt(p.get('trials'), 1, state.catalog.limits.maxTrials);
  if (p.has('seed')) state.seed = +p.get('seed');
  if (p.get('anim') === '0') state.anim = false;
  if (p.get('log') === '0') state.log = false;
  if (p.get('hide') === '1') state.hideTheory = true;
  $('#anim').checked = state.anim;
  $('#log').checked = state.log;
  $('#hide-theory').checked = state.hideTheory;
}

/** Відкрити лабораторію з потрібними параметрами (з уроків, практики, прикладів). */
export async function openLab({ config, trials, run: autoRun = true, from = null, runs = 1 }) {
  state.config = normalize(config);
  if (trials) state.trials = clampInt(trials, 1, state.catalog.limits.maxTrials);
  clearSeries();
  $('#back-to-practice').hidden = from !== 'practice';
  if (location.hash !== '#lab') location.hash = '#lab';
  renderAll();
  await requestTheory();
  if (autoRun) {
    for (let i = 0; i < runs; i++) await run(i === 0 ? 'new' : 'again');
  }
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function normalize(cfg) {
  const type = (cfg && cfg.type) || 'COINS';
  const out = { ...DEFAULTS[type] };
  for (const [k, v] of Object.entries(cfg || {})) if (v !== null && v !== undefined) out[k] = v;
  if (out.event === 'HEADS_AFTER_STREAK') out.count = 1;
  return out;
}

// ------------------------------------------------------------------ рендеринг налаштувань

function renderAll() {
  document.querySelectorAll('#exp-type button').forEach(b =>
    b.setAttribute('aria-pressed', String(b.dataset.type === state.config.type)));
  const info = state.catalog.experiments.find(e => e.type === state.config.type);
  $('#exp-hint').textContent = info ? info.hint : '';
  fixValue(false);
  renderParams();
  renderEvent();
  renderTrials();
  renderTheory();
  renderExp();
  renderCharts();
  renderStrip();
}

function stepperField(name, label, value, min, max, hidden = false) {
  return `<div class="field" data-field="${name}" ${hidden ? 'hidden' : ''}>
    <label class="field-label" for="f-${name}">${esc(label)}</label>
    <div class="stepper">
      <button type="button" data-step="-1" aria-label="Зменшити">−</button>
      <input id="f-${name}" data-name="${name}" type="number" min="${min}" max="${max}" value="${value}" inputmode="numeric">
      <button type="button" data-step="1" aria-label="Збільшити">+</button>
    </div></div>`;
}

function renderParams() {
  const c = state.config;
  const lim = state.catalog.limits;
  let html = '';
  if (c.type === 'COINS') {
    const biased = c.headsProbability && c.headsProbability !== '1/2' && c.headsProbability !== '0,5' && c.headsProbability !== '0.5';
    html += stepperField('count', 'Кількість монет', c.count, 1, lim.maxCoins, c.event === 'HEADS_AFTER_STREAK');
    html += `<label class="switch"><input type="checkbox" id="biased" ${biased ? 'checked' : ''}> Нечесна монета</label>`;
    if (biased) {
      const pv = Math.round(probValue(c.headsProbability) * 100);
      html += `<div class="field"><label class="field-label" for="p-range">Ймовірність герба: <b id="p-out">${dec(pv / 100, 2)}</b></label>
        <input type="range" id="p-range" min="5" max="95" step="5" value="${pv}"></div>`;
    }
  } else if (c.type === 'DICE') {
    html += stepperField('count', 'Кількість кубиків', c.count, 1, lim.maxDice);
  } else {
    html += '<div class="pair">' + stepperField('red', 'Червоних куль', c.red, 0, 20) + stepperField('blue', 'Синіх куль', c.blue, 0, 20) + '</div>';
    let balls = '';
    for (let i = 0; i < c.red; i++) balls += '<span class="ball r"></span>';
    for (let i = 0; i < c.blue; i++) balls += '<span class="ball b"></span>';
    html += `<div class="urn-preview" aria-hidden="true">${balls}</div>`;
    const maxDraw = c.replacement ? lim.maxDraw : Math.max(1, Math.min(lim.maxDraw, c.red + c.blue));
    html += stepperField('draw', c.replacement ? 'Скільки разів виймають кулю' : 'Скільки куль виймають', c.draw, 1, maxDraw);
    html += `<label class="switch"><input type="checkbox" id="replacement" ${c.replacement ? 'checked' : ''}> Кулю повертають назад (з поверненням)</label>`;
  }
  const box = $('#params');
  box.innerHTML = html;
  box.querySelectorAll('.field[data-field]').forEach(f => bindStepper(f));
  box.querySelectorAll('input[data-name]').forEach(inp => inp.addEventListener('change', () => {
    const name = inp.dataset.name;
    state.config[name] = clampInt(inp.value, +inp.min, +inp.max);
    if (state.config.type === 'URN' && !state.config.replacement) {
      state.config.draw = Math.max(1, Math.min(state.config.draw, state.config.red + state.config.blue));
    }
    fixValue(false);
    clearSeries();
    renderParams();
    renderEvent();
    requestTheory();
  }));
  const biasedBox = $('#biased');
  if (biasedBox) biasedBox.addEventListener('change', () => {
    state.config.headsProbability = biasedBox.checked ? '0,3' : '1/2';
    clearSeries();
    renderParams();
    requestTheory();
  });
  const range = $('#p-range');
  if (range) {
    range.addEventListener('input', () => { $('#p-out').textContent = dec(range.value / 100, 2); });
    range.addEventListener('change', () => {
      state.config.headsProbability = dec(range.value / 100, 2);
      clearSeries();
      requestTheory();
    });
  }
  const repl = $('#replacement');
  if (repl) repl.addEventListener('change', () => {
    state.config.replacement = repl.checked;
    if (!repl.checked) state.config.draw = Math.min(state.config.draw, state.config.red + state.config.blue);
    clearSeries();
    renderParams();
    renderEvent();
    requestTheory();
  });
}

function bindStepper(field) {
  field.querySelectorAll('button[data-step]').forEach(btn => btn.addEventListener('click', () => {
    const inp = field.querySelector('input');
    const min = inp.min === '' ? -Infinity : +inp.min;
    const max = inp.max === '' ? Infinity : +inp.max;
    const v = Math.max(min, Math.min(max, (+inp.value || 0) + (+btn.dataset.step)));
    if (v !== +inp.value) {
      inp.value = v;
      inp.dispatchEvent(new Event('change'));
    }
  }));
}

function eventAllowed(code) {
  const c = state.config;
  if (code === 'COINS_ALL_SAME') return c.count >= 2;
  if (code === 'DICE_ALL_SAME') return c.count >= 2;
  if (code === 'URN_ALL_SAME') return c.draw >= 2;
  return true;
}

function renderEvent() {
  const info = state.catalog.experiments.find(e => e.type === state.config.type);
  const sel = $('#event');
  sel.innerHTML = info.events.filter(e => eventAllowed(e.code))
    .map(e => `<option value="${e.code}">${esc(e.label)}</option>`).join('');
  if (!info.events.some(e => e.code === state.config.event && eventAllowed(e.code))) {
    state.config.event = info.events[0].code;
    fixValue(true);
  }
  sel.value = state.config.event;
  const opt = info.events.find(e => e.code === state.config.event);
  const bounds = valueBounds();
  const field = $('#event-value-field');
  field.hidden = !bounds;
  if (bounds) {
    const inp = $('#event-value');
    inp.min = bounds[0];
    inp.max = bounds[1];
    inp.value = state.config.value;
    $('#event-value-label').textContent = valueLabel(opt.valueLabel);
  }
}

function valueLabel(code) {
  const c = state.config;
  switch (code) {
    case 'k': return 'k – кількість гербів';
    case 'r': return 'r – довжина серії гербів';
    case 's': return 's – сума очок';
    case 'v': return 'v – кількість очок на грані';
    case 'm': return c.replacement ? 'm – скільки разів з\'явиться червона куля' : 'm – кількість червоних куль';
    default: return code || '';
  }
}

function valueBounds() {
  const c = state.config;
  switch (c.event) {
    case 'HEADS_EXACTLY': case 'HEADS_AT_LEAST': case 'HEADS_AT_MOST': return [0, c.count];
    case 'HEADS_AFTER_STREAK': return [1, state.catalog.limits.maxStreak];
    case 'SUM_EQ': case 'SUM_AT_LEAST': case 'SUM_AT_MOST': return [c.count, 6 * c.count];
    case 'AT_LEAST_ONE_FACE': return [1, 6];
    case 'RED_EXACTLY': case 'RED_AT_LEAST': return [0, c.draw];
    default: return null;
  }
}

/** Тримає параметр події в допустимих межах (або задає типове значення). */
function fixValue(reset) {
  const b = valueBounds();
  if (!b) {
    state.config.value = null;
    return;
  }
  let v = state.config.value;
  if (reset || v === null || v === undefined || isNaN(v)) {
    const c = state.config;
    v = { HEADS_AFTER_STREAK: 3, AT_LEAST_ONE_FACE: 6, SUM_EQ: Math.round(3.5 * c.count), SUM_AT_LEAST: Math.round(4.5 * c.count), SUM_AT_MOST: Math.round(2.5 * c.count) }[c.event];
    if (v === undefined) v = Math.ceil(b[1] / 2);
  }
  state.config.value = Math.max(b[0], Math.min(b[1], v));
}

function renderTrials() {
  $('#trials').value = state.trials;
  $('#trials').max = state.catalog.limits.maxTrials;
  document.querySelectorAll('#trial-chips .chip').forEach(c =>
    c.setAttribute('aria-pressed', String(+c.dataset.n === state.trials)));
}

function setTrials(v) {
  state.trials = clampInt(v, 1, state.catalog.limits.maxTrials);
  renderTrials();
  if (!state.series.length) renderCharts();
}

// ------------------------------------------------------------------ теорія

let theoryTimer = null;

function requestTheory() {
  clearTimeout(theoryTimer);
  return new Promise(resolve => {
    theoryTimer = setTimeout(async () => {
      const token = ++state.token;
      try {
        const t = await api('api/theory', cleanConfig());
        if (token !== state.token) return resolve();
        state.theory = t;
        $('#config-error').hidden = true;
      } catch (e) {
        if (token !== state.token) return resolve();
        state.theory = null;
        $('#config-error').textContent = e.message;
        $('#config-error').hidden = false;
      }
      renderTheory();
      renderExplain();
      renderCharts();
      resolve();
    }, 120);
  });
}

function cleanConfig() {
  const c = state.config;
  const base = { type: c.type, event: c.event, value: c.value };
  if (c.type === 'COINS') return { ...base, count: c.count, headsProbability: c.headsProbability };
  if (c.type === 'DICE') return { ...base, count: c.count };
  return { ...base, red: c.red, blue: c.blue, draw: c.draw, replacement: !!c.replacement };
}

function renderTheory() {
  const t = state.theory;
  const big = $('#theory-big');
  const sub = $('#theory-sub');
  const text = $('#event-text');
  $('#reveal-btn').hidden = !state.hideTheory;
  $('#explain-btn').disabled = state.hideTheory || !t;
  if (!t) {
    big.innerHTML = 'P(A) = –';
    sub.textContent = '';
    text.innerHTML = '';
    return;
  }
  text.innerHTML = `A = <b>${esc(t.eventText)}</b>`;
  if (state.hideTheory) {
    big.innerHTML = '<span class="hidden-theory">P(A) = ?</span>';
    sub.textContent = 'Проведіть експеримент і спрогнозуйте ймовірність за частотою. Потім відкрийте теорію і порівняйте.';
    if (state.explainOpen) setExplain(false);
    return;
  }
  big.innerHTML = `P(A) = ${fracHtml(t.probability)}<span class="approx">${t.probability.includes('/') ? esc(t.approxSign) + ' ' + esc(t.decimal) : ''}</span>`;
  sub.innerHTML = t.favorable
    ? `m = <b>${int(+t.favorable)}</b> ${plural(+t.favorable, 'сприятливий', 'сприятливі', 'сприятливих')} з n = <b>${int(+t.total)}</b> рівноможливих наслідків`
    : `Спосіб: ${esc(t.method)}`;
}

function renderExplain() {
  const t = state.theory;
  if (!t) return;
  $('#explain-method').textContent = 'Спосіб: ' + t.method;
  $('#explain-steps').innerHTML = t.steps.map(s => `<li><div>
      <h3>${esc(s.title)}</h3>
      <p>${esc(s.text)}</p>
      ${s.formula ? `<div class="formula">${tex(s.formula)}</div>` : ''}
    </div></li>`).join('');
  const box = $('#outcomes-box');
  const o = t.outcomes;
  if (!o) {
    box.hidden = true;
    return;
  }
  box.hidden = false;
  $('#outcomes-caption').textContent = o.caption;
  if (o.kind === 'grid') {
    let h = '<table class="grid6"><thead><tr><th></th>' + o.colLabels.map(c => `<th scope="col">${esc(c)}</th>`).join('') + '</tr></thead><tbody>';
    o.cells.forEach((row, i) => {
      h += `<tr><th scope="row">${esc(o.rowLabels[i])}</th>` + row.map((v, j) => `<td class="${o.marks[i][j] ? 'on' : ''}">${esc(v)}</td>`).join('') + '</tr>';
    });
    $('#outcomes').innerHTML = h + '</tbody></table>';
  } else {
    $('#outcomes').innerHTML = '<div class="seq-list">' + o.cells[0].map((v, j) => `<span class="${o.marks[0][j] ? 'on' : ''}">${esc(v)}</span>`).join('') + '</div>';
  }
}

export function setExplain(open) {
  if (open && (state.hideTheory || !state.theory)) return;
  state.explainOpen = open;
  $('#explain').hidden = !open;
  $('#explain-btn').setAttribute('aria-expanded', String(open));
  $('#explain-btn').textContent = open ? 'Сховати пояснення' : 'Поясни мені';
}

export function toggleExplain() {
  setExplain(!state.explainOpen);
}

// ------------------------------------------------------------------ моделювання

function nextSeed() {
  if (state.seed == null) return undefined;
  return state.seed++;
}

export async function run(mode = 'new') {
  if (state.busy || !state.theory && !state.hideTheory && $('#config-error').hidden === false) return;
  const n = mode === 'once' ? 1 : state.trials;
  state.busy = true;
  setBusy(true);
  try {
    const res = await api('api/simulate', { config: cleanConfig(), trials: n, seed: nextSeed() });
    let cur = state.series.find(s => s.current);
    if (mode === 'once' && cur) {
      cur.points.push([cur.trials + 1, cur.successes + res.successes]);
      cur.trials += 1;
      cur.successes += res.successes;
      res.histogram.forEach((v, i) => { cur.hist[i] += v; });
      cur.last = cur.last.concat(res.lastOutcomes).slice(-30);
      cur.fresh = true;
      renderAfterRun();
    } else {
      if (mode === 'new') state.series = [];
      state.series.forEach(s => { s.current = false; });
      if (state.series.length >= MAX_SERIES) state.series.shift();
      cur = {
        current: true,
        points: res.points.map(([i, m]) => [i, m]),
        trials: res.trials,
        successes: res.successes,
        hist: res.histogram.slice(),
        histMin: res.histogramMin,
        last: res.lastOutcomes,
        fresh: true,
        visible: res.points.length,
      };
      state.series.push(cur);
      if (state.anim && !reducedMotion && n > 1) await animate(cur);
      renderAfterRun();
    }
  } catch (e) {
    ui.toast(e.message);
  } finally {
    state.busy = false;
    setBusy(false);
  }
}

function setBusy(b) {
  for (const id of ['#run', '#again', '#once']) $(id).disabled = b;
}

function animate(series) {
  return new Promise(resolve => {
    const total = series.points.length;
    const N = series.trials;
    const duration = Math.min(2600, 700 + 380 * Math.log10(Math.max(10, N)));
    const t0 = performance.now();
    const step = now => {
      const t = Math.min(1, (now - t0) / duration);
      const e = 1 - (1 - t) ** 2;
      if (state.log) {
        series.visible = Math.max(1, Math.round(e * total));
      } else {
        const nAt = e * N;
        let k = 0;
        while (k < total && series.points[k][0] <= nAt) k++;
        series.visible = Math.max(1, k);
      }
      renderExp(series);
      renderCharts();
      if (t < 1) requestAnimationFrame(step);
      else {
        series.visible = total;
        resolve();
      }
    };
    requestAnimationFrame(step);
  });
}

function renderAfterRun() {
  renderExp();
  renderCharts();
  renderStrip();
}

export function reset() {
  clearSeries();
  renderExp();
  renderCharts();
  renderStrip();
}

function clearSeries() {
  state.series = [];
}

function renderExp(partial) {
  const cur = state.series.find(s => s.current);
  const big = $('#exp-big');
  const sub = $('#exp-sub');
  if (!cur) {
    big.innerHTML = 'W(A) = –';
    sub.textContent = 'Натисніть «Запустити симуляцію», щоб провести випробування.';
    return;
  }
  let N = cur.trials, M = cur.successes;
  if (partial && partial.visible < partial.points.length) {
    [N, M] = partial.points[partial.visible - 1];
  }
  const w = M / N;
  big.innerHTML = `W(A) = ${N <= 99999 ? fracHtml(M + '/' + N) : ''}<span class="approx">≈ ${dec(w, 4)}</span>`;
  let s = `Подія A відбулася <b>${int(M)}</b> ${plural(M, 'раз', 'рази', 'разів')} у <b>${int(N)}</b> ${plural(N, 'випробуванні', 'випробуваннях', 'випробуваннях')}.`;
  if (state.theory && !state.hideTheory) s += ` Відхилення від ймовірності: <b>|W − P| = ${dec(Math.abs(w - state.theory.value), 4)}</b>.`;
  const series = state.series.length;
  if (series > 1) s += ` Серій на графіку: <b>${series}</b>.`;
  sub.innerHTML = s;
}

function renderCharts() {
  const t = state.theory;
  const p = t && !state.hideTheory ? t.value : null;
  const series = state.series.map(s => ({
    current: s.current,
    points: s.points.slice(0, s.current ? s.visible ?? s.points.length : s.points.length).map(([n, m]) => [n, m / n]),
  }));
  const maxN = Math.max(state.trials, ...state.series.map(s => s.trials));
  drawConvergence($('#conv-chart'), { series, p, maxN, log: state.log });
  $('#legend-old').hidden = state.series.length < 2;
  const cur = state.series.find(s => s.current);
  const note = $('#conv-note');
  if (cur && p != null) {
    const sigma = Math.sqrt(p * (1 - p) / cur.trials);
    note.textContent = `При N = ${int(cur.trials)} частота зазвичай відхиляється від P не більше ніж на ±${dec(2 * sigma, 4)} (±2σ). Зі зростанням N смуга звужується – це і є закон великих чисел.`;
  } else {
    note.textContent = '';
  }

  if (t) {
    const d = t.distribution;
    let empirical = null;
    if (cur && (cur.visible ?? cur.points.length) >= cur.points.length) {
      const total = cur.hist.reduce((a, b) => a + b, 0) || 1;
      empirical = cur.hist.map(v => v / total);
    }
    $('#dist-title').textContent = 'Розподіл: ' + d.statisticName;
    drawDistribution($('#dist-chart'), {
      labels: d.labels,
      theory: state.hideTheory ? d.probabilities.map(() => 0) : d.probabilities,
      empirical,
      inEvent: d.inEvent,
      statName: d.statisticName,
    });
  }
}

// ------------------------------------------------------------------ стрічка останніх наслідків

const PIPS = { 1: [4], 2: [0, 8], 3: [0, 4, 8], 4: [0, 2, 6, 8], 5: [0, 2, 4, 6, 8], 6: [0, 2, 3, 5, 6, 8] };

function tokenHtml(tok) {
  if (tok === 'Г') return '<span class="coin" title="герб">Г</span>';
  if (tok === 'Ц') return '<span class="coin t" title="цифра">Ц</span>';
  if (tok === 'Ч') return '<span class="ball r" title="червона"></span>';
  if (tok === 'С') return '<span class="ball b" title="синя"></span>';
  const v = +tok;
  if (v >= 1 && v <= 6) {
    let cells = '';
    for (let i = 0; i < 9; i++) cells += `<i class="${PIPS[v].includes(i) ? 'on' : ''}"></i>`;
    return `<span class="die" title="${v}">${cells}</span>`;
  }
  return esc(tok);
}

function renderStrip() {
  const cur = state.series.find(s => s.current);
  const box = $('#strip');
  if (!cur || !cur.last.length) {
    box.innerHTML = '<span class="muted small">Тут з\'являться результати останніх випробувань.</span>';
    return;
  }
  const streak = state.config.event === 'HEADS_AFTER_STREAK';
  const items = cur.last.slice(-(streak ? 12 : 24));
  box.innerHTML = items.map((o, idx) => {
    let inner;
    if (streak) {
      const t = o.tokens;
      inner = t.slice(0, -1).map(tokenHtml).join('') + '<span class="arrow">→</span>' + tokenHtml(t[t.length - 1]);
    } else {
      inner = o.tokens.map(tokenHtml).join('');
    }
    const fresh = cur.fresh && idx === items.length - 1 ? ' fresh' : '';
    return `<span class="outcome${o.success ? ' hit' : ''}${fresh}" title="${o.success ? 'подія A відбулася' : 'подія A не відбулася'}">${inner}</span>`;
  }).join('');
  cur.fresh = false;
}

// ------------------------------------------------------------------ допоміжне

function clampInt(v, min, max) {
  const n = Math.round(+v);
  if (!isFinite(n)) return min;
  return Math.max(min, Math.min(max, n));
}

function probValue(text) {
  if (!text) return 0.5;
  const s = String(text).replace(',', '.');
  if (s.includes('/')) {
    const [a, b] = s.split('/');
    return +a / +b;
  }
  return +s;
}
