package app.azracelik.harman.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class DateFilterPeriod(val label: String) {
    THIS_MONTH("Bu Ay"),
    LAST_30_DAYS("Son 30 Gün"),
    LAST_MONTH("Geçen Ay"),
    ALL_TIME("Tüm Zamanlar")
}

object DateFilterHelper {
    private val turkishLocale = Locale.forLanguageTag("tr-TR")

    fun getDateRange(
        period: DateFilterPeriod
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()

        return when (period) {
            DateFilterPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis

                Pair(start, end)
            }

            DateFilterPeriod.LAST_30_DAYS -> {
                val end = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis

                Pair(start, end)
            }

            DateFilterPeriod.LAST_MONTH -> {
                calendar.add(Calendar.MONTH, -1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                val end = calendar.timeInMillis

                Pair(start, end)
            }

            DateFilterPeriod.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    fun formatDateRange(startMs: Long, endMs: Long, period: DateFilterPeriod): String {
        if (period == DateFilterPeriod.ALL_TIME) return "Tüm Zamanlar"

        val dayMonthFormat = SimpleDateFormat("d MMM", turkishLocale)
        val startDateStr = dayMonthFormat.format(startMs)
        val endDateStr = dayMonthFormat.format(endMs)

        return "$startDateStr - $endDateStr"
    }

    fun formatTransactionDate(dateMs: Long): String {
        val format = SimpleDateFormat("d MMMM yyyy, HH:mm", turkishLocale)
        return format.format(dateMs)
    }
}