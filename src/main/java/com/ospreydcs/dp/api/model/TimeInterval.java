/*
 * Project: dp-api-common
 * File:	TimeInterval.java
 * Package: com.ospreydcs.dp.api.model
 * Type: 	TimeInterval
 *
 * @author Christopher K. Allen
 * @since Sep 8, 2022
 *
 * TODO:
 * - None
 */
package com.ospreydcs.dp.api.model;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;

/**
 * <p>
 * Record representing an interval in time.  The time interval has a beginning and
 * and ending which are both represented by the Java <code>Instant</code> class.
 * The record includes multiple constructors capable of instantiating time intervals
 * via different objects which must all somehow represent an exact instant in time.
 * In all cases construction will fail if the beginning instant and ending instant
 * are not properly ordered in time (i.e., the beginning occurs on or before the
 * end).
 * </p>
 * <p>
 * The record includes static methods for creating comparators to order time intervals
 * according to start time, duration, etc.
 * The record also includes utility methods for set theoretic and measure operations
 * (e.g., intersections, duration, etc.)
 * </p>
 *
 * @author Christopher K. Allen
 * @since Sep 8, 2022
 *
 * @see java.time.Instant
 * @see java.time.Duration
 */
public record TimeInterval(Instant begin, Instant end) {

    //
    // Class Resources
    //
    
    /** The empty time interval */
    public static final TimeInterval EMPTY = TimeInterval.from(Instant.EPOCH, Instant.EPOCH);
    
    /** The universe - that is, the compliment of the empty interval */
    public static final TimeInterval UNIVERSE = TimeInterval.from(Instant.MIN, Instant.MAX);
    
    
    //
    // Comparators
    //
    
    /**
     * Returns a <code>Comparator</code> object that compares 
     * <code>TimeInterval</code>s according to their beginning.
     * That is, the comparator will order time intervals according to 
     * their beginning, earliest first - regardless of duration.
     * 
     * @return <code>Comparator</code> of time intervals based upon beginning
     */
    public static Comparator<TimeInterval> comparatorBegin() {
        Comparator<TimeInterval> cmp = (i1, i2) -> {
            return i1.begin.compareTo(i2.begin);
        };
        
        return cmp;
    }
    
    /**
     * Returns a <code>Comparator</code> object that compares two 
     * <code>TimeInterval</code> objects according to the length of 
     * their duration.  That is, the comparator will order time intervals
     * according to size, regardless of location.
     * 
     * @return <code>Comparator</code> of time intervals based upon duration
     */
    public static Comparator<TimeInterval> comparatorDuration() {
        Comparator<TimeInterval> cmp = (i1, i2) -> {
            return i1.getDuration().compareTo(i2.getDuration());
        };
        
        return cmp;
    }
    
    
    // 
    // Set Operations
    //
    
    /**
     * Return the intersection of the two time intervals as a set operation.
     * The empty interval (<code>EMPTY</code>) is returned if there is no intersection. 
     * 
     * @param tvl1    interval set to intersect
     * @param tvl2    interval set to intersect
     * 
     * @return  the time interval representing the intersection of arguments
     */
    public static TimeInterval intersection(TimeInterval tvl1, TimeInterval tvl2) {
        
        // Treat exception cases
        if (tvl1.equals(EMPTY) || tvl2.equals(EMPTY))
            return EMPTY;
        
        Instant left = max(tvl1.begin, tvl2.begin);
        Instant right = min(tvl1.end, tvl2.end);
        
        try {
            return new TimeInterval(left, right);
            
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
    
    /**
     * Returns the union of the two time intervals assuming
     * that they have a common intersection.  Under the assumption, the union of the 
     * two time intervals (as a set) can be represented as a single time interval of
     * measure at least that of either the two.  If there is no common intersection
     * between the intervals then an exception is thrown.
     * 
     * @param tvl1    time interval with which to create a set union
     * @param tvl2    time interval with which to create a set union
     * 
     * @return          union of the given arguments
     * 
     * @throws IllegalArgumentException the arguments contains no intersection 
     */
    public static TimeInterval unionCommon(TimeInterval tvl1, TimeInterval tvl2) throws IllegalArgumentException {
        if (!tvl1.hasIntersectionOpen(tvl2))
            throw new IllegalArgumentException("TimeInterval:unionCommon - intervals must have a finite intersection to use this method");
        
        Instant left = min(tvl1.begin, tvl2.begin);
        Instant right = max(tvl1.end, tvl2.end);
        
        return new TimeInterval(left, right);
    }
    
    /**
     * Returns the smallest time interval containing both given intervals, that is,
     * the support of the two time intervals.
     * 
     * @param tvl1  interval under scrutiny  
     * @param tvl2  interval under scrutiny  
     * 
     * @return  the smallest time interval containing both arguments
     */
    public static TimeInterval support(TimeInterval tvl1, TimeInterval tvl2) {
        // Treat exceptional cases
        if (tvl1.equals(EMPTY))
            if (tvl2.equals(EMPTY))
                return EMPTY;
            else 
                return tvl2;
        else
            if (tvl2.equals(EMPTY))
                return tvl1;
                  
        // Both intervals are non null
        Instant left  = min(tvl1.begin, tvl2.begin);
        Instant right = max(tvl1.end, tvl2.end);
        
        return new TimeInterval(left, right);
    }
    

    //
    // Creators 
    //
    
    /**
     * Create and return a new <code>TimeInterval</code> instance from the
     * given start and end time instants.
     * 
     * @param insBeg    starting instant of the time interval
     * @param insEnd    ending instant of the time interval
     * 
     * @return new time interval of the form [<code>insBeg, insEnd</code>]
     */
    public static TimeInterval  from(Instant insBeg, Instant insEnd) {
        return new TimeInterval(insBeg, insEnd);
    }
    
    /**
     * Create and return a new <code>TimeInterval</code> instance  from the 
     * given instant until now (instant of method call).
     * 
     * @param insStart  starting time (instant) of the returned interval
     * 
     * @return  a time interval starting from the argument and ending now
     */
    public static TimeInterval  untilNow(Instant insStart) {
        return new TimeInterval(insStart, Instant.now());
    }
    
    /**
     * Create and return a new <code>TimeInterval</code> instance from now (instant of method call) 
     * until the given instant.
     * 
     * @param insUntil  ending time (instant) of the returned interval
     * 
     * @return  a time interval starting from the argument and ending now
     */
    public static TimeInterval  fromNow(Instant insUntil) {
        return new TimeInterval(Instant.now(), insUntil);
    }
    

    //
    // Constructors
    //
    
    /**
     * Creates a new instance of <code>TimeInterval</code> checking that the begging
     * is before the end of the interval.
     *
     * @param begin start instant of the time interval
     * @param end   end instant of the time interval
     * 
     * @throws IllegalArgumentException begging of the interval occurs before the end
     */
    public TimeInterval {
        if (end.isBefore(begin))
            throw new IllegalArgumentException("TimeInterval.end must occur after TimeInterval.begin");
    }
    
    /**
     * Creates a new instance of <code>TimeInterval</code> from an object exposing
     * the <code>TemporalAccessor</code> interface and, thus, representing time
     * in some fashion.  Note the construction fails if the arguments do not specify
     * both the date and time to the specific instant.
     *
     * @param tacBeg    temporal object representing the interval beginning
     * @param tacEnd    temporal object representing the interval ending
     * 
     * @throws DateTimeException at least one argument does not fully represent a time instant
     * 
     * @see java.time.temporal.TemporalAccessor
     */
    public TimeInterval(TemporalAccessor tacBeg, TemporalAccessor tacEnd) throws DateTimeException {
        this(Instant.from(tacBeg), Instant.from(tacEnd));
    }
    
    /**
     * Creates a new instance of <code>TimeInterval</code> using the string of 
     * integer arguments to specify the beginning instant and the ending instant.
     * The default time zone is used to finalize both instants in time.
     *
     * @param iYrBeg    start instant year
     * @param iMnBeg    start instant month
     * @param iDayBeg   start instant day
     * @param iHrBeg    start instant hour
     * @param iMinBeg   start instant minute
     * @param iSecBeg   start instant second
     * @param iNsBeg    start instant nanosecond
     * @param iYrEnd    end instant year
     * @param iMnEnd    end instant month
     * @param iDayEnd   end instant day
     * @param iHrEnd    end instant hour
     * @param iMinEnd   end instant minute
     * @param iSecEnd   end instant second
     * @param iNsEnd    end instant nanosecond
     */
    public TimeInterval(
            int iYrBeg, int iMnBeg, int iDayBeg, int iHrBeg, int iMinBeg, int iSecBeg, int iNsBeg,
            int iYrEnd, int iMnEnd, int iDayEnd, int iHrEnd, int iMinEnd, int iSecEnd, int iNsEnd
            ) 
    {
        this(ZonedDateTime.of(iYrBeg, iMnBeg, iDayBeg, iHrBeg, iMinBeg, iSecBeg, iNsBeg, ZoneId.systemDefault()),
             ZonedDateTime.of(iYrEnd, iMnEnd, iDayEnd, iHrEnd, iMinEnd, iSecEnd, iNsEnd, ZoneId.systemDefault())
             );
    }
    
    /**
     * <p>
     * Creates a new instance of <code>TimeInterval</code> by parsing the given
     * arguments as strings representing an ISO temporal specification.  For example
     * <br/><br/>
     * <sp/> var tvl = new TimeIntervale("2021-12-03T10:15:30.00Z", "2021-12-03T12:15:30.00Z")
     * <br/><br/>
     * will create a new time interval of two hours duration starting on December 3,
     * 2021 at 10:15:30AM.
     * </p>  
     *
     * @param strBegin string description of interval start in ISO format
     * @param strEnd   string description of interval end in ISO format
     * 
     * @throws DateTimeParseException at least one of the arguments was not properly formatted
     */
    public TimeInterval(String strBegin, String strEnd) throws DateTimeParseException {
        this(Instant.parse(strBegin), Instant.parse(strEnd));
    }

    
    //
    // Getter Methods
    //
    
    //
    // Operations
    //
    
    /**
     * Returns a string representing the <em>range</em> of an InfluxDB service
     * query.  The <em>range</em> represents a restricted time interval 
     * for the entire query and may be the full query or a portion of the query
     * It has the form
     * <br/>
     * <br/>
     * <sp/> "|> range(start:'instant', stop:'instant')"
     * <br/>
     * <br/>
     * where each 'instant' is string representation of a time instant. 
     * 
     * @return range portion of a Datastore query
     */
    public String createInfluxDbRangeQuery() {
        if (this.equals(EMPTY))
            return "|> range()";
        
        String strRng = "|> range(start: " + this.begin()
                      + ", stop: " + this.end() + ")";
        
        return strRng;
    }
    
    
    //
    // Set-Theoretic Functions
    //
    
    /**
     * Returns the left end-point of this interval 
     * 
     * @return starting time instant of this time interval
     */
    public Instant getLeftEndPoint() {
        return this.begin;
    }
    
    /**
     * Returns the right end-point of this interval 
     * 
     * @return stop time instant of this time interval
     */
    public Instant getRightEndPoint() {
        return this.end;
    }
    
    /**
     * Get the measure of the time interval, that is, the duration of time
     * between the start and end times.
     * 
     * @return  measure of time between interval start to finish
     * 
     * @see java.time.Duration
     */
    public Duration getDuration() {
        if (this.equals(EMPTY))
            return Duration.ZERO;
        
        return Duration.between(begin, end);
    }
    
    /**
     * <p>
     * Checks if the given time instant is a member of this time interval when it is closed.
     * </p>
     * 
     * @param t     time instant to be checked for interval membership
     * 
     * @return      <code>true</code> if <code>t</code> &isin; (<code>begin</code>, <code>end</code>),
     *              <code>false</code> otherwise
     */
    public boolean hasMembershipOpen(Instant t) {
        return greaterThan(t, this.begin) && lessThan(t, this.end);
    }
    
    
    /**
     * <p>
     * Checks if the given time instant is a member of this time interval when it is closed.
     * </p>
     * 
     * @param t     time instant to be checked for interval membership
     * 
     * @return      <code>true</code> if <code>t</code> &isin; [<code>begin</code>, <code>end</code>],
     *              <code>false</code> otherwise
     */
    public boolean hasMembershipClosed(Instant t) {
        return greaterThanEqualTo(t, this.begin) && lessThanEqualTo(t, this.end);
    }
    
    /**
     * Checks whether or not the given time interval has a set-wise intersection
     * with this time interval (as open intervals).
     * 
     * @param tvlArg    time interval to be checked
     * 
     * @return      true of both intervals share a common subinterval, false otherwise
     */
    public boolean hasIntersectionOpen(TimeInterval tvlArg) {
        if (this.equals(EMPTY) || tvlArg.equals(EMPTY))
            return false;
        
        Instant left = max(this.begin, tvlArg.begin);
        Instant right = min(this.end, tvlArg.end);

        return left.isBefore(right);
    }

    /**
     * Checks whether or not the given time interval has a setwise intersection
     * with this time interval (as closed intervals).
     * 
     * @param tvlArg    time interval to be checked
     * 
     * @return      true of both intervals share a common subinterval, false otherwise
     */
    public boolean hasIntersectionClosed(TimeInterval tvlArg) {
        if (this.equals(EMPTY) || tvlArg.equals(EMPTY))
            return false;
        
        Instant left = max(this.begin, tvlArg.begin);
        Instant right = min(this.end, tvlArg.end);

        return left.equals(right) || left.isBefore(right);
    }

    
    //
    // Private Methods
    //
    
    /**
     * Determines the maximum time instant of the given arguments.
     * 
     * @param i1    time instant to compare
     * @param i2    time instant to compare
     * 
     * @return  max(i1, i2)
     */
    private static Instant max(Instant i1, Instant i2) {
        if (i1.compareTo(i2) < 0)
            return i2;
        else
            return i1;
    }
    
    /**
     * Determines the minimum time instant of the given arguments.
     * 
     * @param i1    time instant to compare
     * @param i2    time instant to compare
     * 
     * @return  min(i1, i2)
     */
    private static Instant min(Instant i1, Instant i2) {
        if (i1.compareTo(i2) < 0)
            return i1;
        else
            return i2;
    }
    
    /**
     * <p>
     * Returns the result of conditional inequality
     * <pre>
     *   <code>i1</code> &lt; <code>i2</code>
     * </pre>
     * where <code>i1</code> and <code>i2</code> are the respective arguments.
     * </p>
     *  
     * @param i1    first time instance
     * @param i2    second time instance
     * 
     * @return  <code>true</code> if <code>i1</code> &lt; <code>i2</code>,
     *          <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    private static boolean lessThan(Instant i1, Instant i2) {
        int iCmp = i1.compareTo(i2);
        
        return iCmp < 0;
    }
    
    /**
     * <p>
     * Returns the result of conditional inequality
     * <pre>
     *   <code>i1</code> &lt;= <code>i2</code>
     * </pre>
     * where <code>i1</code> and <code>i2</code> are the respective arguments.
     * </p>
     *  
     * @param i1    first time instance
     * @param i2    second time instance
     * 
     * @return  <code>true</code> if <code>i1</code> &lt;= <code>i2</code>,
     *          <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    private static boolean lessThanEqualTo(Instant i1, Instant i2) {
        int iCmp = i1.compareTo(i2);
        
        return iCmp <= 0;
    }
    
    /**
     * <p>
     * Returns the result of conditional inequality
     * <pre>
     *   <code>i1</code> &gt; <code>i2</code>
     * </pre>
     * where <code>i1</code> and <code>i2</code> are the respective arguments.
     * </p>
     *  
     * @param i1    first time instance
     * @param i2    second time instance
     * 
     * @return  <code>true</code> if <code>i1</code> &gt; <code>i2</code>,
     *          <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    private static boolean greaterThan(Instant i1, Instant i2) {
        int iCmp = i1.compareTo(i2);
        
        return iCmp > 0;
    }
    
    /**
     * <p>
     * Returns the result of conditional inequality
     * <pre>
     *   <code>i1</code> &gt;= <code>i2</code>
     * </pre>
     * where <code>i1</code> and <code>i2</code> are the respective arguments.
     * </p>
     *  
     * @param i1    first time instance
     * @param i2    second time instance
     * 
     * @return  <code>true</code> if <code>i1</code> &gt;= <code>i2</code>,
     *          <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    private static boolean greaterThanEqualTo(Instant i1, Instant i2) {
        int iCmp = i1.compareTo(i2);
        
        return iCmp >= 0;
    }
    
}
