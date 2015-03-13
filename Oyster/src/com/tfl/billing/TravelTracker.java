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
        List<Journey> journeys = getRunningCostForCustomer(customer.cardId());

        PaymentsSystem.getInstance().charge(customer, journeys, getTotalChargeForJourneys(journeys));
    }

    public List<Journey> getRunningCostForCustomer(UUID cardID)
    {
        List<JourneyEvent> customerJourneyEvents = new ArrayList<JourneyEvent>();
        for (JourneyEvent journeyEvent : eventLog)
        {
            if (journeyEvent.cardId().equals(cardID))
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
        return journeys;
    }

    public BigDecimal getTotalChargeForJourneys(List<Journey> journeys)
    {
        BigDecimal customerTotal = new BigDecimal(0);
        boolean isAnyPeak = false;
        for (Journey journey : journeys)
        {
            JourneyCharge priceForJourney = getPriceForJourney(journey);
            customerTotal = customerTotal.add(priceForJourney.charge);
            isAnyPeak = isAnyPeak || priceForJourney.isPeak();
        }
        if (isAnyPeak && customerTotal.compareTo(BillingConstants.PEAK_CAP) == 1)
        {
            customerTotal = BillingConstants.PEAK_CAP;
        } else if (!isAnyPeak && customerTotal.compareTo(BillingConstants.OFF_PEAK_CAP) == 1)
        {
            customerTotal = BillingConstants.OFF_PEAK_CAP;
        }
        return roundToNearestPenny(customerTotal);
    }

    public JourneyCharge getPriceForJourney(Journey journey)
    {
        boolean isPeak = peak(journey);
        boolean isShort = isShort(journey);
        if (isPeak && isShort)
        {
            return new JourneyCharge(BillingConstants.PEAK_SHORT_CHARGE, true);
        } else if (!isPeak && isShort)
        {
            return new JourneyCharge(BillingConstants.OFF_PEAK_SHORT_CHARGE, false);
        } else if (isPeak)
        {
            return new JourneyCharge(BillingConstants.PEAK_LONG_CHARGE, true);
        } else
        {
            return new JourneyCharge(BillingConstants.OFF_PEAK_LONG_CHARGE, false);
        }
    }

    public class JourneyCharge
    {
        private BigDecimal charge;
        private boolean isPeak;

        JourneyCharge(BigDecimal charge, boolean isPeak)
        {
            this.isPeak = isPeak;
            this.charge = charge;
        }

        public BigDecimal getCharge()
        {
            return charge;
        }

        public boolean isPeak()
        {
            return isPeak;
        }
    }

    private boolean isShort(Journey journey)
    {
        return (journey.endTime().getTime() - journey.startTime().getTime()) / (BillingConstants.getNumberOfMilisecondsInSecond() * BillingConstants
                .getNumberOfSecondsInMinute()) < 25;
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
