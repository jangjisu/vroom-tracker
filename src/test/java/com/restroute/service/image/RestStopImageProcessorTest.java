package com.restroute.service.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class RestStopImageProcessorTest {

    private final RestStopImageProcessor processor = new RestStopImageProcessor();

    @Test
    void createsLandscapeDetailAndListWebpVariants() throws IOException {
        RestStopImageData result = processor.process(image("jpeg", 2400, 1200));

        assertDimensions(result.detailImageData(), 1600, 800);
        assertDimensions(result.listImageData(), 480, 240);
    }

    @Test
    void createsPortraitDetailAndListWebpVariants() throws IOException {
        RestStopImageData result = processor.process(image("jpeg", 1200, 2400));

        assertDimensions(result.detailImageData(), 800, 1600);
        assertDimensions(result.listImageData(), 240, 480);
    }

    @Test
    void doesNotUpscaleSmallImages() throws IOException {
        RestStopImageData result = processor.process(image("jpeg", 320, 200));

        assertDimensions(result.detailImageData(), 320, 200);
        assertDimensions(result.listImageData(), 320, 200);
    }

    @Test
    void acceptsPngInput() throws IOException {
        RestStopImageData result = processor.process(image("png", 1200, 600));

        assertDimensions(result.detailImageData(), 1200, 600);
        assertDimensions(result.listImageData(), 480, 240);
    }

    @Test
    void rejectsAnEmptyFile() {
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> processor.process(file)).isInstanceOf(InvalidRestStopImageException.class);
    }

    @Test
    void rejectsCorruptJpegData() {
        MultipartFile file = new MockMultipartFile("file", "corrupt.jpg", "image/jpeg", new byte[] {1, 2, 3});

        assertThatThrownBy(() -> processor.process(file)).isInstanceOf(InvalidRestStopImageException.class);
    }

    @Test
    void rejectsGifInput() throws IOException {
        MultipartFile file = image("gif", 100, 100);

        assertThatThrownBy(() -> processor.process(file)).isInstanceOf(InvalidRestStopImageException.class);
    }

    @Test
    void rejectsImagesOverThirtyMegapixelsBeforeDecoding() {
        MultipartFile file = new MockMultipartFile("file", "large.png", "image/png", pngHeader(6000, 5001));

        assertThatThrownBy(() -> processor.process(file)).isInstanceOf(InvalidRestStopImageException.class);
    }

    private MultipartFile image(String format, int width, int height) throws IOException {
        int imageType = "png".equals(format) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        boolean written = ImageIO.write(image, format, output);
        if (!written) {
            throw new IllegalStateException("Unable to write test image");
        }

        return new MockMultipartFile("file", "source." + format, "image/" + format, output.toByteArray());
    }

    private byte[] pngHeader(int width, int height) {
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.put(new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
        buffer.putInt(13);
        buffer.put(new byte[] {0x49, 0x48, 0x44, 0x52});
        buffer.putInt(width);
        buffer.putInt(height);
        return buffer.array();
    }

    private void assertDimensions(byte[] data, int width, int height) {
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException exception) {
            throw new AssertionError("Unable to read generated WebP", exception);
        }

        assertThat(image).isNotNull();
        assertThat(image.getWidth()).isEqualTo(width);
        assertThat(image.getHeight()).isEqualTo(height);
    }
}
