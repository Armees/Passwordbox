package com.example.passwordbox

import android.app.assist.AssistStructure
import android.content.Context
import android.os.CancellationSignal
import android.service.autofill.*
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews

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
                callback.onFailure("No structure found")
                return
            }

            val autofillFields = parseStructure(structure)
            if (autofillFields.isEmpty()) {
                callback.onFailure("No autofill fields found")
                return
            }

            val fillResponse = createFillResponse(applicationContext, autofillFields)
            if (fillResponse != null) {
                callback.onSuccess(fillResponse)
            } else {
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
        val datasetBuilder2 = Dataset.Builder()

        for (field in autofillFields) {
            val autofillId = field.id

            // Первое значение (например, "Аккаунт 1")
            val value1 = "account1@example.com"
            val presentation1 = RemoteViews(context.packageName, R.layout.autofill_item)
            presentation1.setTextViewText(R.id.autofill_text, value1)
            datasetBuilder1.setValue(autofillId, AutofillValue.forText(value1), presentation1)

            // Второе значение (например, "Аккаунт 2")
            val value2 = "account2@example.com"
            val presentation2 = RemoteViews(context.packageName, R.layout.autofill_item)
            presentation2.setTextViewText(R.id.autofill_text, value2)
            datasetBuilder2.setValue(autofillId, AutofillValue.forText(value2), presentation2)
        }

        return FillResponse.Builder()
            .addDataset(datasetBuilder1.build())
            .addDataset(datasetBuilder2.build())
            .build()
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        // Логика сохранения пароля, если необходимо
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