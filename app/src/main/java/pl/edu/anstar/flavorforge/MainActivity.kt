//package pl.edu.anstar.flavorforge
//
//import android.os.Bundle
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//}


//co5 s
package pl.edu.anstar.flavorforge

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val layouts = listOf(
        R.layout.activity_main,
        R.layout.activity_sign_in,
        R.layout.activity_sign_up,
        R.layout.drawer_menu,

    )

    private var index = 0
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startLoop()
    }

    private fun startLoop() {
        handler.post(object : Runnable {
            override fun run() {
                setContentView(layouts[index])

                index = (index + 1) % layouts.size

                handler.postDelayed(this, 5000) // 2 sekundy
            }
        })
    }
}
