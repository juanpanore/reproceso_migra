package com.sura.arl.reproceso.util;

import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author pragma.co
 */
public final class Periodo implements Comparable<Periodo>, Serializable {

    /**
     * Serialization version.
     */
    private static final long serialVersionUID = 4183400860270640070L;

    private final YearMonth yearMonth;

    private static final DateTimeFormatter PERIODO_FORMATTER_YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter PERIODO_FORMATTER_MMYYYY = DateTimeFormatter.ofPattern("MMyyyy");

    private static final DateTimeFormatter DTF_YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private Periodo() {
        this.yearMonth = YearMonth.now();
    }

    private Periodo(int anno, int mes) {
        this.yearMonth = YearMonth.of(anno, mes);
    }

    private Periodo(Date date) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(date);
        this.yearMonth = YearMonth.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1);
    }

    public static Periodo of(int anno, int mes) {
        return new Periodo(anno, mes);
    }

    public static Periodo of(int anno, Month mes) {
        return of(anno, mes.getValue());
    }

    private YearMonth getYearMonth() {
        return this.yearMonth;
    }

    public static Periodo from(Date date) {
        return new Periodo(date);
    }

    public LocalDate firstDayOfMonth() {
        return LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1);
    }

    public LocalDate lastDayOfMonth() {
        return LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    public static Periodo from(TemporalAccessor temporal) {
        if (temporal == null) {
            return null;
        }
        return of(temporal.get(YEAR), temporal.get(MONTH_OF_YEAR));
    }

    public static Periodo parse(CharSequence text, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).parse(text, Periodo::from);
    }

    public String format(String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(yearMonth);
    }

    public static List<Periodo> getPeridos(Periodo inicio, Periodo fin) {
        Periodo init = Periodo.from(inicio.getYearMonth());
        List<Periodo> periodos = new ArrayList<>();
        while (init.compareTo(fin) <= 0) {
            periodos.add(init);
            init = init.plusMonths(1l);
        }
        return periodos;
    }

    @Deprecated
    public static LocalDate getPeriodo(String periodoYYYYMM) {
        return LocalDate.from(DTF_YYYYMMDD.parse(periodoYYYYMM + "01"));
    }

    @Deprecated
    public static String getPeriodo(LocalDate periodo) {
        return PERIODO_FORMATTER_YYYYMM.format(periodo);
    }

    @Deprecated
    public static String getPeriodoMMYYYY(String periodoYYYYMM) {

        return PERIODO_FORMATTER_MMYYYY.format(DTF_YYYYMMDD.parse(periodoYYYYMM + "01"));
    }

    @Deprecated
    public static String getPeriodoMenosXMeses(String periodoYYYYMM, long meses) {

        LocalDate localDate = LocalDate.from(DTF_YYYYMMDD.parse(periodoYYYYMM + "01"));
        localDate = localDate.minusMonths(meses);
        return PERIODO_FORMATTER_YYYYMM.format(localDate);
    }

    public Periodo minusMonths(long monthsToSubtract) {
        return plusMonths(-monthsToSubtract);
    }

    public static Periodo now() {
        return now(Clock.systemDefaultZone());
    }

    private static Periodo now(ZoneId zone) {
        return now(Clock.system(zone));
    }

    public static Periodo now(Clock clock) {
        final LocalDate now = LocalDate.now(clock);
        return Periodo.of(now.getYear(), now.getMonth());
    }

    public boolean isEqual(Periodo other) {
        return compareTo(other) == 0;
    }

    public boolean isAfter(Periodo other) {
        return compareTo(other) > 0;
    }

    public boolean isBefore(Periodo other) {
        return compareTo(other) < 0;
    }

    @Override
    public int compareTo(Periodo o) {
        return this.getYearMonth().compareTo(o.yearMonth);
    }

    public Periodo plusMonths(long monthsToAdd) {
        YearMonth ym = YearMonth.of(this.yearMonth.getYear(), this.yearMonth.getMonth());
        ym = ym.plusMonths(monthsToAdd);
        return Periodo.from(ym);
    }

    public Date toDate() {
        return Date.from(firstDayOfMonth().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public String toString() {
        return PERIODO_FORMATTER_YYYYMM.format(yearMonth);
    }

}
