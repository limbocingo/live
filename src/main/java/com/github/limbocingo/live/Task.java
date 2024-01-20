package com.github.limbocingo.live;

import com.microsoft.playwright.Page;

import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Bukkit task utilized for updating the page and refresh the current MapViews.
 */
public class Task extends BukkitRunnable {
    private MapView[][] views;
    private Page page;

    public Task(Page page, MapView[][] views) {
        this.page  = page;
        this.views = views;
    }

    @Override
    public void run() {
        BufferedImage[][] imagePieces;
        try {
            imagePieces = Utilities.splitImage(
                    128,
                    ImageIO.read(
                            new ByteArrayInputStream(page.screenshot(new Page.ScreenshotOptions().setFullPage(true)))
                    )
            );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < imagePieces.length; i++) {
            for (int j = 0; j < imagePieces[i].length; j++) {
                this.views[i][j].getRenderers().clear();
                this.views[i][j].addRenderer(new Render(imagePieces[i][j]));
            }
        }
    }
}
