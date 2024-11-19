package com.example.passwordbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.*


class MainActivity : AppCompatActivity() {//регистрация
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fileName = File(applicationContext.filesDir, "password.txt")
        if (!fileName.exists()) {
            fileName.createNewFile()
            newPassword()
        } else {//если пароль уже создан
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val password = sharedPreferences.getString("password", "")
            chekPassword(password!!)
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
            val genButton = findViewById<Button>(R.id.genButton)

            genButton.setOnClickListener {
                editText3.setText(generatePassword())
            }

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
            val website = findViewById<Button>(R.id.website)
            val editButton = findViewById<Button>(R.id.editButton)
            val shareButton = findViewById<Button>(R.id.shareButton)
            val listView1 = findViewById<ListView>(R.id.listView1)
            val arr1 = ArrayList<String>()

            arr1.addAll(arr.get(position).dropLast(1).split("\n"))
            listView1.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, arr1)//новый список(надо добавить отдельное редоктирование каждой строки)

            website.setOnClickListener{
                openInBrowser(arr1 [1])
            }

            shareButton.setOnClickListener{
                share(arr1);
            }

            editButton.setOnClickListener{//изменение текста
                setContentView(R.layout.activity_addnew)

                val editText = findViewById<EditText>(R.id.editText)
                val editText1 = findViewById<EditText>(R.id.editText1)
                val editText2 = findViewById<EditText>(R.id.editText2)
                val editText3 = findViewById<EditText>(R.id.editText3)
                val saveButton = findViewById<Button>(R.id.saveButton)
                val cancelButton2 = findViewById<Button>(R.id.cancelButton2)
                val genButton = findViewById<Button>(R.id.genButton)

                genButton.setOnClickListener {
                    editText3.setText(generatePassword())
                }


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
                    passwordFile(arr)

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
                    passwordFile(arr)
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

    private fun share(arr1: List<String>){//вункция деления с помошью qr кода
        setContentView(R.layout.activity_qrcode)
        val qrIV = findViewById<ImageView>(R.id.IVQrcode)
        val cancelButton3 = findViewById<Button>(R.id.cancelButton3)
        val msgEdt=arr1.joinToString(separator = "\n")//преврашение масива в строку
        cancelButton3.setOnClickListener {//кнопка для закрытия qr rкода
            setupPasswordSaving()
        }
        try {

            val writer = QRCodeWriter()// Создается объект, который используется для генерации QR-кодов.
            val bitMatrix = writer.encode(msgEdt, BarcodeFormat.QR_CODE, 300, 300)// генрация матрицы qr кода
            val pixels = IntArray(300 * 300)//массив целых чисел для хранения цветов каждого пикселя изображения QR-кода

            for (y in 0 until 300) {// проходим по каждому элементу масива и определяем какого цвета должен быть пиксель
                val offset = y * 300
                for (x in 0 until 300) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)//бъект размером 300x300 пикселей с использованием конфигурации RGB_565 (каждый пиксель кодируется 16 битами).
            bitmap.setPixels(pixels, 0, 300, 0, 0, 300, 300)// из массива получаем картинку qr кода
            qrIV.setImageBitmap(bitmap)// отображение qr кода

        } catch (e: Exception) {//вывод ошибок
            e.printStackTrace()
        }
    }



    private fun passwordFile(arr: List<String>) {//сохранение изменений
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




    private fun chekPassword(password1: String) {//проерка пароля
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
                    chekPassword(password1)
                }
            }
        }
    }




    private fun newPassword() {//создание пароля
        setContentView(R.layout.activity_new)
        val etPwd2 = findViewById<EditText>(R.id.editTextNumberPassword2)
        val btnCheckPassword2 = findViewById<Button>(R.id.button2)

        btnCheckPassword2.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            val password = etPwd2.text.toString()
            editor.putString("password", password)
            editor.apply()
            chekPassword(password)
        }
    }



    private fun generatePassword(): String {//сгенерировать сложный пароль
        val rand = ('A'..'Z') + ('a'..'z') + ('0'..'9')+'!'+'#'+'$'+'%'+'&'+'/'+'@'
        var hardpass=""
        for(i in 0..20){
            hardpass=hardpass+rand.random()
        }
        return hardpass
    }



    fun openInBrowser(url: String) {//открыть ссылку в браузере
        val formattedUrl = if(url.contains("http://") ||url.contains("https://")){
            url
        }
        else{
            "http://"+url
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
        startActivity(browserIntent)
    }
}