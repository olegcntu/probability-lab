/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Графіки на SVG без сторонніх бібліотек: збіжність частоти та порівняння розподілів.
import { dec, int, esc } from './format.js';
import { AUTHOR } from './author.js';

const NICE_TOPS = [0.05, 0.1, 0.2, 0.25, 0.5, 1];

function niceTop(base) {
  const want = Math.min(1, Math.max(2.2 * base, base + 0.08, 0.05));
  return NICE_TOPS.find(t => t >= want - 1e-12) ?? 1;
}

function niceMax(v) {
  if (v <= 0) return 0.1;
  const steps = [0.05, 0.1, 0.15, 0.2, 0.25, 0.3, 0.4, 0.5, 0.6, 0.8, 1];
  return steps.find(s => s >= v * 1.08) ?? 1;
}

function xTicksLinear(maxN) {
  const raw = maxN / 4;
  const pow = 10 ** Math.floor(Math.log10(raw));
  const step = [1, 2, 2.5, 5, 10].map(m => m * pow).find(s => s >= raw) ?? raw;
  const ticks = [];
  for (let v = 0; v <= maxN + 1e-9; v += step) ticks.push(Math.round(v));
  return ticks;
}

/**
 * Графік збіжності відносної частоти до ймовірності.
 * @param {SVGSVGElement} svg
 * @param {{series: {points:number[][], current:boolean}[], p:number|null, maxN:number, log:boolean}} d
 */
export function drawConvergence(svg, d) {
  const W = 760, H = 320, L = 58, R = 92, T = 14, B = 46;
  const w = W - L - R, h = H - T - B;
  const maxN = Math.max(10, d.maxN || 10);
  const log = !!d.log;
  const current = d.series.find(s => s.current);
  const lastFreq = current && current.points.length ? current.points[current.points.length - 1][1] : null;
  const base = d.p != null ? d.p : (lastFreq != null ? lastFreq : 0.5);
  const yTop = niceTop(base);

  const x = n => log
    ? L + (Math.log10(Math.max(1, n)) / Math.log10(maxN)) * w
    : L + (n / maxN) * w;
  const y = v => T + h - (v / yTop) * h;

  let g = '';
  // сітка «у клітинку»
  g += '<g class="grid">';
  const yTicks = [0, 0.25, 0.5, 0.75, 1].map(f => f * yTop);
  for (const v of yTicks) g += `<line x1="${L}" x2="${L + w}" y1="${y(v)}" y2="${y(v)}"/>`;
  const xTicks = log
    ? [1, 10, 100, 1000, 10000, 100000, 1000000].filter(t => t <= maxN * 1.0001)
    : xTicksLinear(maxN);
  for (const t of xTicks) g += `<line y1="${T}" y2="${T + h}" x1="${x(t)}" x2="${x(t)}"/>`;
  g += '</g>';

  // підписи осей
  g += '<g class="axis">';
  g += `<line x1="${L}" x2="${L + w}" y1="${T + h}" y2="${T + h}"/><line x1="${L}" x2="${L}" y1="${T}" y2="${T + h}"/>`;
  g += '</g>';
  for (const v of yTicks) g += `<text x="${L - 8}" y="${y(v) + 4}" text-anchor="end">${dec(v, 3)}</text>`;
  for (const t of xTicks) g += `<text x="${x(t)}" y="${T + h + 18}" text-anchor="middle">${int(t)}</text>`;
  g += `<text class="axis-title" x="${L + w / 2}" y="${H - 6}" text-anchor="middle">кількість випробувань n${log ? ' (логарифмічна шкала)' : ''}</text>`;
  g += `<text class="axis-title" transform="translate(14 ${T + h / 2}) rotate(-90)" text-anchor="middle">відносна частота</text>`;

  const clipId = 'clip-' + (svg.id || 'c');
  g += `<defs><clipPath id="${clipId}"><rect x="${L}" y="${T - 1}" width="${w}" height="${h + 2}"/></clipPath></defs>`;
  g += `<g clip-path="url(#${clipId})">`;

  // смуга ±2σ навколо теоретичної ймовірності
  if (d.p != null && d.p > 0 && d.p < 1) {
    const up = [], down = [];
    const steps = 120;
    for (let k = 0; k <= steps; k++) {
      const n = log ? 10 ** (Math.log10(maxN) * k / steps) : Math.max(1, maxN * k / steps);
      const s = 2 * Math.sqrt(d.p * (1 - d.p) / n);
      up.push(`${x(n).toFixed(1)},${y(Math.min(yTop * 1.5, d.p + s)).toFixed(1)}`);
      down.push(`${x(n).toFixed(1)},${y(Math.max(-0.5, d.p - s)).toFixed(1)}`);
    }
    g += `<polygon class="band" points="${up.join(' ')} ${down.reverse().join(' ')}"/>`;
  }

  // попередні та поточна серії
  for (const s of d.series) {
    if (!s.points.length) continue;
    const pts = s.points.map(([n, f]) => `${x(n).toFixed(1)},${y(f).toFixed(1)}`).join(' ');
    g += `<polyline class="series ${s.current ? 'current' : 'old'}" points="${pts}"/>`;
  }
  g += '</g>';

  if (d.p != null) {
    g += `<line class="theory-line" x1="${L}" x2="${L + w}" y1="${y(d.p)}" y2="${y(d.p)}"/>`;
    g += `<text class="theory-label" x="${L + w + 8}" y="${y(d.p) + 4}">P = ${dec(d.p, 3)}</text>`;
  }
  if (current && current.points.length) {
    const [n, f] = current.points[current.points.length - 1];
    const cy = Math.max(T, Math.min(T + h, y(f)));
    g += `<circle class="end-dot" cx="${x(n)}" cy="${cy}" r="5"/>`;
    let ly = cy + 4;
    if (d.p != null && Math.abs(ly - (y(d.p) + 4)) < 16) ly = cy > y(d.p) ? y(d.p) + 22 : y(d.p) - 12;
    g += `<text class="end-label" x="${L + w + 8}" y="${ly}">W = ${dec(f, 3)}</text>`;
  } else {
    g += `<text x="${L + w / 2}" y="${T + h / 2 - 10}" text-anchor="middle">Запустіть симуляцію – тут з'явиться крива відносної частоти</text>`;
  }

  g += `<text class="credit" x="${W - 4}" y="${H - 6}" text-anchor="end">© ${AUTHOR.short}, ${AUTHOR.year}</text>`;
  g += `<g class="hover" pointer-events="none"></g>`;
  g += `<rect class="hit" x="${L}" y="${T}" width="${w}" height="${h}" fill="transparent"/>`;
  svg.innerHTML = g;

  // підказка при наведенні
  svg._chart = { L, T, w, h, maxN, log, x, y, current };
  if (!svg._hoverBound) {
    svg._hoverBound = true;
    svg.addEventListener('pointermove', ev => hover(svg, ev));
    svg.addEventListener('pointerleave', () => {
      const hg = svg.querySelector('.hover');
      if (hg) hg.innerHTML = '';
    });
  }
}

function hover(svg, ev) {
  const c = svg._chart;
  const hg = svg.querySelector('.hover');
  if (!c || !hg || !c.current || !c.current.points.length) return;
  const pt = svg.createSVGPoint();
  pt.x = ev.clientX;
  pt.y = ev.clientY;
  const loc = pt.matrixTransform(svg.getScreenCTM().inverse());
  if (loc.x < c.L || loc.x > c.L + c.w) {
    hg.innerHTML = '';
    return;
  }
  const frac = (loc.x - c.L) / c.w;
  const n = c.log ? 10 ** (Math.log10(c.maxN) * frac) : c.maxN * frac;
  const pts = c.current.points;
  let lo = 0, hi = pts.length - 1;
  while (lo < hi) {
    const mid = (lo + hi) >> 1;
    if (pts[mid][0] < n) lo = mid + 1; else hi = mid;
  }
  const [pn, pf] = pts[lo];
  const px = c.x(pn), py = Math.max(c.T, Math.min(c.T + c.h, c.y(pf)));
  const label = `n = ${int(pn)};  W = ${dec(pf, 4)}`;
  const tw = label.length * 6.6 + 16;
  const tx = Math.min(px + 10, c.L + c.w - tw);
  hg.innerHTML = `<line class="hover-line" x1="${px}" x2="${px}" y1="${c.T}" y2="${c.T + c.h}"/>`
    + `<circle cx="${px}" cy="${py}" r="4" fill="var(--exp)"/>`
    + `<rect class="tip-bg" x="${tx}" y="${c.T + 6}" width="${tw}" height="24" rx="6"/>`
    + `<text class="tip" x="${tx + 8}" y="${c.T + 22}">${esc(label)}</text>`;
}

/**
 * Порівняння теоретичного розподілу (контури) та емпіричного (заливка).
 */
export function drawDistribution(svg, { labels, theory, empirical, inEvent, statName }) {
  const W = 420, H = 240, L = 44, R = 10, T = 12, B = 46;
  const w = W - L - R, h = H - T - B;
  const k = labels.length;
  const maxV = Math.max(...theory, ...(empirical || [0]));
  const top = niceMax(maxV);
  const bw = w / k;
  const y = v => T + h - (v / top) * h;
  let g = '<g class="grid">';
  const ticks = [0, 0.5, 1].map(f => f * top);
  for (const v of ticks) g += `<line x1="${L}" x2="${L + w}" y1="${y(v)}" y2="${y(v)}"/>`;
  g += '</g>';
  for (const v of ticks) g += `<text x="${L - 6}" y="${y(v) + 4}" text-anchor="end">${dec(v, 2)}</text>`;
  const every = k > 14 ? 2 : 1;
  for (let i = 0; i < k; i++) {
    const cx = L + bw * (i + 0.5);
    const tw = Math.max(4, bw * 0.72), ew = Math.max(3, bw * 0.4);
    if (empirical) {
      g += `<rect class="bar-e" x="${cx - ew / 2}" y="${y(empirical[i])}" width="${ew}" height="${Math.max(0, T + h - y(empirical[i]))}"/>`;
    }
    g += `<rect class="bar-t" x="${cx - tw / 2}" y="${y(theory[i])}" width="${tw}" height="${Math.max(0, T + h - y(theory[i]))}" rx="2"/>`;
    if (i % every === 0 || inEvent[i]) {
      g += `<text class="bar-label${inEvent[i] ? ' in-event' : ''}" x="${cx}" y="${T + h + 16}" text-anchor="middle">${esc(labels[i])}</text>`;
    }
    if (inEvent[i]) {
      g += `<path class="event-mark" d="M${cx - 4} ${T + h + 26} L${cx + 4} ${T + h + 26} L${cx} ${T + h + 20} Z"/>`;
    }
  }
  g += `<line x1="${L}" x2="${L + w}" y1="${T + h}" y2="${T + h}" stroke="var(--rule)"/>`;
  g += `<text class="axis-title" x="${L + w / 2}" y="${H - 4}" text-anchor="middle">${esc(statName)} (▲ – значення, що входять до події A)</text>`;
  g += `<text class="credit" x="${W - 4}" y="10" text-anchor="end">© ${AUTHOR.short}</text>`;
  svg.innerHTML = g;
}
