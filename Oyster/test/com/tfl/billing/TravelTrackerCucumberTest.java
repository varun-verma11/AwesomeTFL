package com.tfl.billing;

import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;

import com.oyster.OysterCard;
import com.oyster.OysterCardReader;
import com.tfl.underground.OysterReaderLocator;
import com.tfl.underground.Station;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class TravelTrackerCucumberTest
{
    OysterCard myCard;
    TravelTracker travelTracker;

    @Given("^oyster card is \"([^\"]*)\"$")
    public void setOysterCardForTest(String cardID)
    {
        myCard = new OysterCard(cardID);
        travelTracker = new TravelTracker();
    }

    @Given("^journey starts at (\\d+):(\\d+)$")
    public void journey_starts_at(int hour, int minutes) throws InterruptedException
    {
        System.out.println("Journey starts at " + hour + ":" + minutes);
        waitForTime(hour, minutes);
    }

    @When("card touched at\\s+(.+)")
    public void touchCard(String stationName)
    {
        OysterCardReader stationReader = OysterReaderLocator.atStation(Station.valueOf(stationName.toUpperCase()));
        travelTracker.connect(stationReader);
        stationReader.touch(myCard);
    }

    @When("(\\d+) minutes passed")
    public void timePassed(int minutes) throws InterruptedException
    {
        System.out.println("Waiting for " + minutes + " minutes.");
        sleepForMinutes(5);
    }

    @Then("the running total is\\s+(\\d+(?:\\.\\d+)?)")
    public void checkRunningTotal(BigDecimal expectedTotal)
    {
        assertEquals("Running total", roundToNearestPenny(expectedTotal), travelTracker.getRunningCostForCustomer(myCard.id()));
    }

    private static void sleepForMinutes(int n) throws InterruptedException
    {
        Thread.sleep(n * BillingConstants.getNumberOfSecondsInMinute() * BillingConstants.getNumberOfMilisecondsInSecond());
    }

    private static void waitForTime(int hour, int minutes) throws InterruptedException
    {
        int hoursToWait = hour - Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int minutesToWait = Calendar.getInstance().get(Calendar.MINUTE) - minutes;
        int mins = hoursToWait * BillingConstants.getNumberOfSecondsInMinute() - minutesToWait;
        if (mins > 0)
        {
            System.out.println("Waiting " + mins + " minutes");
            Thread.sleep(mins * BillingConstants.getNumberOfSecondsInMinute() * BillingConstants.getNumberOfMilisecondsInSecond());
        } else
        {
            System.out.println("Not waiting. It is probably too late. Rerun the test tomorrow at an earlier time.");
        }
    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence)
    {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

}
