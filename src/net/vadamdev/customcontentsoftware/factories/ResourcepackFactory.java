package net.vadamdev.customcontentsoftware.factories;

import javafx.scene.control.ProgressBar;
import net.vadamdev.customcontentsoftware.utils.AtomicVar;
import net.vadamdev.customcontentsoftware.utils.DumpData;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VadamDev
 * @since 29/09/2022
 */
public class ResourcepackFactory {
    private final Map<DumpData, File> data;
    private final String name, description;
    private final ProgressBar progressBar;

    public ResourcepackFactory(Map<String, File> data, String name, String description, ProgressBar progressBar) {
        this.data = new HashMap<>();
        data.forEach((item, textureFile) -> {
            String[] split = item.split(":");
            this.data.put(new DumpData(split[0], Integer.parseInt(split[1])), textureFile);
        });

        this.name = name;
        this.description = description;
        this.progressBar = progressBar;
    }

    public void build(File directory) {
        File packDirectory = new File(directory, name);
        packDirectory.mkdirs();
        createMCMetaFile(packDirectory);

        File citDirectory = new File(packDirectory, "assets/minecraft/mcpatcher/cit");
        citDirectory.mkdirs();
        createMCPatcherFiles(citDirectory);
    }

    private void createMCMetaFile(File packDirectory) {
        File file = new File(packDirectory, "pack.mcmeta");

        try {
            file.createNewFile();

            JSONObject json = new JSONObject();
            json.put("pack_format", 1);
            json.put("description", description);

            JSONObject pack = new JSONObject();
            pack.put("pack", json);

            FileWriter writer = new FileWriter(file);
            writer.write(pack.toJSONString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createMCPatcherFiles(File citDirectory) {
        AtomicVar<Double> state = new AtomicVar<>(0D);
        data.forEach((dumpData, textureFile) -> {
            String registryName = dumpData.getRegistryName();
            int itemId = dumpData.getItemId();

            try {
                File propertiesFile = new File(citDirectory, registryName + ".properties");
                propertiesFile.createNewFile();

                FileWriter writer = new FileWriter(propertiesFile);

                writer.write("type=item\n");
                writer.write("items=" + itemId + "\n");
                writer.write("texture=" + registryName + ".png\n");
                writer.write("nbt.RegistryName=" + registryName + "\n");

                writer.close();

                Files.copy(textureFile.toPath(), new File(citDirectory, registryName + ".png").toPath(), StandardCopyOption.REPLACE_EXISTING);

                state.set(state.get() + 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            progressBar.setProgress(state.get() / data.size());
        });
    }
}
