package padev.websocketandrest

import android.app.Activity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.google.gson.Gson
import io.socket.client.IO
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import javax.net.ssl.SSLContext


class HomeActivity: Activity() {

    private var jsonObject = JSONObject()

    inner class Data (var _hash: String,
                      var _size: String,
                      var _height: String,
                      var _time: String,
                      var _difficulty: String,
                      var _confirmation: String,
                      var _reward: String,
                      var _count: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        createTableRow("hash", "size", "height", "time", "difficulty", "confirmation", "reward", "count")

        val context = SSLContext.getDefault()

        val okHttpClient = OkHttpClient.Builder()
            .hostnameVerifier(MyHostnameVerifier())
            .sslSocketFactory(context.socketFactory, MyX509TrustManager())
            .build()

        IO.setDefaultOkHttpWebSocketFactory(okHttpClient)
        IO.setDefaultOkHttpCallFactory(okHttpClient)

        val opts = IO.Options()
        opts.callFactory = okHttpClient
        opts.webSocketFactory = okHttpClient
        
        val socket = IO.socket("https://insight.bitpay.com:443/", opts)
        socket.connect()

        socket.on("connect") {socket.emit("subscribe", "inv") }

        socket.on("tx") { args -> jsonObject = args[0] as JSONObject

                var hash = ""
                if (jsonObject.has("txid")) {
                    hash = jsonObject.getString("txid")

                    val data = getTransactionData(hash)

                    if (data != null) {

                        createTableRow (
                            roundingHash(data._hash),
                            data._size,
                            data._height,
                            data._time,
                            roundingDifficulty(data._difficulty),
                            data._confirmation,
                            data._reward,
                            data._count
                        )
                    }
                }
            }
    }

    private fun createTableRow(vararg params: String?) {

        synchronized(this) {
            runOnUiThread {
                val tableLayout = findViewById<TableLayout>(R.id.activity_home_table)

                layoutInflater.inflate(
                    R.layout.activity_home_table_row,
                    tableLayout,
                    true
                )

                val row = tableLayout.getChildAt(tableLayout.childCount - 1) as TableRow

                (0 until params.size).forEach { i ->
                    layoutInflater.inflate(
                        R.layout.activity_home_table_row_item, row,true
                    )

                    val rowItem = row.getChildAt(row.childCount - 1)
                        .findViewById<TextView>(R.id.activity_home_table_row_item)

                    rowItem.text = params[i]
                }
            }

        }
    }

    private fun getTransactionData(hash: String): Data? {
        synchronized(this) {
            var size = ""
            var height = ""
            var time = ""
            var difficulty = ""
            var confirmation = ""
            var reward = ""
            var count = ""

            val url = "https://insight.bitpay.com/api/tx/".plus(hash)

            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .build()
            var responses: Response? = null

            try {
                responses = client.newCall(request).execute()

            } catch (e: IOException) {
                e.printStackTrace()
            }

            val jsonData = responses?.body()?.string() ?: return null

            val gson = Gson()

            try {
                val fjobj = gson.fromJson(jsonData, FromJsonObject().javaClass)

                size = fjobj.size
                height = fjobj.blockheight
                time = fjobj.time
                difficulty = fjobj.difficulty
                confirmation = fjobj.confirmations
                count = fjobj.size

            }catch (e: Exception) {
                // if invalid json
                return null
            }

            return Data(hash, size, height, time, difficulty, confirmation, reward, count)
        }
    }

    private fun roundingDifficulty(difficulty: String): String {
        return difficulty.split(".")[0]
    }

    private fun roundingHash(hash: String): String {
        return hash.dropLast( hash.length-10)
    }
}