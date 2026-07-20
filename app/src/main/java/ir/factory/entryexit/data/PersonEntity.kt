package ir.factory.entryexit.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * مدل داده‌ای مرکزی برای نگهداری مشخصات پرسنل، رانندگان و ماشین‌آلات.
 * نام جدول دقیقاً بر روی "persons" تنظیم شده تا با کوئری‌های PersonDao کاملاً مطابقت داشته باشد و خطای KSP برطرف گردد.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey 
    val id: String = "",                  // کد ملی، کد پرسنلی یا شماره پلاک خودرو به عنوان کلید اصلی
    val name: String = "",                // نام کامل شخص یا عنوان وسیله نقلیه
    val category: String = "",            // بخش دقیق (مانند: آزمایشگاه، امور اداری، راننده لودر)
    val type: String = "personnel",       // دسته‌بندی اصلی: "personnel" (پرسنل)، "machinery" (ماشین‌آلات)، "drivers" (رانندگان)، "visitors" (مهمانان)
    var isInside: Boolean = false,       // وضعیت حضور زنده در کارخانه
    var lastUpdated: Long = System.currentTimeMillis() // تاریخچه زمانی برای اولویت همگام‌سازی
) : Serializable
