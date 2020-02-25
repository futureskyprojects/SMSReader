package vn.vistark.smsreader.data.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import vn.vistark.smsreader.model.Msg

class MsgHandler(context: Context) : SQLiteOpenHelper(context, "MSGER", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(
            "CREATE TABLE ${Msg.TABLE_NAME} (" +
                    "${Msg.COL_ID} INTEGER PRIMARY KEY," +
                    "${Msg.COL_PHONE} TEXT," +
                    "${Msg.COL_MSG} TEXT" +
                    ");"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS ${Msg.TABLE_NAME}")
        onCreate(db)
    }

    fun add(msg: Msg): Long {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(Msg.COL_ID, msg.id)
        cv.put(Msg.COL_PHONE, msg.phone)
        cv.put(Msg.COL_MSG, msg.msg)

        val res = db.insert(Msg.TABLE_NAME, null, cv)
        db.close()
        return res
    }

    fun getOne(): Msg? {
        val db = this.readableDatabase
        var msg: Msg? = null
        val cursor = db.rawQuery(
            "SELECT * FROM ${Msg.TABLE_NAME} ORDER BY ${Msg.COL_ID} ASC LIMIT 1",
            emptyArray()
        )
        if (cursor != null) {
            cursor.moveToFirst()
            msg = Msg(
                cursor.getLong(0),
                cursor.getString(1),
                cursor.getString(2)
            )
        }
        cursor.close()
        db.close()
        return msg
    }

    fun getAll(): ArrayList<Msg> {
        val res = ArrayList<Msg>()

        val db = this.readableDatabase
        val cursor = db.query(Msg.TABLE_NAME, null, null, emptyArray(), null, null, null)
        if (cursor != null) {
            while (cursor.moveToNext()) {
                val msg = Msg(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
                res.add(msg)
            }
        }
        cursor.close()
        db.close()
        return res
    }

    fun remove(msgId: Long): Int {
        val db = this.writableDatabase
        val res = db.delete(Msg.TABLE_NAME, "${Msg.COL_ID}=?", arrayOf(msgId.toString()))
        db.close()
        return res
    }
}