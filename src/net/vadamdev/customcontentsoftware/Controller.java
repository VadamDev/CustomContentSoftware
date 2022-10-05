package net.vadamdev.customcontentsoftware;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import net.vadamdev.customcontentsoftware.factories.ResourcepackFactory;
import net.vadamdev.customcontentsoftware.factories.ResourcepackLoader;
import org.json.simple.parser.ParseException;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VadamDev
 * @since 29/09/2022
 */
public class Controller {
    private static final File HOME_DIRECTORY = FileSystemView.getFileSystemView().getHomeDirectory();

    private static final Map<String, File> textures = new HashMap<>();

    @FXML
    public TextField resourcepackName;
    public TextArea resourcepackDescription;

    @FXML
    private ImageView textureView;

    @FXML
    private ListView<String> itemsList;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button applyTextureButton, createResourcepackButton;

    @FXML
    void loadDumpFile(ActionEvent event) {
        applyTextureButton.setDisable(true);

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(HOME_DIRECTORY);
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File file = fileChooser.showOpenDialog(null);

            if(file != null) {
                textures.clear();

                BufferedReader reader = new BufferedReader(new FileReader(file));

                ObservableList<String> content = FXCollections.observableArrayList();

                String line;
                while((line = reader.readLine()) != null)
                    content.add(line);

                reader.close();

                itemsList.setItems(content);

                if(applyTextureButton.isDisable())
                    applyTextureButton.setDisable(false);
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void loadResourcepack(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(HOME_DIRECTORY);

        File file = directoryChooser.showDialog(null);

        if(file != null) {
            ResourcepackLoader loader = new ResourcepackLoader(file);

            try {
                loader.read();
                loader.applyContent(textures, itemsList, resourcepackName, resourcepackDescription);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void applyToSelectedTexture(ActionEvent event) throws FileNotFoundException {
        String item = itemsList.getSelectionModel().getSelectedItem();

        if(item == null) {
            applyTextureButton.setDisable(true);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(HOME_DIRECTORY);
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("PNG", "*.png"));

        File file = fileChooser.showOpenDialog(null);

        if(file != null) {
            textureView.setImage(new Image(new FileInputStream(file)));

            textures.put(item, file);

            if(createResourcepackButton.isDisable())
                createResourcepackButton.setDisable(false);
        }
    }

    public void computeSelectedItem(MouseEvent mouseEvent) throws FileNotFoundException {
        if(itemsList.getSelectionModel().getSelectedItem() != null) {
            applyTextureButton.setDisable(false);

            String item = itemsList.getSelectionModel().getSelectedItem();

            if(textures.containsKey(item))
                textureView.setImage(new Image(new FileInputStream(textures.get(item))));
            else
                textureView.setImage(null);
        }
    }

    public void createResourcepack(ActionEvent actionEvent) {
        if(resourcepackName.getText().isEmpty() || resourcepackDescription.getText().isEmpty() || textures.isEmpty())
            return;

        ResourcepackFactory factory = new ResourcepackFactory(textures, resourcepackName.getText(), resourcepackDescription.getText(), progressBar);
        factory.build(HOME_DIRECTORY);
    }
}
