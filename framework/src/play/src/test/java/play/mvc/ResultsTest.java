package play.mvc;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ResultsTest {

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfPathIsNull() throws IOException {
    Results.ok().sendPath(null);
  }

  @Test
  public void sendFile() throws IOException {
    File file = new File("test.tmp");
    try {
      file.createNewFile();
      Results.Status status = Results.ok(file);

      assertEquals(status.status(), Http.Status.OK);
      assertEquals(status.header(Result.CONTENT_DISPOSITION), "attachment; filename=\"test.tmp\"");
    } finally {
      file.delete();
    }
  }

  @Test
  public void sendFileAcceptRanges() throws IOException {
    File file = new File("test.tmp");
    try {
      file.createNewFile();
      Results.Status status = Results.ok(file);

      assertEquals(status.status(), Http.Status.OK);
      assertEquals(status.header(Result.ACCEPT_RANGES), "bytes");
    } finally {
      file.delete();
    }
  }

  @Test
  public void sendFileAsPartialContent() throws IOException {
    File file = new File("test.tmp");
    try {
      FileOutputStream out = new FileOutputStream(file);
      out.write(new byte[]{ 1, 2, 3, 4, 5, 6, 7 });
      out.close();
      Results.Status status = Results.ok(file, false, null, "bytes=2-4");

      assertEquals(status.status(), Http.Status.PARTIAL_CONTENT);
      assertEquals(status.header(Result.CONTENT_RANGE), "bytes 2-4/7");
      assertEquals(status.header(Result.CONTENT_LENGTH), "3");
    } finally {
      file.delete();
    }
  }

  @Test
  public void sendPathWithOKStatus() throws IOException {
    Path file = Paths.get("test.tmp");
    Files.createFile(file);
    Results.Status status = Results.ok().sendPath(file);
    Files.delete(file);

    assertEquals(status.status(), Http.Status.OK);
    assertEquals(status.header(Result.CONTENT_DISPOSITION), "attachment; filename=\"test.tmp\"");
  }

  @Test
  public void sendPathWithUnauthorizedStatus() throws IOException {
    Path file = Paths.get("test.tmp");
    Files.createFile(file);
    Results.Status status = Results.unauthorized().sendPath(file);
    Files.delete(file);

    assertEquals(status.status(), Http.Status.UNAUTHORIZED);
    assertEquals(status.header(Result.CONTENT_DISPOSITION), "attachment; filename=\"test.tmp\"");
  }

  @Test
  public void sendPathInlineWithUnauthorizedStatus() throws IOException {
    Path file = Paths.get("test.tmp");
    Files.createFile(file);
    Results.Status status = Results.unauthorized().sendPath(file, true);
    Files.delete(file);

    assertEquals(status.status(), Http.Status.UNAUTHORIZED);
    assertEquals(status.header(Result.CONTENT_DISPOSITION), null);
  }

  @Test
  public void sendPathWithFileName() throws IOException {
    Path file = Paths.get("test.tmp");
    Files.createFile(file);
    Results.Status status = Results.unauthorized().sendPath(file, false, "foo.bar");
    Files.delete(file);

    assertEquals(status.status(), Http.Status.UNAUTHORIZED);
    assertEquals(status.header(Result.CONTENT_DISPOSITION), "attachment; filename=\"foo.bar\"");
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNullPointerExceptionIfPathIsNullForChunkedTransferEncoding() throws IOException {
    Results.unauthorized().sendPath(null, 100);
  }

  @Test
  public void sendPathWithChunkedTransferEncoding() throws IOException {
    Path file = Paths.get("test.tmp");
    Files.createFile(file);
    Results.Status status = Results.unauthorized().sendPath(file, 100);
    Files.delete(file);

    assertEquals(status.status(), Http.Status.UNAUTHORIZED);
    assertEquals(status.header(Result.TRANSFER_ENCODING), "chunked");
  }
}
