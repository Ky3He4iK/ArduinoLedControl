package dev.ky3he4ik.ledusbdriver


import android.os.Bundle
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener


class MainActivity : AppCompatActivity(), ColorPickerDialogListener {
    private var colorsViews: Array<ColorPanelView> = arrayOf()
    private var currColor: ColorPanelView? = null
    private var transmitter = Transmitter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        colorsViews = arrayOf(
            findViewById(R.id.color_0),
            findViewById(R.id.color_1),
            findViewById(R.id.color_2),
            findViewById(R.id.color_3),
            findViewById(R.id.color_4),
            findViewById(R.id.color_5),
            findViewById(R.id.color_6),
            findViewById(R.id.color_7),
            findViewById(R.id.color_8),
            findViewById(R.id.color_9),
            findViewById(R.id.color_10),
            findViewById(R.id.color_11),
            findViewById(R.id.color_12),
            findViewById(R.id.color_13),
            findViewById(R.id.color_14),
            findViewById(R.id.color_15)
        )
//        val textView = findViewById<TextView>(R.id.text)
//        colorView = findViewById(R.id.color_0)
        val colors = arrayOf(
            0xFFFFFF, 0x000000, 0xFFF000, 0x000FFF, 0x0000FF, 0x00FFFF, 0x00FF00, 0xFFFF00,
            0xFFFFFF, 0xFF00FF, 0x0000FF, 0x00FFFF, 0x00FF00, 0xFFFF00, 0xFF0000, 0xFFFF00
        )
        for ((i, colorView) in colorsViews.withIndex()) {
            colorView.color = colors[i] + 0xFF000000.toInt()

            colorView.setOnClickListener {
                currColor = colorView
                ColorPickerDialog.newBuilder().setColor(colorView.color).show(this)
            }
        }

        findViewById<Button>(R.id.connect).setOnClickListener {
            connect()
        }

        findViewById<Button>(R.id.btn_p1).setOnClickListener {
            val arr = ArrayList<Int>()
            arr.add(colorsViews[0].color)
            transmitter.setPalette(arr)
        }
        findViewById<Button>(R.id.btn_p2).setOnClickListener {
            val arr = ArrayList<Int>()
            for (i in 0..1)
                arr.add(colorsViews[i].color)
            transmitter.setPalette(arr)
        }
        findViewById<Button>(R.id.btn_p4).setOnClickListener {
            val arr = ArrayList<Int>()
            for (i in 0..3)
                arr.add(colorsViews[i].color)
            transmitter.setPalette(arr)
        }
        findViewById<Button>(R.id.btn_p8).setOnClickListener {
            val arr = ArrayList<Int>()
            for (i in 0..7)
                arr.add(colorsViews[i].color)
            transmitter.setPalette(arr)
        }
        findViewById<Button>(R.id.btn_p16).setOnClickListener {
            val arr = ArrayList<Int>()
            for (i in 0..15)
                arr.add(colorsViews[i].color)
            transmitter.setPalette(arr)
        }
        findViewById<SeekBar>(R.id.bright_seek).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            var pChange = 0
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                //Perform action to store the progresschange
                pChange = progress
                findViewById<TextView>(R.id.bright_num).text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transmitter.setBrightness(pChange)
            }
        })
        findViewById<SeekBar>(R.id.fps_seek).setOnSeekBarChangeListener(object :
            OnSeekBarChangeListener {
            var pChange = 0
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                //Perform action to store the progresschange
                pChange = progress
                findViewById<EditText>(R.id.fps_num).setText(progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                transmitter.setFps(pChange)
            }
        })
        findViewById<EditText>(R.id.bright_num).addTextChangedListener {
            if (it == null)
                return@addTextChangedListener
            val data = it.substring(0, it.length).toIntOrNull() ?: return@addTextChangedListener
            findViewById<SeekBar>(R.id.bright_seek).progress = data
            transmitter.setBrightness(data)
        }
        findViewById<EditText>(R.id.fps_num).addTextChangedListener {
            if (it == null)
                return@addTextChangedListener
            val data = it.substring(0, it.length).toIntOrNull() ?: return@addTextChangedListener
            findViewById<SeekBar>(R.id.fps_seek).progress = data
            transmitter.setFps(data)
        }

        findViewById<Button>(R.id.pause).setOnClickListener {
            transmitter.pause()
        }
        findViewById<Button>(R.id.off).setOnClickListener {
            transmitter.off()
        }

        findViewById<Button>(R.id.info_bnt).setOnClickListener {
            transmitter.requestInfo()
        }

        findViewById<EditText>(R.id.series).addTextChangedListener {
            if (it == null)
                return@addTextChangedListener
            val data = it.substring(0, it.length).toIntOrNull() ?: return@addTextChangedListener
            transmitter.setServes(data)
        }
        findViewById<EditText>(R.id.step).addTextChangedListener {
            if (it == null)
                return@addTextChangedListener
            val data = it.substring(0, it.length).toIntOrNull() ?: return@addTextChangedListener
            transmitter.setStep(data)
        }

        for ((i, id) in arrayOf(
            R.id.btn_pr0, R.id.btn_pr1, R.id.btn_pr2, R.id.btn_pr3,
            R.id.btn_pr4, R.id.btn_pr5, R.id.btn_pr6, R.id.btn_pr7,
            R.id.btn_pr8, R.id.btn_pr9, R.id.btn_pr10, R.id.btn_pr11,
            R.id.btn_pr12, R.id.btn_pr13, R.id.btn_pr14, R.id.btn_pr15
        ).withIndex()) {
            findViewById<Button>(id).setOnClickListener {
                transmitter.setPalettePredefined(i)
            }
        }

        findViewById<Switch>(R.id.groupSwitch).setOnCheckedChangeListener { _, isChecked ->
            transmitter.isHead = isChecked
        }
        findViewById<Switch>(R.id.safePalette).setOnCheckedChangeListener { _, isChecked ->
            transmitter.isSafe = isChecked
        }

        findViewById<Button>(R.id.reset).setOnClickListener { transmitter.reset() }

        transmitter.start()
    }

    private fun connect() {
        // Find all available drivers from attached devices.
        findViewById<TextView>(R.id.text).text = "Conecting..."
        transmitter.connect()
    }

    override fun onDialogDismissed(dialogId: Int) {
        currColor = null
    }

    override fun onColorSelected(dialogId: Int, color: Int) {
        val color = color and 0x00FFFFFF
        for ((i, col) in colorsViews.withIndex()) {
            if (col.id == currColor?.id) {
                colorsViews[i].color = color
                transmitter.setColor(i, color)
            }
        }
    }

    fun onInfoGot(info: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.info_text).text = info //.replace(' ', '\n')
        }
    }

    fun onConnect() {
        runOnUiThread {
            findViewById<TextView>(R.id.text).text = "Conected"
        }
    }

    override fun onDestroy() {
        transmitter.running = false
        super.onDestroy()
    }
}
