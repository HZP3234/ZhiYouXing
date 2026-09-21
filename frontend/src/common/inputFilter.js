/**
 * 输入期的字符合法性过滤。
 *
 * 分两层：
 *   1. 纯函数（digitsOnly / idCardOnly）—— 不依赖 Vue 也不依赖 Element Plus，可直接调用；
 *   2. 给 el-input 用的 { formatter, parser } 组合（phoneGuard / idCardGuard）——
 *      模板里写成 v-bind="phoneGuard" 就行，全站二十多个输入框共用一份，不会漏。
 *
 * 这里只做「输入时就把非法字符挡掉」，不做格式校验 —— 手机号是不是 1 开头 11 位、
 * 身份证校验位对不对，由 common/validate.js 在提交时负责。两件事分开：
 * 过滤保证用户打不出意外字符，校验保证打出来的号码真的存在。
 */

/** 手机号位数 */
export const PHONE_LEN = 11
/** 身份证位数（17 位数字 + 1 位校验位） */
export const ID_CARD_LEN = 18

/**
 * 只留数字。
 * @param {*} value 原始输入
 * @param {number} max 截断长度，0 表示不限
 */
export function digitsOnly(value, max = 0) {
  const out = String(value == null ? '' : value).replace(/\D/g, '')
  return max > 0 ? out.slice(0, max) : out
}

/**
 * 身份证：留数字 + 末位 X。
 *
 * 校验位 X 只可能出现在第 18 位，所以做法是「先剔掉所有非数字，再看原串是不是以 X/x 结尾，
 * 是就补回一个大写 X」。这样三种情形都能一次成型：
 *   - 从证件上抄下来带空格/连字符的（1101 0119-9003…X）→ 去空格连字符后位数不乱
 *   - 粘错号、中间混进 X 的 → X 被丢掉，不会把长度顶掉一位
 *   - 末尾小写 x → 自动变大写 X
 * 另外要求「补 X 之前已经有 17 位数字」，否则单敲一个 X 就会留在框里。
 */
export function idCardOnly(value) {
  const raw = String(value == null ? '' : value).replace(/[\s-]/g, '').toUpperCase()
  const body = raw.replace(/\D/g, '')
  const tail = raw.endsWith('X') && body.length >= ID_CARD_LEN - 1 ? 'X' : ''
  return (body.slice(0, ID_CARD_LEN - tail.length) + tail).slice(0, ID_CARD_LEN)
}

/*
 * 为什么用 el-input 的 formatter / parser，而不是在 @input 里手动清洗（或包一层组件）：
 *
 *   1. parser 改的是「emit 出去的那个值」本身 —— element-plus 的 handleInput 里是先跑
 *      formatValue()（内部调用 parser）再 emit update:modelValue 的，所以模型里从来不会
 *      出现字母，不存在「显示已经干净、模型还脏着」的时间窗；而 @input 里回写模型发生在
 *      emit 之后，会闪一下，也更容易写成把中文输入法吃掉的死循环。
 *   2. 输入法合成期间 handleInput 直接 return，等 compositionend 之后再补跑，清洗不会跟
 *      中文输入法打架（老版本 element-plus 没这个保护，2.14.5 有）。
 *   3. 粘贴走的也是 input 事件，parser 一样会跑。
 *   4. 不用包组件：游客端那几个手机号输入框各自带着 size、#prefix 插槽，包一层还得逐个转发，
 *      而 v-bind 只是两个词，对原有属性零影响。
 *
 * 代价：formatter 与 parser 必须成对出现，只给一个 element-plus 会告警。
 *
 * 长度为什么不写在 el-input 的 maxlength 上：浏览器是「先按 maxlength 截断、再进 parser」。
 * 粘「138-0000-0000」（含连字符 13 个字符）会先被截成「138-0000-00」，清洗完只剩 9 位数字 ——
 * 末尾两位凭空消失，而且用户看不出来。长度一律交给过滤函数末尾的 slice。
 */

/** 手机号：最多 11 位数字 */
export const phoneGuard = {
  formatter: (v) => digitsOnly(v, PHONE_LEN),
  parser: (v) => digitsOnly(v, PHONE_LEN),
}

/** 座机位数上限（020-81391234 是 12 位，00853-28827123 是 14 位，留点余量） */
export const TEL_MAX = 20

/**
 * 座机电话：允许数字、连字符，以及区号前那个 +（+86-20-81391234）。
 *
 * 为什么不一律用 phoneGuard：餐厅表的 contact_phone 有 124 行全是座机
 * （020-81391234、00853-28827123 这种）。手机号的「11 位纯数字」口径会把
 * 「020-81391234」截成「0208139123」—— 少一位，而且用户一碰这个框就会把截断值存回去，
 * 等于把真实联系电话改坏。所以「联系电话」这类字段用这个更宽的口径。
 */
export function telOnly(value, max = TEL_MAX) {
  const out = String(value == null ? '' : value)
    .replace(/[^\d+-]/g, '')
    .replace(/-{2,}/g, '-')
  return max > 0 ? out.slice(0, max) : out
}

export const telGuard = {
  formatter: (v) => telOnly(v),
  parser: (v) => telOnly(v),
}

/**
 * 身份证号：最多 18 位，末位可 X。
 * ⚠️ 只给「用户自己录入」的框用。实名认证审核页读的是后端脱敏过的 id_card_masked
 * （形如 110105******1234），挂上这个守卫会把 * 全吃掉、显示成乱码。
 */
export const idCardGuard = {
  formatter: idCardOnly,
  parser: idCardOnly,
}

export default { digitsOnly, idCardOnly, phoneGuard, idCardGuard, telGuard, telOnly, PHONE_LEN, ID_CARD_LEN, TEL_MAX }
