package app.azracelik.harman.data.model

import androidx.annotation.StringRes
import app.azracelik.harman.R

enum class TransactionType { INCOME, EXPENSE }

/** Sabit kategori listesi: basit tutmak için kullanıcı tanımlı kategori yok. */
enum class Category(@param:StringRes val labelRes: Int, val type: TransactionType) {
    MARKET(R.string.cat_market, TransactionType.EXPENSE),
    BILLS(R.string.cat_bills, TransactionType.EXPENSE),
    TRANSPORT(R.string.cat_transport, TransactionType.EXPENSE),
    SHOPPING(R.string.cat_shopping, TransactionType.EXPENSE),
    FUN(R.string.cat_fun, TransactionType.EXPENSE),
    HEALTH(R.string.cat_health, TransactionType.EXPENSE),
    EDUCATION(R.string.cat_education, TransactionType.EXPENSE),
    OTHER_EXPENSE(R.string.cat_other, TransactionType.EXPENSE),
    SALARY(R.string.cat_salary, TransactionType.INCOME),
    EXTRA_INCOME(R.string.cat_extra_income, TransactionType.INCOME);

    companion object {
        fun forType(type: TransactionType) = entries.filter { it.type == type }
    }
}
