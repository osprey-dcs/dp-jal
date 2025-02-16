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
package com.ospreydcs.dp.api.common;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Comparator;
import java.util.List;

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
public record TimeInterval(Instant begin, Instant end) implements Serializable {

    
    //
    // Class Types - Comparators
    //
    
    /**
     * <p>
     * Creates and returns a <code>Comparator</code> object that compares 
     * <code>TimeInterval</code> instances according to their beginning (left endpoint).
     * </p>
     * <p>
     * When used in collection ordering the comparator orders time intervals according to 
     * their start instant, earliest first - regardless of duration.
     * </p>
     * 
     * @return <code>Comparator</code> of time intervals based upon start instant
     */
    public static Comparator<TimeInterval> comparatorBegin() {
        Comparator<TimeInterval> cmp = (i1, i2) -> {
            return i1.begin.compareTo(i2.begin);
        };
        
        return cmp;
    }
    
    /**
     * <p>
     * Creates and returns a <code>Comparator</code> object that compares two 
     * <code>TimeInterval</code> objects according to the length of their duration.  
     * </p>
     * <p>
     * When used as a collection ordering the comparator orders time intervals
     * according to their time duration, smallest first, regardless of start times.
     * </p>
     * 
     * @return <code>Comparator</code> of time intervals based upon time duration
     */
    public static Comparator<TimeInterval> comparatorDuration() {
        Comparator<TimeInterval> cmp = (i1, i2) -> {
            return i1.getDuration().compareTo(i2.getDuration());
        };
        
        return cmp;
    }
    
    
    //
    // Class Constants
    //
    
    /** The empty time interval */
    public static final TimeInterval EMPTY = TimeInterval.from(Instant.EPOCH, Instant.EPOCH);
    
    /** The universe - that is, the compliment of the empty interval */
    public static final TimeInterval UNIVERSE = TimeInterval.from(Instant.MIN, Instant.MAX);
    
    
    //
    // Creators 
    //
    
    /**
     * Create and return a deep copy of the given <code>TimeInteval</code> instance.
     * 
     * @param tvl   <code>TimeInterval</code> to be copied
     * 
     * @return      deep copy of the given time instant
     */
    public static TimeInterval  from(TimeInterval tvl) {
        return new TimeInterval(tvl);
    }
    
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
    // Set Operations
    //
    
    /**
     * <p>
     * Determines whether or not the two arguments have a common intersetion as a set.
     * </p>
     * 
     * @param tvl1  time interval under test
     * @param tvl2  time interval under test
     * 
     * @return  <code>true</code> if two intervals share a common (set-wise) intersection, <code>false</code> otherwise
     */
    public static boolean   isDisjoint(TimeInterval tvl1, TimeInterval tvl2) {
        
        if (tvl1.hasIntersectionClosed(tvl2))
            return true;
        else
            return false;
    }
    
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
            return new TimeInterval(Instant.from(left), Instant.from(right));
            
        } catch (IllegalArgumentException e) {
            return EMPTY;
        }
    }
    
    /**
     * <p>
     * Returns the union of the two time intervals assuming that they have a common intersection.
     * </p>
     * <p>  
     * Under the assumption, the union of the 
     * two time intervals (as a set) can be represented as a single time interval of
     * measure at least that of either the two.  If there is no common intersection
     * between the intervals then an exception is thrown.
     * </p>
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
        
        return new TimeInterval(Instant.from(left), Instant.from(right));
    }
    
    /**
     * <p>
     * Returns the set-wise union of the two time intervals.
     * </p>
     * <p>
     * The returned value is necessarily a list of <code>TimeInterval</code> as the union
     * can potentially be two disjoint time intervals. There are 3 cases to consider:
     * <ol>
     * <li>
     * The two intervals are disjoint - the returned list contains copies of the original intervals
     * with the left-most interval first.
     * </li>
     * <li>
     * The two intervals have a common intersection which is a proper subset of each interval - the returned
     * list contains a single interval with endpoints [min{tvl1.begin, tvl2.begin}, max{tvl1.end, tvl2.end}].
     * </li>
     * <li>
     * One interval is a subset of the other - the returns list contains a single interval which is a copy of
     * the largest interval.
     * </ol>
     * </p>
     * 
     * @param tvl1    time interval with which to create a set union
     * @param tvl2    time interval with which to create a set union
     * 
     * @return          union of the given arguments
     */
    public static List<TimeInterval>    union(TimeInterval tvl1, TimeInterval tvl2) {
        
        // Check if intervals are disjoint - returns left-most interval first
        if (TimeInterval.isDisjoint(tvl1, tvl2)) {
            if (TimeInterval.lessThan(tvl1.begin, tvl2.begin))
                return List.of(TimeInterval.from(tvl1), TimeInterval.from(tvl2));
            else
                return List.of(TimeInterval.from(tvl2), TimeInterval.from(tvl1));
        }
        
        // Left endpoint of tvl2 is within tvl1
        if (tvl1.hasMembershipClosed(tvl2.begin)) {
            Instant insEnd = TimeInterval.max(tvl1.end, tvl2.end);
            
            TimeInterval    tvlUnion = TimeInterval.from(tvl1.begin, insEnd);
            
            return List.of(tvlUnion);
        }
        
        // Right endpoint of tvl2 is within tvl1
        if (tvl1.hasMembershipClosed(tvl2.end)) {
            Instant insBeg = TimeInterval.min(tvl1.begin, tvl2.begin);
            
            TimeInterval    tvlUnion = TimeInterval.from(insBeg, tvl1.end);
            
            return List.of(tvlUnion);
        }
        
        // One interval is a subset of the other
        if (tvl1.contains(tvl2))
            return List.of(TimeInterval.from(tvl1));
        else
            return List.of(TimeInterval.from(tvl2));
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
                return TimeInterval.from(tvl2);
        else
            if (tvl2.equals(EMPTY))
                return TimeInterval.from(tvl1);
                  
        // Both intervals are non null
        Instant left  = min(tvl1.begin, tvl2.begin);
        Instant right = max(tvl1.end, tvl2.end);
        
        return new TimeInterval(Instant.from(left), Instant.from(right));
    }

    /**
     * <p>
     * Performs a set-wise subtraction of the second interval (subtrahend) from the first interval (minuend).
     * </p>
     * <p>
     * This method performs the set operation on the argument intervals 
     * <i>I<sub>min</sub></i> and <i>I<sub>sub</sub></i> 
     * <pre>
     *   <i>I<sub>diff</sub></i> = <i>I<sub>min</sub></i> \ <i>I<sub>sub</sub></i>
     * </pre>
     * where
     * <ul>
     * <li><i>I<sub>diff</sub></i> is the returned <em>difference</em></li>
     * <li><i>I<sub>min</sub></i> = [<i>t</i><sub>1</sub>, <i>t</i><sub>2</sub>] is the target <em>minuend</em></li> 
     * <li><i>I<sub>sub</sub></i> = [<i>&tau;</i><sub>1</sub>, <i>&tau;</i><sub>2</sub>] is the <em>subtrahend</em> (subtractor)</li>
     * </ul>
     * Before considering the results of the operation we make the following definitions:
     * <pre>
     *   <i>I</i><sub>1</sub> &#8796; [<i>t</i><sub>1</sub>, <i>&tau;</i><sub>1</sub>)
     *   <i>I</i><sub>2</sub> &#8796; (<i>t</i><sub>2</sub>, <i>&tau;</i><sub>2</sub>]
     * </pre>
     * </p>
     * <p>
     * There are essentially 5 cases to consider. The first is the trivial case where   
     * intervals <i>I<sub>min</sub></i> and <i>I<sub>sub</sub></i> are disjoint, that is
     * <pre>
     *   <i>I<sub>min</sub></i> &cap; <i>I<sub>sub</sub></i> = &empty;.  
     * </pre>
     * In this case a deep copy of the minuend <i>I<sub>min</sub></i> is returned as a list of one element
     * (i.e., nothing is subtracted from <i>I<sub>min</sub></i>).
     * </p>
     * <p>
     * In the remaining cases operation <i>I<sub>min</sub></i> &cap; <i>I<sub>sub</sub></i> has a non-empty
     * intersection <i>I<sub>int</sub></i>; that is,
     * <pre> 
     *   <i>I<sub>min</sub></i> &cap; <i>I<sub>sub</sub></i> = <i>I<sub>int</sub></i> &ne; &empty;.
     * </pre>
     * The remaining results are as follows:
     * <ol>
     * <li>
     * <i>I<sub>sub</sub></i> has one endpoint to the right of <i>I<sub>min</sub></i> and the other
     * within <i>I<sub>min</sub></i>.  The result is a list containing the single contiguous interval
     * <i>I<sub>diff</sub></i> = <i>I<sub>min</sub></i> \ <i>I<sub>sub</sub></i> = <i>I</i><sub>1</sub> 
     * </li>
     * <li>
     * <i>I<sub>sub</sub></i> has one endpoint to the left of <i>I<sub>min</sub></i> and the other
     * within <i>I<sub>min</sub></i>.  The result is a list containing the single contiguous interval
     * <i>I<sub>diff</sub></i> = <i>I<sub>min</sub></i> \ <i>I<sub>sub</sub></i> = <i>I</i><sub>2</sub> 
     * </li>
     * <li>
     * <i>I<sub>sub</sub></i> is properly contained within <i>I<sub>min</sub></i>,  
     * that is, <i>I<sub>sub</sub></i> &sub; <i>I<sub>min</sub></i>.
     * The result here is a list containing the two disjoint intervals {<i>I</i><sub>1</sub>, <i>I</i><sub>2</sub>}.
     * That is,
     * <i>I<sub>diff</sub></i> = <i>I<sub>min</sub></i> \ <i>I<sub>sub</sub></i> = <i>I</i><sub>2</sub> &cup; <i>I</i><sub>2</sub> 
     * </li> 
     * <li>
     * <i>I<sub>sub</sub></i> contains the entire minuend <i>I<sub>min</sub></i>,
     * that is, <i>I<sub>sub</sub></i> &#8839; <i>I<sub>min</sub></i>.
     * The result is is the empty set &empty; which is returned as the empty list.
     * That is,
     * <i>I<sub>diff</sub></i> = <i>I<sub>min</sub></i> \ <i>I<sub>sub</sub></i> = &empty; 
     * </li>
     * </ol>
     *   
     * @param tvlMinuend        the minuend to be subtracted from
     * @param tvlSubtrahend     the subtrahend to subtract from the minuend
     * 
     * @return      the set-wise difference of the minuend and subtrahend
     */
    public static List<TimeInterval>  subtract(TimeInterval tvlMinuend, TimeInterval tvlSubtrahend) {
        
        // Left endpoint of subtrahend is within minuend
        if (tvlMinuend.hasMembershipClosed(tvlSubtrahend.begin)) {
            TimeInterval I1 = TimeInterval.from(tvlMinuend.begin, tvlSubtrahend.begin);
            
            // Right endpoint of subtrahend is outside minuend 
            if (TimeInterval.lessThanEqualTo(tvlMinuend.end, tvlSubtrahend.end)) 
                return List.of( I1 );
            
            // Subtrahend is contained within minuend
            else {
                TimeInterval I2 = TimeInterval.from(tvlSubtrahend.end, tvlMinuend.end);
    
                return List.of(I1, I2); 
            }
        }
        
        // Right endpoint of subtrahend is within minuend
        if (tvlMinuend.hasMembershipClosed(tvlSubtrahend.end)) {
            TimeInterval    I2 = TimeInterval.from(tvlSubtrahend.end, tvlMinuend.end);
            
            // Left endpoint of subtrahend is outside minuend
            if (TimeInterval.greaterThanEqualTo(tvlMinuend.begin, tvlSubtrahend.end))
                return List.of( I2 );
            
            // Subtrahend is contained within minuend
            else {
                TimeInterval I1 = TimeInterval.from(tvlMinuend.begin, tvlSubtrahend.begin);
    
                return List.of(I1, I2);
            }
        }
        
        // No endpoints of subtrahend are within minuend ...
        
        // Subtrahend is disjoint and to the left of minuend
        if (TimeInterval.lessThan(tvlSubtrahend.end, tvlMinuend.begin))
            return List.of( TimeInterval.from(tvlMinuend) );
        
        // Subtrahend is disjoint and to the right of minuend
        if (TimeInterval.greaterThan(tvlSubtrahend.begin, tvlMinuend.end))
            return List.of( TimeInterval.from(tvlMinuend) );
        
        // Only case left - Subtrahend contains minuend
        return List.of();
    }

    
    

    //
    // Non-Canonical Constructors
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
     * <p>
     * Constructs a new <code>TimeInterval</code> instance as a deep copy of the given instance.
     * </p>
     *
     * @param tvl   <code>TimeInterval</code> to be copied
     */
    public TimeInterval(TimeInterval tvl) {
        this(Instant.from(tvl.begin), Instant.from(tvl.end));
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
     * Checks whether or not the given time interval has a set-wise intersection
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
    
    /**
     * Checks whether or not this time interval <i>I</i> contains the given time interval <i>I</i><sub>test</sub>
     * as a set.
     *  
     * @param tvlArg    time interval <i>I</i><sub>test</sub> under test
     * 
     * @return  <code>true</code> if <i>I</i><sub>test</sub> &sube; <i>I</i>,
     *          <code>false</code> otherwise
     */
    public boolean contains(TimeInterval tvlArg) {
        if (this.equals(EMPTY) || tvlArg.equals(EMPTY))
            return false;

        return (this.begin.compareTo(tvlArg.begin) <= 0) 
            && (this.end.compareTo(tvlArg.end) >= 0);
    }

    
    //
    // Record Overrides
    //
    
    /**
     * <p>
     * Determines equality of intervals.
     * </p>
     * <p>
     * Performs the following checks:
     * <ol>
     * <li>Checks to see that the argument is a <code>TimeInterval</code> type.</li>
     * <li>Checks equality of left endpoint with <code>{@link Instant#equals(Object)}</code>.</li>  
     * <li>Checks equality of right endpoint with <code>{@link Instant#equals(Object)}</code>.</li>
     * </ol>
     * If all the above answer <code>true</code> the result is <code>true</code>.  Otherwise
     * the result is false.
     *   
     * @param   obj     equality comparison object
     * 
     * @return  <code>true</code> if the argument is a <code>TimeInterval</code> with equal endpoints
     *
     * @see java.lang.Record#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeInterval tvl) 
            return this.begin.equals(tvl.begin) && this.end.equals(tvl.end);
        
        return false;
    }

    /**
     * <p>
     * Customized <code>toString()</code> method for <code>TimeInterval</code> class.
     * </p>
     * <p>
     * Writes the time interval to a string with the given format
     * <code>
     * <pre>
     *   [epochSecond:nanoSecond, epochSecond:nanoSecond] 
     * </pre>
     * </code>
     * where "<code>epochSeconde</code>" and "<code>nanoSecond</code>" refer to the respective
     * <code>{@link Instant}</code> components of the interval endpoints.
     * </p>
     * 
     * @return  string representation of the this time interval  
     * 
     * @see @see java.lang.Record#toString()
     */
    @Override
    public String toString() {
        StringBuffer    buf = new StringBuffer();
        
        buf.append("[");
        buf.append( Long.toString(this.begin.getEpochSecond()) );
        buf.append(":");
        buf.append( Long.toString(this.begin.getNano()) );
        buf.append(", ");
        buf.append( Long.toString(this.end.getEpochSecond()) );
        buf.append(":");
        buf.append( Long.toString(this.end.getNano()) );
        buf.append("]");
        
        return buf.toString();
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
