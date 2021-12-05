package com.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.*
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Exception

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    @Bean
    fun someEndpoint() = SomeEndpoint()

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(someEndpoint(), "/ws").addInterceptors(SimpleHandshakeInterceptor)
    }
}

object SimpleHandshakeInterceptor : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        // Stores a URI builder obtained from the request.
        // This URI builder can overlay "Forwarded" headers (RFC 7239), or "X-Forwarded-Host", "X-Forwarded-Port", and
        // "X-Forwarded-Proto" headers if "Forwarded" is not found.
        attributes["builder"] = UriComponentsBuilder.fromHttpRequest(request).replacePath("")
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
    }
}

class SomeEndpoint : TextWebSocketHandler() {
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {

        // the map of session attributes contains the attributes set in the handshake
        val builder = session.attributes["builder"] as UriComponentsBuilder

        // we can use an MVC builder
        val uri = fromMethodName(builder, Example::class.java, "example", "57").build()

        // returns the IP of the remote client
        println("Remote address: ${session.remoteAddress}")

        // returns the WebSocket URI
        println("URI: ${session.uri}")

        // returns a link to a controller build from the handshake request
        println("Link to a method: $uri")
    }
}

/**
 * Dummy controller to test the link generation
 */
@Controller
class Example {
    @GetMapping("/some/{id}/value")
    fun example(@PathVariable id: String): String = TODO()
}

