package com.example.passwordbox

import com.example.passwordbox.R
import android.app.assist.AssistStructure
import android.content.Context
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews

class MyAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
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
    }

    private fun parseStructure(structure: AssistStructure): List<AutofillField> {
        val autofillFields = mutableListOf<AutofillField>()
        val nodes = structure.windowNodeCount

        for (i in 0 until nodes) {
            val node = structure.getWindowNodeAt(i).rootViewNode
            Log.d("MyAutofillService", "node: $node")
            parseNode(node, autofillFields)
        }
        Log.d("MyAutofillService", "autofillFields1: $autofillFields")
        return autofillFields
    }

    private fun parseNode(node: AssistStructure.ViewNode, autofillFields: MutableList<AutofillField>) {
        val hint = node.autofillHints
        val autofillId = node.autofillId // Получаем AutofillId из ViewNode
        Log.d("MyAutofillService", "autofillId: $autofillId")
        Log.d("MyAutofillService", "hint: $hint")
        if (!hint.isNullOrEmpty() && autofillId != null) {
            val autofillField = AutofillField(autofillId, hint, node.text?.toString())
            Log.d("MyAutofillService", "autofillField: $autofillField")
            autofillFields.add(autofillField)
        }

        for (i in 0 until node.childCount) {
            Log.d("MyAutofillService", "autofillField: $autofillFields")
            parseNode(node.getChildAt(i), autofillFields)
        }
    }


    private fun createFillResponse(
        context: Context,
        autofillFields: List<AutofillField>
    ): FillResponse {
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

        // Создаём FillResponse с двумя Dataset
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

// Класс для хранения данных полей
data class AutofillField(
    val id: AutofillId,
    val hint: Array<String>,
    val value: String?
)
