package com.vikku.dailyschedules.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

 data class Habit(val id: Long, val name: String, val done: Boolean)
 data class ScheduleTask(val id: Long, val title: String, val time: String, val priority: String, val done: Boolean, val reminderAt: Long?)

class LocalDb(context: Context) : SQLiteOpenHelper(context, "vikku_daily.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE habits(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, day TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE tasks(id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, time TEXT NOT NULL, priority TEXT NOT NULL, day TEXT NOT NULL, done INTEGER NOT NULL DEFAULT 0, reminder_at INTEGER)")
        db.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT NOT NULL)")
        val defaults = listOf("Drink water", "Exercise", "Read 10 pages", "Meditate", "Sleep on time")
        defaults.forEach { name ->
            val cv = ContentValues().apply { put("name", name); put("day", today()); put("done", 0) }
            db.insert("habits", null, cv)
        }
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun habits(): List<Habit> = readableDatabase.rawQuery("SELECT id,name,done FROM habits WHERE day=? ORDER BY id", arrayOf(today())).use { c ->
        buildList { while (c.moveToNext()) add(Habit(c.getLong(0), c.getString(1), c.getInt(2) == 1)) }
    }
    fun addHabit(name: String) { val cv=ContentValues().apply { put("name",name); put("day",today()); put("done",0) }; writableDatabase.insert("habits",null,cv) }
    fun toggleHabit(id: Long, done: Boolean) { writableDatabase.execSQL("UPDATE habits SET done=? WHERE id=?", arrayOf(if(done) 1 else 0,id)) }
    fun tasks(): List<ScheduleTask> = readableDatabase.rawQuery("SELECT id,title,time,priority,done,reminder_at FROM tasks WHERE day=? ORDER BY time", arrayOf(today())).use { c ->
        buildList { while(c.moveToNext()) add(ScheduleTask(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getInt(4)==1,if(c.isNull(5)) null else c.getLong(5))) }
    }
    fun addTask(title: String, time: String, priority: String, reminderAt: Long?) { val cv=ContentValues().apply { put("title",title);put("time",time);put("priority",priority);put("day",today());put("done",0); if(reminderAt!=null) put("reminder_at",reminderAt) }; writableDatabase.insert("tasks",null,cv) }
    fun toggleTask(id: Long, done: Boolean) { writableDatabase.execSQL("UPDATE tasks SET done=? WHERE id=?", arrayOf(if(done) 1 else 0,id)) }
    fun deleteTask(id: Long) { writableDatabase.delete("tasks","id=?", arrayOf(id.toString())) }
    companion object { fun today(): String = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()) }
}
