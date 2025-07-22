package view;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageUtils {
    
    public static final String IMAGES_FOLDER = "images/menu/";
    public static final int THUMBNAIL_WIDTH = 150;
    public static final int THUMBNAIL_HEIGHT = 100;
    public static final int LARGE_WIDTH = 300;
    public static final int LARGE_HEIGHT = 200;

    /**
     * Create images directory if it doesn't exist
     */
    public static void createImagesDirectory() {
        try {
            Path imagesPath = Paths.get(IMAGES_FOLDER);
            if (!Files.exists(imagesPath)) {
                Files.createDirectories(imagesPath);
                System.out.println("Created images directory: " + IMAGES_FOLDER);
            }
        } catch (IOException e) {
            System.err.println("Error creating images directory: " + e.getMessage());
        }
    }

    /**
     * Load and resize image for display
     * @param imagePath Path to the image file
     * @param width Target width
     * @param height Target height
     * @return ImageIcon or null if failed
     */
    public static ImageIcon loadResizedImage(String imagePath, int width, int height) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return null;
        }

        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                System.err.println("Image file not found: " + imagePath);
                return null;
            }

            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                System.err.println("Cannot read image file: " + imagePath);
                return null;
            }

            Image resizedImage = resizeImage(originalImage, width, height);
            return new ImageIcon(resizedImage);

        } catch (IOException e) {
            System.err.println("Error loading image: " + imagePath + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Load thumbnail version of image
     */
    public static ImageIcon loadThumbnail(String imagePath) {
        return loadResizedImage(imagePath, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
    }

    /**
     * Load large version of image
     */
    public static ImageIcon loadLargeImage(String imagePath) {
        return loadResizedImage(imagePath, LARGE_WIDTH, LARGE_HEIGHT);
    }

    /**
     * Resize image with high quality
     */
    public static Image resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Calculate aspect ratio
        double aspectRatio = (double) originalImage.getWidth() / originalImage.getHeight();
        
        int newWidth = targetWidth;
        int newHeight = targetHeight;
        
        // Maintain aspect ratio
        if (targetWidth / aspectRatio < targetHeight) {
            newHeight = (int) (targetWidth / aspectRatio);
        } else {
            newWidth = (int) (targetHeight * aspectRatio);
        }

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Enable high quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        return resizedImage;
    }

    /**
     * Generate unique filename for uploaded image
     */
    public static String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        return "menu_" + System.currentTimeMillis() + "_" + 
               Math.random() * 1000 + "." + extension;
    }

    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "jpg";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // default extension
    }

    /**
     * Validate if file is a supported image format
     */
    public static boolean isValidImageFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".jpg") || 
               fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || 
               fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp");
    }

    /**
     * Validate if file content is actually an image
     */
    public static boolean isValidImageContent(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            return image != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete image file safely
     */
    public static boolean deleteImageFile(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return true; // Nothing to delete
        }

        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return imageFile.delete();
            }
            return true; // File doesn't exist, consider it "deleted"
        } catch (Exception e) {
            System.err.println("Error deleting image file: " + imagePath + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Get full path for image in images folder
     */
    public static String getImagePath(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        return IMAGES_FOLDER + fileName;
    }

    /**
     * Check if image file exists
     */
    public static boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.trim().isEmpty()) {
            return false;
        }
        File imageFile = new File(imagePath);
        return imageFile.exists() && imageFile.isFile();
    }
}
