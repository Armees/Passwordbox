package com.example.passwordbox

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
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
import androidx.appcompat.app.AppCompatDelegate
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File
import kotlin.collections.ArrayList
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.zxing.EncodeHintType


//        val KeyManager= KeyManager(keyAlias())
//
//        fileName.writeText(KeyManager.encrypt(fileName.readText()))// зашифровка
//        fileName.writeText(KeyManager.decrypt(fileName.readText()))//расшифровка

class MainActivity : AppCompatActivity() {//регистрация
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPreferences = getSharedPreferences("AppThemePrefs", MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme", "System")

        when (savedTheme) {
            "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

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




    private fun setupPasswordSaving() {
        setContentView(R.layout.activity_verify)
        val fileName = File(applicationContext.filesDir, "password.txt")
        val addNewButton = findViewById<ImageButton>(R.id.addNewButton)
        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)

        listSaving()
        settingsButton.setOnClickListener { settings() }
        addNewButton.setOnClickListener {setupAddNewButton( fileName)}
    }




    private fun setupAddNewButton( fileName: File) {
        setContentView(R.layout.activity_addnew)

        val editText = findViewById<EditText>(R.id.editText)
        val editText1 = findViewById<EditText>(R.id.editText1)
        val editText2 = findViewById<EditText>(R.id.editText2)
        val editText3 = findViewById<EditText>(R.id.editText3)
        val saveButton = findViewById<ImageButton>(R.id.saveButton)
        val cancelButton2 = findViewById<ImageButton>(R.id.cancelButton2)
        val genButton = findViewById<ImageButton>(R.id.genButton)

        genButton.setOnClickListener {editText3.setText(generatePassword())}
        saveButton.setOnClickListener {setupSaveButton(editText, editText1, editText2, editText3, fileName)}
        cancelButton2.setOnClickListener {setupPasswordSaving()}
    }




    private fun setupSaveButton(
        editText: EditText,
        editText1: EditText,
        editText2: EditText,
        editText3: EditText,
        fileName: File
    ) {
        val name = editText.text.toString()
        val url = editText1.text.toString()
        val login = editText2.text.toString()
        val password = editText3.text.toString()

        val keyManager = KeyManager(keyAlias())
        val decryptedData = keyManager.decrypt(fileName.readText())
        fileName.writeText(decryptedData)

        fileName.appendText("$name\n$url\n$login\n$password\n")

        val encryptedData = keyManager.encrypt(fileName.readText())
        fileName.writeText(encryptedData)

        clearFields(editText, editText1, editText2, editText3)
        setupPasswordSaving()
    }




    private fun clearFields(vararg fields: EditText) {
        for (field in fields) {
            field.text.clear()
        }
    }




    private fun settings() {
        setContentView(R.layout.activity_settings)

        val cancelButton = findViewById<ImageButton>(R.id.cancelImageButton)
        val uncButton = findViewById<Button>(R.id.uncButton)
        val pcButton = findViewById<Button>(R.id.pcButton)
        val wipeDataButton = findViewById<Button>(R.id.wipeDataButton)

        setupThemeSpinner()
        cancelButton.setOnClickListener { setupPasswordSaving() }
        uncButton.setOnClickListener { openNameChangeScreen() }
        pcButton.setOnClickListener { openPasswordChangeScreen() }
        wipeDataButton.setOnClickListener { openWipeConfirmationScreen() }
    }




    private fun setupThemeSpinner() {
        val sharedPreferences = getSharedPreferences("AppThemePrefs", MODE_PRIVATE)
        val savedTheme = sharedPreferences.getString("theme", "System")
        val items = listOf("System", "Light", "Dark").sortedBy { if (it == savedTheme) 0 else 1 }

        val spinner = findViewById<Spinner>(R.id.spinner2)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, items)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = items[position]
                when (selected) {
                    "System" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                    "Light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "Dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
                saveTheme(selected, sharedPreferences)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }




    private fun saveTheme(theme: String, sharedPreferences: SharedPreferences) {
        val editor = sharedPreferences.edit()
        editor.putString("theme", theme)
        editor.apply()
    }




    private fun openNameChangeScreen() {
        setContentView(R.layout.activity_unc)

        val editText = findViewById<EditText>(R.id.editTextUnc)
        val saveButton = findViewById<ImageButton>(R.id.saveButtonunc)
        val cancelButton = findViewById<ImageButton>(R.id.cancelButtonunc)

        saveButton.setOnClickListener {
            val name = editText.text.toString()
            val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE).edit()
            prefs.putString("name", name).apply()
            setupPasswordSaving()
        }

        cancelButton.setOnClickListener { settings() }
    }




    private fun openPasswordChangeScreen() {
        setContentView(R.layout.activity_wipedata)

        val etPwd = findViewById<EditText>(R.id.editTextNumberPasswordWipe)
        val btnCheck = findViewById<Button>(R.id.buttonWipe)
        val cancelBtn = findViewById<ImageButton>(R.id.cancelButtonWipe)
        val helloText = findViewById<TextView>(R.id.helloWorldText1)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "my Lord")
        val currentPassword = prefs.getString("password", "my Lord")

        helloText.text = "$name, enter password"
        btnCheck.text = "login"

        btnCheck.setOnClickListener {
            if (etPwd.text.toString() == currentPassword) {
                openNewPasswordEntry()
            } else {
                settings()
            }
        }

        cancelBtn.setOnClickListener { settings() }
    }




    private fun openNewPasswordEntry() {
        setContentView(R.layout.activity_unc)
        val editTextUnc = findViewById<EditText>(R.id.editTextUnc)
        val saveButtonUnc = findViewById<ImageButton>(R.id.saveButtonunc)
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
    }




    private fun openWipeConfirmationScreen() {
        setContentView(R.layout.activity_wipedata)

        val etPwd = findViewById<EditText>(R.id.editTextNumberPasswordWipe)
        val btnWipe = findViewById<Button>(R.id.buttonWipe)
        val cancelBtn = findViewById<ImageButton>(R.id.cancelButtonWipe)
        val hello = findViewById<TextView>(R.id.helloWorldText1)

        val prefs = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val name = prefs.getString("name", "my Lord")
        val password = prefs.getString("password", "my Lord")

        hello.setTextColor(Color.parseColor("#FA0000"))
        btnWipe.setTextColor(Color.parseColor("#FA0000"))

        hello.text = "$name, you sure?"
        btnWipe.text = "Wipe data"

        btnWipe.setOnClickListener {
            if (etPwd.text.toString() == password) {
                deleteAppData()
            } else {
                settings()
            }
        }

        cancelBtn.setOnClickListener { settings() }
    }




    private fun listSaving() {
        val listView = findViewById<ListView>(R.id.listView)
        val fileName = File(applicationContext.filesDir, "password.txt")
        var arr = ArrayList<String>()

        val keyManager = KeyManager(keyAlias())
        fileName.writeText(keyManager.decrypt(fileName.readText())) // расшифровка

        val savedText = fileName.readText()
        arr.addAll(splitText(savedText, 4).filter { it.isNotBlank() }) // создание списка

        var arr2 = hidePassword(arr)
        listView.adapter = ArrayAdapter(this, R.layout.navigation_item, arr2)

        fileName.writeText(keyManager.encrypt(fileName.readText())) // шифровка

        setupSpinner(savedText, arr, arr2, listView)
        setupListViewClickListener(listView, arr, arr2)
    }




    private fun setupSpinner(savedText: String, arr: ArrayList<String>, arr2: List<String>, listView: ListView) {
        val spinner: Spinner = findViewById(R.id.spinner)
        val items = listOf("date", "date(reverse)", "name", "name(reverse)")
        val adapterSpinner = ArrayAdapter(this, R.layout.spinner_item, items)
        spinner.adapter = adapterSpinner

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> updateListView(savedText, arr, arr2, listView, false, false)
                    1 -> updateListView(savedText, arr, arr2, listView, true, false)
                    2 -> updateListView(savedText, arr, arr2, listView, false, true)
                    3 -> updateListView(savedText, arr, arr2, listView, true, true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }




    private fun updateListView(savedText: String, arr: ArrayList<String>, arr2: List<String>, listView: ListView, reverse: Boolean, sortByName: Boolean) {
        arr.clear()
        arr.addAll(splitText(savedText, 4).filter { it.isNotBlank() })
        var updatedArr2 = hidePassword(arr)

        if (updatedArr2.size > 1) {
            if (sortByName) {
                updatedArr2 = ArrayList(updatedArr2.sorted())
                arr.sort()
            }
            if (reverse) {
                updatedArr2 = ArrayList(updatedArr2.reversed())
                arr.reverse()
            }
            listView.adapter = ArrayAdapter(this@MainActivity, R.layout.navigation_item, updatedArr2)
        }
    }




    private fun setupListViewClickListener(listView: ListView, arr: ArrayList<String>, arr2: List<String>) {
        listView.setOnItemClickListener { parent, view, position, id ->
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
            var flag = true

            arr1.addAll(arr[position].dropLast(1).split("\n"))
            arr3.addAll(arr2[position].dropLast(1).split("\n"))

            listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr3)

            setupShowButton(showButton, listView1, arr1, arr3, flag)
            setupShareqrButton(shareButton, arr1)
            setupShareButton(shareButton2, arr1)
            setupWebsiteButton(website, arr1)
            setupEditButton(editButton, arr, arr1, position)
            setupDeleteButton(deleteButton2, arr, position)
            setupCancelButton(cancelButton2)

            listView1.setOnItemLongClickListener { parent, view, position, id ->
                copyText(arr1[position])
                true
            }
        }
    }




    private fun setupShowButton(showButton: ImageButton, listView1: ListView, arr1: ArrayList<String>, arr3: ArrayList<String>, initialFlag: Boolean) {
        var flag = initialFlag
        showButton.setOnClickListener {
            flag = !flag

            if (flag) {
                listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr3)
                showButton.setImageResource(R.drawable.show)
            } else {
                listView1.adapter = ArrayAdapter(this, R.layout.navigation_item, arr1)
                showButton.setImageResource(R.drawable.notshow)
            }
        }
    }




    private fun setupShareButton(shareButton2: ImageButton, arr1: ArrayList<String>) {
        shareButton2.setOnClickListener {
            shareMessage(this, arrToTextShare(arr1))
        }
    }




    private fun setupShareqrButton(shareButton: ImageButton, arr1: ArrayList<String>) {
        shareButton.setOnClickListener {
            share(arr1)
        }
    }




    private fun setupWebsiteButton(website: ImageButton, arr1: ArrayList<String>) {
        website.setOnClickListener {
            openInBrowser(arr1[1])
        }
    }




    private fun setupEditButton(editButton: ImageButton, arr: ArrayList<String>, arr1: ArrayList<String>, position: Int) {
        editButton.setOnClickListener {
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

            editText.setText(arr1[0])
            editText1.setText(arr1[1])
            editText2.setText(arr1[2])
            editText3.setText(arr1[3])

            saveButton.setOnClickListener {
                val password = editText3.text.toString()
                val login = editText2.text.toString()
                val url = editText1.text.toString()
                val name = editText.text.toString()

                arr1[0] = name
                arr1[1] = url
                arr1[2] = login
                arr1[3] = password

                arr[position] = arr1[0] + "\n" + arr1[1] + "\n" + arr1[2] + "\n" + arr1[3] + "\n"
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
    }




    private fun setupDeleteButton(deleteButton2: ImageButton, arr: ArrayList<String>, position: Int) {
        deleteButton2.setOnClickListener {
            setContentView(R.layout.activity_delete)

            val deleteButton = findViewById<ImageButton>(R.id.deleteButton)
            val cancelButton = findViewById<ImageButton>(R.id.cancelButton)
            val textView1 = findViewById<TextView>(R.id.textView1)
            val textView2 = findViewById<TextView>(R.id.textView2)

            textView1.text = "Do you want to delete?"
            textView2.text = arr[position].substringBefore("\n")

            deleteButton.setOnClickListener {
                arr.removeAt(position)
                passwordFile(arr)
                setupPasswordSaving()
            }
            cancelButton.setOnClickListener {
                setupPasswordSaving()
            }
        }
    }




    private fun setupCancelButton(cancelButton2: ImageButton) {
        cancelButton2.setOnClickListener {
            setupPasswordSaving()
        }
    }




    private fun share(arr1: List<String>) {
        setContentView(R.layout.activity_qrcode)

        val qrIV = findViewById<ImageView>(R.id.IVQrcode)
        val cancelButton3 = findViewById<ImageButton>(R.id.cancelButton3)
        val msgEdt = arr1.joinToString(separator = "\n")

        cancelButton3.setOnClickListener {
            setupPasswordSaving()
        }

        try {
            val writer = QRCodeWriter()
            val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
            val bitMatrix = writer.encode(msgEdt, BarcodeFormat.QR_CODE, 300, 300, hints)
            val pixels = IntArray(300 * 300)

            for (y in 0 until 300) {
                val offset = y * 300
                for (x in 0 until 300) {
                    pixels[offset + x] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.RGB_565)
            bitmap.setPixels(pixels, 0, 300, 0, 0, 300, 300)
            qrIV.setImageBitmap(bitmap)

        } catch (e: Exception) {
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
        authenticateWithFingerprint()
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




    fun authenticateWithFingerprint() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // нет сканера отпечатка
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Биометрические функции в настоящее время недоступны
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Пользователь не привязал никаких биометрических данных к своей учетной записи
            }
        }
    }




    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setupPasswordSaving()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                authenticateWithFingerprint()
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}