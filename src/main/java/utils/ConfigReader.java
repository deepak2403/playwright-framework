package utils;

import java.util.Map;

public class ConfigReader {

    private static Map<String, Object> config;

    static {
        config = YamlConfigReader.readYaml("config.yml");
    }

    public static String getString(String key) {
        return config.get(key).toString();
    }
    public static Map<String,String> getDeviceConfig(String key)
    {
        Map<String,String> deviceConfig = (Map<String, String>) config.get(key);
        return deviceConfig;
    }
}
