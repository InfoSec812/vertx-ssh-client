package io.vertx.ssh.client;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author <a href="">Deven Phillips</a>
 */
public class ReadableOutputStream extends OutputStream implements ReadStream<Buffer> {
  
  private Buffer buffer;
  private int maxSize = 1024;
  private Vertx vertx;
  private Handler<Throwable> exHandler;
  private Handler<Buffer> handler;
  private Handler<Void> endHandler;

  public ReadableOutputStream(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void write(int b) throws IOException {
    buffer.appendInt(b);
  }

  @Override
  public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
    this.exHandler = handler;
    return this;
  }

  @Override
  public ReadStream<Buffer> handler(Handler<Buffer> handler) {
    this.handler = handler;
    return this;
  }

  @Override
  public ReadStream<Buffer> pause() {
    return this;
  }

  @Override
  public ReadStream<Buffer> resume() {
    return this;
  }

  @Override
  public ReadStream<Buffer> endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public void close() throws IOException {
    super.close();
    endHandler.handle(null);
  }
}
