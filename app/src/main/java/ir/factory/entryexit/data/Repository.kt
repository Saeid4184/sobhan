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

    val allPeople: LiveData<List<PersonEntity>> = personDao.getAllPersons()
    val allLogs: LiveData<List<LogEntity>> = logDao.getAllLogs()

    suspend fun ensureFleetSeeded() {
        // متد پیش‌فرض مقداردهی اولیه ناوگان
    }

    fun getPersonsByType(type: PersonType): LiveData<List<PersonEntity>> {
        return personDao.getPersonsByType(type)
    }

    fun getInsidePersonsByType(type: PersonType): LiveData<List<PersonEntity>> {
        return personDao.getInsidePersonsByType(type)
    }

    fun getAllCurrentlyInside(): LiveData<List<PersonEntity>> {
        return personDao.getAllCurrentlyInside()
    }

    fun getRecentActivityByType(type: PersonType): LiveData<List<LogEntity>> {
        return logDao.getRecentActivityByType(type)
    }

    fun search(query: String): LiveData<List<PersonEntity>> {
        return personDao.search(query)
    }

    suspend fun addPerson(name: String, group: String, type: PersonType): Result<Unit> {
        return try {
            val person = PersonEntity(
                id = System.currentTimeMillis(),
                name = name,
                group = group,
                type = type,
                isInside = false,
                lastEventAt = System.currentTimeMillis()
            )
            personDao.insert(person)
            uploadPersonToCloud(person)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePerson(id: Long, name: String, group: String, type: PersonType): Result<Unit> {
        return try {
            val person = personDao.getPersonByIdOnce(id) ?: PersonEntity(id = id, name = name, group = group, type = type)
            val updated = person.copy(name = name, group = group, type = type, lastEventAt = System.currentTimeMillis())
            personDao.update(updated)
            uploadPersonToCloud(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePerson(person: PersonEntity) {
        personDao.update(person)
        uploadPersonToCloud(person)
    }

    suspend fun updatePersonImage(id: Long, imageUri: String?): Result<Unit> {
        return try {
            val person = personDao.getPersonByIdOnce(id)
            if (person != null) {
                val updated = person.copy(imageUri = imageUri, lastEventAt = System.currentTimeMillis())
                personDao.update(updated)
                uploadPersonToCloud(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRosterOnce(): List<PersonEntity> {
        return personDao.getAllPersonsOnce()
    }

    suspend fun getLogsInRange(start: Long, end: Long): List<LogEntity> {
        return logDao.getLogsInRange(start, end)
    }

    suspend fun countCurrentlyInside(): Map<PersonType, Int> {
        val all = personDao.getAllPersonsOnce()
        return PersonType.values().associateWith { type ->
            all.count { it.type == type && it.isInside }
        }
    }

    suspend fun checkIn(personId: Long, time: Long = System.currentTimeMillis()): Result<Unit> {
        return try {
            val person = personDao.getPersonByIdOnce(personId)
            if (person != null) {
                person.isInside = true
                person.lastEventAt = time
                personDao.update(person)
                uploadPersonToCloud(person)

                val log = LogEntity(
                    personId = personId,
                    personName = person.name,
                    type = person.type,
                    isCheckIn = true,
                    timestamp = time
                )
                logDao.insert(log)
                uploadLogToCloud(log)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkOut(personId: Long, time: Long = System.currentTimeMillis()): Result<Unit> {
        return try {
            val person = personDao.getPersonByIdOnce(personId)
            if (person != null) {
                person.isInside = false
                person.lastEventAt = time
                personDao.update(person)
                uploadPersonToCloud(person)

                val log = LogEntity(
                    personId = personId,
                    personName = person.name,
                    type = person.type,
                    isCheckIn = false,
                    timestamp = time
                )
                logDao.insert(log)
                uploadLogToCloud(log)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkInVisitor(name: String, group: String, company: String, time: Long): Result<Unit> {
        return try {
            val person = PersonEntity(
                id = System.currentTimeMillis(),
                name = name,
                group = group,
                type = PersonType.VISITORS,
                isInside = true,
                lastEventAt = time
            )
            personDao.insert(person)
            uploadPersonToCloud(person)

            val log = LogEntity(
                personId = person.id,
                personName = person.name,
                type = PersonType.VISITORS,
                isCheckIn = true,
                timestamp = time
            )
            logDao.insert(log)
            uploadLogToCloud(log)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkInDriver(name: String, group: String, plate: String, time: Long): Result<Unit> {
        return try {
            val person = PersonEntity(
                id = System.currentTimeMillis(),
                name = name,
                group = group,
                type = PersonType.DRIVERS,
                isInside = true,
                lastEventAt = time
            )
            personDao.insert(person)
            uploadPersonToCloud(person)

            val log = LogEntity(
                personId = person.id,
                personName = person.name,
                type = PersonType.DRIVERS,
                isCheckIn = true,
                timestamp = time
            )
            logDao.insert(log)
            uploadLogToCloud(log)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun uploadPersonToCloud(person: PersonEntity) {
        syncCollection.document("person_${person.id}")
            .set(person)
            .addOnFailureListener { }
    }

    private fun uploadLogToCloud(log: LogEntity) {
        syncCollection.document("log_${log.id}")
            .set(log)
    }

    fun startRealtimeSync(scope: CoroutineScope) {
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
                            personDao.insertOrUpdate(person)
                        } else if (docId.startsWith("log_")) {
                            val log = data.toObject(LogEntity::class.java)
                            logDao.insertOrUpdate(log)
                        }
                    } catch (parseError: Exception) {
                        parseError.printStackTrace()
                    }
                }
            }
        }
    }

    fun stopRealtimeSync() {
        firestoreListener?.remove()
        firestoreListener = null
    }
}
