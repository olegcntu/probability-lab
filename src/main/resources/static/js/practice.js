/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Режим практики: завдання з миттєвим зворотним зв'язком. Відповіді перевіряє сервер.
import { api } from './api.js';
import { esc, rich } from './format.js';
import { openLab } from './lab.js';

const $ = sel => document.querySelector(sel);

const state = {
  level: 0,
  question: null,
  attempt: 1,
  chosen: null,
  done: false,
  results: [],
  lab: null,
  seed: null,
};

let ui;

export function initPractice(app) {
  ui = app;
  const p = app.params;
  if (p.has('level')) state.level = Math.max(0, Math.min(3, +p.get('level')));
  if (p.has('qseed')) state.seed = +p.get('qseed');
  document.querySelectorAll('#level-chips .chip').forEach(c => {
    c.setAttribute('aria-pressed', String(+c.dataset.level === state.level));
    c.addEventListener('click', () => {
      state.level = +c.dataset.level;
      document.querySelectorAll('#level-chips .chip').forEach(x => x.setAttribute('aria-pressed', String(x === c)));
      next();
    });
  });
  renderScore();
}

let started = false;

/** Викликається, коли учень відкриває розділ «Практика». */
export async function showPractice() {
  if (started) return;
  started = true;
  await next();
  const demo = ui.params.get('answer');
  if (demo && state.question) {
    if (state.question.kind === 'CHOICE') state.chosen = +demo;
    else $('#answer').value = demo;
    await check();
  }
}

async function next() {
  state.attempt = 1;
  state.chosen = null;
  state.done = false;
  state.lab = null;
  const q = $('#question');
  try {
    const seed = state.seed != null ? `&seed=${state.seed++}` : '';
    state.question = await api(`api/practice/next?level=${state.level}${seed}`);
  } catch (e) {
    q.innerHTML = `<div class="error-box">${esc(e.message)}</div>`;
    return;
  }
  renderQuestion();
}

function renderQuestion() {
  const q = state.question;
  const levelName = { 1: 'рівень 1', 2: 'рівень 2', 3: 'рівень 3' }[q.level] || '';
  let answer;
  if (q.kind === 'CHOICE') {
    answer = `<div class="choices" role="group" aria-label="Варіанти відповіді">${q.choices.map((c, i) =>
      `<button type="button" class="btn choice" data-i="${i}" aria-pressed="false">${String.fromCharCode(1040 + i)}) ${esc(c)}</button>`).join('')}</div>`;
  } else {
    answer = `<div class="answer-row">
      <label class="field-label" for="answer" style="width:100%">Ваша відповідь</label>
      <input id="answer" class="input" autocomplete="off" inputmode="decimal" placeholder="наприклад, 3/8 або 0,375">
    </div>`;
  }
  $('#question').innerHTML = `
    <div class="q-meta"><span class="badge">${esc(q.topic)}</span><span class="badge">${levelName}</span></div>
    <p class="q-text">${esc(q.text)}</p>
    ${answer}
    <div class="btn-row" style="max-width:640px">
      <button type="button" class="btn btn-run" id="check">Перевірити</button>
      <button type="button" class="btn" id="next">Наступне завдання</button>
    </div>
    <div id="feedback" aria-live="polite"></div>`;
  if (q.kind === 'CHOICE') {
    document.querySelectorAll('.choice').forEach(b => b.addEventListener('click', () => {
      if (state.done) return;
      state.chosen = +b.dataset.i;
      document.querySelectorAll('.choice').forEach(x => x.setAttribute('aria-pressed', String(x === b)));
    }));
  } else {
    const inp = $('#answer');
    inp.addEventListener('keydown', e => { if (e.key === 'Enter') check(); });
    setTimeout(() => inp.focus({ preventScroll: true }), 50);
  }
  $('#check').addEventListener('click', check);
  $('#next').addEventListener('click', next);
}

async function check() {
  const q = state.question;
  if (!q || state.done) return;
  const answer = q.kind === 'CHOICE' ? (state.chosen == null ? '' : String(state.chosen)) : $('#answer').value;
  if (answer.trim() === '') {
    showFeedback({ cls: 'info', title: 'Спершу дайте відповідь', message: q.kind === 'CHOICE' ? 'Оберіть один із варіантів.' : 'Введіть число у поле відповіді.' });
    return;
  }
  let fb;
  try {
    fb = await api('api/practice/check', { id: q.id, answer, attempt: state.attempt });
  } catch (e) {
    showFeedback({ cls: 'no', title: 'Помилка', message: e.message });
    return;
  }
  if (!fb.counted) {
    showFeedback({ cls: 'info', title: 'Не вдалося прочитати відповідь', message: fb.message });
    return;
  }
  state.lab = fb.lab;
  const final = fb.correct || state.attempt >= 2;
  if (final) {
    state.done = true;
    state.results.push(fb.correct && state.attempt === 1 ? 'ok' : fb.correct ? 'ok2' : 'no');
    renderScore();
    if (q.kind === 'CHOICE') {
      document.querySelectorAll('.choice').forEach(b => {
        const i = +b.dataset.i;
        if (fb.expected && q.choices[i] === fb.expected) b.classList.add('right');
        else if (i === state.chosen && !fb.correct) b.classList.add('wrong');
      });
    }
    $('#check').disabled = true;
  }
  state.attempt++;
  showFeedback({
    cls: fb.correct ? 'ok' : 'no',
    title: fb.correct ? 'Правильно' : final ? 'Неправильно' : 'Спробуйте ще раз',
    message: fb.message,
    hint: fb.hint,
    solution: final ? fb.solution : [],
    lab: final && fb.lab,
  });
}

function showFeedback({ cls, title, message, hint, solution = [], lab }) {
  let h = `<div class="feedback ${cls}"><h3>${esc(title)}</h3><p>${esc(message || '')}</p>`;
  if (hint) h += `<p><b>Підказка.</b> ${esc(hint)}</p>`;
  if (solution && solution.length) h += `<ol class="solution rich">${solution.map(s => `<li>${rich(s)}</li>`).join('')}</ol>`;
  if (lab) h += `<div class="btn-row" style="margin-top:12px;max-width:520px"><button type="button" class="btn btn-theory" id="to-lab">Перевірити експериментом</button></div>`;
  h += '</div>';
  $('#feedback').innerHTML = h;
  const b = $('#to-lab');
  if (b) b.addEventListener('click', () => openLab({ config: state.lab, trials: 2000, run: true, from: 'practice' }));
}

function renderScore() {
  const total = state.results.length;
  const ok = state.results.filter(r => r !== 'no').length;
  $('#score-big').textContent = `${ok} з ${total}`;
  $('#score-dots').innerHTML = state.results.map(r => `<i class="${r === 'no' ? 'no' : 'ok'}" title="${r === 'no' ? 'неправильно' : r === 'ok2' ? 'правильно з другої спроби' : 'правильно з першої спроби'}"></i>`).join('');
}
