/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Звернення до сервера (Java, Spring Boot).

export class ApiError extends Error {}

export async function api(path, body) {
  const options = body === undefined
    ? { headers: { Accept: 'application/json' } }
    : { method: 'POST', headers: { 'Content-Type': 'application/json', Accept: 'application/json' }, body: JSON.stringify(body) };
  let res;
  try {
    res = await fetch(path, options);
  } catch (e) {
    throw new ApiError('Сервер не відповідає. Перевірте, чи запущено програму, і оновіть сторінку.');
  }
  let data = null;
  try {
    data = await res.json();
  } catch (e) {
    data = null;
  }
  if (!res.ok) {
    throw new ApiError((data && data.error) || `Помилка сервера (${res.status}).`);
  }
  return data;
}

export async function getJson(path) {
  return api(path);
}
