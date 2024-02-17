package ru.sample.duckapp

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.sample.duckapp.domain.Duck
import ru.sample.duckapp.infra.Api
import java.io.ByteArrayInputStream

fun isValid(input: String): Boolean {
    return input.matches("^(|100|101|102|103|200|201|202|203|204|205|206|207|208|226|300|301|302|303|304|305|306|307|308|400|401|402|403|404|405|406|407|408|409|410|411|412|413|414|415|416|417|418|421|422|423|424|425|426|428|429|431|451|500|501|502|503|504|505|506|507|508|510|511)".toRegex())
}

class MainActivity : AppCompatActivity() {
    private lateinit var duckImageView: ImageView

    fun getDuck(){
        Api.ducksApi.getRandomDuck().enqueue(object : Callback<Duck> {
            override fun onResponse(call: Call<Duck>, response: Response<Duck>) {
                if (response.isSuccessful) {
                    val duck = response.body()
                    duck?.let {
                        Picasso.get().load(it.url).fit().centerInside().into(duckImageView)
                    }
                } else {
                    // Обработка ошибок, если запрос не был успешным
                    // Например, можно показать пользователю сообщение об ошибке
                }
            }

            override fun onFailure(call: Call<Duck>, t: Throwable) {
                // Обработка ошибок при выполнении запроса
                // Например, можно показать пользователю сообщение о проблеме с сетью
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val btn = findViewById<Button>(R.id.generateButton)
        duckImageView = findViewById(R.id.imageView)

        val duckCodeEditText = findViewById<EditText>(R.id.textInputEditText)
        duckCodeEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (isValid(s.toString())) {
                    duckCodeEditText.error = null
                    btn.isEnabled = true
                } else {
                    duckCodeEditText.error = "Error: Input correct http code!"
                    btn.isEnabled = false
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btn.setOnClickListener {
            val duckCode = duckCodeEditText.text.toString()
            if (duckCode.isNotEmpty()) {
                getCodeDuck(duckCode)
            } else {
                getDuck()
            }
        }
        duckCodeEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btn.performClick()
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                true
            } else {
                false
            }
        }
    }
    fun getCodeDuck(duckCode: String){
        Api.ducksApi.getCodeDuck(duckCode).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val duckByteArray = response.body()?.bytes()
                    duckByteArray?.let {
                        val inputStream = ByteArrayInputStream(it)
                        val bitmap =
                            BitmapFactory.decodeStream(inputStream)
                        duckImageView.setImageBitmap(bitmap)
                    }
                } else {
                    Toast.makeText(applicationContext, "Oops! No duck under that code", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // Обработка ошибок при выполнении запроса
                // Например, можно показать пользователю сообщение о проблеме с сетью
            }
        })
    }

}


