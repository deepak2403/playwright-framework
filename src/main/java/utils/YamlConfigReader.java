package utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class YamlConfigReader {


    public static Map<String, Object> readYaml(String filePath) {
        Yaml yaml = new Yaml();
        InputStream inputStream = YamlConfigReader.class.getClassLoader().getResourceAsStream(filePath);
        Map<String, Object> yamlData = yaml.load(inputStream);
        return yamlData;
    }




}
