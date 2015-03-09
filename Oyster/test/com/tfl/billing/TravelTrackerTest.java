package com.tfl.billing;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BillingConstants.class)
public class TravelTrackerTest extends TestCase
{
    OysterCard myCard;
    OysterCardReader paddingtonReader;
    OysterCardReader bakerStreetReader;
    OysterCardReader kingsCrossReader;
    TravelTracker travelTracker;

    @Override
    public void setUp()
    {
        myCard = new OysterCard("38400000-8cf0-11bd-b23e-10b96e4ef00d");
        paddingtonReader = OysterReaderLocator.atStation(Station.PADDINGTON);
        bakerStreetReader = OysterReaderLocator.atStation(Station.BAKER_STREET);
        kingsCrossReader = OysterReaderLocator.atStation(Station.KINGS_CROSS);

        travelTracker = new TravelTracker();
        travelTracker.connect(paddingtonReader, bakerStreetReader, kingsCrossReader);

        // Using PowerMockito to set up the static methods
        PowerMockito.mockStatic(BillingConstants.class);
        PowerMockito.when(BillingConstants.getNumberOfMilisecondsInSecond()).thenReturn(10);
        PowerMockito.when(BillingConstants.getNumberOfSecondsInMinute()).thenReturn(6);
    }

    @Test
    public void testOffPeakJourney() throws InterruptedException
    {
        paddingtonReader.touch(myCard);
        minutesPass(25);
        bakerStreetReader.touch(myCard);
        minutesPass(15);
        bakerStreetReader.touch(myCard);
        minutesPass(10);
        kingsCrossReader.touch(myCard);
        travelTracker.chargeAccounts();
    }

    private void minutesPass(int n) throws InterruptedException
    {
        Thread.sleep(n * BillingConstants.getNumberOfSecondsInMinute() * BillingConstants.getNumberOfMilisecondsInSecond());
    }
}
