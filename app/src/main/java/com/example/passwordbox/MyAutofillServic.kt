package com.example.passwordbox

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.content.Context
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.*
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import java.io.File

class MyAutofillService : AutofillService() {

    private val cache = mutableMapOf<AutofillId, AutofillField>()

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        try {
            val structure = request.fillContexts.lastOrNull()?.structure
            if (structure == null) {
                Log.e("MyAutofillService", "No structure found")
                callback.onFailure("No structure found")
                return
            }

            val autofillFields = parseStructure(structure)
            if (autofillFields.isEmpty()) {
                Log.e("MyAutofillService", "No autofill fields found")
                callback.onFailure("No autofill fields found")
                return
            }

            val fillResponse = createFillResponse(applicationContext, autofillFields)
            if (fillResponse != null) {
                callback.onSuccess(fillResponse)
            } else {
                Log.e("MyAutofillService", "Failed to create FillResponse")
                callback.onFailure("Failed to create FillResponse")
            }
        } catch (e: Exception) {
            Log.e("MyAutofillService", "Error processing fill request", e)
            callback.onFailure("Error processing fill request")
        }
    }

    private fun parseStructure(structure: AssistStructure): List<AutofillField> {
        val autofillFields = mutableListOf<AutofillField>()
        val nodes = structure.windowNodeCount

        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i).rootViewNode
            parseNode(node, autofillFields)
        }
        return autofillFields
    }

    private fun parseNode(node: AssistStructure.ViewNode, autofillFields: MutableList<AutofillField>) {
        val hint = node.autofillHints
        val autofillId = node.autofillId
        val autofillType = node.autofillType
        val inputType = node.inputType

        if (!hint.isNullOrEmpty() && autofillId != null) {
            val autofillField = AutofillField(autofillId, hint, node.text?.toString())
            autofillFields.add(autofillField)
            cache[autofillId] = autofillField
        } else if (autofillType != View.AUTOFILL_TYPE_NONE && autofillId != null) {
            val autofillField = AutofillField(autofillId, arrayOf(""), node.text?.toString())
            autofillFields.add(autofillField)
            cache[autofillId] = autofillField
        } else if (inputType != InputType.TYPE_NULL && autofillId != null) {
            val autofillField = AutofillField(autofillId, arrayOf(""), node.text?.toString())
            autofillFields.add(autofillField)
            cache[autofillId] = autofillField
        }

        for (i in 0 until node.childCount) {
            parseNode(node.getChildAt(i), autofillFields)
        }
    }

    private fun createFillResponse(
        context: Context,
        autofillFields: List<AutofillField>
    ): FillResponse? {
        val datasetBuilder1 = Dataset.Builder()

        for (field in autofillFields) {
            val autofillId = field.id

            val arr = valueEncoder()
            if (arr.size < 2) {
                Log.e("MyAutofillService", "valueEncoder returned invalid data")
                return null
            }

            val value1 = arr[0]
            val value2 = arr[1]

            val presentation1 = RemoteViews(context.packageName, R.layout.autofill_item)
            presentation1.setTextViewText(R.id.autofill_text, value1)
            datasetBuilder1.setValue(autofillId, AutofillValue.forText(value2), presentation1)
        }

        return FillResponse.Builder()
            .addDataset(datasetBuilder1.build())
            .build()
    }
    private fun keyAlias():String {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val keyAlias = sharedPreferences.getString("password", "")
        return keyAlias!!
    }

    private fun valueEncoder(): ArrayList<String> {
        val fileName = File(applicationContext.filesDir, "password.txt")
        val arr = ArrayList<String>()

        val keyManager = KeyManager(keyAlias())
        val decryptedText = keyManager.decrypt(fileName.readText())

        fileName.writeText(decryptedText) // Расшифрованный текст
        val savedText = fileName.readText()
        fileName.writeText(keyManager.encrypt(savedText)) // Снова зашифровали

        return splitText(savedText)
    }

    fun splitText(savedText: String): ArrayList<String> {
        val arr = ArrayList<String>()
        val lines = savedText.split("\n")

        if (lines.size >= 3) {
            arr.add(lines[0])
            arr.add(lines[3])
        } else {
            Log.e("MyAutofillService", "splitText: Not enough lines in savedText")
        }

        return arr
    }


    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d("MyAutofillService", "Save request received")
        callback.onSuccess()
    }

    override fun onConnected() {
        Log.d("MyAutofillService", "Service connected")
    }

    override fun onDisconnected() {
        Log.d("MyAutofillService", "Service disconnected")
    }
}

data class AutofillField(
    val id: AutofillId,
    val hint: Array<String>,
    val value: String?
)