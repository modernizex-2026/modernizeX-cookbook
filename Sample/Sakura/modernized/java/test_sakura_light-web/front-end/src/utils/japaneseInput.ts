/** Shared character filter for "Japanese text only" fields — hiragana,
 * katakana, kanji, CJK punctuation (、。「」etc. — U+3000-303F, the block
 * right before hiragana), and full-width digits (０-９). Half-width digits
 * (0-9) are rejected along with Latin letters — this is a 全角-only field,
 * so a half-width digit is as out-of-place as an ASCII letter. Used by
 * Input.vue's `japaneseOnly` prop and by raw `<textarea>`-based fields
 * (comment / incident-description blocks) that can't use that component
 * directly. */
const JAPANESE_DISALLOWED =
  /[^　-ヿ一-鿿０-９]/g;

export function filterJapaneseOnly(value: string): string {
  return value.replace(JAPANESE_DISALLOWED, '');
}

/** Complementary filter for fields that must NOT contain Japanese text (e.g.
 * TEL numbers) — strips hiragana/katakana/kanji only, leaving digits, Latin
 * letters, and symbols (-, (, ), etc.) untouched. Used by Input.vue's
 * `noJapanese` prop. */
const JAPANESE_CHARS = /[぀-ゟ゠-ヿ一-鿿]/g;

export function filterNoJapanese(value: string): string {
  return value.replace(JAPANESE_CHARS, '');
}
