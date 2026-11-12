/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Шаблони уроків з кнопками, що налаштовують лабораторію.
import { getJson } from './api.js';
import { esc } from './format.js';
import { openLab } from './lab.js';
import { AUTHOR } from './author.js';

const $ = sel => document.querySelector(sel);

export async function initLessons(app) {
  let data;
  try {
    data = await getJson('data/lessons.json');
  } catch (e) {
    $('#lessons').innerHTML = `<div class="error-box">${esc(e.message)}</div>`;
    return;
  }
  const open = +(app.params.get('lesson') || 0);
  $('#lessons').innerHTML = data.lessons.map(l => `
    <details class="panel lesson" ${l.id === open ? 'open' : ''}>
      <summary>
        <span class="lesson-no">${l.id}</span>
        <span><h2>${esc(l.title)}</h2><span class="meta">${esc(l.grade)} · ${esc(l.duration)} · ${esc(l.type)}</span></span>
        <span class="chev" aria-hidden="true">⌄</span>
      </summary>
      <div class="lesson-body">
        <div><h3>Мета</h3><p class="muted">${esc(l.goal)}</p></div>
        <div><h3>Очікувані результати</h3><ul>${l.results.map(r => `<li>${esc(r)}</li>`).join('')}</ul></div>
        <div><h3>Обладнання</h3><p class="muted">${esc(l.equipment)}</p></div>
        <div class="table-wrap"><table class="stages">
          <thead><tr><th>Етап</th><th>Хв</th><th>Діяльність учителя</th><th>Діяльність учнів</th><th></th></tr></thead>
          <tbody>${l.stages.map((s, i) => `<tr>
            <td><b>${esc(s.name)}</b></td>
            <td class="min">${s.minutes}</td>
            <td>${esc(s.teacher)}</td>
            <td>${esc(s.students)}</td>
            <td>${s.lab ? `<button type="button" class="btn btn-theory" data-lesson="${l.id}" data-stage="${i}">Відкрити в лабораторії</button>` : ''}</td>
          </tr>`).join('')}</tbody>
        </table></div>
        <div><h3>Домашнє завдання</h3><p class="muted">${esc(l.homework)}</p></div>
        <p class="attribution">Автор сценарію: ${esc(AUTHOR.full)}, ${esc(AUTHOR.course)}, гр. ${esc(AUTHOR.group)}</p>
      </div>
    </details>`).join('');
  document.querySelectorAll('#lessons button[data-lesson]').forEach(b => b.addEventListener('click', () => {
    const lesson = data.lessons.find(l => l.id === +b.dataset.lesson);
    const stage = lesson.stages[+b.dataset.stage];
    openLab({ config: stage.lab.config, trials: stage.lab.trials, run: stage.lab.run !== false, runs: stage.lab.runs || 1 });
  }));
}
