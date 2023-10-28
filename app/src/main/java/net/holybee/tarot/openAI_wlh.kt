package net.holybee.tarot

import android.util.Log
import com.aallam.openai.api.BetaOpenAI
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIHost
import kotlinx.serialization.json.*
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.HolybeeURL

private const val TAG = "OpenAI_wlh"


val host = OpenAIHost (baseUrl = HolybeeURL.openAI) //) "http://192.168.1.104:5000/v1/"


@OptIn(BetaOpenAI::class)
val chatMessages:MutableList<ChatMessage>  = mutableListOf() /* mutableListOf(
    ChatMessage (
        role = ChatRole.User,
        content = "Hello, World."
    )
) */

@OptIn(BetaOpenAI::class)
val chatSystems:MutableList<ChatMessage> = mutableListOf()

@OptIn(BetaOpenAI::class)
fun changeGPTSystem (s: String) {
    chatSystems.clear()
    val messages = s.split("\n")
    messages.forEach {
        chatSystems.add(
            ChatMessage(
                role = ChatRole.System,
                content = it
            )
        )
    }
}

@OptIn(BetaOpenAI::class)
fun clearHistory () {
    chatMessages.clear()
}

//Call to GPT and change system message

suspend fun askGPT(q: String, systemMessage: String, modelId: String): OpenAIResponse {
    changeGPTSystem(systemMessage)
    return askGPT(q, modelId)
}

// Basic call to GPT and ask a question
@OptIn(BetaOpenAI::class)
suspend fun askGPT(q: String, modelId: String): OpenAIResponse {
    val token = AccountInformation.authToken
    if (token.length < 20) {
        return OpenAIResponse(
            "error",
            "No token found. Are you logged in?"
        )
    }
    val openAI = OpenAI(token = token, host = host, logging = LoggingConfig(LogLevel.None))

// add user prompt to conversation
    chatMessages.add(
        ChatMessage(
            role = ChatRole.User,
            content = q
        )
    )
// trim to 3 messages in history
    if (chatMessages.size > 3) {
        chatMessages.removeAt(0)
    }

// build the request
    val request = chatCompletionRequest {
        model = ModelId(modelId)
        messages = chatSystems + chatMessages

    }

    try {
        val response = openAI.chatCompletion(request)
        val message = response.choices.first().message
        if (message!=null)
            chatMessages.add(message)
        else
            return OpenAIResponse(
                "error",
                "Error in Response, Please try again."
            )

    } catch (e: Exception) {
        Log.e(TAG, "OpenAI error: ${e.message}")
        return OpenAIResponse(
            "error",
            "Error in Response, Please try again."
        )
    }

    val message = chatMessages.last()

    return OpenAIResponse(
     "OK",
     message.content ?: "Error in Response. Please try again."
    )
}

