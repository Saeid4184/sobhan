package ir.factory.entryexit.data

import androidx.lifecycle.LiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * مدیریت مخزن داده‌ها: هماهنگ‌سازی دیتابیس محلی SQLite (Room) با شبکه ابری Firebase Firestore.
 * این متد تضمین می‌کند که داده‌های ثبت‌شده در هر گوشی به سرعت در ۴ گوشی دیگر بروزرسانی شوند.
 */
class Repository(
    private val personDao: PersonDao,
    private val logDao: LogDao
) {
    // راه‌اندازی فایربیس کلاینت به عنوان همگام‌ساز زنده
    private val firestore = FirebaseFirestore.getInstance()
    private val syncCollection = firestore.collection("concrete_factory_sync")
    private var firestoreListener: ListenerRegistration? = null

    val allPeople: LiveData<List<PersonEntity>> = personDao.getAllPeople()
    val allLogs: LiveData<List<LogEntity>> = logDao.getAllLogs()

    fun getPeopleByCategory(category: String): LiveData<List<PersonEntity>> {
        return personDao.getPeopleByCategory(category)
    }

    suspend fun insertPerson(person: PersonEntity) {
        personDao.insert(person)
        uploadPersonToCloud(person)
    }

    suspend fun updatePerson(person: PersonEntity) {
        personDao.update(person)
        uploadPersonToCloud(person)
    }

    suspend fun insertLog(log: LogEntity) {
        logDao.insert(log)
        uploadLogToCloud(log)
    }

    // آپلود ناهمگام در کلود فایربیس (Firebase به صورت خودکار حالت آفلاین را مدیریت می‌کند)
    private fun uploadPersonToCloud(person: PersonEntity) {
        syncCollection.document("person_${person.id}")
            .set(person)
            .addOnFailureListener {
                // ذخیره‌سازی محلی کلود در زمان قطع شبکه به صورت خودکار توسط Firestore انجام می‌شود
            }
    }

    private fun uploadLogToCloud(log: LogEntity) {
        syncCollection.document("log_${log.id}")
            .set(log)
    }

    /**
     * همگام‌ساز ابری دوطرفه (Real-time Cloud Sync):
     * این شنونده مداوم در پس‌زمینه با فایربیس ارتباط برقرار کرده و هر زمان که گوشی‌های نگهبانی دیگر تغییرات
     * جدیدی ثبت کنند، آن‌ها را دریافت کرده و در دیتابیس محلی این گوشی ذخیره می‌کند.
     */
    fun startRealtimeSync(scope: CoroutineScope) {
        // جلوگیری از ایجاد چند لیسنر همزمان بر روی منابع سیستم
        firestoreListener?.remove()

        firestoreListener = syncCollection.addSnapshotListener { snapshots, e ->
            if (e != null || snapshots == null) return@addSnapshotListener

            scope.launch(Dispatchers.IO) {
                for (doc in snapshots.documentChanges) {
                    val data = doc.document
                    val docId = data.id

                    try {
                        if (docId.startsWith("person_")) {
                            val person = data.toObject(PersonEntity::class.java)
                            // از بروز بودن داده محلی اطمینان حاصل شده و دیتابیس محلی با دیتابیس ابری یکی می‌شود
                            personDao.insertOrUpdate(person)
                        } else if (docId.startsWith("log_")) {
                            val log = data.toObject(LogEntity::class.java)
                            logDao.insertOrUpdate(log)
                        }
                    } catch (parseError: Exception) {
                        parseError.printStackTrace() // مدیریت و ثبت خطای احتمالی در پارس فایل JSON ابری
                    }
                }
            }
        }
    }

    /**
     * لغو همگام‌سازی زنده جهت آزادسازی رم و بهینه‌سازی عالی مصرف باتری دستگاه نگهبانی.
     */
    fun stopRealtimeSync() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}
