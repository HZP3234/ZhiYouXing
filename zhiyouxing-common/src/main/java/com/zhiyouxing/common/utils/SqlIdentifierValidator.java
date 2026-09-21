package com.zhiyouxing.common.utils;

import com.zhiyouxing.common.entity.EIException;

import java.util.*;

public class SqlIdentifierValidator {

    private static final Set<String> ALLOWED_TABLES = new HashSet<>(Arrays.asList(
        "group_tour", "restaurant_reservation", "chat_message", "config",
        "tour_guide", "hotel_comment", "travel_guide_comment",
        "travel_route_comment", "restaurant_comment", "attraction_comment",
        "forum", "friend", "friend_link", "attraction_type",
        "hotel_info", "hotel_reservation", "room_type",
        "travel_guide", "travel_route", "restaurant",
        "ticket_order", "news", "news_type", "help_request",
        "attraction", "store_up", "token", "users", "user",
        "route_type"
    ));

    private static final String IDENTIFIER_PATTERN = "^[a-zA-Z][a-zA-Z0-9_]*$";

    private static final Map<String, Set<String>> ALLOWED_COLUMNS = new HashMap<>();

    static {
        ALLOWED_COLUMNS.put("users", new HashSet<>(Arrays.asList(
            "id", "username", "password", "image", "role", "add_time"
        )));
        ALLOWED_COLUMNS.put("user", new HashSet<>(Arrays.asList(
            "id", "user_account", "password", "user_name", "avatar",
            "gender", "contact_phone", "status", "password_wrong_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("tour_guide", new HashSet<>(Arrays.asList(
            "id", "guide_no", "password", "guide_name", "avatar",
            "specialty", "language_skill", "contact_phone", "guide_resume",
            "status", "password_wrong_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("token", new HashSet<>(Arrays.asList(
            "id", "user_id", "username", "table_name", "role", "token", "expirated_time", "add_time"
        )));
        ALLOWED_COLUMNS.put("config", new HashSet<>(Arrays.asList(
            "id", "name", "value"
        )));
        ALLOWED_COLUMNS.put("forum", new HashSet<>(Arrays.asList(
            "id", "user_id", "nickname", "content", "add_time"
        )));
        ALLOWED_COLUMNS.put("friend", new HashSet<>(Arrays.asList(
            "id", "user_id", "friend_id", "add_time"
        )));
        ALLOWED_COLUMNS.put("friend_link", new HashSet<>(Arrays.asList(
            "id", "name", "url", "add_time"
        )));
        ALLOWED_COLUMNS.put("attraction_type", new HashSet<>(Arrays.asList(
            "id", "type_name", "add_time"
        )));
        ALLOWED_COLUMNS.put("hotel_info", new HashSet<>(Arrays.asList(
            "id", "hotel_name", "hotel_recommend", "image", "address",
            "hotel_intro", "hotel_location", "hotel_phone", "hotel_lowest_price",
            "add_time"
        )));
        ALLOWED_COLUMNS.put("hotel_reservation", new HashSet<>(Arrays.asList(
            "id", "hotel_name", "hotel_type", "hotel_phone",
            "hotel_address", "hotel_image", "reservation_count", "reservation_amount",
            "remark", "order_user", "hotel_name", "reservation_time", "add_time"
        )));
        ALLOWED_COLUMNS.put("room_type", new HashSet<>(Arrays.asList(
            "id", "room_type", "add_time"
        )));
        ALLOWED_COLUMNS.put("travel_guide", new HashSet<>(Arrays.asList(
            "id", "guide_no", "guide_name", "publisher", "image",
            "address", "guide_intro", "guide_location", "click_num",
            "discuss_num", "store_up_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("travel_route", new HashSet<>(Arrays.asList(
            "id", "route_no", "route_name", "departure_place", "destination",
            "route_price", "image", "route_intro", "click_num",
            "discuss_num", "store_up_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("restaurant", new HashSet<>(Arrays.asList(
            "id", "restaurant_name", "restaurant_address", "restaurant_intro",
            "restaurant_phone", "restaurant_image", "restaurant_reservation", "click_num",
            "discuss_num", "store_up_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("ticket_order", new HashSet<>(Arrays.asList(
            "id", "attraction_name", "attraction_type", "ticket_price",
            "purchase_quantity", "total_amount", "remark", "order_user",
            "purchase_time", "add_time"
        )));
        ALLOWED_COLUMNS.put("news", new HashSet<>(Arrays.asList(
            "id", "title", "detail", "publish_date", "publisher", "add_time"
        )));
        ALLOWED_COLUMNS.put("news_type", new HashSet<>(Arrays.asList(
            "id", "type_name", "add_time"
        )));
        ALLOWED_COLUMNS.put("help_request", new HashSet<>(Arrays.asList(
            "id", "hotel_name", "hotel_type", "hotel_phone",
            "hotel_address", "hotel_image", "publisher", "add_time"
        )));
        ALLOWED_COLUMNS.put("attraction", new HashSet<>(Arrays.asList(
            "id", "attraction_name", "attraction_type", "attraction_image",
            "attraction_address", "attraction_description", "click_num", "discuss_num",
            "store_up_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("store_up", new HashSet<>(Arrays.asList(
            "id", "user_id", "ref_id", "table_name", "name", "add_time"
        )));
        ALLOWED_COLUMNS.put("route_type", new HashSet<>(Arrays.asList(
            "id", "route_type_name", "add_time"
        )));
        ALLOWED_COLUMNS.put("chat_message", new HashSet<>(Arrays.asList(
            "id", "user_id", "admin_id", "ask", "reply", "is_reply", "add_time"
        )));
        ALLOWED_COLUMNS.put("restaurant_reservation", new HashSet<>(Arrays.asList(
            "id", "restaurant_name", "restaurant_address", "restaurant_phone",
            "restaurant_image", "restaurant_reservation", "reserver", "reservation_time",
            "remark", "add_time"
        )));
        ALLOWED_COLUMNS.put("group_tour", new HashSet<>(Arrays.asList(
            "id", "group_tour_name", "group_tour_intro", "departure_place", "destination",
            "departure_time", "return_time", "price", "image", "click_num",
            "discuss_num", "store_up_num", "add_time"
        )));
        ALLOWED_COLUMNS.put("hotel_comment", new HashSet<>(Arrays.asList(
            "id", "ref_id", "user_id", "content", "reply", "add_time"
        )));
        ALLOWED_COLUMNS.put("travel_guide_comment", new HashSet<>(Arrays.asList(
            "id", "ref_id", "user_id", "content", "reply", "add_time"
        )));
        ALLOWED_COLUMNS.put("travel_route_comment", new HashSet<>(Arrays.asList(
            "id", "ref_id", "user_id", "content", "reply", "add_time"
        )));
        ALLOWED_COLUMNS.put("restaurant_comment", new HashSet<>(Arrays.asList(
            "id", "ref_id", "user_id", "content", "reply", "add_time"
        )));
        ALLOWED_COLUMNS.put("attraction_comment", new HashSet<>(Arrays.asList(
            "id", "ref_id", "user_id", "content", "reply", "add_time"
        )));
    }

    private static final Set<String> OPTION_SENSITIVE_COLUMNS = new HashSet<>(Arrays.asList(
        "password", "password", "token", "expirated_time", "password_wrong_num"
    ));

    public static void validateTable(String tableName) {
        if (tableName == null || !tableName.matches(IDENTIFIER_PATTERN)) {
            throw new EIException("非法表名");
        }
        if (!ALLOWED_TABLES.contains(tableName.toLowerCase())) {
            throw new EIException("表名不允许");
        }
    }

    public static void validateColumn(String tableName, String columnName) {
        if (columnName == null || !columnName.matches(IDENTIFIER_PATTERN)) {
            throw new EIException("非法列名");
        }
        String table = tableName.toLowerCase();
        String column = columnName.toLowerCase();
        Set<String> allowed = ALLOWED_COLUMNS.get(table);
        if (allowed == null || !allowed.contains(column)) {
            throw new EIException("列名不允许");
        }
    }

    public static void validateOptionColumn(String tableName, String columnName) {
        validateColumn(tableName, columnName);
        if (OPTION_SENSITIVE_COLUMNS.contains(columnName.toLowerCase())) {
            throw new EIException("敏感字段不允许查询");
        }
    }
}
