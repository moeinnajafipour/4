/*!-----------------------------------------------------------------------------
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * monaco-languages version: 0.9.0(e162b4ba29044167bc7181c42b3270fa8a467424)
 * Released under the MIT license
 * https://github.com/Microsoft/monaco-languages/blob/master/LICENSE.md
 *-----------------------------------------------------------------------------*/
define('vs/basic-languages/src/lua', ['require', 'exports'], function(e, n) {
  'use strict';
  Object.defineProperty(n, '__esModule', { value: !0 }),
    (n.conf = {
      comments: { lineComment: '--', blockComment: ['--[[', ']]'] },
      brackets: [['{', '}'], ['[', ']'], ['(', ')']],
      autoClosingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: "'", close: "'" },
      ],
      surroundingPairs: [
        { open: '{', close: '}' },
        { open: '[', close: ']' },
        { open: '(', close: ')' },
        { open: '"', close: '"' },
        { open: "'", close: "'" },
      ],
    }),
    (n.language = {
      defaultToken: '',
      tokenPostfix: '.lua',
      keywords: [
        'and',
        'break',
        'do',
        'else',
        'elseif',
        'end',
        'false',
        'for',
        'function',
        'goto',
        'if',
        'in',
        'local',
        'nil',
        'not',
        'or',
        'repeat',
        'return',
        'then',
        'true',
        'until',
        'while',
      ],
      brackets: [
        { token: 'delimiter.bracket', open: '{', close: '}' },
        { token: 'delimiter.array', open: '[', close: ']' },
        { token: 'delimiter.parenthesis', open: '(', close: ')' },
      ],
      operators: [
        '+',
        '-',
        '*',
        '/',
        '%',
        '^',
        '#',
        '==',
        '~=',
        '<=',
        '>=',
        '<',
        '>',
        '=',
        ';',
        ':',
        ',',
        '.',
        '..',
        '...',
      ],
      symbols: /[=><!~?:&|+\-*\/\^%]+/,
      escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
      tokenizer: {
        root: [
          [
            /[a-zA-Z_]\w*/,
            { cases: { '@keywords': { token: 'keyword.$0' }, '@default': 'identifier' } },
          ],
          { include: '@whitespace' },
          [/(,)(\s*)([a-zA-Z_]\w*)(\s*)(:)(?!:)/, ['delimiter', '', 'key', '', 'delimiter']],
          [/({)(\s*)([a-zA-Z_]\w*)(\s*)(:)(?!:)/, ['@brackets', '', 'key', '', 'delimiter']],
          [/[{}()\[\]]/, '@brackets'],
          [/@symbols/, { cases: { '@operators': 'delimiter', '@default': '' } }],
          [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
          [/0[xX][0-9a-fA-F_]*[0-9a-fA-F]/, 'number.hex'],
          [/\d+?/, 'number'],
          [/[;,.]/, 'delimiter'],
          [/"([^"\\]|\\.)*$/, 'string.invalid'],
          [/'([^'\\]|\\.)*$/, 'string.invalid'],
          [/"/, 'string', '@string."'],
          [/'/, 'string', "@string.'"],
        ],
        whitespace: [
          [/[ \t\r\n]+/, ''],
          [/--\[([=]*)\[/, 'comment', '@comment.$1'],
          [/--.*$/, 'comment'],
        ],
        comment: [
          [/[^\]]+/, 'comment'],
          [
            /\]([=]*)\]/,
            { cases: { '$1==$S2': { token: 'comment', next: '@pop' }, '@default': 'comment' } },
          ],
          [/./, 'comment'],
        ],
        string: [
          [/[^\\"']+/, 'string'],
          [/@escapes/, 'string.escape'],
          [/\\./, 'string.escape.invalid'],
          [
            /["']/,
            { cases: { '$#==$S2': { token: 'string', next: '@pop' }, '@default': 'string' } },
          ],
        ],
      },
    });
});
