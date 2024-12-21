package com.example.passwordbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import kotlin.collections.ArrayList


//        val KeyManager= KeyManager(keyAlias())
//
//        fileName.writeText(KeyManager.encrypt(fileName.readText()))// зашифровка
//        fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка

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
            if (password == "") {
                newPassword()
            }else{
                chekPassword(password!!,0)
            }

        }
    }


    private fun keyAlias():String {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val keyAlias = sharedPreferences.getString("password", "")
        return keyAlias!!
    }




    private fun setupPasswordSaving() {//сохранение новых паролей
        setContentView(R.layout.activity_verify)
        val fileName = File(applicationContext.filesDir, "password.txt")
        val addNewButton = findViewById<ImageButton>(R.id.addNewButton)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)


        listSaving()
        settingsButton.setOnClickListener {
            settings()
        }

        addNewButton.setOnClickListener {
            setContentView(R.layout.activity_addnew)

            val editText = findViewById<EditText>(R.id.editText)
            val editText1 = findViewById<EditText>(R.id.editText1)
            val editText2 = findViewById<EditText>(R.id.editText2)
            val editText3 = findViewById<EditText>(R.id.editText3)
            val saveButton = findViewById<ImageButton>(R.id.saveButton)
            val cancelButton2 = findViewById<ImageButton>(R.id.cancelButton2)
            val genButton = findViewById<ImageButton>(R.id.genButton)

            genButton.setOnClickListener {
                editText3.setText(generatePassword())
            }

            saveButton.setOnClickListener {
                val password = editText3.text.toString()
                val login = editText2.text.toString()
                val url = editText1.text.toString()
                val name = editText.text.toString()

                val KeyManager= KeyManager(keyAlias())
                fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка

                fileName.appendText(name + "\n")
                fileName.appendText(url + "\n")
                fileName.appendText(login + "\n")
                fileName.appendText(password + "\n")

                fileName.writeText(KeyManager.encrypt(fileName.readText()))//шифровка

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


    private fun settings() {//настройки
        setContentView(R.layout.activity_settings)
        val cancelButton = findViewById<ImageButton>(R.id.cancelImageButton)
        val uncButton=findViewById<Button>(R.id.uncButton)
        val pcButton=findViewById<Button>(R.id.pcButton)
        val wipeDataButton=findViewById<Button>(R.id.wipeDataButton)

        cancelButton.setOnClickListener {
            setupPasswordSaving()
        }
        uncButton.setOnClickListener {
            setContentView(R.layout.activity_unc)
            val editTextUnc = findViewById<EditText>(R.id.editTextUnc)
            val saveButtonUnc = findViewById<ImageButton>(R.id.saveButtonunc)
            val cancelButtonUnc = findViewById<ImageButton>(R.id.cancelButtonunc)

            saveButtonUnc.setOnClickListener {
                val name = editTextUnc.text.toString()
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("name", name)
                editor.apply()
                setupPasswordSaving()
            }
            cancelButtonUnc.setOnClickListener {
                settings()
            }

        }
        pcButton.setOnClickListener {
            setContentView(R.layout.activity_unc)
            val editTextUnc = findViewById<EditText>(R.id.editTextUnc)
            val saveButtonUnc = findViewById<ImageButton>(R.id.saveButtonunc)
            val cancelButtonUnc = findViewById<ImageButton>(R.id.cancelButtonunc)
            val textView7=findViewById<TextView>(R.id.textView7)

            editTextUnc.setHint("new password")
            textView7.text="write a new password"

            saveButtonUnc.setOnClickListener {
                val fileName = File(applicationContext.filesDir, "password.txt")
                val KeyManager= KeyManager(keyAlias())
                fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка
                val password = editTextUnc.text.toString()
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("password", password)
                editor.apply()
                val KeyManager2= KeyManager(keyAlias())
                fileName.writeText(KeyManager2.encrypt(fileName.readText()))//шифровка
                setupPasswordSaving()
            }
            cancelButtonUnc.setOnClickListener {
                settings()
            }
        }
        wipeDataButton.setOnClickListener {
            setContentView(R.layout.activity_main)
            val etPwd = findViewById<EditText>(R.id.editTextNumberPassword)
            val btnCheckPassword = findViewById<Button>(R.id.button)
            val hello = findViewById<TextView>(R.id.helloWorldText)
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val name = sharedPreferences.getString("name", "my Lord")
            val password1 = sharedPreferences.getString("password", "my Lord")
            hello.setTextColor(Color.parseColor("#FA0000"));
            btnCheckPassword.setTextColor(Color.parseColor("#FA0000"));
            btnCheckPassword.text="Wipe data"
            hello.text = "$name, you sure?"
            btnCheckPassword.setOnClickListener {
                val text1 = etPwd.text.toString()
                if (text1 == password1) {
                    deleteAppData()
                }else {
                    settings()
                }
            }
        }

    }




    private fun listSaving(){

        val listView = findViewById<ListView>(R.id.listView)
        val fileName = File(applicationContext.filesDir, "password.txt")
        var arr = ArrayList<String>()

        val KeyManager= KeyManager(keyAlias())
        fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка

        val savedtext = fileName.readText()
        arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка

        var arr2=hidePassword(arr)
        listView.adapter = ArrayAdapter(this, R.layout.navigation_item, arr2)

        fileName.writeText(KeyManager.encrypt(fileName.readText()))//шифровка

        val spinner: Spinner = findViewById(R.id.spinner)
        val items = listOf("date","date(reverse)","name","name(reverse)")
        val adapterSpiner = ArrayAdapter(this, R.layout.spinner_item, items)
        spinner.adapter = adapterSpiner

        spinner.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                if(position==0){
                    arr.clear()
                    arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка
                    arr2=hidePassword(arr)
                    if(arr2.size>1){
                        listView.adapter = ArrayAdapter(this@MainActivity, R.layout.navigation_item, arr2)
                    }
                }
                if(position==1){
                    arr.clear()
                    arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка
                    arr2=hidePassword(arr)
                    if(arr2.size>1){
                        arr2= arr2.reversed() as ArrayList<String>
                        arr= arr.reversed() as ArrayList<String>
                        listView.adapter = ArrayAdapter(this@MainActivity, R.layout.navigation_item, arr2)
                    }

                }
                if(position==2){
                    arr.clear()
                    arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка
                    arr2=hidePassword(arr)
                    if(arr2.size>1){
                        arr2= arr2.sorted().reversed().reversed() as ArrayList<String>
                        arr= arr.sorted().reversed().reversed() as ArrayList<String>
                        listView.adapter = ArrayAdapter(this@MainActivity, R.layout.navigation_item, arr2)
                    }
                }
                if(position==3){
                    arr.clear()
                    arr.addAll(splitText(savedtext,4).filter { it.isNotBlank() })//создание списка
                    arr2=hidePassword(arr)
                    if(arr2.size>1){
                        arr2= arr2.sorted().reversed() as ArrayList<String>
                        arr= arr.sorted().reversed().reversed() as ArrayList<String>
                        listView.adapter = ArrayAdapter(this@MainActivity, R.layout.navigation_item, arr2)
                    }

                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
            }
        })

        //нажатие на элемент списка
        listView.setOnItemClickListener{parent,view,position,id->
            setContentView(R.layout.activity_edit)
            val cancelButton2 = findViewById<ImageButton>(R.id.cancelButton2)
            val deleteButton2 = findViewById<ImageButton>(R.id.deleteButton2)
            val website = findViewById<ImageButton>(R.id.website)
            val editButton = findViewById<ImageButton>(R.id.editButton)
            val shareButton = findViewById<ImageButton>(R.id.shareButton)
            val showButton = findViewById<ImageButton>(R.id.showButton)
            val shareButton2 = findViewById<ImageButton>(R.id.shareButton2)
            val listView1 = findViewById<ListView>(R.id.listView1)
            val arr1 = ArrayList<String>()
            val arr3 = ArrayList<String>()
            var flag =true;

            arr1.addAll(arr.get(position).dropLast(1).split("\n"))
            arr3.addAll(arr2.get(position).dropLast(1).split("\n"))

            listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr3)

            showButton.setOnClickListener {
                flag=!flag;

                if(flag){
                    listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr3)
                    showButton.setImageResource(R.drawable.show)
                }else{
                    listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr1)
                    showButton.setImageResource(R.drawable.notshow)
                }
            }
            shareButton2.setOnClickListener{
                shareMessage(this, arrToTextShare(arr1))
            }

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
                val saveButton = findViewById<ImageButton>(R.id.saveButton)
                val cancelButton2 = findViewById<ImageButton>(R.id.cancelButton2)
                val genButton = findViewById<ImageButton>(R.id.genButton)

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

                val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
                val cancelButton = findViewById<ImageButton>(R.id.cancelButton)
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
        val cancelButton3 = findViewById<ImageButton>(R.id.cancelButton3)
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
        val KeyManager= KeyManager(keyAlias())
        val fileName = File(applicationContext.filesDir, "password.txt")
        fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка

        fileName.writeText(arr.joinToString(""))
        fileName.writeText(KeyManager.encrypt(fileName.readText()))//шифровка
    }

    private fun hidePassword(arr: List<String>):List<String> {//скрытие пароля
        val arr2 = ArrayList<String>()
        if (arr.size==0){
            return arr
        }else{
            for(i in 0..arr.size-1){
                val arr3 = ArrayList<String>()
                arr3.addAll(arr.get(i).dropLast(1).split("\n"))
                arr3.removeAt(3)
                arr3.add("********")
                arr2.add(arr3.joinToString("\n"))
            }

            return arr2
        }
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




    private fun chekPassword(password1: String, i: Int) {//проверка пароля
        setContentView(R.layout.activity_main)
        val etPwd = findViewById<EditText>(R.id.editTextNumberPassword)
        val btnCheckPassword = findViewById<Button>(R.id.button)
        val hello = findViewById<TextView>(R.id.helloWorldText)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "my Lord")
        hello.text = "Hello, $name"
        btnCheckPassword.setOnClickListener {
            val text1 = etPwd.text.toString()
            if (text1 == password1) {
                setupPasswordSaving()
            }else {
                setContentView(R.layout.activity_false)
                val btnTryAgain = findViewById<Button>(R.id.btnTryAgain)
                btnTryAgain.setOnClickListener {
                    if (i<4){
                        chekPassword(password1,i+1)
                    }else{
                        Toast.makeText(this, "to many password attempts",Toast.LENGTH_SHORT).show()
                        deleteAppData()
                    }
                }
            }
        }
    }


    private fun arrToTextShare(arr: List<String>):String{
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "my Lord")
        val text="Информация из приложения Password box:\n" +
                "От пользователя: $name\n" +
                "Название: ${arr[0]}\n" +
                "Логин: ${arr[1]}\n" +
                "Пароль: ${arr[2]}\n" +
                "Ссылка: ${arr[3]}"

        return text
    }



    private fun deleteAppData() {
        val packageName = applicationContext.packageName
        val runtime = Runtime.getRuntime()
        runtime.exec("pm clear $packageName")
    }





    private fun newPassword() {//создание пароля
        setContentView(R.layout.activity_new)
        val etPwd2 = findViewById<EditText>(R.id.editTextNumberPassword2)
        val btnCheckPassword2 = findViewById<Button>(R.id.button2)
        val userName=findViewById<EditText>(R.id.editTextTextuName)

        btnCheckPassword2.setOnClickListener {
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val password = etPwd2.text.toString()
            val name=userName.text.toString()
            if(password==""){
                Toast.makeText(this, "your password is too simple",Toast.LENGTH_SHORT).show()
                newPassword()
            }else{
                editor.putString("name", name)
                editor.putString("password", password)
                editor.apply()

                val KeyManager= KeyManager(keyAlias())
                val fileName = File(applicationContext.filesDir, "password.txt")
                fileName.writeText(KeyManager.encrypt(fileName.readText()))//шифровка

                setupPasswordSaving()
            }
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


    fun shareMessage(context: Context, message: String) {
        if (message.isNotEmpty()) {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Отправить сообщение через")
            context.startActivity(shareIntent)
        } else {
            Toast.makeText(context, "Сообщение не может быть пустым", Toast.LENGTH_SHORT).show()
        }
    }
}