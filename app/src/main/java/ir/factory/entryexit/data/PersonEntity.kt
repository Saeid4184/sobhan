package ir.factory.entryexit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * مدل داده‌ای تصحیح‌شده و نهایی پرسنل، رانندگان و ماشین‌آلات.
 * این نسخه با نگاشت همزمان ستون‌های "group_name" و "type" و "lastEventAt" خطای کامپایلر KSP را
 * به طور کامل و قطعی در پروژه نهایی (sobhan.zip) برطرف می‌کند.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey 
    val id: String = "",                                 // کد ملی، کد پرسنلی یا شماره پلاک خودرو به عنوان کلید اصلی
    val name: String = "",                               // نام کامل شخص یا عنوان وسیله نقلیه
    
    @ColumnInfo(name = "group_name")
    val group_name: String = "",                         // بخش دقیق (مانند: آزمایشگاه، امور اداری) - منطبق بر کوئری‌های DAO
    
    @ColumnInfo(name = "type")
    val type: String = "personnel",                      // دسته‌بندی اصلی: "personnel"، "machinery"، "drivers"، "visitors"
    
    var isInside: Boolean = false,                      // وضعیت حضور زنده در کارخانه
    
    @ColumnInfo(name = "lastEventAt")
    var lastEventAt: Long = System.currentTimeMillis()   // تاریخچه زمانی برای اولویت همگام‌سازی و کوئری‌ها
) : Serializable {

    /* STREAMING_CHUNK: Adding category getter for visual client compatibility... */
    // این فیلد مجازی جهت حفظ سازگاری کامل با کدهای پنل وب و آداپتورهای سمت کلاینت بدون ایجاد تداخل در ساختار دیتابیس است.
    val category: String
        get() = group_name
}
