package com.tfl.billing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.oyster.OysterCardReader;
import com.oyster.ScanListener;
import com.tfl.external.Customer;
import com.tfl.external.CustomerDatabase;
import com.tfl.external.PaymentsSystem;

public class TravelTracker implements ScanListener
{
    private final List<JourneyEvent> eventLog = new ArrayList<JourneyEvent>();
    private final Set<UUID> currentlyTravelling = new HashSet<UUID>();

    public void chargeAccounts()
    {
        CustomerDatabase customerDatabase = CustomerDatabase.getInstance();

        List<Customer> customers = customerDatabase.getCustomers();
        for (Customer customer : customers)
        {
            totalJourneysFor(customer);
        }
    }

    private void totalJourneysFor(Customer customer)
    {
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        for (JourneyEvent journeyEvent : eventLog)
        {
            if (journeyEvent.cardId().equals(customer.cardId()))
            {
                customerJourneyEvents.add(journeyEvent);
            }
        }

        List<Journey> journeys = new ArrayList<Journey>();
        JourneyEvent start = null;
        for (JourneyEvent event : customerJourneyEvents)
        {
            if (event instanceof JourneyStart)
            {
                start = event;
            }
            if (event instanceof JourneyEnd && start != null)
            {
                journeys.add(new Journey(start, event));
                start = null;
            }
        }

        BigDecimal customerTotal = new BigDecimal(0);
        for (Journey journey : journeys)
        {
            customerTotal = customerTotal.add(getPriceForJourney(journey));
        }

        PaymentsSystem.getInstance().charge(customer, journeys, roundToNearestPenny(customerTotal));
    }

    public BigDecimal getPriceForJourney(Journey journey)
    {
        boolean isPeak = peak(journey);
        boolean isShort = isShort(journey);
        if (isPeak && isShort)
        {
            return BillingConstants.PEAK_SHORT_CHARGE;
        } else if (!isPeak && isShort)
        {
           return BillingConstants.OFF_PEAK_SHORT_CHARGE;
        } else if (isPeak)
        {
           return BillingConstants.PEAK_LONG_CHARGE;
        } else
        {
             return BillingConstants.OFF_PEAK_LONG_CHARGE;
        }
    }

    private boolean isShort(Journey journey)
    {
        return (journey.endTime().getTime() - journey.startTime().getTime())
                / (BillingConstants.getNumberOfMilisecondsInSecond() * BillingConstants.getNumberOfSecondsInMinute()) < 25;
    }

    private BigDecimal roundToNearestPenny(BigDecimal poundsAndPence)
    {
        return poundsAndPence.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private boolean peak(Journey journey)
    {
        return peak(journey.startTime()) || peak(journey.endTime());
    }

    private boolean peak(Date time)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= 6 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

    public void connect(OysterCardReader... cardReaders)
    {
        for (OysterCardReader cardReader : cardReaders)
        {
            cardReader.register(this);
        }
    }

    @Override
    public void cardScanned(UUID cardId, UUID readerId)
    {
        if (currentlyTravelling.contains(cardId))
        {
            eventLog.add(new JourneyEnd(cardId, readerId));
            currentlyTravelling.remove(cardId);
        } else
        {
            if (CustomerDatabase.getInstance().isRegisteredId(cardId))
            {
                currentlyTravelling.add(cardId);
                eventLog.add(new JourneyStart(cardId, readerId));
            } else
            {
                throw new UnknownOysterCardException(cardId);
            }
        }
    }

}
