package com.github.limbocingo.live;

import com.google.gson.Gson;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import java.io.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


/**
 * Plugin main class.
 *
 * @version 0.0.1
 * @author mrcingo
 */
public class Live extends JavaPlugin implements CommandExecutor {

    Browser browser;
    Playwright playwright;
    Page page;
    HashMap object;
    Reader reader;
    FileWriter writer;

    @Override
    public void onEnable() {
        // Creating the browser and page.
        this.playwright = Playwright.create();

        Bukkit.getLogger().info("Starting Firefox browser and opening a page...");
        this.browser = playwright.firefox().launch();

        Bukkit.getLogger().info("Firefox browser started, with version " + this.browser.version() + ".");

        this.page = browser.newPage();
        Bukkit.getLogger().info("Page opened successfully.");

        /*
            If the server is reloaded or started up again all the images
            made before will be saved in the `data.json` file.
         */
        try {
            if (!this.getDataFolder().exists())
                this.getDataFolder().mkdir();

            File file = new File(this.getDataFolder(), "data.json");

            this.writer = new FileWriter(file);

            if (!file.exists()) {
                file.createNewFile();

                writer.write("{}");
                writer.flush();
            }

            this.reader = new FileReader(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Gson parser = new Gson();
        this.object = parser.fromJson(this.reader, HashMap.class);

        if (this.object == null)
            this.object = new HashMap();

        if (!this.object.isEmpty()) {
            Location location = new Location(
                    this.getServer().getWorld((String) this.object.get("world")),
                    (Double) this.object.get("x"),
                    (Double) this.object.get("y"),
                    (Double) this.object.get("z")
            );

            Collection<Entity> e = location.getWorld().getNearbyEntities(
                    location,
                    1,
                    (Integer) this.object.get("height"),
                    (Integer) this.object.get("width"),
                    (entity) -> entity.getType() == EntityType.ITEM_FRAME
            );

            MapView[][] views = new MapView[(Integer) this.object.get("height")][(Integer) this.object.get("width")];
            e.forEach(entity -> {
                for (int i = 0; i < (Integer) this.object.get("height"); i++) {
                    for (int j = 0; j < (Integer) this.object.get("width"); j++) {
                        // get
                        ItemFrame itemFrame = (ItemFrame) entity;
                        ItemStack itemStack = itemFrame.getItem();

                        MapMeta itemMeta = (MapMeta) itemStack.getItemMeta();
                        MapView mapView = itemMeta.getMapView();

                        // add
                        views[i][j] = mapView;
                    }
                }
            });

            new Task(this.page, views).runTaskTimerAsynchronously(this, 0, (Integer) this.object.get("rate"));
        }

        /*
            Commands
         */
        this.getCommand("stream").setExecutor(this);
    }

    @Override
    public void onDisable() {
        try {
            this.playwright.close();
            this.reader.close();
            this.writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] arguments) {
        Player player = (Player) sender;

        List<List<MapView>> views = new ArrayList<>();

        page.navigate(arguments[0]);

        BufferedImage[][] pieces;
        try {
            pieces = Utilities.splitImage(
                    128,
                    ImageIO.read(
                            new ByteArrayInputStream(page.screenshot(new Page.ScreenshotOptions().setFullPage(true)))
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Location location = player.getLocation();

        MapView[][] entities = new MapView[pieces.length][pieces[0].length];
        for (int v = 0; v < pieces.length; v++) {
            views.add(new ArrayList<>());
            for (int h = 0; h < pieces[v].length; h++) {
                Block block = location.getWorld().getBlockAt(location.getBlockX(), location.getBlockY() - v, location.getBlockZ() - h);
                block.setType(Material.STONE);

                MapView view = Bukkit.createMap(player.getWorld());
                view.addRenderer(new Render(pieces[v][h]));
                views.get(v).add(view);

                player.sendMap(view);

                ItemStack item = new ItemStack(Material.FILLED_MAP);

                MapMeta meta = (MapMeta) item.getItemMeta();
                meta.setMapView(view);
                item.setItemMeta(
                        meta
                );

                entities[v][h] = view;

                ItemFrame frame = (ItemFrame) player.getWorld().spawnEntity(block.getLocation().add(1, 0, 0), EntityType.ITEM_FRAME);

                frame.setItem(item);

                frame.setInvulnerable(true);
                frame.setFixed(true);
                frame.setVisible(false);
            }
        }

        new Task(this.page, entities).runTaskTimerAsynchronously(this, 0, Long.parseLong(arguments[1])*20L);

        return true;
    }
}