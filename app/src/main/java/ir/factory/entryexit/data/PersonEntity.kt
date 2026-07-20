package ir.factory.entryexit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * مدل داده‌ای نهایی پرسنل، رانندگان و ماشین‌آلات.
 * ستون‌ها دقیقاً به نام‌های "type" و "lastEventAt" نگاشت شده‌اند تا با کوئری‌های موجود در PersonDao 
 * پروژه نهایی (sobhan.zip) همخوانی کامل داشته باشند و خطای کامپایل برطرف شود.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey 
    val id: String = "",                                 // کد ملی، کد پرسنلی یا شماره پلاک خودرو به عنوان کلید اصلی
    val name: String = "",                               // نام کامل شخص یا عنوان وسیله نقلیه
    val category: String = "",                           // بخش دقیق (مانند: آزمایشگاه، امور اداری)
    
    @ColumnInfo(name = "type")
    val type: String = "personnel",                      // دسته‌بندی اصلی: "personnel"، "machinery"، "drivers"، "visitors"
    
    var isInside: Boolean = false,                      // وضعیت حضور زنده در کارخانه
    
    @ColumnInfo(name = "lastEventAt")
    var lastEventAt: Long = System.currentTimeMillis()   // تاریخچه زمانی برای اولویت همگام‌سازی و کوئری‌ها
) : Serializable
