/*
 * 极简 Markdown → HTML。
 *
 * 后端 /agent/travel/plan 返回的是大模型写的行程方案（标题、列表、表格、加粗），
 * 页面得把它渲染出来，而 frontend 里没装 marked / markdown-it。
 * 与其为几种固定语法引一个包，不如写一个小渲染器：
 *
 * 关键是「先转义、再套标签」——所有文本都过一遍 escapeHtml 才拼进 HTML 串，
 * 所以模型返回什么都不可能被当成标签执行（即使调用方直接 v-html）。
 *
 * 支持：标题、有序/无序列表（含一级缩进）、表格、引用、围栏代码块、分隔线、
 *      **加粗**、*斜体*、`行内代码`、~~删除线~~、[链接](https://…)
 */

const ESCAPE_MAP = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }

function escapeHtml(text) {
  return String(text).replace(/[&<>"']/g, (c) => ESCAPE_MAP[c])
}

/*
 * 行内代码先抠成占位符，否则里面的 * _ 会被当成强调符号。
 * 占位符得能一眼看出不是正文，又不能和正文撞上——用 @@MDCODE0@@ 这种
 * 带序号的自定义标记，模型基本不可能原样写出来。
 */
const CODE_PLACEHOLDER = /@@MDCODE(\d+)@@/g

/** 行内语法。入参是原始文本，转义在内部做。 */
function inline(text) {
  const codes = []
  let out = escapeHtml(text)

  out = out.replace(/(`+)([^`]+?)\1/g, (_, __, code) => {
    codes.push(code)
    return `@@MDCODE${codes.length - 1}@@`
  })

  out = out
    .replace(/\*\*([^*]+?)\*\*/g, '<strong>$1</strong>')
    .replace(/~~([^~]+?)~~/g, '<del>$1</del>')
    // 强调前面拦一个非字母数字字符，避免 a*b*c 这类算式被误伤
    .replace(/(^|[^*\w])\*([^*\n]+?)\*(?!\*)/g, '$1<em>$2</em>')
    .replace(
      /\[([^\]]+?)\]\((https?:\/\/[^\s)]+)\)/g,
      '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>',
    )

  return out.replace(CODE_PLACEHOLDER, (_, i) => `<code class="md-code">${codes[+i]}</code>`)
}

const FENCE_RE = /^\s*```\s*([\w-]*)\s*$/
const HEADING_RE = /^(#{1,6})\s+(.*)$/
const LIST_ITEM_RE = /^(\s*)([-*+]|\d+[.)])\s+(.*)$/
const HR_RE = /^\s*(?:-{3,}|\*{3,}|_{3,})\s*$/
const QUOTE_RE = /^\s*>\s?(.*)$/
const TABLE_ROW_RE = /^\s*\|(.+)\|\s*$/
const TABLE_SEP_RE = /^\s*\|[\s:|-]+\|\s*$/

function isTableSep(line) {
  return Boolean(line) && TABLE_SEP_RE.test(line) && line.includes('-')
}

function splitRow(line) {
  return line
    .trim()
    .replace(/^\||\|$/g, '')
    .split('|')
    .map((cell) => cell.trim())
}

/** 该行是否开启一个块级结构（段落收集时用来判断何时收尾） */
function isBlockStart(line) {
  return (
    FENCE_RE.test(line) ||
    HEADING_RE.test(line) ||
    HR_RE.test(line) ||
    QUOTE_RE.test(line) ||
    LIST_ITEM_RE.test(line) ||
    TABLE_ROW_RE.test(line)
  )
}

/*
 * 连续若干个列表项 → 一个列表。
 * 缩进只分「同级 + 二级」两档：缩进 ≥2 空格的项挂 .md-li--sub 往里收。
 * 不建真正的嵌套树——模型输出的缩进经常不规整，扁平化 + 缩进样式更不容易崩。
 */
function renderList(lines, start) {
  const items = []
  let i = start

  while (i < lines.length) {
    const match = LIST_ITEM_RE.exec(lines[i])
    if (match) {
      items.push({
        indent: match[1].replace(/\t/g, '  ').length,
        ordered: /^\d/.test(match[2]),
        text: match[3],
      })
      i++
      continue
    }
    // 列表项的续行（缩进但没有项目符号）并回上一项
    if (items.length && /^\s+\S/.test(lines[i]) && !isBlockStart(lines[i])) {
      items[items.length - 1].text += ' ' + lines[i].trim()
      i++
      continue
    }
    break
  }

  let html = ''
  let open = ''
  for (const item of items) {
    const tag = item.ordered ? 'ol' : 'ul'
    if (tag !== open) {
      if (open) html += `</${open}>`
      html += `<${tag} class="md-list">`
      open = tag
    }
    html += `<li class="md-li${item.indent >= 2 ? ' md-li--sub' : ''}">${inline(item.text)}</li>`
  }
  if (open) html += `</${open}>`

  return { html, next: i }
}

/**
 * 把 Markdown 源码渲染成 HTML 字符串。
 * 调用方直接 v-html 即可——产出只含 md- 前缀的类名与安全转义过的文本。
 */
export function renderMarkdown(source) {
  const lines = String(source ?? '')
    .replace(/\r\n?/g, '\n')
    .split('\n')
  const html = []
  let i = 0

  while (i < lines.length) {
    const line = lines[i]

    if (!line.trim()) {
      i++
      continue
    }

    const fence = FENCE_RE.exec(line)
    if (fence) {
      const lang = fence[1]
      const body = []
      i++
      while (i < lines.length && !/^\s*```/.test(lines[i])) {
        body.push(lines[i])
        i++
      }
      i++ // 跳过收尾的 ```
      const cls = lang ? `md-pre__code md-pre__code--${escapeHtml(lang)}` : 'md-pre__code'
      html.push(`<pre class="md-pre"><code class="${cls}">${escapeHtml(body.join('\n'))}</code></pre>`)
      continue
    }

    const heading = HEADING_RE.exec(line)
    if (heading) {
      const level = heading[1].length
      html.push(`<h${level} class="md-h md-h${level}">${inline(heading[2])}</h${level}>`)
      i++
      continue
    }

    if (HR_RE.test(line)) {
      html.push('<hr class="md-hr"/>')
      i++
      continue
    }

    if (isTableSep(lines[i + 1])) {
      const head = splitRow(line)
      i += 2
      const bodyRows = []
      while (i < lines.length && TABLE_ROW_RE.test(lines[i])) {
        bodyRows.push(splitRow(lines[i]))
        i++
      }
      const headHtml = head.map((cell) => `<th>${inline(cell)}</th>`).join('')
      // 按表头列数补齐/截断，模型偶尔少写一个 | 时不至于整行错位
      const bodyHtml = bodyRows
        .map((cells) => {
          const tds = head.map((_, k) => `<td>${inline(cells[k] ?? '')}</td>`).join('')
          return `<tr>${tds}</tr>`
        })
        .join('')
      html.push(
        `<div class="md-table-wrap"><table class="md-table"><thead><tr>${headHtml}</tr></thead><tbody>${bodyHtml}</tbody></table></div>`,
      )
      continue
    }

    if (QUOTE_RE.test(line)) {
      const body = []
      while (i < lines.length && QUOTE_RE.test(lines[i])) {
        body.push(QUOTE_RE.exec(lines[i])[1])
        i++
      }
      html.push(`<blockquote class="md-quote">${inline(body.join(' '))}</blockquote>`)
      continue
    }

    if (LIST_ITEM_RE.test(line)) {
      const list = renderList(lines, i)
      html.push(list.html)
      i = list.next
      continue
    }

    // 段落：连着吃到空行或下一个块级起点为止；段内单换行按 <br/> 处理
    const para = []
    while (i < lines.length && lines[i].trim() && !(para.length && isBlockStart(lines[i]))) {
      para.push(lines[i])
      i++
    }
    html.push(`<p class="md-p">${para.map(inline).join('<br/>')}</p>`)
  }

  return html.join('\n')
}

export default { renderMarkdown }
