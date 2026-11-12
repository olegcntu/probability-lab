/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab;

/**
 * Відомості про автора програми.
 *
 * @author Красавін Олег Олександрович
 */
public final class Author {

    public static final String FULL_NAME = "Красавін Олег Олександрович";
    public static final String SHORT_NAME = "О. О. Красавін";
    public static final String COURSE = "3 курс";
    public static final String GROUP = "51-фмт";
    public static final String SPECIALTY = "А4.04 Середня освіта (Математика)";
    public static final String FACULTY = "Природничо-математичний факультет";
    public static final String UNIVERSITY = "Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка";
    public static final String YEAR = "2026";

    private Author() {
    }

    /** Рядок для журналу запуску і довідки. */
    public static String signature() {
        return FULL_NAME + ", " + COURSE + ", група " + GROUP + ", " + SPECIALTY + ", " + UNIVERSITY + ", " + YEAR;
    }
}
