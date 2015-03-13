package com.tfl.billing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
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
    private TravelTracker travelTracker;

    @Before
    public void setUp()
    {
        travelTracker = new TravelTracker();

        // PowerMockito.mockStatic(BillingConstants.class);
        // PowerMockito.when(BillingConstants.getNumberOfMilisecondsInSecond()).thenReturn(10);
        // PowerMockito.when(BillingConstants.getNumberOfSecondsInMinute()).thenReturn(6);
    }

    @Test
    public void testOffPeakShortJourneyEdgeCaseBefore5()
    {
        Journey journey = getJourneyMock("2015.03.12 16:45:00", "2015.03.12 16:59:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyInMorning()
    {
        Journey journey = getJourneyMock("2015.03.12 05:00:00", "2015.03.12 05:24:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyInMorningEdgeCase()
    {
        Journey journey = getJourneyMock("2015.03.12 05:45:00", "2015.03.12 05:59:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakShortJourneyAfter10()
    {
        Journey journey = getJourneyMock("2015.03.12 10:23:00", "2015.03.12 10:32:00");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_SHORT_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyInMorning()
    {
        Journey journey = getJourneyMock("2015.03.12 05:00:00", "2015.03.12 05:59:00");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyInAfternoon()
    {
        Journey journey = getJourneyMock("2015.03.12 12:00:00", "2015.03.12 13:19:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testOffPeakLongJourneyFor25MinutesInEvening()
    {
        Journey journey = getJourneyMock("2015.03.12 20:00:00", "2015.03.12 20:25:01");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.OFF_PEAK_LONG_CHARGE);
    }

    @Test
    public void testPeakShortJourneyChargeInMorning()
    {
        Journey journey = getJourneyMock("2015.03.12 08:00:00", "2015.03.12 08:24:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testPeakShortJourneyChargeInEvening()
    {
        Journey journey = getJourneyMock("2015.03.12 17:00:00", "2015.03.12 17:14:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testPeakLongJourneyChargeInMorning()
    {
        Journey journey = getJourneyMock("2015.03.12 08:00:00", "2015.03.12 09:14:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testPeakLongJourneyChargeInEvening()
    {
        Journey journey = getJourneyMock("2015.03.12 06:00:00", "2015.03.12 06:35:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInMorningPeakAndOutInOffPeakShortJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 09:55:00", "2015.03.12 10:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInEveningPeakAndOutInOffPeakShortJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 19:55:00", "2015.03.12 20:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInMorningPeakAndOutInOffPeakLongJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 09:55:00", "2015.03.12 10:40:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInEveningPeakAndOutInOffPeakLongJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 19:55:00", "2015.03.12 20:30:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInMorningOffPeakAndOutInPeakShortJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 05:55:00", "2015.03.12 06:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInEveningOffPeakAndOutInPeakShortJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 16:54:00", "2015.03.12 17:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_SHORT_CHARGE);
    }

    @Test
    public void testTouchingInMorningOffPeakAndOutInPeakLongJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 05:55:00", "2015.03.12 07:10:59");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    @Test
    public void testTouchingInEveningOffPeakAndOutInPeakLongJourney()
    {
        Journey journey = getJourneyMock("2015.03.12 16:54:00", "2015.03.12 17:45:49");
        assertEquals(travelTracker.getPriceForJourney(journey).getCharge(), BillingConstants.PEAK_LONG_CHARGE);
    }

    private Journey getJourneyMock(String start, String end)
    {
        Journey journey = mock(Journey.class);
        when(journey.startTime()).thenReturn(getDate(start));
        when(journey.endTime()).thenReturn(getDate(end));
        return journey;
    }

    @Test
    public void testOffPeakCapForShortJourneys()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 11:54:00", "2015.03.12 11:59:00"), getJourneyMock("2015.03.12 12:04:00", "2015.03.12 12:14:00"),
                        getJourneyMock("2015.03.12 13:10:00", "2015.03.12 13:20:00"), getJourneyMock("2015.03.12 13:25:00", "2015.03.12 13:34:00"),
                        getJourneyMock("2015.03.12 21:54:00", "2015.03.12 22:00:00"), getJourneyMock("2015.03.12 22:54:00", "2015.03.12 23:00:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.OFF_PEAK_CAP) == 0);
    }

    @Test
    public void testOffPeakCapForLongJourneys()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 11:54:00", "2015.03.12 12:59:00"), getJourneyMock("2015.03.12 12:04:00", "2015.03.12 12:34:00"),
                        getJourneyMock("2015.03.12 13:10:00", "2015.03.12 13:51:00"), getJourneyMock("2015.03.12 13:25:00", "2015.03.12 13:59:00"),
                        getJourneyMock("2015.03.12 21:54:00", "2015.03.12 22:30:00"), getJourneyMock("2015.03.12 22:54:00", "2015.03.12 23:40:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.OFF_PEAK_CAP) == 0);
    }

    @Test
    public void testPeakCapForShortJourneys()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 06:12:00", "2015.03.12 06:30:00"), getJourneyMock("2015.03.12 07:04:00", "2015.03.12 07:14:00"),
                        getJourneyMock("2015.03.12 09:10:00", "2015.03.12 09:20:00"), getJourneyMock("2015.03.12 17:25:00", "2015.03.12 17:34:00"),
                        getJourneyMock("2015.03.12 18:54:00", "2015.03.12 19:00:00"), getJourneyMock("2015.03.12 17:54:00", "2015.03.12 18:00:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.PEAK_CAP) == 0);
    }

    @Test
    public void testPeakCapForLongJourneys()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 06:12:00", "2015.03.12 06:50:00"), getJourneyMock("2015.03.12 07:04:00", "2015.03.12 07:44:00"),
                        getJourneyMock("2015.03.12 09:10:00", "2015.03.12 09:20:00"), getJourneyMock("2015.03.12 17:25:00", "2015.03.12 17:54:00"),
                        getJourneyMock("2015.03.12 18:54:00", "2015.03.12 19:40:00"), getJourneyMock("2015.03.12 17:54:00", "2015.03.12 18:50:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.PEAK_CAP) == 0);
    }

    @Test
    public void testWithOnePeakAndRestOffJourneyForPeakCap()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 11:54:00", "2015.03.12 11:59:00"), getJourneyMock("2015.03.12 12:04:00", "2015.03.12 12:14:00"),
                        getJourneyMock("2015.03.12 13:10:00", "2015.03.12 13:20:00"), getJourneyMock("2015.03.12 13:25:00", "2015.03.12 13:34:00"),
                        getJourneyMock("2015.03.12 06:54:00", "2015.03.12 07:00:00"), getJourneyMock("2015.03.12 22:54:00", "2015.03.12 23:00:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.PEAK_CAP) == 0);
    }

    @Test
    public void testWithMultiplePeakJourneysForPeakCap()
    {
        List<Journey> journeys = Arrays
                .asList(getJourneyMock("2015.03.12 06:12:00", "2015.03.12 06:50:00"), getJourneyMock("2015.03.12 07:04:00", "2015.03.12 07:44:00"),
                        getJourneyMock("2015.03.12 09:10:00", "2015.03.12 09:20:00"), getJourneyMock("2015.03.12 13:25:00", "2015.03.12 13:34:00"),
                        getJourneyMock("2015.03.12 21:54:00", "2015.03.12 22:00:00"), getJourneyMock("2015.03.12 22:54:00", "2015.03.12 23:00:00"));
        assertTrue(travelTracker.getTotalChargeForJourneys(journeys).compareTo(BillingConstants.PEAK_CAP) == 0);
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
