package dev.ky3he4ik.ledusbdriver

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.nio.charset.Charset

class Transmitter(private val callback: MainActivity) : Thread() {
    private var port: UsbSerialPort? = null
    var running = true
    var isHead = false
    var isSafe = true
    private val response = ByteArray(300)
    private var wasTalk = false
    private var setBr = -1
    private var setFp = -1
    private var setPau = false
    private var setOf = false
    private var setSt = -1
    private var setSe = -1
    private var setIn = false
    private var setPal: Array<Array<Int>> = arrayOf()
    private val newColors: MutableMap<Int, ByteArray?> = mutableMapOf()
    private var setCol: ByteArray? = null
    private var setPalRnd = false
    private var setPalPre = -1
    private var isCon = false

    fun setColor(ind: Int, color: Int) {
        val r = ((color shr 16) and 0xFF).toByte()
        val g = ((color shr 8) and 0xFF).toByte()
        val b = (color and 0xFF).toByte()
        newColors[ind] = byteArrayOf(r, g, b)
        Log.d("Transmitter/setColor", "New color n $ind: $color ($r $g $b)")
    }

    fun setPalette(palette: ArrayList<Int>) {
        if (palette.size == 1)
            setCol = byteArrayOf(
                ((palette[0] shr 16) and 0xFF).toByte(),
                ((palette[0] shr 8) and 0xFF).toByte(),
                (palette[0] and 0xFF).toByte()
            )
        else if (isSafe)
            for (i in 0..15) {
                val color = palette[i % palette.size]
                newColors[i] = byteArrayOf(
                    ((color shr 16) and 0xFF).toByte(),
                    ((color shr 8) and 0xFF).toByte(),
                    (color and 0xFF).toByte()
                )
            }
        else
            setPal = Array(16) {
                val color = palette[it % palette.size]
                arrayOf(
                    (color shr 16) and 0xFF,
                    (color shr 8) and 0xFF,
                    color and 0xFF
                )
            }
    }

    fun setBrightness(brightness: Int) {
        setBr = brightness
    }

    fun setFps(fps: Int) {
        setFp = fps
    }

    fun pause() {
        setPau = true
    }

    fun off() {
        setOf = true
    }

    fun setStep(step: Int) {
        setSt = step
    }

    fun setServes(serves: Int) {
        setSe = serves
    }

    fun setPaletteRandom() {
        setPalRnd = true
    }

    fun setPalettePredefined(palette: Int) {
        setPalPre = palette
    }

    fun requestInfo() {
        setIn = true
    }

    fun isOpen() = port?.isOpen ?: false

    fun connect() {
        isCon = true
    }

    fun reset() {
        wasTalk = false
        setBr = -1
        setFp = -1
        setPau = false
        setOf = false
        setSt = -1
        setSe = -1
        setIn = false
        for (i in 0..15)
            newColors[i] = null
    }

    override fun run() {
        while (running) {
            sleep(100)
            if (isCon) {
                port = null
                val manager = callback.getSystemService(Context.USB_SERVICE) as UsbManager
                val availableDrivers: List<UsbSerialDriver> =
                    UsbSerialProber.getDefaultProber().findAllDrivers(manager)
                if (availableDrivers.isEmpty()) {
                    return
                }

                // Open a connection to the first available driver.

                // Open a connection to the first available driver.
                val driver: UsbSerialDriver = availableDrivers[0]
                val connection = manager.openDevice(driver.device)
                    ?: // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
                    return

                port = driver.ports[0] // Most devices have just one port (port 0)

                port?.open(connection)
                port?.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                isCon = false
                callback.onConnect()
            }
            if (port != null) {
                wasTalk = false
                if (setBr >= 0 && sendData('b', setBr.toChar()))
                    setBr = -1
                else if (setFp >= 0 && sendData('f', (setFp + 'A'.toByte()).toChar()))
                    setFp = -1
                else if (setSt >= 0 && sendData('t', (setSt + 'A'.toByte()).toChar()))
                    setSt = -1
                else if (setSe >= 0 && sendData('s', (setSe + 'A'.toByte()).toChar()))
                    setSe = -1
                else if (setPau && sendData('a', '_'))
                    setPau = false
                else if (setOf && sendData('o', '_'))
                    setOf = false
                else if (setIn && sendData('i', '_'))
                    setIn = false
                else if (setPalRnd && sendData('p', 'a'))
                    setPalRnd = false
                else if (setPalPre >= 0 && sendData('p', (setPalPre + 'A'.toByte()).toChar()))
                    setPalPre = -1
                else if (setCol != null && sendDataLong(
                        byteArrayOf('c'.toByte()),
                        setCol ?: continue
                    )
                ) {
                    setCol = null
                } else if (setPal.size == 16 && sendDataLong("pb".toByteArray(), ByteArray(16 * 3) {
                        setPal[it / 3][it % 3].toByte()
                    }))
                    setPal = arrayOf()
                else
                    for (i in 0..15) {
                        val ncv = newColors[i] ?: continue
                        if (ncv.isNotEmpty()) {
                            sendDataLong(byteArrayOf('n'.toByte()), ByteArray(4) {
                                if (it == 0)
                                    i.toByte()
                                else
                                    ncv[it - 1]
                            })
                            newColors[i] = null
                            break
                        }
                    }
                if (wasTalk) {
                    callback.onInfoGot(response.toString(Charset.defaultCharset()))
                }
            }
        }
    }

    private fun sendData(cmd: Char, data: Char): Boolean {
        val port = port ?: return false
        var cmd_ = cmd
        if (isHead)
            cmd_ -= ('a' - 'A')

        port.write(byteArrayOf(cmd_.toByte()), 2000)
        val l = port.read(response, 2000)
        if (l > 0) {
            port.write(byteArrayOf(data.toByte()), 2000)
            port.read(response, 2000)
//            sleep(500)
            wasTalk = true
            return true
        }
        return false
    }

    private fun sendDataLong(cmd: ByteArray, data: ByteArray): Boolean {
        if (cmd.isEmpty())
            return false
        val port = port ?: return false
        if (isHead)
            cmd[0] = (cmd[0] - ('a' - 'A').toByte()).toByte()
        val cmdRest = ByteArray(cmd.size - 1) { cmd[it + 1] }

        port.write(byteArrayOf(cmd[0]), 2000)

        val l = port.read(response, 2000)

        if (l > 0) {
            if (cmdRest.isNotEmpty())
                port.write(cmdRest, 2000)
            port.write(data, 2000)
//            sleep(500)
            port.read(response, 2000)
            wasTalk = true
            return true
        }

        return false
    }
}
