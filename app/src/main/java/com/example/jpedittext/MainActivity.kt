package com.example.jpedittext

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.jpedittext.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.jpEditText.setUnderlinePrimaryColor(android.R.color.holo_orange_dark)
//        binding.jpEditText.setBottomTextSize(20f.dp2px(this))


        binding.jpEditText.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.jpEditText.error = if (binding.jpEditText.hasFocus() && (s!!.length < 8 || s!!.length > 16))
                    "8자 이상, 16자 이하로 부탁합니다."
                else
                    null
                binding.jpEditText.invalidate()
            }
        })
    }
}
