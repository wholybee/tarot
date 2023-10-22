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

private const val TAG = "OpenAI_wlh"

var rtn: String? = ""
// val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJfaWQiOiI2NTFjOWJkM2Q1NjJlZTk2ZjVhNWRlMTciLCJ1c2VybmFtZSI6ImZvcmV2ZXIiLCJpYXQiOjE2OTc4NjEzNzV9.5vfoE8RIn5NNsGw5yeBzeCklN4HfIKCEJliZ1JXlSU0" // "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"
val host = OpenAIHost (baseUrl = "https://app.holybee.net/v1/") //) "http://192.168.1.104:5000/v1/"
// val openAI = OpenAI(token = token, host = host, logging = LoggingConfig(LogLevel.None))
val modelId = ModelId("gpt-3.5-turbo")


@OptIn(BetaOpenAI::class)
val chatMessages:MutableList<ChatMessage>  = mutableListOf<ChatMessage>() /* mutableListOf(
    ChatMessage (
        role = ChatRole.User,
        content = "Hello, World."
    )
) */

@OptIn(BetaOpenAI::class)
val chatSystems:MutableList<ChatMessage> = mutableListOf(
    ChatMessage(
        role = ChatRole.System,
        content = "You are a helpful assistant."
    )
)

@OptIn(BetaOpenAI::class)
suspend fun changeGPTSystem (s: String) {
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


//Call to GPT and change system message
@OptIn(BetaOpenAI::class)
suspend fun askGPT(q: String, systemMessage: String): OpenAIResponse {
    changeGPTSystem(systemMessage)
    return askGPT(q)
}

// Basic call to GPT and ask a question
@OptIn(BetaOpenAI::class)
suspend fun askGPT(q: String): OpenAIResponse {
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
// trim to 10 messages in history
    if (chatMessages.size > 1) {
        chatMessages.removeAt(0)
    }

// build the request
    val request = chatCompletionRequest {
        model = modelId
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

