package net.vadamdev.customcontentsoftware.factories;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.vadamdev.customcontentsoftware.utils.AtomicVar;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VadamDev
 * @since 03/10/2022
 */
public class ResourcepackLoader {
    private final File packDirectory;

    private final Map<String, File> data;
    private final String name;
    private String description;

    public ResourcepackLoader(File packDirectory) {
        this.data = new HashMap<>();
        this.packDirectory = packDirectory;
        this.name = packDirectory.getName();
    }

    public void read() throws IOException, ParseException {
        File pack = new File(packDirectory, "pack.mcmeta");
        BufferedReader packReader = new BufferedReader(new FileReader(pack));

        StringBuilder jsonString = new StringBuilder();
        String packLine;
        while((packLine = packReader.readLine()) != null)
            jsonString.append(packLine);

        packReader.close();

        description = (String) ((JSONObject) ((JSONObject) new JSONParser().parse(jsonString.toString())).get("pack")).get("description");

        File citDirectory = new File(packDirectory, "assets/minecraft/mcpatcher/cit");
        File[] propertiesFiles = citDirectory.listFiles(file -> file.getName().endsWith(".properties"));
        File[] texturesFiles = citDirectory.listFiles(file -> file.getName().endsWith(".png"));

        if(propertiesFiles == null || texturesFiles == null)
            return;

        if(propertiesFiles.length != texturesFiles.length)
            return;

        for (int i = 0; i < propertiesFiles.length; i++) {
            File propertiesFile = propertiesFiles[i];

            BufferedReader propertiesReader = new BufferedReader(new FileReader(propertiesFile));

            List<String> content = new ArrayList<>();
            String line;
            while((line = propertiesReader.readLine()) != null)
                content.add(line);

            AtomicVar<String> itemName = new AtomicVar<>("INTERNAL ERROR");
            AtomicVar<String> itemId = new AtomicVar<>("INTERNAL ERROR");

            content.forEach(str -> {
                if(str.startsWith("nbt.RegistryName"))
                    itemName.set(str.split("=")[1]);
                else if(str.startsWith("items"))
                    itemId.set(str.split("=")[1]);
            });

            data.put(itemName.get() + ":" + itemId.get(), texturesFiles[i]);
        }
    }

    public void applyContent(Map<String, File> textures, ListView<String> itemsList, TextField resourcepackName, TextArea resourcepackDescription) {
        textures.clear();
        textures.putAll(data);

        itemsList.setItems(FXCollections.observableArrayList(data.keySet()));

        resourcepackName.setText(name);
        resourcepackDescription.setText(description);
    }
}
