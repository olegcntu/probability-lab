/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Генератор паралельних варіантів з ключем відповідей; друк або збереження в PDF засобами браузера.
import { api } from './api.js';
import { esc } from './format.js';
import { AUTHOR } from './author.js';

const $ = sel => document.querySelector(sel);
const LEVEL_NAMES = { 1: 'базовий', 2: 'середній', 3: 'достатній і високий' };

let current = null;
let level = 2;
let ui;

export function initTasks(app) {
  ui = app;
  const p = app.params;
  if (p.has('variants')) $('#t-variants').value = p.get('variants');
  if (p.has('tasks')) $('#t-count').value = p.get('tasks');
  if (p.has('tseed')) $('#t-seed').value = p.get('tseed');
  if (p.has('tlevel')) level = Math.max(1, Math.min(3, +p.get('tlevel')));
  document.querySelectorAll('#t-level .chip').forEach(c => {
    c.setAttribute('aria-pressed', String(+c.dataset.level === level));
    c.addEventListener('click', () => {
      level = +c.dataset.level;
      document.querySelectorAll('#t-level .chip').forEach(x => x.setAttribute('aria-pressed', String(x === c)));
    });
  });
  $('#tasks-form').addEventListener('submit', e => {
    e.preventDefault();
    generate();
  });
  $('#t-key').addEventListener('change', () => current && render());
  $('#t-print').addEventListener('click', () => {
    if (!current) {
      ui.toast('Спершу згенеруйте варіанти.');
      return;
    }
    window.print();
  });
  $('#t-copy').addEventListener('click', copyText);
  if (p.get('gen') === '1') generate();
}

async function generate() {
  const topics = [...document.querySelectorAll('input[name="topic"]:checked')].map(i => i.value);
  const seedRaw = $('#t-seed').value.trim();
  try {
    current = await api('api/tasks/generate', {
      variants: +$('#t-variants').value || 2,
      tasksPerVariant: +$('#t-count').value || 5,
      level,
      topics,
      seed: seedRaw ? +seedRaw : null,
      title: $('#t-title').value,
    });
  } catch (e) {
    ui.toast(e.message);
    return;
  }
  render();
}

function render() {
  const s = current;
  const withKey = $('#t-key').checked;
  let h = s.variants.map(v => `
    <article class="sheet">
      <h3>${esc(s.title)}</h3>
      <p class="sheet-sub">Варіант ${v.number} · рівень: ${LEVEL_NAMES[s.level]}</p>
      <div class="fill"><span>Прізвище, ім'я:</span><span>Клас:</span><span>Дата:</span></div>
      <ol>${v.tasks.map(t => `<li>${esc(t.text)}</li>`).join('')}</ol>
      <p class="sheet-foot">Згенеровано «Лабораторією ймовірностей» (автор – ${AUTHOR.short}, гр. ${AUTHOR.group}) · код набору ${s.seed}</p>
    </article>`).join('');
  if (withKey) {
    h += `<article class="sheet">
      <h3>Ключ відповідей</h3>
      <p class="sheet-sub">${esc(s.title)} · код набору ${s.seed}</p>
      <table class="key"><thead><tr><th>Варіант</th>${s.variants[0].tasks.map(t => `<th>№ ${t.number}</th>`).join('')}</tr></thead>
      <tbody>${s.variants.map(v => `<tr><td>${v.number}</td>${v.tasks.map(t => `<td>${esc(t.answer)}${t.answer.includes('/') ? ` ≈ ${esc(t.answerDecimal)}` : ''}</td>`).join('')}</tr>`).join('')}</tbody></table>
      <p class="sheet-foot">Згенеровано «Лабораторією ймовірностей» (автор – ${AUTHOR.short}, гр. ${AUTHOR.group})</p>
    </article>`;
  }
  $('#sheets').innerHTML = h;
  $('#t-seed-note').innerHTML = `Код набору: <b>${s.seed}</b>. Введіть його в поле «Код набору», щоб пізніше отримати ті самі варіанти.`;
}

async function copyText() {
  if (!current) {
    ui.toast('Спершу згенеруйте варіанти.');
    return;
  }
  const s = current;
  let text = '';
  for (const v of s.variants) {
    text += `${s.title}\nВаріант ${v.number}\n`;
    for (const t of v.tasks) text += `${t.number}. ${t.text}\n`;
    text += '\n';
  }
  text += 'Ключ відповідей\n';
  for (const v of s.variants) text += `Варіант ${v.number}: ` + v.tasks.map(t => `${t.number}) ${t.answer}`).join('; ') + '\n';
  text += `\nЗгенеровано «Лабораторією ймовірностей» (автор – ${AUTHOR.short}, гр. ${AUTHOR.group}), код набору ${s.seed}\n`;
  try {
    await navigator.clipboard.writeText(text);
    ui.toast('Текст варіантів скопійовано.');
  } catch (e) {
    const ta = document.createElement('textarea');
    ta.value = text;
    document.body.appendChild(ta);
    ta.select();
    try {
      document.execCommand('copy');
      ui.toast('Текст варіантів скопійовано.');
    } catch (e2) {
      ui.toast('Не вдалося скопіювати. Виділіть текст вручну.');
    }
    ta.remove();
  }
}
