package app.azracelik.harman.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun getCategoryIcon(categoryName: String): ImageVector {
    return when (categoryName.trim()) {
        "Faturalar & Abonelikler" -> Icons.Default.Receipt
        "Sosyal & Eğlence" -> Icons.Default.TheaterComedy
        "Market" -> Icons.Default.ShoppingCart
        "Ulaşım" -> Icons.Default.DirectionsBus
        "Alışveriş" -> Icons.Default.ShoppingBag
        "Sağlık & Bakım" -> Icons.Default.Favorite
        "Eğitim & Kariyer" -> Icons.Default.Work
        "Yatırım" -> Icons.Default.AttachMoney
        "Maaş" -> Icons.Default.Payments
        "Diğer" -> Icons.Default.MoreHoriz
        else -> Icons.Default.MoreHoriz
    }
}