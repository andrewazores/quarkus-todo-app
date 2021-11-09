package io.quarkus.sample;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.inject.Singleton;

import io.quarkus.arc.Lock;

@Lock
@Singleton
class TodoLoggerImpl implements TodoLogger {

    private static final Path LOG_FILE = Path.of("var", "log", "todo.log");
    private final BlockingQueue<String> q = new LinkedBlockingQueue<>(64);
    private final ExecutorService service = Executors.newSingleThreadExecutor();
    private final ReentrantLock flock = new ReentrantLock();

    TodoLoggerImpl() {
        try {
            if (!Files.isRegularFile(LOG_FILE)) {
                Files.createFile(LOG_FILE, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void log(String s) {
        try {
            q.put(s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // this is terrible on purpose
        this.service.submit(() -> {
            flock.lock();
            try {
                StringBuffer sb = new StringBuffer();
                String logLine;
                try {
                    logLine = String.format("[LOG %s]: %s", Thread.currentThread().getName(), q.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
                System.out.println(logLine);
                try (FileReader reader = new FileReader(LOG_FILE.toAbsolutePath().toFile())) {
                    while (reader.ready()) {
                        char[] cbuf = new char[256];
                        int read = reader.read(cbuf);
                        if (read >= 0) {
                            sb.append(Arrays.copyOfRange(cbuf, 0, read));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (FileOutputStream writer = new FileOutputStream(LOG_FILE.toAbsolutePath().toFile())) {
                    for (byte b : sb.toString().getBytes()) {
                        writer.write(b);
                    }
                    for (byte b : System.getProperty("line.separator").getBytes()) {
                        writer.write(b);
                    }
                    for (byte b : logLine.getBytes()) {
                        writer.write(b);
                    }
                    for (byte b : System.getProperty("line.separator").getBytes()) {
                        writer.write(b);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } finally {
                flock.unlock();
            }
        });
    }

}
