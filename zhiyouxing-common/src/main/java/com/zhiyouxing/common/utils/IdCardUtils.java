package com.zhiyouxing.common.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * 实名认证的证件校验。
 *
 * 只做「格式是否成立」这一件事：长度、字符集、出生日期、ISO 7064 校验位。
 * 真伪要到公安接口查，本项目没有那个条件，所以能保证的是「不会把一个手抖打错的号存进库」，
 * 而不是「这个号真实存在」。
 *
 * 前端 frontend/src/common/validate.js 里有一份等价实现。两边都要有：
 * 前端那份是为了即时提示，这份是为了不能绕过的兜底 —— 接口是公开的，
 * 只靠前端校验等于没校验。
 */
public class IdCardUtils {

    /** 18 位：6 位地址码 + 8 位出生日期 + 3 位顺序码 + 1 位校验位（数字或 X） */
    private static final Pattern ID_CARD = Pattern.compile("^\\d{17}[\\dX]$");

    /** 加权因子，与下面的校验码一一对应（ISO 7064:1983 MOD 11-2） */
    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /** 余数 0..10 各自对应的校验位 */
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /** 严格模式：拒绝 2 月 30 日这种「格式对但日子不存在」的日期 */
    private static final DateTimeFormatter BIRTH_FORMAT =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    /**
     * 姓名：2~15 位汉字，或「汉字·汉字」的少数民族姓名，或 2~30 位拉丁字母。
     * 带间隔号的名字不限制每段字数 —— 「2 字起」只对不含间隔号的普通姓名成立。
     */
    private static final Pattern NAME_CN =
            Pattern.compile("^[\\u4e00-\\u9fa5]{2,15}$|^[\\u4e00-\\u9fa5]{1,15}·[\\u4e00-\\u9fa5]{1,15}$");
    private static final Pattern NAME_EN = Pattern.compile("^[A-Za-z][A-Za-z\\s.]{1,29}$");

    private IdCardUtils() {
    }

    /**
     * 归一化：去掉空格与连字符，校验位的小写 x 统一成大写。
     * 用户从证件上抄号码时会带空格，不归一化会把「格式正确」判成错误。
     */
    public static String normalize(String idCard) {
        if (idCard == null) {
            return null;
        }
        return idCard.replaceAll("[\\s-]", "").toUpperCase();
    }

    /** 18 位身份证号是否成立（含校验位与出生日期） */
    public static boolean isValid(String idCard) {
        if (idCard == null || !ID_CARD.matcher(idCard).matches()) {
            return false;
        }
        if (!isValidBirthday(idCard.substring(6, 14))) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < WEIGHTS.length; i++) {
            sum += (idCard.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == idCard.charAt(17);
    }

    /**
     * 出生日期。除了「是不是合法日期」，还卡了 1900 年之后、且不能晚于今天 ——
     * 校验位只保证「号码自洽」，1999 年出生的人写成 2999 也能算出合法校验位，
     * 那种错误只能靠日期区间拦。
     */
    private static boolean isValidBirthday(String yyyymmdd) {
        LocalDate birthday;
        try {
            birthday = LocalDate.parse(yyyymmdd, BIRTH_FORMAT);
        } catch (Exception e) {
            return false;
        }
        return !birthday.isBefore(LocalDate.of(1900, 1, 1)) && !birthday.isAfter(LocalDate.now());
    }

    /** 姓名是否成立（2~15 个汉字，或 2~30 个拉丁字母） */
    public static boolean isValidRealName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return NAME_CN.matcher(trimmed).matches() || NAME_EN.matcher(trimmed).matches();
    }

    /**
     * 脱敏：保留前 6 位（地区码）与后 4 位。
     * 认证接口的返回值一律走这里 —— 认证之后前端再拿不到完整证件号，
     * 订单表里的证件号由服务端从库里取，不经过浏览器。
     */
    public static String mask(String idCard) {
        if (idCard == null || idCard.length() < 11) {
            return null;
        }
        int tail = idCard.length() - 4;
        return idCard.substring(0, 6) + repeat('*', tail - 6) + idCard.substring(tail);
    }

    private static String repeat(char c, int times) {
        StringBuilder sb = new StringBuilder(Math.max(0, times));
        for (int i = 0; i < times; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
