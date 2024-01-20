package com.github.limbocingo.live;

import org.bukkit.entity.Player;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.jetbrains.annotations.NotNull;


/**
 * Render normal images to a {@link org.bukkit.map.MapRenderer}.
 */
public class Render extends MapRenderer {
    BufferedImage image;
    Boolean rendered;

    public Render(BufferedImage image) {
        this.image = image;
        rendered = false; // Know if process is finished.
    }

    @Override
    public void render(@NotNull MapView map, @NotNull MapCanvas canvas, @NotNull Player player) {
        if (rendered)
            return;

        map.setTrackingPosition(false);
        BufferedImage resizeImage = MapPalette.resizeImage(this.image);
        for (int y = 0; y < 128; y++)
            for (int x = 0; x < 128; x++)
                canvas.setPixelColor(y, x, new Color(resizeImage.getRGB(y, x)));

        rendered = true;
    }
}
