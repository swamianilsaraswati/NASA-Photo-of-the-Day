package com.example.nasaphotooftheday

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    var hdurl : String? = null
    var url : String? = null
    var title : String? = null
    var explanation : String? = null
    var mediatype : String? = null

    var imageTitle : TextView? = null
    var imageDescription : TextView? = null
    var playZoomButton : ImageView? = null
    var calendarButton : ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageTitle = findViewById(R.id.title)
        imageDescription = findViewById(R.id.description)
        playZoomButton = findViewById(R.id.playZoomButton)
        calendarButton = findViewById(R.id.calendarButton)

        val calendar= Calendar.getInstance()
        var currentYear = calendar.get(Calendar.YEAR)
        var month = calendar.get(Calendar.MONTH)
        var day = calendar.get(Calendar.DAY_OF_MONTH)

        calendarButton?.setOnClickListener{
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, DatePickerDialog.OnDateSetListener
                { view, year, monthOfYear, dayOfMonth ->
                    currentYear = year
                    month = monthOfYear
                    day = dayOfMonth
                    callNasaApi("&date=" + year + "-" + (monthOfYear + 1) + "-" + dayOfMonth)

                }, currentYear, month, day
                )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.updateDate(currentYear, month, day)

            datePickerDialog.show()
        }

        callNasaApi("")

        textViewScrollable()
    }

    private fun textViewScrollable() {
        description.setOnTouchListener(OnTouchListener { view, event ->
            if (view.id == R.id.description || view.id == R.id.title) {
                view.parent.requestDisallowInterceptTouchEvent(true)
                when (event.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> view.parent
                        .requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        })
    }

    fun callNasaApi(specificDate : String) {
        val queue = Volley.newRequestQueue(this)
        var url: String = "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY"+specificDate
        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->

                var strResp = response.toString()
                val jsonObj: JSONObject = JSONObject(strResp)
                title = jsonObj.get("title") as String
                explanation = jsonObj.get("explanation") as String
                mediatype = jsonObj.get("media_type") as String

                imageTitle?.text = title
                imageDescription?.text = explanation

                imageTitle!!.movementMethod = ScrollingMovementMethod()
                imageDescription!!.movementMethod = ScrollingMovementMethod()

                if(mediatype.equals("image")) {
                    loadImage(jsonObj)

                }else{
                    loadVideo(url, jsonObj)
                }
            },
            Response.ErrorListener { Toast.makeText(applicationContext,"Something went wrong.",Toast.LENGTH_LONG).show() })
        queue.add(stringReq)
    }

    private fun loadVideo(url: String, jsonObj: JSONObject) {
        var url1 = url
        url1 = jsonObj.get("url") as String
        playZoomButton?.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        Picasso.get().load("https://img.youtube.com/vi/" + extractYTId(url1) + "/0.jpg")
            .transform(RoundedCornersTransformation(30, 30))
            .placeholder(R.drawable.progress_animation).into(imageView);

        playZoomButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, FullScreenVideo::class.java)
            intent.putExtra("videoId", extractYTId(url1))
            startActivity(intent)
        })
    }

    private fun loadImage(jsonObj: JSONObject) {
        hdurl = jsonObj.get("hdurl") as String
        playZoomButton?.setImageResource(R.drawable.ic_baseline_zoom_in_24)
        Picasso.get().load(hdurl).transform(RoundedCornersTransformation(100, 30))
            .placeholder(R.drawable.progress_animation).error(R.drawable.ic_baseline_refresh_24)
            .into(imageView);

        playZoomButton?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, FullScreenPhoto::class.java)
            intent.putExtra("hdurl", hdurl)
            startActivity(intent)
        })
    }

    fun extractYTId(ytUrl: String?): String? {
        var vId: String? = null
        val pattern: Pattern = Pattern.compile(
            "^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
            Pattern.CASE_INSENSITIVE
        )
        val matcher: Matcher = pattern.matcher(ytUrl)
        if (matcher.matches()) {
            vId = matcher.group(1)
        }
        return vId
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.licences){
            val intent = Intent(this, LicencesActivity::class.java)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}
