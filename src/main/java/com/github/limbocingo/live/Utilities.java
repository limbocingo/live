package com.github.limbocingo.live;

import java.awt.image.BufferedImage;

import java.io.IOException;


public class Utilities {

    /**
     * Split an image in equal pieces.
     *
     * @param size Size of each image.
     * @param image The image you want to split.
     *
     * @return A matrix with all the pieces.
     *
     * @throws IOException              Something went wrong trying to split the image.
     * @throws IllegalArgumentException The size is higher than the height or the width of the image.
     */
    public static BufferedImage[][] splitImage(int size, BufferedImage image) throws IOException, IllegalArgumentException {
        int verticalParts        = Math.round(image.getHeight() / size);
        int horizontalParts      = Math.round(image.getWidth() / size);

        BufferedImage[][] pieces = new BufferedImage[verticalParts][horizontalParts];
        BufferedImage     piece;

        if (image.getHeight() < size || image.getWidth() < size)
            throw new IllegalArgumentException("The size is to high.");

        for (int v = 0; v < verticalParts; v++) {
            for (int h = 0; h < horizontalParts; h++) {
                piece = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
                for (int y = 0; y < size; y++)
                    for (int x = 0; x < size; x++)
                        piece.setRGB(x, y, image.getRGB(x + (h * size), y + (v * size)));
                pieces[v][h] = piece;
            }
        }

        return pieces;
    }
}
