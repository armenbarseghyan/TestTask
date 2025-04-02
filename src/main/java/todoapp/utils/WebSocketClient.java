package todoapp.utils;

import todoapp.config.TestConfig;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket client for testing WebSocket functionality
 */
public class WebSocketClient extends org.java_websocket.client.WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    private final List<String> receivedMessages = new ArrayList<>();
    private CountDownLatch messageLatch;
    private boolean isConnected = false;
    private URI endpointURI;

    /**
     * Constructor
     *
     * @param endpointURI WebSocket URI
     */
    public WebSocketClient(URI endpointURI) {
        super(endpointURI);
        this.endpointURI = endpointURI;
    }

    /**
     * Reconnect to the WebSocket server
     *
     * @throws URISyntaxException if the URI is invalid
     */
    public void reconnect(){
        if (this.isOpen()) {
            try {
                this.closeBlocking();
            } catch (InterruptedException e) {
                logger.error("Error closing WebSocket connection: ", e);
                Thread.currentThread().interrupt();
            }
        }

        // Reset state
        isConnected = false;
        receivedMessages.clear();

        // Create a new WebSocketClient with the same URI
        this.uri = this.endpointURI;
    }

    /**
     * Create a WebSocketClient with default configuration
     *
     * @return WebSocketClient instance
     * @throws URISyntaxException if URI is invalid
     */
    public static WebSocketClient createDefault() throws URISyntaxException {
        TestConfig config = TestConfig.getInstance();
        return new WebSocketClient(new URI(config.getWsUrl()));
    }

    /**
     * Called when connection opens
     *
     * @param handshakedata ServerHandshake data
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        logger.info("WebSocket connection opened with status: {}", handshakedata.getHttpStatus());
        isConnected = true;
    }

    /**
     * Called when a message is received
     *
     * @param message Received message
     */
    @Override
    public void onMessage(String message) {
        logger.info("WebSocket message received: {}", message);
        receivedMessages.add(message);
        if (messageLatch != null) {
            messageLatch.countDown();
        }
    }

    /**
     * Called when connection is closed
     *
     * @param code   Status code
     * @param reason Reason for closure
     * @param remote Whether closed by remote host
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("WebSocket connection closed. Code: {}, Reason: {}, Remote: {}", code, reason, remote);
        isConnected = false;
    }

    /**
     * Called when an error occurs
     *
     * @param ex Exception
     */
    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error: ", ex);
    }

    /**
     * Wait for a specific number of messages
     *
     * @param count   Number of messages to wait for
     * @param timeout Timeout in seconds
     * @return true if received expected number of messages, false otherwise
     * @throws InterruptedException if interrupted
     */
    public boolean waitForMessages(int count, int timeout) throws InterruptedException {
        messageLatch = new CountDownLatch(count);
        return messageLatch.await(timeout, TimeUnit.SECONDS);
    }

    /**
     * Get received messages
     *
     * @return List of received messages
     */
    public List<String> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }

    /**
     * Clear received messages
     */
    public void clearMessages() {
        receivedMessages.clear();
    }

    /**
     * Check if connected
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return isConnected;
    }
}