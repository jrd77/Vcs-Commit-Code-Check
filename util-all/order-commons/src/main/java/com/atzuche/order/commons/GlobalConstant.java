package com.atzuche.order.commons;

import java.time.format.DateTimeFormatter;

public class GlobalConstant {
    //会员权益-默认的guide_price临界值
    public static final int GUIDE_PRICE = 1500000;
    //会员权益-车辆押金-最大减免比例
    public static final int REDUCTION_RATE_MAX = 70;
    //会员权益-内部员工-车辆押金
    public static final int MEMBER_RIGHT_STAFF_CAR_DEPOSIT = 300;
    //会员权益-内部员工-违章押金
    public static final int MEMBER_RIGHT_STAFF_WZ_DEPOSIT = 1;

    public static final int GET_RETURN_OVER_COST = 50;

    public static final DateTimeFormatter DATE_TIME_FORMAT_2 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static final String FORMAT_STR = "yyyyMMddHHmmss";

    public static final String FORMAT_DATE_STR = "yyyyMMdd";

    public static final String CAR_YEAR_NEQTWO = "car_year_neqtwo";
    public static final double CAR_YEAR_NEQTWO_DEFAULT_VALUE = 1.4;
    public static final String CAR_YEAR_LTTWO = "car_year_lttwo";
    public static final double CAR_YEAR_LTTWO_DEFAULT_VALUE = 1D;
    public static final double NEW_CAR_COEFFICIENT_DEFAULT_VALUE=1D;

    public static final String SPECIAL_CITY_CODE = "special_city_code";
    public static final String SPECIAL_ILLEGAL_DEPOSIT_AMT_CODE = "special_illegal_deposit_amt";

    public static final String GET_RETURN_FINE_AMT = "get_return_fine_amt";
    public static final String GET_FINE_AMT = "get_fine_amt";
    public static final String RETURN_FINE_AMT = "return_fine_amt";

}
