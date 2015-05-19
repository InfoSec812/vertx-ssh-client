package io.vertx.ssh.client;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.WriteStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author <a href="">Deven Phillips</a>
 */
public class WriteableInputStream extends InputStream implements WriteStream<Buffer> {
  
  private Buffer isBuffer;
  private int maxSize = 1024;
  private Handler<Void> drainHandler;
  private Vertx vertx;

  public WriteableInputStream(Vertx vertx) {
    this.vertx = vertx;
  }
  
  public int read() throws IOException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public WriteStream<Buffer> write(Buffer data) {
    if (isBuffer==null) {
      isBuffer = data;
    } else {
      isBuffer.appendBuffer(data);
    }
    return this;
  }

  @Override
  public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return (isBuffer.length()>=maxSize);
  }

  @Override
  public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
    this.drainHandler = handler;
    vertx.runOnContext(v -> callDrainHandler()); //If the channel is already drained, we want to call it immediately
    return this;
  }

  private synchronized void callDrainHandler() {
    if (drainHandler != null) {
      if (!writeQueueFull()) {
        drainHandler.handle(null);
      }
    }
  }
}
