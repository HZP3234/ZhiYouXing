package com.zhiyouxing.common.utils;

import java.util.regex.Pattern;

/**
 * 手机号的格式校验。
 *
 * 前端 frontend/src/common/validate.js 里有一份等价实现（isMobile）。两边都要有：
 * 前端那份是为了即时提示，这份是为了不能绕过的兜底 —— 接口是公开的，
 * 只靠前端校验等于没校验。
 *
 * 这里只管「格式成不成立」。这个号是不是机主本人的，靠支付时的
 * 「与账号绑定号码比对」来判断（见 ConsumptionController.pay）。
 */
public class PhoneUtils {

    /** 11 位：1 开头，第二位 3~9 */
    private static final Pattern MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    private PhoneUtils() {
    }

    /**
     * 归一化：去掉空格与连字符。
     * 从通讯录复制号码时常带上这些字符，不归一化会把「格式正确」判成错误，
     * 也会让「同一个号」在比对时判成不一致。
     */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        return phone.replaceAll("[\\s-]", "");
    }

    /** 是否是一个格式成立的手机号 */
    public static boolean isValidMobile(String phone) {
        return phone != null && MOBILE.matcher(phone).matches();
    }
}
