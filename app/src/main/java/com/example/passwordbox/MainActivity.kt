package com.example.passwordbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class MainActivity : AppCompatActivity() {//регистрация
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    val fileName = File(applicationContext.filesDir, "password.txt")
    if (!fileName.exists()) {
        fileName.createNewFile()
        newpassword()
    } else {//если пароль уже создан
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val password = sharedPreferences.getString("password", null)
        chekpassword(password!!)
    }
    }




    private fun setupPasswordSaving() {//сохранение новых паролей
        setContentView(R.layout.activity_verify)
        val fileName = File(applicationContext.filesDir, "password.txt")
        val addNewButton = findViewById<Button>(R.id.addNewButton)

        listSaving()
        addNewButton.setOnClickListener {
            setContentView(R.layout.activity_addnew)

            val editText = findViewById<EditText>(R.id.editText)
            val editText1 = findViewById<EditText>(R.id.editText1)
            val editText2 = findViewById<EditText>(R.id.editText2)
            val editText3 = findViewById<EditText>(R.id.editText3)
            val saveButton = findViewById<Button>(R.id.saveButton)
            val cancelButton2 = findViewById<Button>(R.id.cancelButton2)

            saveButton.setOnClickListener {
                val password = editText3.text.toString()
                val login = editText2.text.toString()
                val url = editText1.text.toString()
                val name = editText.text.toString()

                fileName.appendText(name + "\n")
                fileName.appendText(url + "\n")
                fileName.appendText(login + "\n")
                fileName.appendText(password + "\n")

                editText.text.clear()
                editText1.text.clear()
                editText2.text.clear()
                editText3.text.clear()

                setupPasswordSaving()
            }
            cancelButton2.setOnClickListener {
                setupPasswordSaving()
            }

        }
    }




    private fun listSaving(){

        val listView = findViewById<ListView>(R.id.listView)
        val fileName = File(applicationContext.filesDir, "password.txt")
        val savedtext = fileName.readText()
        val arr = ArrayList<String>()

        arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr)

        //нажатие на элемент списка
        listView.setOnItemClickListener{parent,view,position,id->
            setContentView(R.layout.activity_edit)
            val cancelButton2 = findViewById<Button>(R.id.cancelButton2)
            val deleteButton2 = findViewById<Button>(R.id.deleteButton2)
            val editButton = findViewById<Button>(R.id.editButton)
            val listView1 = findViewById<ListView>(R.id.listView1)
            val arr1 = ArrayList<String>()

            arr1.addAll(arr.get(position).dropLast(1).split("\n"))
            listView1.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr1)//новый список(надо добавить отдельное редоктирование каждой строки)

            editButton.setOnClickListener{//изменение текста
                setContentView(R.layout.activity_addnew)

                val editText = findViewById<EditText>(R.id.editText)
                val editText1 = findViewById<EditText>(R.id.editText1)
                val editText2 = findViewById<EditText>(R.id.editText2)
                val editText3 = findViewById<EditText>(R.id.editText3)
                val saveButton = findViewById<Button>(R.id.saveButton)
                val cancelButton2 = findViewById<Button>(R.id.cancelButton2)

                editText.setText(arr1 [0])
                editText1.setText(arr1 [1])
                editText2.setText(arr1 [2])
                editText3.setText(arr1 [3])


                saveButton.setOnClickListener {//сохранение изменениний
                    val password = editText3.text.toString()
                    val login = editText2.text.toString()
                    val url = editText1.text.toString()
                    val name = editText.text.toString()

                    arr1 [0]=name
                    arr1 [1]=url
                    arr1 [2]=login
                    arr1 [3]=password

                    arr [position]=arr1 [0] +"\n"+ arr1 [1]+"\n"+ arr1 [2]+"\n"+ arr1 [3]+"\n"
                    PasswordFile(arr)

                    editText.text.clear()
                    editText1.text.clear()
                    editText2.text.clear()
                    editText3.text.clear()

                    setupPasswordSaving()
                }
                cancelButton2.setOnClickListener {
                    setupPasswordSaving()
                }

            }

            deleteButton2.setOnClickListener {//подтверждение удаления пункта списка
                setContentView(R.layout.activity_delete)

                val deleteButton = findViewById<Button>(R.id.deleteButton)
                val cancelButton = findViewById<Button>(R.id.cancelButton)
                val textView1=findViewById<TextView>(R.id.textView1)
                val textView2=findViewById<TextView>(R.id.textView2)

                textView1.text = "Do you want to delete?"
                textView2.text = arr.get(position).substringBefore("\n")

                deleteButton.setOnClickListener {
                    arr.removeAt(position)
                    PasswordFile(arr)
                    setupPasswordSaving()
                }
                cancelButton.setOnClickListener {
                    setupPasswordSaving()
                }
            }
            cancelButton2.setOnClickListener {
                setupPasswordSaving()
            }
            listView1.setOnItemLongClickListener {parent,view,position,id->//долгое нажатие что бы скопировать текст из ячейки списка
                copyText(arr1.get(position))
                true
            }

        }
    }




    private fun PasswordFile(arr: List<String>) {//сохранение изменений
        val fileName = File(applicationContext.filesDir, "password.txt")
        fileName.writeText(arr.joinToString(""))
    }




    private fun copyText(text: String){// копирование текста
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)

        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "text copy",Toast.LENGTH_SHORT).show()
    }



    private fun splitText(text: String, linesPerBlock: Int): List<String> {//разделение текста на блоки пароль логин ссылка и название
        val lines = text.split("\n")

        val result = mutableListOf<String>()
        var currentBlock = StringBuilder()

        lines.forEachIndexed { index, line ->
            currentBlock.append(line).append("\n")
            if ((index + 1) % linesPerBlock == 0 || index == lines.size - 1) {
                result.add(currentBlock.toString())
                currentBlock = StringBuilder()
            }
        }
        return result
    }



    private fun chekpassword(password1: String) {//проерка пароля
        setContentView(R.layout.activity_main)
        val etPwd = findViewById<EditText>(R.id.editTextNumberPassword)
        val btnCheckPassword = findViewById<Button>(R.id.button)
        btnCheckPassword.setOnClickListener {
            val text1 = etPwd.text.toString()
            if (text1 == password1) {

                setupPasswordSaving()
            }else {
                setContentView(R.layout.activity_false)
                val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)
                btnTryAgain.setOnClickListener {
                    chekpassword(password1)
                }
            }
        }
    }




    private fun newpassword() {//создание пароля
        setContentView(R.layout.activity_new)
        val etPwd2 = findViewById<EditText>(R.id.editTextNumberPassword2)
        val btnCheckPassword2 = findViewById<Button>(R.id.button2)

        btnCheckPassword2.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val password = etPwd2.text.toString()
            editor.putString("password", password)
            editor.apply()
            chekpassword(password)
        }
    }
}