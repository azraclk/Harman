package app.azracelik.harman.domain

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val TrLocale: Locale = Locale.forLanguageTag("tr-TR")

/** "1.234,50 ₺" */
fun formatMoney(minor: Long, showDecimals: Boolean = true): String {
    val nf = NumberFormat.getNumberInstance(TrLocale).apply {
        minimumFractionDigits = if (showDecimals && minor % 100 != 0L) 2 else 0
        maximumFractionDigits = if (showDecimals) 2 else 0
    }
    return nf.format(minor / 100.0) + " ₺"
}

/** Kullanıcı girişini ("1.250,5", "1250.50", "12") kuruşa çevirir; geçersizse null. */
fun parseMoneyToMinor(input: String): Long? {
    val s = input.trim().replace(" ", "")
    if (s.isEmpty()) return null
    val normalized = when {
        s.contains(',') -> s.replace(".", "").replace(',', '.')
        // Yalnızca nokta: "1.250" binlik ayracı, "12.5" ondalık kabul edilir.
        Regex("""\d{1,3}(\.\d{3})+""").matches(s) -> s.replace(".", "")
        else -> s
    }
    val value = normalized.toBigDecimalOrNull() ?: return null
    if (value.signum() <= 0) return null
    return value.movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP)
        .takeIf { it <= BigDecimal(MAX_MINOR) }?.toLong()
}

/** Düzenleme alanı için tutarı "1250,5" biçiminde geri verir. */
fun minorToInput(minor: Long): String {
    if (minor <= 0) return ""
    val whole = minor / 100
    val cents = (minor % 100).toInt()
    return if (cents == 0) "$whole" else "$whole,${cents.toString().padStart(2, '0').trimEnd('0')}"
}

private const val MAX_MINOR = 99_999_999_999L

private val monthFormatter = DateTimeFormatter.ofPattern("LLLL yyyy", TrLocale)
private val dayFormatter = DateTimeFormatter.ofPattern("d MMMM, EEEE", TrLocale)

fun YearMonth.label(): String = format(monthFormatter).replaceFirstChar { it.titlecase(TrLocale) }
fun LocalDate.dayLabel(): String = format(dayFormatter)
