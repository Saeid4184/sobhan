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
    val id: Long = 0L,                                   // کد ملی، کد پرسنلی یا شماره پلاک خودرو به عنوان کلید اصلی عددی
    val name: String = "",                               // نام کامل شخص یا عنوان وسیله نقلیه
    
    @ColumnInfo(name = "group_name")
    val group: String = "",                              // بخش دقیق (مانند: آزمایشگاه، امور اداری) - منطبق بر کوئری‌های DAO
    
    val type: PersonType = PersonType.PERSONNEL,         // دسته‌بندی اصلی زنده بر اساس کلاس انوم پروژه شما
    var isInside: Boolean = false,                      // وضعیت حضور زنده در کارخانه
    
    @ColumnInfo(name = "lastEventAt")
    var lastEventAt: Long = System.currentTimeMillis(),  // تاریخچه زمانی برای اولویت همگام‌سازی و کوئری‌ها
    
    val imageUri: String? = null                         // آدرس ذخیره‌سازی عکس پرسنلی
) : Serializable {

    // این فیلد مجازی جهت حفظ سازگاری کامل با کدهای پنل وب بدون تغییر فیلد اصلی دیتابیس است
    val category: String
        get() = group
}
