package com.tfl.billing;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oyster.OysterCard;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BillingConstants.class)
public class TravelTrackerTest
{
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    private OysterCard myCard;
    private TravelTracker travelTracker;

    @Before
    public void setUp()
    {
        myCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        travelTracker = new TravelTracker();

        // PowerMockito.mockStatic(BillingConstants.class);
        // PowerMockito.when(BillingConstants.getNumberOfMilisecondsInSecond()).thenReturn(10);
        // PowerMockito.when(BillingConstants.getNumberOfSecondsInMinute()).thenReturn(6);
    }

    @Test
    public void testOffPeakShortJourneyEdgeCaseBefore5()
    {
        Journey journey = getJourney("2015.03.12 16:45:00", "2015.03.12 16:59:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyInMorning()
    {
        Journey journey = getJourney("2015.03.12 05:00:00", "2015.03.12 05:24:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyInMorningEdgeCase()
    {
        Journey journey = getJourney("2015.03.12 05:45:00", "2015.03.12 05:59:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyAfter10()
    {
        Journey journey = getJourney("2015.03.12 10:23:00", "2015.03.12 10:32:00");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyInMorning()
    {
        Journey journey = getJourney("2015.03.12 05:00:00", "2015.03.12 05:59:00");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyInAfternoon()
    {
        Journey journey = getJourney("2015.03.12 12:00:00", "2015.03.12 13:19:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyFor25MinutesInEvening()
    {
        Journey journey = getJourney("2015.03.12 20:00:00", "2015.03.12 20:25:01");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testPeakShortJourneyChargeInMorning()
    {
        Journey journey = getJourney("2015.03.12 08:00:00", "2015.03.12 08:24:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testPeakShortJourneyChargeInEvening()
    {
        Journey journey = getJourney("2015.03.12 17:00:00", "2015.03.12 17:14:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testPeakLongJourneyChargeInMorning()
    {
        Journey journey = getJourney("2015.03.12 08:00:00", "2015.03.12 09:14:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testPeakLongJourneyChargeInEvening()
    {
        Journey journey = getJourney("2015.03.12 06:00:00", "2015.03.12 06:35:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInMorningPeakAndOutInOffPeakShortJourney()
    {
        Journey journey = getJourney("2015.03.12 09:55:00", "2015.03.12 10:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInEveningPeakAndOutInOffPeakShortJourney()
    {
        Journey journey = getJourney("2015.03.12 19:55:00", "2015.03.12 20:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInMorningPeakAndOutInOffPeakLongJourney()
    {
        Journey journey = getJourney("2015.03.12 09:55:00", "2015.03.12 10:40:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInEveningPeakAndOutInOffPeakLongJourney()
    {
        Journey journey = getJourney("2015.03.12 19:55:00", "2015.03.12 20:30:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInMorningOffPeakAndOutInPeakShortJourney()
    {
        Journey journey = getJourney("2015.03.12 05:55:00", "2015.03.12 06:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInEveningOffPeakAndOutInPeakShortJourney()
    {
        Journey journey = getJourney("2015.03.12 16:54:00", "2015.03.12 17:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInMorningOffPeakAndOutInPeakLongJourney()
    {
        Journey journey = getJourney("2015.03.12 05:55:00", "2015.03.12 07:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInEveningOffPeakAndOutInPeakLongJourney()
    {
        String start = "2015.03.12 16:54:00";
        String end = "2015.03.12 17:45:49";
        Journey journey = getJourney(start, end);
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    private Journey getJourney(String start, String end)
    {
        Journey journey = mock(Journey.class);
        when(journey.startTime()).thenReturn(getDate(start));
        when(journey.endTime()).thenReturn(getDate(end));
        return journey;
    }

    public void testOffPeakCapForShortJourneys()
    {

    }

    private static Date getDate(String date)
    {
        try
        {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
