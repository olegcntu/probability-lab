/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
// Форматування чисел українською та відображення шкільних формул.

const nf = new Intl.NumberFormat('uk-UA');

/** Ціле число з розділенням розрядів: 10 000. */
export function int(n) {
  return nf.format(n);
}

/** Десятковий дріб з комою, без зайвих нулів: 0,375; 0,1667. */
export function dec(x, digits = 4) {
  if (!isFinite(x)) return '–';
  let s = (Math.round(x * 10 ** digits) / 10 ** digits).toFixed(digits);
  s = s.replace(/0+$/, '').replace(/\.$/, '');
  return s.replace('.', ',');
}

export function plural(n, one, few, many) {
  const n100 = Math.abs(n) % 100, n10 = n100 % 10;
  if (n100 >= 11 && n100 <= 14) return many;
  if (n10 === 1) return one;
  if (n10 >= 2 && n10 <= 4) return few;
  return many;
}

export function esc(s) {
  return String(s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

/**
 * Перетворює розмітку формул (\frac{a}{b}, x^{2}, C_{n}^{k}, \bar{A}, \cdot) на HTML.
 */
export function tex(src) {
  if (src == null) return '';
  let i = 0;
  const s = String(src);

  function group() {
    if (s[i] !== '{') {
      const ch = s[i] ?? '';
      i++;
      return esc(ch);
    }
    i++;
    const out = seq('}');
    i++;
    return out;
  }

  function seq(stop) {
    let out = '';
    while (i < s.length && s[i] !== stop) {
      const c = s[i];
      if (c === '\\') {
        const m = /^\\([a-zA-Z]+)/.exec(s.slice(i));
        const name = m ? m[1] : '';
        i += m ? m[0].length : 1;
        if (name === 'frac') {
          const a = group();
          const b = group();
          out += `<span class="frac"><span>${a}</span><span>${b}</span></span>`;
        } else if (name === 'bar') {
          out += `<span class="ovl">${group()}</span>`;
        } else if (name === 'cdot') {
          out += ' · ';
          while (s[i] === ' ') i++;
        } else if (name === 'approx') {
          out += '≈';
        } else if (name === 'left' || name === 'right') {
          // розмір дужок не змінюємо – далі йде сама дужка
        } else {
          out += esc(name);
        }
      } else if (c === '^' || c === '_') {
        i++;
        const first = group();
        const other = c === '^' ? '_' : '^';
        if (s[i] === other) {
          i++;
          const second = group();
          const sup = c === '^' ? first : second;
          const sub = c === '_' ? first : second;
          out += `<span class="ss"><span>${sup}</span><span>${sub}</span></span>`;
        } else {
          out += c === '^' ? `<sup>${first}</sup>` : `<sub>${first}</sub>`;
        }
      } else if (c === '{') {
        out += group();
      } else if (c === '-' && s[i - 1] === ' ') {
        out += '−';
        i++;
      } else {
        out += esc(c);
        i++;
      }
    }
    return out;
  }

  return seq(undefined);
}

/** Текст з формулами у $…$ – для кроків розв'язання. */
export function rich(text) {
  if (text == null) return '';
  return String(text).split(/(\$[^$]*\$)/g).map(part =>
    part.startsWith('$') && part.endsWith('$') && part.length > 1
      ? `<span class="formula-inline">${tex(part.slice(1, -1))}</span>`
      : esc(part)
  ).join('');
}

/** Дріб "3/8" → HTML зі стовпчиком. */
export function fracHtml(text) {
  if (!text) return '';
  const [a, b] = String(text).split('/');
  return b ? `<span class="frac"><span>${esc(a)}</span><span>${esc(b)}</span></span>` : esc(a);
}
