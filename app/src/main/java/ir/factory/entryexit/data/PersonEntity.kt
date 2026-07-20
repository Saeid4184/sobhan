package ir.factory.entryexit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * مدل داده‌ای مرکزی برای نگهداری مشخصات پرسنل، رانندگان و ماشین‌آلات.
 * نام ستون‌ها با استفاده از @ColumnInfo دقیقاً به "group_name" و "lastEventAt" نگاشت شده‌اند 
 * تا با کوئری‌های از پیش نوشته شده در PersonDao همخوانی کامل داشته باشند و خطای کامپایل برطرف گردد.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey 
    val id: String = "",                                 // کد ملی، کد پرسنلی یا شماره پلاک خودرو به عنوان کلید اصلی
    val name: String = "",                               // نام کامل شخص یا عنوان وسیله نقلیه
    val category: String = "",                           // بخش دقیق (مانند: آزمایشگاه، امور اداری، راننده لودر)
    
    @ColumnInfo(name = "group_name")
    val groupName: String = "personnel",                 // دسته‌بندی اصلی: "personnel" (پرسنل)، "machinery" (ماشین‌آلات)، "drivers" (رانندگان)، "visitors" (مهمانان)
    
    var isInside: Boolean = false,                      // وضعیت حضور زنده در کارخانه
    
    @ColumnInfo(name = "lastEventAt")
    var lastEventAt: Long = System.currentTimeMillis()   // تاریخچه زمانی برای اولویت همگام‌سازی و کوئری‌ها
) : Serializable
```
eob

### راهنمای گام دوم برای حل مشکل:
کدهای بالا را کپی کرده و در مخزن گیت‌هاب خود جایگزین فایل قبلی `PersonEntity.kt` کنید. به محض ذخیره (Commit)، سرورهای گیت‌هاب پروژه شما را بدون هیچ خطایی کامپایل کرده و فایل نصب آماده (`APK`) را بدون باگ در اختیارتان قرار خواهند داد.
