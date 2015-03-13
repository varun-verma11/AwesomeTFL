package com.tfl.billing;

import java.math.BigDecimal;

/**
 * This class is used for constants used in the TFL Billing.
 * 
 * @author varun
 *
 */
public class BillingConstants
{
    // Time constants
    private static final int NUMBER_OF_MILISECONDS_IN_SECOND = 1000;
    private static final int NUMBER_OF_SECONDS_IN_MINUTES = 60;

    // Charge for journeys
    public static final BigDecimal OFF_PEAK_SHORT_CHARGE = new BigDecimal(1.60);
    public static final BigDecimal OFF_PEAK_LONG_CHARGE = new BigDecimal(2.70);
    public static final BigDecimal PEAK_SHORT_CHARGE = new BigDecimal(2.90);
    public static final BigDecimal PEAK_LONG_CHARGE = new BigDecimal(3.80);
    public static final BigDecimal OFF_PEAK_CAP = new BigDecimal(7);
    public static final BigDecimal PEAK_CAP = new BigDecimal(9);

    public static int getNumberOfMilisecondsInSecond()
    {
        return NUMBER_OF_MILISECONDS_IN_SECOND;
    }

    public static int getNumberOfSecondsInMinute()
    {
        return NUMBER_OF_SECONDS_IN_MINUTES;
    }

}
